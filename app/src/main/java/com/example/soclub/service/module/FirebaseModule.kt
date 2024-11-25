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

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun auth(): FirebaseAuth = Firebase.auth

    @Singleton
    @Provides
    fun firestore(): FirebaseFirestore = Firebase.firestore

    @Singleton
    @Provides
    fun firebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Singleton
    @Provides
    fun provideActivityService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): ActivityService {
        return ActivityServiceImpl(firestore, context)
    }

    @Singleton
    @Provides
    fun provideActivityDetailService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): ActivityDetailService {
        return ActivityDetailServiceImpl(firestore, context)
    }

    @Singleton
    @Provides
    fun provideEntriesService(
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): EntriesService {
        return EntriesServiceImpl(firestore, context)
    }

    @Singleton
    @Provides
    fun provideNotificationService(
        firestore: FirebaseFirestore,
        accountService: AccountService,
        @ApplicationContext context: Context
    ): NotificationService {
        return NotificationServiceImpl(firestore, accountService, context)
    }

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
