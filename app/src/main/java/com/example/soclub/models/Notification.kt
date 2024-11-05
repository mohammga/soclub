package com.example.soclub.models

data class Notification(
    val userId: String = "",
    val activityId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val message: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "activityId" to activityId,
            "timestamp" to timestamp,
            "message" to message
        )
    }
}
