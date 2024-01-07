package com.example.background_location_tracking.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Update

@Dao
interface BaseDao<T> {

    @Insert(onConflict = REPLACE)
    fun insertAll(location: List<T>)

    @Insert(onConflict = REPLACE)
    fun insert(location: T): Long

    @Update(onConflict = REPLACE)
    fun update(location: T): Int

    @Delete
    fun delete(location: T): Int
}