// FirebaseModule.kt
package com.example.soclub.service.module

import com.example.soclub.service.AccountService
import com.example.soclub.service.ActivityService
import com.example.soclub.service.ActivityDetailService
import com.example.soclub.service.EntriesService
import com.example.soclub.service.NotificationService
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

    @Provides
    @Singleton
    fun provideActivityService(firestore: FirebaseFirestore): ActivityService {
        return ActivityServiceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideActivityDetailService(firestore: FirebaseFirestore): ActivityDetailService {
        return ActivityDetailServiceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideEntriesService(firestore: FirebaseFirestore): EntriesService {
        return EntriesServiceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationService(
        firestore: FirebaseFirestore,
        accountService: AccountService
    ): NotificationService {
        return NotificationServiceImpl(firestore, accountService)
    }
}
