package com.event.chats.di

import android.content.Context
import androidx.room.Room
import com.event.chats.data.local.ChatDao
import com.event.chats.data.local.ChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChatDatabase{
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "Chat_Databse"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideDao(database: ChatDatabase): ChatDao {
        return database.dao()
    }
}