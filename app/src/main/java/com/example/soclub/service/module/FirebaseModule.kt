package com.example.soclub.service.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.soclub.service.ActivityService
import com.example.soclub.service.impl.ActivityServiceImpl
import com.example.soclub.service.impl.ActivityDetailServiceImpl
import com.example.soclub.service.ActivityDetaillService
import com.example.soclub.service.EntriesService
import com.example.soclub.service.impl.EntriesServiceImpl


@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Singleton
    @Provides
    fun auth(): FirebaseAuth = Firebase.auth
    @Singleton
    @Provides
    fun firestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideActivityService(firestore: FirebaseFirestore): ActivityService {
        return ActivityServiceImpl(firestore)
    }
    @Provides
    @Singleton
    fun provideActivityDetaillService(firestore: FirebaseFirestore): ActivityDetaillService {
        return ActivityDetailServiceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideEntriesService(firestore: FirebaseFirestore): EntriesService{
        return EntriesServiceImpl(firestore)
    }
    
    






}

