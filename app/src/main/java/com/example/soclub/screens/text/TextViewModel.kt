package com.example.soclub.screens.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.soclub.models.TextEntry
import com.example.soclub.service.AccountService
import com.example.soclub.service.StorageService
import com.example.soclub.components.navigation.AppScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextViewModel @Inject constructor(
    private val storageService: StorageService,
    private val accountService: AccountService
) : ViewModel() {

    var textEntry by mutableStateOf<TextEntry?>(null)
        private set

    var inputText by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchText()
    }

    fun onInputChange(newText: String) {
        inputText = newText
    }

    fun fetchText() {
        viewModelScope.launch {
            isLoading = true
            textEntry = storageService.readDocument(
                collection = "texts",
                documentId = "display_text",
                clazz = TextEntry::class.java
            )
            isLoading = false
        }
    }

    fun uploadText() {
        viewModelScope.launch {
            isLoading = true
            val newTextEntry = TextEntry(content = inputText)
            storageService.updateDocument(
                collection = "texts",
                documentId = newTextEntry.id,
                data = newTextEntry
            )
            fetchText()
            isLoading = false
        }
    }

    fun deleteText() {
        viewModelScope.launch {
            isLoading = true
            storageService.deleteDocument(
                collection = "texts",
                documentId = "display_text"
            )
            textEntry = null
            isLoading = false
        }
    }

    fun onSignOut(navController: NavController) {
        viewModelScope.launch {
            accountService.signOut()
            navController.navigate(AppScreens.LOGIN_SELECTION.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
