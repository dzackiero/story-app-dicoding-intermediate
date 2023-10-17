package com.pnj.storyapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKeys(remoteKeys: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys where id = :id")
    suspend fun getRemoteKey(id: String): RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAllKeys()
}