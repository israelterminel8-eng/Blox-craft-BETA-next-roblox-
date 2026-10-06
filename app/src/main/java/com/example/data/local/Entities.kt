package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val username: String,
  val passwordHash: String,
  val displayName: String,
  val coins: Long = 100L, // Starter bonus coins
  val playtimeSeconds: Long = 0L,
  val level: Int = 1,
  val xp: Int = 0,
  val avatarSkinColor: Long = 0xFFFFDBAC, // Default peach skin
  val avatarTorsoColor: Long = 0xFF00E5FF, // Cyan shirt
  val avatarLegsColor: Long = 0xFF1E293B, // Dark jeans
  val equippedHatId: String? = "hat_cap",
  val equippedWingsId: String? = null,
  val equippedGearId: String? = "gear_sword",
  val equippedShirtId: String? = "shirt_neon",
  val equippedFaceId: String? = "face_visor",
  val equippedPetId: String? = null,
  val isCurrentLoggedIn: Boolean = false
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val itemId: String,
  val name: String,
  val category: String, // HAT, WINGS, GEAR, SHIRT, FACE, PET, WORLD_COLLECTIBLE, TROPHY
  val iconType: String,
  val colorHex: Long,
  val rarity: String, // COMUN, RARO, EPICO, LEGENDARIO
  val sourceGame: String,
  val obtainedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "published_games")
data class PublishedGameEntity(
  @PrimaryKey val id: String,
  val creatorUserId: String,
  val creatorName: String,
  val title: String,
  val description: String,
  val genre: String, // OBBY, PARKOUR, ARENA, AVENTURA, CIUDAD, TYCOON
  val luauCode: String,
  val visits: Int = 0,
  val likes: Int = 0,
  val pendingPayoutCoins: Int = 0,
  val worldType: String = "OBBY",
  val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_accessories")
data class CatalogAccessoryEntity(
  @PrimaryKey val id: String,
  val creatorUserId: String,
  val creatorName: String,
  val name: String,
  val category: String, // HAT, WINGS, GEAR, SHIRT, FACE, PET
  val price: Int,
  val rarity: String,
  val primaryColorHex: Long,
  val accentColorHex: Long,
  val styleId: String,
  val salesCount: Int = 0,
  val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friends")
data class FriendEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val friendUsername: String,
  val friendDisplayName: String,
  val status: String = "ONLINE", // ONLINE, IN_GAME, IN_STUDIO, OFFLINE
  val currentGame: String = "Blox City 3D",
  val avatarColorHex: Long = 0xFF00E5FF
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val channel: String = "GLOBAL",
  val senderId: String,
  val senderName: String,
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isSystem: Boolean = false
)

@Entity(tableName = "private_rooms")
data class PrivateRoomEntity(
  @PrimaryKey val roomCode: String,
  val roomName: String,
  val hostUserId: String,
  val hostName: String,
  val gameId: String,
  val gameTitle: String,
  val playerCount: Int = 1,
  val maxPlayers: Int = 6
)
