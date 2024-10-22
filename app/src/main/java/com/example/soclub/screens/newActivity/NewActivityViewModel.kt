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
import org.json.JSONArray
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

    // Liste som lagrer alle kommuner
    private var kommuner: List<String> = listOf()

    init {
        // Last inn kommuner ved oppstart
        loadKommuner()
    }

    // Funksjon for å hente kommuner fra Kartverket API
    private fun loadKommuner() {
        viewModelScope.launch {
            try {
                val url = "https://api.kartverket.no/kommuneinfo/v1/kommuner"
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val json = JSONArray(responseString)

                        // Ekstrakt kommunenavn fra JSON-responsen
                        kommuner = List(json.length()) { index ->
                            val kommuneObj = json.getJSONObject(index)
                            kommuneObj.getString("kommunenavnNorsk").uppercase()
                        }
                    }
                } else {
                    Log.e("NewActivityViewModel", "Error loading kommuner: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error: ${e.message}")
            }
        }
    }


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

    // Funksjon for å håndtere input i LocationField og gi forslag fra kommuner-listen
    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(location = newValue)

        if (newValue.length >= 2) {
            val suggestions = kommuner.filter { it.startsWith(newValue.uppercase()) }
            uiState.value = uiState.value.copy(locationSuggestions = suggestions)
        } else {
            uiState.value = uiState.value.copy(locationSuggestions = emptyList())
        }

        // Tøm adresse- og postkodesuggesjoner når lokasjonen endres
        uiState.value = uiState.value.copy(addressSuggestions = emptyList(), postalCodeSuggestions = emptyList())
    }

    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(address = newValue)
        fetchAddressSuggestions(newValue)
    }

    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(address = selectedAddress)

        // Hent postnummer basert på valgt adresse og kommune
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
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
                // Bygg URL-en for å tillate fuzzy søk og bruke adressetekst som inneholder filter
                val url = "https://ws.geonorge.no/adresser/v1/sok" +
                        "?fuzzy=true" +  // Tillat mer fleksible treff
                        "&adressetekst=$query" +  // Søk med adressetekst
                        "&kommunenavn=$kommune" +  // Filtrer på valgt kommune
                        "&utkoordsys=4258" +  // Koordinatsystem
                        "&treffPerSide=12" +  // Øk antall treff per side for å få flere adresser
                        "&side=0" +  // Første side med resultater
                        "&asciiKompatibel=true"  // Håndtering av spesialtegn

                Log.d("NewActivityViewModel", "Fetching address suggestions from: $url")

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
                            // Returner kun adresser fra riktig kommune
                            if (addressKommune.equals(kommune, ignoreCase = true)) {
                                address.getString("adressetekst")
                            } else {
                                null
                            }
                        }.filterNotNull()
                    }.distinct()

                    Log.d("NewActivityViewModel", "Fetched address suggestions: $suggestions")
                    uiState.value = uiState.value.copy(addressSuggestions = suggestions)
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching address suggestions: ${e.message}")
            }
        }
    }



    private fun fetchPostalCodeForAddress(address: String, kommune: String) {
        viewModelScope.launch {
            try {
                // Bygg URL-en for å hente postnummer basert på adressen og kommunenavnet
                val url = "https://ws.geonorge.no/adresser/v1/sok" +
                        "?fuzzy=false" +  // Kan settes til true hvis du ønsker å tillate små feil
                        "&adressetekst=$address" +  // Søk etter spesifikk adresse
                        "&kommunenavn=$kommune" +  // Kommunenavn for mer presist resultat
                        "&utkoordsys=4258" +  // Koordinatsystem
                        "&treffPerSide=1" +  // Vi trenger kun én presis treff
                        "&side=0" +  // Første side med resultater
                        "&asciiKompatibel=true"  // ASCII-kompatibel for håndtering av spesialtegn

                Log.d("NewActivityViewModel", "Fetching postal code from: $url")  // Logg forespørselen

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

                    // Hvis det er treff, hent postnummer
                    if (addresses.length() > 0) {
                        val postalCode = addresses.getJSONObject(0).getString("postnummer")
                        Log.d("NewActivityViewModel", "Fetched postal code: $postalCode")

                        // Oppdater state med postnummeret
                        uiState.value = uiState.value.copy(postalCode = postalCode)
                    } else {
                        Log.e("NewActivityViewModel", "No addresses found for: $address, $kommune")
                    }
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching postal code: ${e.message}")
            }
        }
    }

}



