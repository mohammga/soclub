package com.example.soclub.utils
import com.example.soclub.service.NotificationService
import com.example.soclub.service.impl.NotificationServiceImpl
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {

    private var notificationService: NotificationService? = null

    fun provideNotificationService(): NotificationService {
        return notificationService ?: synchronized(this) {
            notificationService ?: NotificationServiceImpl(FirebaseFirestore.getInstance()).also {
                notificationService = it
            }
        }
    }
}
