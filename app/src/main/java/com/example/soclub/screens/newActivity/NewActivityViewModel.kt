package com.example.soclub.screens.newActivity

import android.icu.util.Calendar
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
    val postalCodeSuggestions: List<String> = emptyList(),
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val locationError: String? = null,
    val addressError: String? = null,
    val postalCodeError: String? = null,
    val maxParticipantsError: String? = null,
    val ageLimitError: String? = null,
    val dateError: String? = null,
    val startTimeError: String? = null,
    val selectedImageUri: Uri? = null
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

                        kommuner = List(json.length()) { index ->
                            val kommuneObj = json.getJSONObject(index)
                            val name = kommuneObj.getString("kommunenavnNorsk")
                            name.lowercase().replaceFirstChar { it.uppercase() }
                        }

                        Log.d("NewActivityViewModel", "Antall kommuner lastet: ${kommuner.size}")
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
        uiState.value = uiState.value.copy(title = newValue, titleError = null)
    }

    fun onDescriptionChange(newValue: String) {
        uiState.value = uiState.value.copy(description = newValue, descriptionError = null)
    }

    fun onImageSelected(uri: Uri?) {
        uiState.value = uiState.value.copy(selectedImageUri = uri, imageUrl = uri?.toString() ?: "")
    }

    fun onCategoryChange(newValue: String) {
        uiState.value = uiState.value.copy(category = newValue, categoryError = null)
    }

    // Funksjon for å håndtere input i LocationField og gi forslag fra kommuner-listen
    fun onLocationChange(newValue: String) {
        uiState.value = uiState.value.copy(location = newValue, locationError = null)

        if (newValue.length >= 2) {
            val suggestions = kommuner.filter { it.startsWith(newValue, ignoreCase = true) }
            uiState.value = uiState.value.copy(locationSuggestions = suggestions)
        } else {
            uiState.value = uiState.value.copy(locationSuggestions = emptyList())
        }
    }

    fun onAddressChange(newValue: String) {
        uiState.value = uiState.value.copy(address = newValue, addressError = null)
        fetchAddressSuggestions(newValue)
    }

    fun onAddressSelected(selectedAddress: String) {
        uiState.value = uiState.value.copy(address = selectedAddress, addressError = null)

        // Hent postnummer basert på valgt adresse og kommune
        fetchPostalCodeForAddress(selectedAddress, uiState.value.location)
    }

    fun onMaxParticipantsChange(newValue: String) {
        uiState.value = uiState.value.copy(maxParticipants = newValue, maxParticipantsError = null)
    }

    fun onAgeLimitChange(newValue: String) {
        val age = newValue.toIntOrNull()

        if (age != null && age > 100) {
            uiState.value = uiState.value.copy(errorMessage = R.string.error_age_limit_exceeded)
        } else {
            uiState.value = uiState.value.copy(ageLimit = newValue, errorMessage = null, ageLimitError = null)
        }
    }

    fun onDateChange(newValue: Timestamp) {
        uiState.value = uiState.value.copy(date = newValue, dateError = null)
    }

    fun onStartTimeChange(newValue: String) {
        uiState.value = uiState.value.copy(startTime = newValue, startTimeError = null)
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
        var hasError = false
        var titleError: String? = null
        var descriptionError: String? = null
        var categoryError: String? = null
        var locationError: String? = null
        var addressError: String? = null
        var postalCodeError: String? = null
        var maxParticipantsError: String? = null
        var ageLimitError: String? = null
        var dateError: String? = null
        var startTimeError: String? = null

        if (uiState.value.title.isBlank()) {
            titleError = "Du må fylle inn tittel"
            hasError = true
        }
        if (uiState.value.description.isBlank()) {
            descriptionError = "Du må fylle inn beskrivelse"
            hasError = true
        }
        if (uiState.value.category.isBlank()) {
            categoryError = "Du må velge kategori"
            hasError = true
        }
        if (uiState.value.location.isBlank()) {
            locationError = "Du må velge sted"
            hasError = true
        }
        if (uiState.value.address.isBlank()) {
            addressError = "Du må velge adresse"
            hasError = true
        }
        if (uiState.value.postalCode.isBlank()) {
            postalCodeError = "Postnummer er påkrevd"
            hasError = true
        }
        if (uiState.value.maxParticipants.isBlank()) {
            maxParticipantsError = "Du må fylle inn maks antall deltakere"
            hasError = true
        } else if (uiState.value.maxParticipants.toIntOrNull() == null) {
            maxParticipantsError = "Må være et tall"
            hasError = true
        }
        if (uiState.value.ageLimit.isBlank()) {
            ageLimitError = "Du må fylle inn aldersgrense"
            hasError = true
        } else if (uiState.value.ageLimit.toIntOrNull() == null) {
            ageLimitError = "Må være et tall"
            hasError = true
        }
        if (uiState.value.date == null) {
            dateError = "Du må velge dato"
            hasError = true
        }
        if (uiState.value.startTime.isBlank()) {
            startTimeError = "Du må velge starttidspunkt"
            hasError = true
        }

        if (uiState.value.date != null && uiState.value.startTime.isNotBlank()) {
            val currentTimeMillis = System.currentTimeMillis()

            val selectedDate = uiState.value.date!!.toDate()
            val startTimeParts = uiState.value.startTime.split(":")
            if (startTimeParts.size == 2) {
                val hour = startTimeParts[0].toIntOrNull()
                val minute = startTimeParts[1].toIntOrNull()

                if (hour != null && minute != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = selectedDate
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val selectedDateTimeInMillis = calendar.timeInMillis

                    if (selectedDateTimeInMillis < currentTimeMillis) {
                        dateError = "Dato og starttidspunkt kan ikke være i fortiden"
                        startTimeError = "Dato og starttidspunkt kan ikke være i fortiden"
                        hasError = true
                    }
                } else {
                    startTimeError = "Ugyldig starttidspunkt"
                    hasError = true
                }
            } else {
                startTimeError = "Ugyldig starttidspunkt"
                hasError = true
            }
        }


        // Oppdater uiState med feilmeldingene
        uiState.value = uiState.value.copy(
            titleError = titleError,
            descriptionError = descriptionError,
            categoryError = categoryError,
            locationError = locationError,
            addressError = addressError,
            postalCodeError = postalCodeError,
            maxParticipantsError = maxParticipantsError,
            ageLimitError = ageLimitError,
            dateError = dateError,
            startTimeError = startTimeError
        )

        if (hasError) {
            return
        }

        val combinedLocation = "${uiState.value.address}, ${uiState.value.postalCode} ${uiState.value.location}"
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

                val uniqueCode = generateUniqueCode() // Genererer unike koden 

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
                    soclubCode = uniqueCode // Adder unike koden "Soclubkode"
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
        return Random.nextInt(10000000, 99999999) // Genererer 8-sifret kode
    }

    private fun fetchAddressSuggestions(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                uiState.value = uiState.value.copy(addressSuggestions = emptyList())
                return@launch
            }

            // Regex for å finne gatenavn og husnummer
            val regex = """^([a-zA-ZøæåØÆÅ\s]+)(\d+.*)?$""".toRegex()
            val matchResult = regex.find(query.trim())

            val streetName = matchResult?.groups?.get(1)?.value?.trim() ?: ""
            val houseNumber = matchResult?.groups?.get(2)?.value?.trim() ?: ""

            // Sjekk om brukeren har skrevet et gyldig gatenavn
            if (streetName.isEmpty()) {
                Log.e("NewActivityViewModel", "Ugyldig gatenavn")
                return@launch
            }

            val kommune = uiState.value.location
            if (kommune.isEmpty()) {
                Log.e("NewActivityViewModel", "Kommune er ikke valgt")
                return@launch
            }

            try {
                val allSuggestions = mutableListOf<String>()
                var page = 0
                var hasMoreResults = true

                while (hasMoreResults) {
                    // Konstruer API-forespørselen med gatenavn og husnummer hvis husnummer er skrevet inn
                    val url = "https://ws.geonorge.no/adresser/v1/sok" +
                            "?fuzzy=true" +  // Tillat fleksible treff
                            "&adressenavn=$streetName" +  // Gatenavn
                            if (houseNumber.isNotEmpty()) "&nummer=$houseNumber" else "" +  // Husnummer hvis det er skrevet
                                    "&kommunenavn=$kommune" +  // Kommune
                                    "&utkoordsys=4258" +  // Koordinatsystem
                                    "&treffPerSide=12" +  // Øk antall treff per side
                                    "&side=$page" +  // Paginering
                                    "&asciiKompatibel=true"  // Håndter spesialtegn

                    Log.d("NewActivityViewModel", "Henter adresseforslag fra: $url")

                    val request = Request.Builder().url(url).build()
                    val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

                    if (!response.isSuccessful) {
                        Log.e("NewActivityViewModel", "Feil ved forespørsel: ${response.code}")
                        return@launch
                    }

                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val json = JSONObject(responseString)
                        val suggestions = json.getJSONArray("adresser").let { addresses ->
                            List(addresses.length()) { index ->
                                val address = addresses.getJSONObject(index)
                                val adressetekst = address.getString("adressetekst")
                                adressetekst
                            }
                        }

                        allSuggestions.addAll(suggestions)

                        // Hvis vi får mindre enn 100 treff, stopp paginering
                        hasMoreResults = suggestions.size == 12
                        page++
                    }
                }

                Log.d("NewActivityViewModel", "Hentede adresseforslag: $allSuggestions")
                uiState.value = uiState.value.copy(addressSuggestions = allSuggestions)

            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Feil ved henting av adresseforslag: ${e.message}")
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
                        uiState.value = uiState.value.copy(postalCode = postalCode, postalCodeError = null)
                    } else {
                        Log.e("NewActivityViewModel", "No addresses found for: $address, $kommune")
                        uiState.value = uiState.value.copy(postalCode = "", postalCodeError = "Fant ikke postnummer")
                    }
                }
            } catch (e: Exception) {
                Log.e("NewActivityViewModel", "Error fetching postal code: ${e.message}")
                uiState.value = uiState.value.copy(postalCode = "", postalCodeError = "Feil ved henting av postnummer")
            }
        }
    }
}
