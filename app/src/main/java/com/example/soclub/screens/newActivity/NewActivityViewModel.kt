package com.example.soclub.screens.newActivity

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import android.util.Log
import com.example.soclub.R
import com.example.soclub.models.createActivity
import com.example.soclub.service.ActivityService
import com.example.soclub.service.AccountService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.random.Random




data class NewActivityState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val address: String = "",
    val postalCode: String = "",
    val maxParticipants: String = "",
    val ageLimit: String = "",
    val imageUrl: String = "",
    val date: Timestamp? = Timestamp.now(),
    val startTime: String = "",
    val errorMessage: Int? = null,
    val locationSuggestions: List<String> = emptyList(),
    val addressSuggestions: List<String> = emptyList(),
    val postalCodeSuggestions: List<String> = emptyList()
)



@HiltViewModel
class NewActivityViewModel @Inject constructor(
    private val activityService: ActivityService,
    private val accountService: AccountService
) : ViewModel() {

    var uiState = mutableStateOf(NewActivityState())
        private set

    private val client = OkHttpClient()

    fun onTitleChange(newValue: String) {
        uiState.value = uiState.value.copy(title = newValue)
    }

    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue)
    }

    fun onImageSelected(imagePath: String) {
        uiState.value = uiState.value.copy(imageUrl = imagePath)
    }

    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue)
    }

    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(location = newValue)
        fetchKommuneSuggestions(newValue)

        // Clear address and postal code suggestions when the location changes
        uiState.value = uiState.value.copy(addressSuggestions = emptyList(), postalCodeSuggestions = emptyList())
    }

    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(address = newValue)
        fetchAddressSuggestions(newValue)
    }

    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(address = selectedAddress)
        fetchPostalCodeForAddress(selectedAddress)
    }


    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue)
    }

    fun onAgeLimitChange(newValue: String) {
        uiState.value = uiState.value.copy(ageLimit = newValue)
    }

    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue)
    }

    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue)
    }

    private fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.d("NewActivityViewModel", "Image uploaded successfully")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("NewActivityViewModel", "Image URL fetched: $uri")
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NewActivityViewModel", "Error uploading image: ${exception.message}")
                onError(exception)
            }
    }

    fun onPublishClick(navController: NavController) {
        Log.d("NewActivityViewModel", "Publish button clicked")

        if (uiState.value.title.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_title_required)
            return
        }

        if (uiState.value.category.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_category_required)
            return
        }

        val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode}, ${uiState.value.location}"
        val creatorId = accountService.currentUserId
        val timestampDate = uiState.value.date ?: Timestamp.now()
        val startTime = uiState.value.startTime

        if (uiState.value.imageUrl.isNotBlank()) {
            uploadImageToFirebase(
                Uri.parse(uiState.value.imageUrl),
                onSuccess = { imageUrl ->
                    createActivityAndNavigate(navController, imageUrl, combinedLocation, timestampDate, startTime, creatorId)
                },
                onError = { error ->
                    Log.e("NewActivityViewModel", "Error uploading image: ${error.message}")
                }
            )
        } else {
            createActivityAndNavigate(navController, "", combinedLocation, timestampDate, startTime, creatorId)
        }
    }

    private fun createActivityAndNavigate(
        navController: NavController,
        imageUrl: String,
        combinedLocation: String,
        date: Timestamp,
        startTime: String,
        creatorId: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("NewActivityViewModel", "Creating activity with imageUrl: $imageUrl and location: $combinedLocation")

                val uniqueCode = generateUniqueCode() // Generate the unique code

                val newActivity = createActivity(
                    createdAt = Timestamp.now(),
                    lastUpdated = Timestamp.now(),
                    creatorId = creatorId,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = combinedLocation,
                    maxParticipants = uiState.value.maxParticipants.toIntOrNull() ?: 0,
                    ageGroup = uiState.value.ageLimit.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    date = date,
                    startTime = startTime,
                    soclubCode = uniqueCode // Adder unike coden "Soclubkode"
                )

                activityService.createActivity(uiState.value.category, newActivity)
                Log.d("NewActivityViewModel", "Activity created successfully with code $uniqueCode")

                navController.navigate("home")
                Log.d("NewActivityViewModel", "Navigation to home successful")
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error creating activity: ${e.message}")
            }
        }
    }

    private fun generateUniqueCode(): Int {
        return Random.nextInt(10000000, 99999999) // gennrener 8 shiffer "code"
    }

    private fun fetchKommuneSuggestions(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                uiState.value = uiState.value.copy(locationSuggestions = emptyList())
                Log.d("NewActivityViewModel", "Query too short for kommune suggestions")
                return@launch
            }
            try {
                val url = "https://ws.geonorge.no/adresser/v1/sok?fuzzy=true&kommunenavn=$query&utkoordsys=4258&treffPerSide=1000&asciiKompatibel=true"
                Log.d("NewActivityViewModel", "Fetching kommune suggestions from: $url")

                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

                if (!response.isSuccessful) {
                    Log.e("NewActivityViewModel", "Unsuccessful response: ${response.code}")
                    return@launch
                }

                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    Log.d("NewActivityViewModel", "Response received: $responseString")

                    // Parse JSON and extract the kommunenavn
                    val json = JSONObject(responseString)
                    val suggestions = json.getJSONArray("adresser").let { addresses ->
                        List(addresses.length()) { index ->
                            val address = addresses.getJSONObject(index)
                            address.optString("kommunenavn", "")
                        }.filter { it.isNotBlank() } // Filter ut tomme kommunenavn
                    }.distinct() // Ensuring uniqueness

                    Log.d("NewActivityViewModel", "Parsed kommune suggestions: $suggestions")
                    uiState.value = uiState.value.copy(locationSuggestions = suggestions)
                    Log.d("NewActivityViewModel", "Kommune suggestions updated in uiState")
                } ?: run {
                    Log.e("NewActivityViewModel", "Response body is null")
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching kommune suggestions: ${e.message}")
            }
        }
    }

    private fun fetchAddressSuggestions(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                uiState.value = uiState.value.copy(addressSuggestions = emptyList())
                return@launch
            }
            val kommune = uiState.value.location
            if (kommune.isEmpty()) {
                Log.e("NewActivityViewModel", "Kommune is not selected")
                return@launch
            }
            try {
                val url = "https://ws.geonorge.no/adresser/v1/sok?sok=$query&kommunenavn=$kommune&treffPerSide=10"
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (!response.isSuccessful) {
                    Log.e("NewActivityViewModel", "Unsuccessful response: ${response.code}")
                    return@launch
                }

                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val json = JSONObject(responseString)
                    val suggestions = json.getJSONArray("adresser").let { addresses ->
                        List(addresses.length()) { index ->
                            val address = addresses.getJSONObject(index)
                            val addressKommune = address.optString("kommunenavn", "")
                            if (addressKommune.equals(kommune, ignoreCase = true)) {
                                address.getString("adressetekst")
                            } else {
                                null
                            }
                        }.filterNotNull()
                    }.distinct() // fjerne doblikater
                    uiState.value = uiState.value.copy(addressSuggestions = suggestions)
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching address suggestions: ${e.message}")
            }
        }
    }

    private fun fetchPostalCodeForAddress(address: String) {
        viewModelScope.launch {
            try {
                val url = "https://ws.geonorge.no/adresser/v1/sok?sok=$address&treffPerSide=1"
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (!response.isSuccessful) {
                    Log.e("NewActivityViewModel", "Unsuccessful response: ${response.code}")
                    return@launch
                }

                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val json = JSONObject(responseString)
                    val addresses = json.getJSONArray("adresser")
                    if (addresses.length() > 0) {
                        val postalCode = addresses.getJSONObject(0).getString("postnummer")
                        uiState.value = uiState.value.copy(postalCode = postalCode)
                    }
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching postal code for address: ${e.message}")
            }
        }
    }
}



