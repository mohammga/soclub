package com.example.soclub.service

import kotlinx.coroutines.flow.Flow

interface LocationService {
    suspend fun fetchMunicipalities(): Flow<List<String>>
    suspend fun fetchAddressSuggestions(streetName: String, houseNumber: String?, municipality: String): Flow<List<String>>
    suspend fun fetchPostalCodeForAddress(address: String, municipality: String): Flow<String?>
}
