package com.lkjhgfdsa.logic.dao

import androidx.room.*
import com.lkjhgfdsa.logic.model.Option
import kotlinx.coroutines.flow.Flow

@Dao
interface OptionDao {

    @Query("select * from option where `id` = :id")
    suspend fun getOptionById(id: String): Option?

    @Query("select * from option")
    fun observeOptions(): Flow<List<Option>>

    @Update
    suspend fun updateOptionsAsync(vararg option: Option)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptionsAsync(vararg option: Option)

    @Delete
    suspend fun deleteOptionsAsync(vararg option: Option)

}