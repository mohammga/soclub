package com.example.soclub.utils

import android.content.Context
import com.example.soclub.service.AccountService
import com.example.soclub.service.NotificationService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.impl.NotificationServiceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {

    private var notificationService: NotificationService? = null
    private var accountService: AccountService? = null

    private fun provideAccountService(context: Context): AccountService {
        return accountService ?: synchronized(this) {
            accountService ?: AccountServiceImpl(
                auth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance(),
                context = context
            ).also {
                accountService = it
            }
        }
    }

    fun provideNotificationService(context: Context): NotificationService {
        val accountServiceInstance = provideAccountService(context)
        return notificationService ?: synchronized(this) {
            notificationService ?: NotificationServiceImpl(
                firestore = FirebaseFirestore.getInstance(),
                accountService = accountServiceInstance,
                context = context
            ).also {
                notificationService = it
            }
        }
    }
}
