package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.R
import com.example.soclub.models.User
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : AccountService {


    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<User>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser?.let {
                    User(
                        id = it.uid,
                        isAnonymous = it.isAnonymous
                    )
                } ?: User())
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override suspend fun getUserInfo(): UserInfo {
        try {
            val userId = currentUserId
            val documentSnapshot = firestore.collection("users").document(userId).get().await()

            return documentSnapshot.toObject(UserInfo::class.java)
                ?: throw Exception(context.getString(R.string.error_user_data_not_found))
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_fetching_user_info, e.message), e)
        }
    }

override suspend fun authenticateWithEmail(
    email: String,
    password: String,
    onResult: (Throwable?) -> Unit
) {
    try {
        auth.signInWithEmailAndPassword(email, password).await()
        onResult(null)
    } catch (e: Exception) {
        onResult(e)
    }
}
    override suspend fun createEmailAccount(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int
    ) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception(context.getString(R.string.error_user_creation_failed))

            val userData = hashMapOf(
                "email" to email,
                "firstname" to firstname,
                "lastname" to lastname,
                "age" to age
            )
            firestore.collection("users").document(user.uid).set(userData).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw Exception(context.getString(R.string.user_already_registered))
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_account_creation_failed, e.message), e)
        }
    }


    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_sign_out_failed, e.message), e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_password_reset_failed, e.message), e)
        }
    }


    override suspend fun updateProfile(
        firstname: String,
        lastname: String,
        imageUrl: String
    ) {
        val userId = currentUserId
        val updates = hashMapOf(
            "firstname" to firstname,
            "lastname" to lastname,
            "imageUrl" to imageUrl
        )

        try {
            firestore.collection("users").document(userId).update(updates as Map<String, Any>).await()
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_profile_update_failed, e.message), e)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: throw Exception(context.getString(R.string.error_user_not_logged_in))

        try {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_password_change_failed, e.message), e)
        }
    }

}
