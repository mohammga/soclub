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


/**
 * Dagger module to provide service dependencies.
 * This module ensures that dependencies for various services are available
 * and properly managed within the application's singleton scope.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    /**
     * Provides an instance of [OkHttpClient].
     *
     * @return A new instance of [OkHttpClient] configured with default settings.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    /**
     * Provides an instance of [StorageService].
     *
     * @param firebaseStorage The Firebase Storage instance to be used by the service.
     * @return An implementation of [StorageService] using Firebase Storage.
     */
    @Provides
    @Singleton
    fun provideStorageService(
        firebaseStorage: com.google.firebase.storage.FirebaseStorage
    ): StorageService {
        return StorageServiceImpl(firebaseStorage)
    }

    /**
     * Provides an instance of [LocationService].
     *
     * @param context The application context used by the service.
     * @param okHttpClient The [OkHttpClient] instance used for network requests.
     * @return An implementation of [LocationService] using the given context and HTTP client.
     */
    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): LocationService {
        return LocationServiceImpl(okHttpClient, context)
    }
}

