package com.example.soclub.service.impl

import com.example.soclub.models.User
import com.example.soclub.models.UserInfo
import com.example.soclub.service.AccountService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        return documentSnapshot.toObject(UserInfo::class.java) ?: throw Exception("User data not found")
    }

    override suspend fun createAnonymousAccount() {
        auth.signInAnonymously().await()
    }

    override suspend fun authenticateWithEmail(
        email: String,
        password: String,
        onResult: (Throwable?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { onResult(it.exception) }.await()
    }

    override suspend fun createEmailAccount(
        email: String,
        password: String,
        name: String,
        onResult: (Throwable?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "name" to name
                        )
                        firestore.collection("users").document(it.uid)
                            .set(userData)
                            .addOnCompleteListener { firestoreTask ->
                                onResult(firestoreTask.exception)
                            }
                    }
                } else {
                    onResult(task.exception)
                }
            }.await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { onResult(it.exception) }
            .await()
    }

    // Implementasjon for å oppdatere brukerprofilen (navn og e-post)
    override suspend fun updateProfile(name: String, email: String, onResult: (Throwable?) -> Unit) {
        val user = auth.currentUser
        user?.let {
            val profileUpdates = hashMapOf(
                "name" to name
            )
            // Oppdater Firestore med nytt navn
            firestore.collection("users").document(it.uid).update(profileUpdates as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Oppdater e-posten i Firebase Authentication
                        it.updateEmail(email)
                            .addOnCompleteListener { emailUpdateTask ->
                                onResult(emailUpdateTask.exception)
                            }
                    } else {
                        onResult(task.exception)
                    }
                }.await()
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Throwable?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    // Implementasjon for å endre passord
    suspend fun updatePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Throwable?) -> Unit
    ) {
        val user = auth.currentUser
        user?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, oldPassword)

            // Re-autentiser brukeren med det gamle passordet før du endrer passordet
            it.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        it.updatePassword(newPassword)
                            .addOnCompleteListener { passwordUpdateTask ->
                                onResult(passwordUpdateTask.exception)
                            }
                    } else {
                        onResult(reauthTask.exception)
                    }
                }.await()
        }
    }
}
