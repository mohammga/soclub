package com.example.soclub.service.impl

import android.content.Context
import com.example.soclub.R
import com.example.soclub.service.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class LocationServiceImpl @Inject constructor(
    private val client: OkHttpClient,
    private val context: Context
) : LocationService {

    override suspend fun fetchMunicipalities(): Flow<List<String>> = flow {
        val url = "https://api.kartverket.no/kommuneinfo/v1/kommuner"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
                throw Exception(context.getString(R.string.error_fetch_municipalities_failed, response.message))

        }

        val responseBody = response.body ?: throw Exception(context.getString(R.string.error_fetch_municipalities_failed))
        val responseString = responseBody.string()

        try {
            val json = JSONArray(responseString)
            val municipalities = List(json.length()) { index ->
                val kommuneObj = json.getJSONObject(index)
                kommuneObj.getString("kommunenavnNorsk")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
            }
            emit(municipalities)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_parse_municipalities_response, e.message), e)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun fetchAddressSuggestions(
        streetName: String,
        houseNumber: String?,
        municipality: String
    ): Flow<List<String>> = flow {
        val url = buildString {
            append("https://ws.geonorge.no/adresser/v1/sok")
            append("?fuzzy=true")
            append("&adressenavn=$streetName")
            if (!houseNumber.isNullOrEmpty()) append("&nummer=$houseNumber")
            append("&kommunenavn=$municipality")
            append("&utkoordsys=4258&treffPerSide=1000&asciiKompatibel=true")
        }
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception(context.getString(R.string.error_fetch_address_suggestions_failed, response.message))
        }

        val responseBody = response.body ?: throw Exception(context.getString(R.string.error_fetch_address_suggestions_failed))
        val responseString = responseBody.string()

        try {
            val json = JSONObject(responseString)
            val addresses = json.getJSONArray("adresser").let { addressArray ->
                List(addressArray.length()) { index ->
                    addressArray.getJSONObject(index).getString("adressetekst")
                }
            }
            emit(addresses)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_parse_address_suggestions_response, e.message), e)
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun fetchPostalCodeForAddress(
        address: String,
        municipality: String
    ): Flow<String> = flow {
        val url =
            "https://ws.geonorge.no/adresser/v1/sok?fuzzy=false&adressetekst=$address&kommunenavn=$municipality&utkoordsys=4258&treffPerSide=1&asciiKompatibel=true"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception(context.getString(R.string.error_fetch_postal_code_failed, response.message))
        }
        val responseBody = response.body ?: throw Exception(context.getString(R.string.error_fetch_postal_code_failed))

        val responseString = responseBody.string()

        try {
            val json = JSONObject(responseString)
            val addresses = json.getJSONArray("adresser")
            if (addresses.length() > 0) {
                val postalCode = addresses.getJSONObject(0).getString("postnummer")
                emit(postalCode)
            } else {
                throw Exception(context.getString(R.string.error_fetch_postal_code_failed))
            }
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_parse_postal_code_response, e.message), e)
        }
    }.flowOn(Dispatchers.IO)
}


