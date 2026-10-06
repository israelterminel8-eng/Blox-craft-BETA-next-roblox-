package com.example.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class BloxRepository(private val dao: UserDao) {

  val currentUserFlow: Flow<UserEntity?> = dao.getCurrentLoggedInUserFlow()
  val allPublishedGamesFlow: Flow<List<PublishedGameEntity>> = dao.getAllPublishedGamesFlow()
  val allAccessoriesFlow: Flow<List<CatalogAccessoryEntity>> = dao.getAllCatalogAccessoriesFlow()
  val globalChatFlow: Flow<List<ChatMessageEntity>> = dao.getChatMessagesFlow("GLOBAL")
  val privateRoomsFlow: Flow<List<PrivateRoomEntity>> = dao.getAllPrivateRoomsFlow()

  fun getUserInventoryFlow(userId: String): Flow<List<InventoryItemEntity>> =
    dao.getUserInventoryFlow(userId)

  fun getUserGamesFlow(userId: String): Flow<List<PublishedGameEntity>> =
    dao.getUserGamesFlow(userId)

  fun getFriendsFlow(userId: String): Flow<List<FriendEntity>> =
    dao.getFriendsFlow(userId)

  fun getRoomChatFlow(roomCode: String): Flow<List<ChatMessageEntity>> =
    dao.getChatMessagesFlow("ROOM_$roomCode")

  // Hash password with SHA-256 for secure local storage
  private fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
  }

  suspend fun registerUser(username: String, pass: String, displayName: String): Result<UserEntity> =
    withContext(Dispatchers.IO) {
      val trimmedUser = username.trim()
      if (trimmedUser.length < 3) {
        return@withContext Result.failure(Exception("El nombre de usuario debe tener al menos 3 caracteres"))
      }
      if (pass.length < 4) {
        return@withContext Result.failure(Exception("La contraseña debe tener al menos 4 caracteres"))
      }

      val existing = dao.getUserByUsername(trimmedUser)
      if (existing != null) {
        return@withContext Result.failure(Exception("El usuario ya existe. ¡Inicia sesión!"))
      }

      dao.clearAllLoggedIn()
      val newUser = UserEntity(
        id = UUID.randomUUID().toString(),
        username = trimmedUser,
        passwordHash = hashPassword(pass),
        displayName = if (displayName.isNotBlank()) displayName.trim() else trimmedUser,
        coins = 250L, // Starter bonus coins
        isCurrentLoggedIn = true
      )
      dao.insertUser(newUser)

      // Add default starter items to user's inventory
      dao.insertInventoryItem(
        InventoryItemEntity(
          id = UUID.randomUUID().toString(),
          userId = newUser.id,
          itemId = "starter_hat_cap",
          name = "Gorra Deportiva Neón",
          category = "HAT",
          iconType = "HAT",
          colorHex = 0xFF00E5FF,
          rarity = "COMUN",
          sourceGame = "Regalo de Bienvenida"
        )
      )
      dao.insertInventoryItem(
        InventoryItemEntity(
          id = UUID.randomUUID().toString(),
          userId = newUser.id,
          itemId = "starter_sword_laser",
          name = "Espada Láser de Bloques",
          category = "GEAR",
          iconType = "SWORD",
          colorHex = 0xFFFFC400,
          rarity = "RARO",
          sourceGame = "Regalo de Bienvenida"
        )
      )

      Result.success(newUser)
    }

  suspend fun loginUser(username: String, pass: String): Result<UserEntity> =
    withContext(Dispatchers.IO) {
      val trimmedUser = username.trim()
      val user = dao.getUserByUsername(trimmedUser)
        ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

      val hash = hashPassword(pass)
      if (user.passwordHash != hash) {
        return@withContext Result.failure(Exception("Contraseña incorrecta"))
      }

      dao.clearAllLoggedIn()
      dao.setLoggedIn(user.id)
      Result.success(user.copy(isCurrentLoggedIn = true))
    }

  suspend fun logout() = withContext(Dispatchers.IO) {
    dao.clearAllLoggedIn()
  }

  suspend fun addPlaytimeReward(userId: String, seconds: Long): Long = withContext(Dispatchers.IO) {
    dao.addPlaytimeSeconds(userId, seconds)
    // Reward 15 BloxCoins every 60 seconds
    val coinBonus = 15L
    dao.addCoins(userId, coinBonus)
    coinBonus
  }

  suspend fun addCoins(userId: String, amount: Long) = withContext(Dispatchers.IO) {
    dao.addCoins(userId, amount)
  }

  suspend fun updateAvatar(
    userId: String,
    skin: Long,
    torso: Long,
    legs: Long,
    hat: String?,
    wings: String?,
    gear: String?,
    shirt: String?,
    face: String?,
    pet: String?
  ) = withContext(Dispatchers.IO) {
    dao.updateAvatar(userId, skin, torso, legs, hat, wings, gear, shirt, face, pet)
  }

  // Award in-game collectible items found inside 3D worlds
  suspend fun collectItemInWorld(
    userId: String,
    name: String,
    category: String,
    iconType: String,
    colorHex: Long,
    rarity: String,
    sourceGame: String
  ): Boolean = withContext(Dispatchers.IO) {
    val itemId = "world_item_" + name.lowercase().replace(" ", "_")
    val alreadyHas = dao.hasInventoryItem(userId, itemId)
    if (!alreadyHas) {
      dao.insertInventoryItem(
        InventoryItemEntity(
          id = UUID.randomUUID().toString(),
          userId = userId,
          itemId = itemId,
          name = name,
          category = category,
          iconType = iconType,
          colorHex = colorHex,
          rarity = rarity,
          sourceGame = sourceGame
        )
      )
      // Bonus coins when finding a rare game artifact!
      dao.addCoins(userId, 50L)
      true
    } else {
      false
    }
  }

  suspend fun buyCatalogAccessory(
    userId: String,
    accessory: CatalogAccessoryEntity
  ): Result<Unit> = withContext(Dispatchers.IO) {
    val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("Usuario no válido"))
    if (user.coins < accessory.price) {
      return@withContext Result.failure(Exception("BloxCoins insuficientes (Necesitas ${accessory.price})"))
    }

    // Deduct coins
    dao.addCoins(userId, -accessory.price.toLong())
    dao.incrementAccessorySales(accessory.id)

    // Creator royalty! If created by another player, pay them
    if (accessory.creatorUserId.isNotBlank() && accessory.creatorUserId != userId) {
      dao.addCoins(accessory.creatorUserId, (accessory.price * 0.7).toLong())
    }

    // Add to inventory
    dao.insertInventoryItem(
      InventoryItemEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        itemId = accessory.id,
        name = accessory.name,
        category = accessory.category,
        iconType = accessory.category,
        colorHex = accessory.primaryColorHex,
        rarity = accessory.rarity,
        sourceGame = "Tienda de Catálogo"
      )
    )

    Result.success(Unit)
  }

  suspend fun createCustomAccessory(
    userId: String,
    creatorName: String,
    name: String,
    category: String,
    price: Int,
    rarity: String,
    primaryColorHex: Long,
    accentColorHex: Long,
    styleId: String,
    publishToCatalog: Boolean
  ) = withContext(Dispatchers.IO) {
    val accId = "custom_acc_" + UUID.randomUUID().toString().take(8)

    // Add directly to creator's inventory
    dao.insertInventoryItem(
      InventoryItemEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        itemId = accId,
        name = name,
        category = category,
        iconType = category,
        colorHex = primaryColorHex,
        rarity = rarity,
        sourceGame = "Estudio de Creación"
      )
    )

    if (publishToCatalog) {
      dao.insertCatalogAccessory(
        CatalogAccessoryEntity(
          id = accId,
          creatorUserId = userId,
          creatorName = creatorName,
          name = name,
          category = category,
          price = price,
          rarity = rarity,
          primaryColorHex = primaryColorHex,
          accentColorHex = accentColorHex,
          styleId = styleId
        )
      )
    }
  }

  suspend fun publishGame(
    userId: String,
    creatorName: String,
    title: String,
    description: String,
    genre: String,
    luauCode: String,
    worldType: String
  ): PublishedGameEntity = withContext(Dispatchers.IO) {
    val game = PublishedGameEntity(
      id = "game_" + UUID.randomUUID().toString().take(8),
      creatorUserId = userId,
      creatorName = creatorName,
      title = title,
      description = description,
      genre = genre,
      luauCode = luauCode,
      worldType = worldType
    )
    dao.insertPublishedGame(game)
    // Publishing reward!
    dao.addCoins(userId, 30L)
    game
  }

  suspend fun visitGame(gameId: String) = withContext(Dispatchers.IO) {
    // Each visit generates creator payout pool (e.g. +5 BloxCoins)
    dao.registerGameVisit(gameId, payoutBonus = 5)
  }

  suspend fun likeGame(gameId: String) = withContext(Dispatchers.IO) {
    // Each like adds +10 to creator payout pool
    dao.likeGame(gameId, likeBonus = 10)
  }

  suspend fun claimGamePayout(userId: String, game: PublishedGameEntity) = withContext(Dispatchers.IO) {
    if (game.pendingPayoutCoins > 0) {
      dao.addCoins(userId, game.pendingPayoutCoins.toLong())
      dao.clearPendingPayout(game.id)
    }
  }

  suspend fun sendChatMessage(
    channel: String,
    senderId: String,
    senderName: String,
    text: String
  ) = withContext(Dispatchers.IO) {
    dao.insertChatMessage(
      ChatMessageEntity(
        id = UUID.randomUUID().toString(),
        channel = channel,
        senderId = senderId,
        senderName = senderName,
        message = text
      )
    )
  }

  suspend fun addFriend(userId: String, friendUsername: String, friendDisplayName: String) = withContext(Dispatchers.IO) {
    dao.insertFriend(
      FriendEntity(
        id = UUID.randomUUID().toString(),
        userId = userId,
        friendUsername = friendUsername,
        friendDisplayName = friendDisplayName,
        status = "ONLINE",
        currentGame = "Obby de Lava 3D"
      )
    )
  }

  suspend fun removeFriend(id: String) = withContext(Dispatchers.IO) {
    dao.deleteFriend(id)
  }

  suspend fun createPrivateRoom(
    roomName: String,
    hostUserId: String,
    hostName: String,
    gameId: String,
    gameTitle: String
  ): String = withContext(Dispatchers.IO) {
    val code = "BLOX-" + (1000..9999).random()
    dao.insertPrivateRoom(
      PrivateRoomEntity(
        roomCode = code,
        roomName = roomName,
        hostUserId = hostUserId,
        hostName = hostName,
        gameId = gameId,
        gameTitle = gameTitle,
        playerCount = 1,
        maxPlayers = 6
      )
    )
    code
  }

  // Initial seeding for rich game experience
  suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
    val accessories = dao.getAllCatalogAccessoriesFlow().firstOrNull()
    if (accessories.isNullOrEmpty()) {
      // Seed default catalog accessories
      val initialAccessories = listOf(
        CatalogAccessoryEntity(
          id = "acc_corona_dorada",
          creatorUserId = "system",
          creatorName = "BloxCraft Studios",
          name = "Corona Imperial de Oro",
          category = "HAT",
          price = 120,
          rarity = "LEGENDARIO",
          primaryColorHex = 0xFFFFC400,
          accentColorHex = 0xFFFF9100,
          styleId = "CROWN",
          salesCount = 420
        ),
        CatalogAccessoryEntity(
          id = "acc_alas_ciber",
          creatorUserId = "system",
          creatorName = "CyberDev",
          name = "Alas Neón Cyberpunk",
          category = "WINGS",
          price = 150,
          rarity = "EPICO",
          primaryColorHex = 0xFF00E5FF,
          accentColorHex = 0xFFA855F7,
          styleId = "CYBER_WINGS",
          salesCount = 890
        ),
        CatalogAccessoryEntity(
          id = "acc_espada_dragon",
          creatorUserId = "system",
          creatorName = "ValkyrieBlox",
          name = "Filo del Dragón Carmesí",
          category = "GEAR",
          price = 90,
          rarity = "EPICO",
          primaryColorHex = 0xFFEF4444,
          accentColorHex = 0xFFFFC400,
          styleId = "FIRE_SWORD",
          salesCount = 630
        ),
        CatalogAccessoryEntity(
          id = "acc_mascota_gatito",
          creatorUserId = "system",
          creatorName = "PetMasters",
          name = "Mascota Robo-Gatito 3D",
          category = "PET",
          price = 200,
          rarity = "LEGENDARIO",
          primaryColorHex = 0xFF10B981,
          accentColorHex = 0xFF00E5FF,
          styleId = "ROBO_CAT",
          salesCount = 1120
        ),
        CatalogAccessoryEntity(
          id = "acc_casco_espacial",
          creatorUserId = "system",
          creatorName = "AstroCraft",
          name = "Casco Astral Galáctico",
          category = "HAT",
          price = 80,
          rarity = "RARO",
          primaryColorHex = 0xFF6366F1,
          accentColorHex = 0xFFEC4899,
          styleId = "SPACE_HELMET",
          salesCount = 310
        ),
        CatalogAccessoryEntity(
          id = "acc_gafas_hologram",
          creatorUserId = "system",
          creatorName = "NeonVibe",
          name = "Visor Holográfico VR",
          category = "FACE",
          price = 60,
          rarity = "RARO",
          primaryColorHex = 0xFF00E5FF,
          accentColorHex = 0xFF10B981,
          styleId = "VR_VISOR",
          salesCount = 540
        )
      )
      initialAccessories.forEach { dao.insertCatalogAccessory(it) }

      // Seed initial community games with Luau scripts
      val initialGames = listOf(
        PublishedGameEntity(
          id = "game_obby_lava",
          creatorUserId = "system",
          creatorName = "ProBuilder",
          title = "Obby de Lava & Cristales 3D",
          description = "¡Supera el circuito de plataformas flotantes, esquiva bloques de lava y recoge gemas misteriosas!",
          genre = "OBBY",
          luauCode = """-- Luau Script: Obby de Lava
local Workspace = game.Workspace
Workspace.Gravity = 19.6
Workspace.PlayerSpeed = 7.0

-- Creación de plataformas de salto
local platforms = {}
for i = 1, 10 do
    local part = Instance.new("Part")
    part.Position = Vector3.new(0, i * 1.8, i * 4.5)
    part.Color = Color3.fromRGB(0, 229, 255)
    part.Size = Vector3.new(3, 0.5, 3)
    table.insert(platforms, part)
end

-- Recompensa de meta
Workspace.OnGoalReached = function(player)
    player.AddCoins(50)
    print("¡Obby completado con éxito!")
end
""".trimIndent(),
          visits = 3450,
          likes = 890,
          pendingPayoutCoins = 145,
          worldType = "OBBY"
        ),
        PublishedGameEntity(
          id = "game_blox_city",
          creatorUserId = "system",
          creatorName = "MetaCityTeam",
          title = "Blox City: Parque & Monedas",
          description = "Explora la ciudad 3D, salta en trampolines gigantes y reúne monedas de oro con tus amigos.",
          genre = "CIUDAD",
          luauCode = """-- Luau Script: Blox City
local City = game.Workspace.City
City.DayNightCycle = true
City.CoinSpawnInterval = 5 -- Segundos

function OnPlayerJoin(player)
    print("Bienvenido a Blox City, " .. player.Name)
    player.SpawnAt(Vector3.new(0, 1, 0))
end

-- Trampolines de alta propulsión
City.TrampolineBounce = 24.0
""".trimIndent(),
          visits = 5820,
          likes = 1420,
          pendingPayoutCoins = 220,
          worldType = "CIUDAD"
        ),
        PublishedGameEntity(
          id = "game_isla_dragon",
          creatorUserId = "system",
          creatorName = "SkyBuilder",
          title = "Isla Flotante del Dragón",
          description = "Aventura en las nubes. Encuentra la espada legendaria en el templo de cristal.",
          genre = "AVENTURA",
          luauCode = """-- Luau Script: Isla del Dragón
local Island = game.Workspace
Island.WindForce = 2.5

local Relic = Instance.new("LegendaryArtifact")
Relic.Name = "Espada del Sol"
Relic.Position = Vector3.new(12, 15, 30)

Relic.OnInteract = function(player)
    player.Inventory.Add(Relic)
    print("¡Has obtenido una reliquia legendaria!")
end
""".trimIndent(),
          visits = 2190,
          likes = 670,
          pendingPayoutCoins = 95,
          worldType = "AVENTURA"
        )
      )
      initialGames.forEach { dao.insertPublishedGame(it) }

      // Seed initial global chat messages
      val initialChat = listOf(
        ChatMessageEntity(
          id = "chat_1",
          channel = "GLOBAL",
          senderId = "user_alex",
          senderName = "AlexGamer99",
          message = "¡Hola a todos! ¿Quién se une al Obby de Lava en sala privada?"
        ),
        ChatMessageEntity(
          id = "chat_2",
          channel = "GLOBAL",
          senderId = "user_luna",
          senderName = "LunaCraft",
          message = "Acabo de publicar un nuevo mapa hecho con Luau, ¡denle like en Descubrir!"
        ),
        ChatMessageEntity(
          id = "chat_3",
          channel = "GLOBAL",
          senderId = "user_dev",
          senderName = "CyberDev",
          message = "Las recompensas por tiempo activo están geniales, ya junté para las alas neón 🪙🔥"
        )
      )
      initialChat.forEach { dao.insertChatMessage(it) }
    }
  }
}
