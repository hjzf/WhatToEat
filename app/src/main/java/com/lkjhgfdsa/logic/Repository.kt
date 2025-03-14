package com.lkjhgfdsa.logic

import com.lkjhgfdsa.logic.dao.AppDatabase
import com.lkjhgfdsa.logic.model.Option
import kotlinx.coroutines.flow.Flow

object Repository {

    suspend fun getOptionAsync(id: String): Option? {
        return AppDatabase.getInstance().optionDao().getOptionById(id)
    }

    fun getOptionsFlow(): Flow<List<Option>> {
        return AppDatabase.getInstance().optionDao().observeOptions()
    }

    suspend fun insertOptionsAsync(vararg option: Option) {
        AppDatabase.getInstance().optionDao().insertOptionsAsync(*option)
    }

    suspend fun updateOptionsAsync(vararg option: Option) {
        AppDatabase.getInstance().optionDao().updateOptionsAsync(*option)
    }

    suspend fun deleteOptionsAsync(vararg option: Option) {
        AppDatabase.getInstance().optionDao().deleteOptionsAsync(*option)
    }

}