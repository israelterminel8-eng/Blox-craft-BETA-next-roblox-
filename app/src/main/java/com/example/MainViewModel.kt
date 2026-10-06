package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BloxDatabase
import com.example.data.local.BloxRepository
import com.example.data.local.CatalogAccessoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FriendEntity
import com.example.data.local.InventoryItemEntity
import com.example.data.local.PrivateRoomEntity
import com.example.data.local.PublishedGameEntity
import com.example.data.local.UserEntity
import com.example.game3d.AvatarCustomization
import com.example.game3d.World3D
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenNav {
  HOME,
  STUDIO,
  AVATAR,
  SHOP,
  INVENTORY,
  SOCIAL
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BloxRepository

  init {
    val db = BloxDatabase.getDatabase(application)
    repository = BloxRepository(db.userDao())
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty()
    }
  }

  // --- CURRENT USER & AUTH ---
  val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  // --- NAVIGATION & ACTIVE GAME ---
  private val _currentScreen = MutableStateFlow(ScreenNav.HOME)
  val currentScreen: StateFlow<ScreenNav> = _currentScreen.asStateFlow()

  private val _activeWorld3D = MutableStateFlow<World3D?>(null)
  val activeWorld3D: StateFlow<World3D?> = _activeWorld3D.asStateFlow()

  // --- REWARDS & PLAYTIME TICKER ---
  private val _playtimeSeconds = MutableStateFlow(0L)
  val playtimeSeconds: StateFlow<Long> = _playtimeSeconds.asStateFlow()

  private val _currentIntervalSeconds = MutableStateFlow(0L)
  val currentIntervalSeconds: StateFlow<Long> = _currentIntervalSeconds.asStateFlow()

  private val _showPlaytimeSheet = MutableStateFlow(false)
  val showPlaytimeSheet: StateFlow<Boolean> = _showPlaytimeSheet.asStateFlow()

  // --- PUBLISHED GAMES & STUDIO ---
  val allGames: StateFlow<List<PublishedGameEntity>> = repository.allPublishedGamesFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _userGames = MutableStateFlow<List<PublishedGameEntity>>(emptyList())
  val userGames: StateFlow<List<PublishedGameEntity>> = _userGames.asStateFlow()

  // --- INVENTORY ---
  private val _userInventory = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
  val userInventory: StateFlow<List<InventoryItemEntity>> = _userInventory.asStateFlow()

  // --- CATALOG ACCESSORIES ---
  val catalogAccessories: StateFlow<List<CatalogAccessoryEntity>> = repository.allAccessoriesFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // --- SOCIAL, FRIENDS & CHAT ---
  val globalChat: StateFlow<List<ChatMessageEntity>> = repository.globalChatFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val privateRooms: StateFlow<List<PrivateRoomEntity>> = repository.privateRoomsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _friendsList = MutableStateFlow<List<FriendEntity>>(emptyList())
  val friendsList: StateFlow<List<FriendEntity>> = _friendsList.asStateFlow()

  init {
    // Collect user-specific data when logged in
    viewModelScope.launch {
      currentUser.collect { user ->
        if (user != null) {
          _playtimeSeconds.value = user.playtimeSeconds
          launch {
            repository.getUserInventoryFlow(user.id).collect { _userInventory.value = it }
          }
          launch {
            repository.getUserGamesFlow(user.id).collect { _userGames.value = it }
          }
          launch {
            repository.getFriendsFlow(user.id).collect { _friendsList.value = it }
          }
        }
      }
    }

    // Playtime Ticker (Continuous rewards for spending time in the game)
    viewModelScope.launch {
      while (true) {
        delay(1000L)
        val user = currentUser.value
        if (user != null) {
          _playtimeSeconds.value += 1L
          _currentIntervalSeconds.value += 1L

          // Every 60 seconds of playtime, award +15 BloxCoins
          if (_currentIntervalSeconds.value >= 60L) {
            _currentIntervalSeconds.value = 0L
            repository.addPlaytimeReward(user.id, 60L)
          }
        }
      }
    }
  }

  // --- AUTH METHODS ---
  fun login(username: String, pass: String) {
    viewModelScope.launch {
      _authError.value = null
      val res = repository.loginUser(username, pass)
      res.onFailure {
        _authError.value = it.message ?: "Error al iniciar sesión"
      }
    }
  }

  fun register(username: String, pass: String, displayName: String) {
    viewModelScope.launch {
      _authError.value = null
      val res = repository.registerUser(username, pass, displayName)
      res.onFailure {
        _authError.value = it.message ?: "Error al registrar cuenta"
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      repository.logout()
      _activeWorld3D.value = null
      _currentScreen.value = ScreenNav.HOME
    }
  }

  // --- NAVIGATION ---
  fun navigateTo(screen: ScreenNav) {
    _currentScreen.value = screen
  }

  fun openPlaytimeSheet() {
    _showPlaytimeSheet.value = true
  }

  fun dismissPlaytimeSheet() {
    _showPlaytimeSheet.value = false
  }

  // --- 3D GAME LAUNCHING & PLAYING ---
  fun launchGame(game: PublishedGameEntity) {
    viewModelScope.launch {
      repository.visitGame(game.id)
      val world = when (game.genre) {
        "CIUDAD" -> World3D.createBloxCity()
        "AVENTURA" -> World3D.createDragonIsland()
        "OBBY" -> World3D.createObbyLava()
        else -> World3D.createFromLuau(game.title, game.luauCode)
      }
      _activeWorld3D.value = world
    }
  }

  fun launchWorldByTitle(title: String) {
    val world = when {
      title.contains("City") || title.contains("Ciudad") -> World3D.createBloxCity()
      title.contains("Dragón") || title.contains("Isla") -> World3D.createDragonIsland()
      else -> World3D.createObbyLava()
    }
    _activeWorld3D.value = world
  }

  fun launchLuauTestWorld(title: String, code: String) {
    _activeWorld3D.value = World3D.createFromLuau(title, code)
  }

  fun exitActiveGame() {
    _activeWorld3D.value = null
  }

  fun earnCoins(amount: Long) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.addCoins(user.id, amount)
    }
  }

  fun onRareItemFound(name: String, category: String, rarity: String, sourceGame: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.collectItemInWorld(
        userId = user.id,
        name = name,
        category = category,
        iconType = category,
        colorHex = 0xFFFFC400,
        rarity = rarity,
        sourceGame = sourceGame
      )
    }
  }

  fun likeGame(gameId: String) {
    viewModelScope.launch {
      repository.likeGame(gameId)
    }
  }

  // --- LUAU STUDIO & PUBLISHING ---
  fun publishGame(title: String, desc: String, genre: String, luauCode: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.publishGame(
        userId = user.id,
        creatorName = user.displayName,
        title = title,
        description = desc,
        genre = genre,
        luauCode = luauCode,
        worldType = genre
      )
    }
  }

  fun claimGamePayout(game: PublishedGameEntity) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.claimGamePayout(user.id, game)
    }
  }

  // --- AVATAR & ACCESSORIES ---
  fun saveAvatarConfig(
    skin: Long,
    torso: Long,
    legs: Long,
    hat: String?,
    wings: String?,
    gear: String?,
    shirt: String?,
    face: String?,
    pet: String?
  ) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.updateAvatar(user.id, skin, torso, legs, hat, wings, gear, shirt, face, pet)
    }
  }

  fun createCustomAccessory(
    name: String,
    category: String,
    price: Int,
    rarity: String,
    primaryColor: Long,
    accentColor: Long,
    styleId: String,
    publishToCatalog: Boolean
  ) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.createCustomAccessory(
        userId = user.id,
        creatorName = user.displayName,
        name = name,
        category = category,
        price = price,
        rarity = rarity,
        primaryColorHex = primaryColor,
        accentColorHex = accentColor,
        styleId = styleId,
        publishToCatalog = publishToCatalog
      )
    }
  }

  fun buyCatalogAccessory(item: CatalogAccessoryEntity) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.buyCatalogAccessory(user.id, item)
    }
  }

  fun equipInventoryItem(item: InventoryItemEntity) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      when (item.category) {
        "HAT" -> repository.updateAvatar(user.id, user.avatarSkinColor, user.avatarTorsoColor, user.avatarLegsColor, item.itemId, user.equippedWingsId, user.equippedGearId, user.equippedShirtId, user.equippedFaceId, user.equippedPetId)
        "WINGS" -> repository.updateAvatar(user.id, user.avatarSkinColor, user.avatarTorsoColor, user.avatarLegsColor, user.equippedHatId, item.itemId, user.equippedGearId, user.equippedShirtId, user.equippedFaceId, user.equippedPetId)
        "GEAR" -> repository.updateAvatar(user.id, user.avatarSkinColor, user.avatarTorsoColor, user.avatarLegsColor, user.equippedHatId, user.equippedWingsId, item.itemId, user.equippedShirtId, user.equippedFaceId, user.equippedPetId)
        "PET" -> repository.updateAvatar(user.id, user.avatarSkinColor, user.avatarTorsoColor, user.avatarLegsColor, user.equippedHatId, user.equippedWingsId, user.equippedGearId, user.equippedShirtId, user.equippedFaceId, item.itemId)
      }
    }
  }

  // --- SOCIAL & CHAT ---
  fun sendChatMessage(channel: String, text: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.sendChatMessage(channel, user.id, user.displayName, text)
    }
  }

  fun addFriend(username: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.addFriend(user.id, username, username)
    }
  }

  fun createPrivateRoom(roomName: String, gameTitle: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      val code = repository.createPrivateRoom(roomName, user.id, user.displayName, "game_custom", gameTitle)
      // Automatically launch game in private room
      launchWorldByTitle(gameTitle)
    }
  }
}
