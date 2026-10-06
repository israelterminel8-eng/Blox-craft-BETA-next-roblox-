package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

  // --- USERS & AUTH ---
  @Query("SELECT * FROM users WHERE isCurrentLoggedIn = 1 LIMIT 1")
  fun getCurrentLoggedInUserFlow(): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE isCurrentLoggedIn = 1 LIMIT 1")
  suspend fun getCurrentLoggedInUser(): UserEntity?

  @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): UserEntity?

  @Query("SELECT * FROM users")
  fun getAllUsersFlow(): Flow<List<UserEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET isCurrentLoggedIn = 0")
  suspend fun clearAllLoggedIn()

  @Query("UPDATE users SET isCurrentLoggedIn = 1 WHERE id = :userId")
  suspend fun setLoggedIn(userId: String)

  @Query("UPDATE users SET coins = coins + :amount WHERE id = :userId")
  suspend fun addCoins(userId: String, amount: Long)

  @Query("UPDATE users SET playtimeSeconds = playtimeSeconds + :seconds WHERE id = :userId")
  suspend fun addPlaytimeSeconds(userId: String, seconds: Long)

  @Query("UPDATE users SET avatarSkinColor = :skin, avatarTorsoColor = :torso, avatarLegsColor = :legs, equippedHatId = :hat, equippedWingsId = :wings, equippedGearId = :gear, equippedShirtId = :shirt, equippedFaceId = :face, equippedPetId = :pet WHERE id = :userId")
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
  )

  // --- INVENTORY ---
  @Query("SELECT * FROM inventory WHERE userId = :userId ORDER BY obtainedTimestamp DESC")
  fun getUserInventoryFlow(userId: String): Flow<List<InventoryItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInventoryItem(item: InventoryItemEntity)

  @Query("SELECT COUNT(*) > 0 FROM inventory WHERE userId = :userId AND itemId = :itemId")
  suspend fun hasInventoryItem(userId: String, itemId: String): Boolean

  // --- PUBLISHED GAMES (LUAU PLATFORM) ---
  @Query("SELECT * FROM published_games ORDER BY visits DESC, createdTimestamp DESC")
  fun getAllPublishedGamesFlow(): Flow<List<PublishedGameEntity>>

  @Query("SELECT * FROM published_games WHERE creatorUserId = :userId ORDER BY createdTimestamp DESC")
  fun getUserGamesFlow(userId: String): Flow<List<PublishedGameEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPublishedGame(game: PublishedGameEntity)

  @Query("UPDATE published_games SET visits = visits + 1, pendingPayoutCoins = pendingPayoutCoins + :payoutBonus WHERE id = :gameId")
  suspend fun registerGameVisit(gameId: String, payoutBonus: Int)

  @Query("UPDATE published_games SET likes = likes + 1, pendingPayoutCoins = pendingPayoutCoins + :likeBonus WHERE id = :gameId")
  suspend fun likeGame(gameId: String, likeBonus: Int)

  @Query("UPDATE published_games SET pendingPayoutCoins = 0 WHERE id = :gameId")
  suspend fun clearPendingPayout(gameId: String)

  // --- CATALOG ACCESSORIES ---
  @Query("SELECT * FROM catalog_accessories ORDER BY salesCount DESC, createdTimestamp DESC")
  fun getAllCatalogAccessoriesFlow(): Flow<List<CatalogAccessoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCatalogAccessory(item: CatalogAccessoryEntity)

  @Query("UPDATE catalog_accessories SET salesCount = salesCount + 1 WHERE id = :id")
  suspend fun incrementAccessorySales(id: String)

  // --- FRIENDS ---
  @Query("SELECT * FROM friends WHERE userId = :userId")
  fun getFriendsFlow(userId: String): Flow<List<FriendEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFriend(friend: FriendEntity)

  @Query("DELETE FROM friends WHERE id = :id")
  suspend fun deleteFriend(id: String)

  // --- CHAT MESSAGES ---
  @Query("SELECT * FROM chat_messages WHERE channel = :channel ORDER BY timestamp ASC LIMIT 100")
  fun getChatMessagesFlow(channel: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChatMessage(msg: ChatMessageEntity)

  // --- PRIVATE ROOMS ---
  @Query("SELECT * FROM private_rooms")
  fun getAllPrivateRoomsFlow(): Flow<List<PrivateRoomEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPrivateRoom(room: PrivateRoomEntity)

  @Query("DELETE FROM private_rooms WHERE roomCode = :roomCode")
  suspend fun deletePrivateRoom(roomCode: String)
}
