package com.example.soclub.service.module

import com.example.soclub.service.AccountService
import com.example.soclub.service.StorageService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.impl.StorageServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideStorageService(impl: StorageServiceImpl): StorageService

    @Binds
    abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

}