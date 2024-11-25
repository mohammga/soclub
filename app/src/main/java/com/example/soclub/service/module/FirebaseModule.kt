package com.example.soclub.service.module

import android.content.Context
import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import com.example.soclub.service.ActivityDetailService
import com.example.soclub.service.EntriesService
import com.example.soclub.service.NotificationService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.impl.ActivityServiceImpl
import com.example.soclub.service.impl.ActivityDetailServiceImpl
import com.example.soclub.service.impl.EntriesServiceImpl
import com.example.soclub.service.impl.NotificationServiceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Dagger module for providing Firebase services and application-specific service implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides a singleton instance of [FirebaseAuth].
     * @return The [FirebaseAuth] instance.
     */
    @Singleton
    @Provides
    fun auth(): FirebaseAuth = Firebase.auth

    /**
     * Provides a singleton instance of [FirebaseFirestore].
     * @return The [FirebaseFirestore] instance.
     */
    @Singleton
    @Provides
    fun firestore(): FirebaseFirestore = Firebase.firestore

    /**
     * Provides a singleton instance of [FirebaseStorage].
     * @return The [FirebaseStorage] instance.
     */
    @Singleton
    @Provides
    fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Provides a singleton instance of [ActivityService].
     * @param firestore The [FirebaseFirestore] instance.
     * @param context The application [Context].
     * @return The [ActivityService] implementation.
     */
    @Singleton
    @Provides
    fun provideActivityService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): ActivityService {
        return ActivityServiceImpl(firestore, context)
    }

    /**
     * Provides a singleton instance of [ActivityDetailService].
     * @param firestore The [FirebaseFirestore] instance.
     * @param context The application [Context].
     * @return The [ActivityDetailService] implementation.
     */
    @Singleton
    @Provides
    fun provideActivityDetailService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): ActivityDetailService {
        return ActivityDetailServiceImpl(firestore, context)
    }

    /**
     * Provides a singleton instance of [EntriesService].
     * @param firestore The [FirebaseFirestore] instance.
     * @param context The application [Context].
     * @return The [EntriesService] implementation.
     */
    @Singleton
    @Provides
    fun provideEntriesService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): EntriesService {
        return EntriesServiceImpl(firestore, context)
    }

    /**
     * Provides a singleton instance of [NotificationService].
     * @param firestore The [FirebaseFirestore] instance.
     * @param accountService The [AccountService] instance.
     * @param context The application [Context].
     * @return The [NotificationService] implementation.
     */
    @Singleton
    @Provides
    fun provideNotificationService(
        firestore: FirebaseFirestore,
        accountService: AccountService,
        @ApplicationContext context: Context
    ): NotificationService {
        return NotificationServiceImpl(firestore, accountService, context)
    }

    /**
     * Provides a singleton instance of [AccountService].
     * @param auth The [FirebaseAuth] instance.
     * @param firestore The [FirebaseFirestore] instance.
     * @param context The application [Context].
     * @return The [AccountService] implementation.
     */
    @Singleton
    @Provides
    fun provideAccountService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): AccountService {
        return AccountServiceImpl(auth, firestore, context)
    }
}

