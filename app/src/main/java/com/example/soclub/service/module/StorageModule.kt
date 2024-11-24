package com.example.soclub.service.module

import android.content.Context
import com.example.soclub.service.LocationService
import com.example.soclub.service.StorageService
import com.example.soclub.service.impl.LocationServiceImpl
import com.example.soclub.service.impl.StorageServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideStorageService(
        firebaseStorage: com.google.firebase.storage.FirebaseStorage
    ): StorageService {
        return StorageServiceImpl(firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): LocationService {
        return LocationServiceImpl(okHttpClient, context)
    }
}
