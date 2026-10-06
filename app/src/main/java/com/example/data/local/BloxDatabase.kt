package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    UserEntity::class,
    InventoryItemEntity::class,
    PublishedGameEntity::class,
    CatalogAccessoryEntity::class,
    FriendEntity::class,
    ChatMessageEntity::class,
    PrivateRoomEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class BloxDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao

  companion object {
    @Volatile
    private var INSTANCE: BloxDatabase? = null

    fun getDatabase(context: Context): BloxDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BloxDatabase::class.java,
          "bloxcraft_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
