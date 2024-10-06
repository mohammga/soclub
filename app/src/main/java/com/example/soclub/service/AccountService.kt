package com.example.soclub.service

import com.example.soclub.models.User
import com.example.soclub.models.UserInfo
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUserId: String
    val hasUser: Boolean
    val currentUser: Flow<User>
    suspend fun authenticateWithEmail(email: String, password: String, onResult: (Throwable?) -> Unit)
    suspend fun createEmailAccount(email: String, password: String, name: String, age: String, onResult: (Throwable?) -> Unit)
    suspend fun signOut()
    suspend fun getUserInfo(): UserInfo
    suspend fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit)
    suspend fun updateProfile(name: String, email: String, onResult: (Throwable?) -> Unit)
    suspend fun changePassword(oldPassword: String, newPassword: String, onResult: (Throwable?) -> Unit)
}
