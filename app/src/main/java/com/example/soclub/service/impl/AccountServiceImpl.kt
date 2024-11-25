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


/**
 * Implementation of the AccountService interface, providing user account management functionality.
 *
 * @property auth Instance of FirebaseAuth to manage authentication.
 * @property firestore Instance of FirebaseFirestore to manage user data in Firestore.
 * @property context Application context for accessing string resources.
 */
class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : AccountService {

    /**
     * Gets the current user's ID.
     */
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    /**
     * Checks if a user is currently logged in.
     */
    override val hasUser: Boolean
        get() = auth.currentUser != null


    /**
     * Provides a Flow that emits the current user object whenever authentication state changes.
     */
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


    /**
     * Fetches additional information about the current user from Firestore.
     *
     * @return A [UserInfo] object containing user data.
     * @throws Exception If the data cannot be fetched or parsed.
     */
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


    /**
     * Authenticates the user with email and password.
     *
     * @param email The email address.
     * @param password The password.
     * @param onResult Callback that returns any error encountered during authentication.
     */

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


    /**
     * Creates a new email account and stores additional user data in Firestore.
     *
     * @param email The email address.
     * @param password The password.
     * @param firstname The user's first name.
     * @param lastname The user's last name.
     * @param age The user's age.
     * @param onResult Callback that returns any error encountered during account creation.
     */

    override suspend fun createEmailAccount(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int,
        onResult: (Throwable?) -> Unit
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
            throw Exception(
                context.getString(R.string.error_account_creation_failed, e.message), e)
        }
    }


    /**
     * Signs out the currently logged-in user.
     *
     * @throws Exception If sign-out fails.
     */
    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_sign_out_failed, e.message), e)
        }
    }

    /**
     * Sends a password reset email to the specified address.
     *
     * @param email The email address.
     * @param onResult Callback that returns any error encountered during the operation.
     */
    override suspend fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_password_reset_failed, e.message), e)
        }
    }

    /**
     * Updates the user's profile information in Firestore.
     *
     * @param firstname The user's new first name.
     * @param lastname The user's new last name.
     * @param imageUrl The URL of the user's new profile image.
     * @param onResult Callback that returns any error encountered during the update.
     */

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


    /**
     * Changes the current user's password.
     *
     * @param oldPassword The current password.
     * @param newPassword The new password.
     * @param onResult Callback that returns any error encountered during the operation.
     * @throws Exception If the user is not logged in or the operation fails.
     */

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Throwable?) -> Unit
    ) {
        val user = auth.currentUser ?: throw Exception(context.getString(R.string.error_user_not_logged_in))


        try {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
        } catch (e: Exception) {
            throw Exception(
                context.getString(R.string.error_password_change_failed, e.message), e)
        }
    }
}
