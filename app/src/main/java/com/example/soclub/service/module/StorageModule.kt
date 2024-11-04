package com.example.soclub.service.module

import com.example.soclub.service.AccountService
import com.example.soclub.service.LocationService
import com.example.soclub.service.StorageService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.impl.LocationServiceImpl
import com.example.soclub.service.impl.StorageServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun provideStorageService(impl: StorageServiceImpl): StorageService

    @Binds
    @Singleton
    abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

    @Binds
    @Singleton
    abstract fun provideLocationService(impl: LocationServiceImpl): LocationService

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder().build()
        }
    }
}
