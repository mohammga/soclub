package com.example.soclub.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * A Dagger module responsible for providing dependencies related to location services.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * Provides a singleton instance of [FusedLocationProviderClient].
     *
     * @param context The application context injected by Hilt using the [ApplicationContext] qualifier.
     * @return A [FusedLocationProviderClient] instance for accessing location services.
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}

