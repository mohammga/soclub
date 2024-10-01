package com.example.soclub.service

interface StorageService {
    suspend fun <T : Any> createDocument(collection: String, data: T)
    suspend fun <T> readDocument(collection: String, documentId: String, clazz: Class<T>): T?
    suspend fun <T : Any> updateDocument(collection: String, documentId: String, data: T)
    suspend fun deleteDocument(collection: String, documentId: String)

}

