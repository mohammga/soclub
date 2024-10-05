package com.example.soclub.models
//import com.google.firebase.firestore.FirebaseFirestore



data class Activity(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: String = "",
    val maxParticipants: String = "",
    val location: String = "",
    val restOfAddress: String = ""
)

//data class Activity2(
//    val imageUrl: String = "",
//    val title: String = "",
//    val description: String = "",
//    val ageGroup: String = "",
//    val maxParticipants: String = "",
//    val location: String = ""
//)

//fun sendAllCategoriesAndActivitiesToFirestore() {
//    // Firestore referanse
//    val db = FirebaseFirestore.getInstance()
//
//    // Definer kategoriene med tilhørende aktiviteter (uten 'id' som første parameter)
//    val categoryActivities = mapOf(
//        "Festivaler" to listOf(
//            Activity2("https://example.com/festival1.jpg", "Sommerfestival", "En morsom festival med konserter, mat og aktiviteter.", "Alle", "500", "Oslo sentrum, 0150 Oslo"),
//            Activity2("https://example.com/festival2.jpg", "Vinterfest", "En vinterfestival med is-skulpturer og konserter.", "Alle", "300", "Bergen sentrum, 5003 Bergen"),
//            Activity2("https://example.com/festival3.jpg", "Kulturfestival", "Opplev kulturelle opptredener og mat fra hele verden.", "Alle", "1000", "Trondheim Torg, 7013 Trondheim"),
//            Activity2("https://example.com/festival4.jpg", "Musikkfest", "En musikkfestival med nasjonale og internasjonale artister.", "18+", "2000", "Stavanger konserthus, 4005 Stavanger"),
//            Activity2("https://example.com/festival5.jpg", "Filmfestival", "En festival for filmelskere med premierer og visninger.", "Alle", "400", "Tromsø sentrum, 9008 Tromsø")
//        ),
//        "Forslag" to listOf(
//            Activity2("https://example.com/forslag1.jpg", "Tegnekonkurranse", "Bli med på en kreativ tegnekonkurranse for alle aldre.", "Alle", "50", "Paul Holmsens vei 9, 1613 Fredrikstad"),
//            Activity2("https://example.com/forslag2.jpg", "Litteraturklubb", "Diskusjoner og bokanmeldelser med en lokal bokklubb.", "18+", "20", "Deichman Bjørvika, 0150 Oslo"),
//            Activity2("https://example.com/forslag3.jpg", "Fotomaraton", "Delta i et fotomaraton og fang spennende øyeblikk i byen.", "Alle", "100", "Markens gate 1, 4610 Kristiansand"),
//            Activity2("https://example.com/forslag4.jpg", "Gatekunstverksted", "Lær å lage gatekunst med lokale kunstnere.", "12+", "30", "Bragernes Torg, 3017 Drammen"),
//            Activity2("https://example.com/forslag5.jpg", "Skrivekurs", "Et skrivekurs for de som vil forbedre sine skriveferdigheter.", "16+", "15", "Hamar kulturhus, 2317 Hamar")
//        ),
//        "Klatring" to listOf(
//            Activity2("https://example.com/klatring1.jpg", "Innendørs klatring", "Prøv ut innendørs klatring på vår klatrehall.", "Alle", "20", "Toppidrettssenteret, Sognsveien 228, 0863 Oslo"),
//            Activity2("https://example.com/klatring2.jpg", "Utendørs klatretur", "Bli med på en utendørs klatretur i naturen.", "18+", "10", "Svolvær, 8300 Lofoten"),
//            Activity2("https://example.com/klatring3.jpg", "Klatrekurs for nybegynnere", "Et kurs for de som vil lære å klatre fra bunnen av.", "Alle", "15", "Bergen Klatreklubb, 5005 Bergen"),
//            Activity2("https://example.com/klatring4.jpg", "Klatrekonkurranse", "Bli med på en spennende klatrekonkurranse.", "16+", "25", "Stavanger Klatresenter, 4015 Stavanger"),
//            Activity2("https://example.com/klatring5.jpg", "Fjellklatring", "Guidet fjellklatring for de som ønsker å utfordre seg selv.", "18+", "8", "Trollveggen, 6320 Rauma")
//        ),
//        "Mat" to listOf(
//            Activity2("https://example.com/mat1.jpg", "Matlagingskurs", "Lær å lage lokale retter med en profesjonell kokk.", "Alle", "12", "Mathallen Oslo, Vulkan 5, 0178 Oslo"),
//            Activity2("https://example.com/mat2.jpg", "Gourmetfestival", "En festival for alle matelskere med smaksprøver og konkurranser.", "Alle", "500", "Bryggen, 5003 Bergen"),
//            Activity2("https://example.com/mat3.jpg", "Bakeworkshop", "Bli med på en bakeworkshop og lær å lage fantastiske kaker.", "Alle", "20", "Torvet, 7011 Trondheim"),
//            Activity2("https://example.com/mat4.jpg", "Vin og mat sammenkobling", "Lær hvordan du kombinerer vin med ulike retter.", "18+", "30", "Clarion Hotel, 4006 Stavanger"),
//            Activity2("https://example.com/mat5.jpg", "Matmarked", "Besøk vårt matmarked med lokale produsenter.", "Alle", "1000", "Torvet, 4611 Kristiansand")
//        ),
//        "Reise" to listOf(
//            Activity2("https://example.com/reise1.jpg", "Guidet byvandring", "Utforsk byen med en guidet byvandring.", "Alle", "20", "Karl Johans gate 1, 0154 Oslo"),
//            Activity2("https://example.com/reise2.jpg", "Weekendtur til fjordene", "Opplev de norske fjordene på en uforglemmelig helgetur.", "Alle", "50", "Geirangerfjorden, 6216 Geiranger"),
//            Activity2("https://example.com/reise3.jpg", "Oppdagelsesferd til Nordkapp", "En eventyrlig tur til det nordligste punktet i Europa.", "Alle", "30", "Nordkapp, 9764 Honningsvåg"),
//            Activity2("https://example.com/reise4.jpg", "Safari i villmarken", "Bli med på safari og opplev den norske naturen.", "Alle", "10", "Finnmark, 9600 Hammerfest"),
//            Activity2("https://example.com/reise5.jpg", "Oppdag Skandinavias hovedsteder", "En tur gjennom de skandinaviske hovedstedene Oslo, Stockholm og København.", "Alle", "40", "Rådhusplassen, 0151 Oslo")
//        ),
//        "Trening" to listOf(
//            Activity2("https://example.com/trening1.jpg", "Yoga i parken", "Morgentrening med yoga under åpen himmel.", "Alle", "30", "Frognerparken, 0268 Oslo"),
//            Activity2("https://example.com/trening2.jpg", "Bootcamp", "En intensiv treningsøkt med profesjonelle instruktører.", "18+", "50", "Byparken, 5003 Bergen"),
//            Activity2("https://example.com/trening3.jpg", "Løpetrening for nybegynnere", "Lær å løpe riktig med veiledning fra en erfaren trener.", "Alle", "20", "Nidarosdomen, 7013 Trondheim"),
//            Activity2("https://example.com/trening4.jpg", "Styrketrening", "Bli sterkere med en treningsøkt tilpasset styrketrening.", "18+", "15", "Stavanger Stadion, 4020 Stavanger"),
//            Activity2("https://example.com/trening5.jpg", "Sykkeltur", "Utforsk byen på sykkel med en guide.", "Alle", "20", "Sognsvann, 0863 Oslo")
//        )
//    )
//
//    // Iterer gjennom kategoriene og aktivitetene
//    for ((category, activities) in categoryActivities) {
//        val collectionRef = db.collection("category").document(category).collection("activities")
//
//        for (activity in activities) {
//            // Bruker .add() for å la Firestore generere en unik ID
//            collectionRef.add(activity)
//                .addOnSuccessListener { documentReference ->
//                }
//                .addOnFailureListener { e ->
//                }
//        }
//    }
//}



//fun deleteAllActivitiesFromCategories() {
//    // Firestore referanse
//    val db = FirebaseFirestore.getInstance()
//
//    // Definer kategoriene som skal slettes
//    val categories = listOf("Festivaler", "Forslag", "Klatring", "Mat", "Reise", "Trening")
//
//    // Iterer gjennom kategoriene
//    for (category in categories) {
//        val collectionRef = db.collection("category").document(category).collection("activities")
//
//        // Hent alle aktivitetene i hver kategori
//        collectionRef.get()
//            .addOnSuccessListener { snapshot ->
//                for (document in snapshot.documents) {
//                    // Slett hvert dokument (aktivitet) i kategorien
//                    collectionRef.document(document.id).delete()
//                        .addOnSuccessListener {
//                        }
//                        .addOnFailureListener { e ->
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//            }
//    }
//}

