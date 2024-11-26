package com.example.soclub.service

import com.example.soclub.models.User
import com.example.soclub.models.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining account-related operations for the SoClub application.
 *
 * This interface abstracts the authentication and user management functionalities,
 * allowing different implementations (e.g., Firebase, custom backend) to be used interchangeably.
 */
interface AccountService {

    /**
     * The unique identifier of the currently authenticated user.
     *
     * @return A [String] representing the current user's ID.
     */
    val currentUserId: String

    /**
     * Indicates whether a user is currently authenticated.
     *
     * @return `true` if a user is authenticated, `false` otherwise.
     */
    val hasUser: Boolean

    /**
     * A [Flow] emitting the current authenticated [User] details.
     *
     * This flow updates in real-time as the user's authentication state changes.
     *
     * @return A [Flow] emitting [User] objects.
     */
    val currentUser: Flow<User>

    /**
     * Authenticates a user using their email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param onResult A callback invoked upon completion.
     * Passes `null` if authentication is successful, or a [Throwable] if an error occurs.
     */
    suspend fun authenticateWithEmail(
        email: String,
        password: String,
        onResult: (Throwable?) -> Unit
    )

    /**
     * Creates a new user account using email and password, along with additional user information.
     *
     * @param email The user's email address.
     * @param password The user's chosen password.
     * @param firstname The user's first name.
     * @param lastname The user's last name.
     * @param age The user's age.
     *
     * Passes `null` if account creation is successful, or a [Throwable] if an error occurs.
     */
    suspend fun createEmailAccount(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int
    )

    /**
     * Signs out the currently authenticated user.
     *
     * This operation clears the user's authentication state and related data.
     *
     * @throws [Exception] if the sign-out process fails.
     */
    suspend fun signOut()

    /**
     * Retrieves the information of the currently authenticated user.
     *
     * @return A [UserInfo] object containing the user's details.
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun getUserInfo(): UserInfo

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the reset link to.
     *
     */
    suspend fun sendPasswordResetEmail(
        email: String
    )

    /**
     * Updates the profile information of the currently authenticated user.
     *
     * @param firstname The new first name.
     * @param lastname The new last name.
     * @param imageUrl The URL of the new profile image.
     *.
     */
    suspend fun updateProfile(
        firstname: String,
        lastname: String,
        imageUrl: String,
    )
    /**
     * Changes the password of the currently authenticated user.
     *
     * @param oldPassword The user's current password.
     * @param newPassword The user's new desired password.
     *
     */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
    )
}
