package com.example.soclub.utils

import android.content.Context
import com.example.soclub.service.AccountService
import com.example.soclub.service.NotificationService
import com.example.soclub.service.impl.AccountServiceImpl
import com.example.soclub.service.impl.NotificationServiceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



/**
 * ServiceLocator is a utility object responsible for providing service instances
 * with a singleton pattern for efficient resource management and dependency injection.
 */
object ServiceLocator {

    private var notificationService: NotificationService? = null
    private var accountService: AccountService? = null

    /**
     * Provides an instance of AccountService. If the instance does not already exist,
     * it initializes it with the required dependencies.
     *
     * @param context The context required for initializing the AccountService implementation.
     * @return A singleton instance of AccountService.
     */
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

    /**
     * Provides an instance of NotificationService. If the instance does not already exist,
     * it initializes it with the required dependencies.
     *
     * @param context The context required for initializing the NotificationService implementation.
     * @return A singleton instance of NotificationService.
     */
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

