package com.example.soclub.service

import kotlinx.coroutines.flow.Flow

/**
 * Interface defining location-related operations for the SoClub application.
 *
 * This interface abstracts the functionalities for managing location data,
 * allowing different implementations (e.g., Google Maps API, OpenStreetMap, custom backend) to be used interchangeably.
 */
interface LocationService {

    /**
     * Fetches a list of municipalities.
     *
     * This function retrieves all available municipalities that users can select or use
     * for address-related operations within the application.
     *
     * @return A [Flow] emitting a [List] of [String] objects, each representing a municipality name.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun fetchMunicipalities(): Flow<List<String>>

    /**
     * Fetches address suggestions based on the provided street name, house number, and municipality.
     *
     * This function provides autocomplete suggestions for addresses as the user types,
     * enhancing the user experience by reducing input effort and minimizing errors.
     *
     * @param streetName The name of the street for which to fetch address suggestions.
     * @param houseNumber An optional house number to narrow down address suggestions.
     * @param municipality The municipality within which to search for address suggestions.
     *
     * @return A [Flow] emitting a [List] of [String] objects, each representing a suggested address.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun fetchAddressSuggestions(
        streetName: String,
        houseNumber: String?,
        municipality: String
    ): Flow<List<String>>

    /**
     * Fetches the postal code for a given address and municipality.
     *
     * This function retrieves the postal code associated with a specific address within a municipality,
     * facilitating accurate and standardized address information.
     *
     * @param address The full address for which to fetch the postal code.
     * @param municipality The municipality where the address is located.
     *
     * @return A [Flow] emitting a nullable [String] representing the postal code.
     *         Returns `null` if the postal code cannot be found.
     *
     * @throws [Exception] if the retrieval process fails.
     */
    suspend fun fetchPostalCodeForAddress(
        address: String,
        municipality: String
    ): Flow<String?>
}
