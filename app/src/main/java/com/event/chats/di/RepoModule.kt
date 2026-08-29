package com.event.chats.di

import com.event.chats.data.repository.Repository
import com.event.chats.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindRepository(repositoryImpl: RepositoryImpl): Repository
}