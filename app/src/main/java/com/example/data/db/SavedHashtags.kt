package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_hashtags")
data class SavedHashtagSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val platform: String,
    val hashtags: String, // Comma or space separated hashtags, e.g. "#travel #dxb"
    val category: String = "",
    val reachScore: String = "",
    val tips: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SavedHashtagsDao {
    @Query("SELECT * FROM saved_hashtags ORDER BY timestamp DESC")
    fun getAllSavedHashtags(): Flow<List<SavedHashtagSet>>

    @Insert
    suspend fun insertHashtagSet(hashtagSet: SavedHashtagSet)

    @Delete
    suspend fun deleteHashtagSet(hashtagSet: SavedHashtagSet)

    @Query("DELETE FROM saved_hashtags WHERE id = :id")
    suspend fun deleteHashtagSetById(id: Int)
}
