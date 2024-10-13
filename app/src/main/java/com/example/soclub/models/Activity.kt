package com.example.soclub.models

import com.google.firebase.firestore.FirebaseFirestore


data class Activity(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: Int = 0 ,
    val maxParticipants: Int = 0,
    val location: String = "",
    val restOfAddress: String = "",
    val date: String = "",
    val time: String= ""
)

data class createActivity(
    val creatorId: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: Int = 0 ,
    val maxParticipants: Int = 0,
    val location: String = "",
    val date: String = "",
    val time: String= ""
)

//data class Activity2(
//    val imageUrl: String = "",
//    val title: String = "",
//    val description: String = "",
//    val ageGroup: Int = 0,
//    val maxParticipants: Int = 0,
//    val location: String = ""
//)

//fun sendAllCategoriesAndActivitiesToFirestore() {
//    // Firestore referanse
//    val db = FirebaseFirestore.getInstance()
//
//    // Definer kategoriene med tilhørende aktiviteter (uten 'id' som første parameter)
//val categoryActivities = mapOf(
//    "Festivaler" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Fencrypted_tbn0_image_3.jpg?alt=media&token=7f1c43fd-98eb-4c1e-9d5e-de3c958c054d", "Sommerfestival", "En morsom festival med konserter, mat og aktiviteter.", 7, 500, "Oslo sentrum, 0150 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Frosenheime_sommerfestival_2023.jpg?alt=media&token=4934dacb-25e2-454c-a0bc-c5516f056a5a", "Vinterfest", "En vinterfestival med is-skulpturer og konserter.", 10, 300, "Bergen sentrum, 5003 Bergen"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Fstockholm_film_festival_2023.jpg?alt=media&token=8af8979f-f1b6-42de-b16a-216eb019e845", "Kulturfestival", "Opplev kulturelle opptredener og mat fra hele verden.", 12, 1000, "Trondheim Torg, 7013 Trondheim"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Funderground_gallery_streetart.jpg?alt=media&token=4cda0c1b-cbff-4031-8fcc-8da5e7f2fd41", "Musikkfest", "En musikkfestival med nasjonale og internasjonale artister.", 18, 2000, "Stavanger konserthus, 4005 Stavanger"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Funderground_gallery_streetart.jpg?alt=media&token=4cda0c1b-cbff-4031-8fcc-8da5e7f2fd41", "Filmfestival", "En festival for filmelskere med premierer og visninger.", 15, 400, "Tromsø sentrum, 9008 Tromsø")
//    ),
//    "Forslag" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Forslag%2Ftegning.jpg?alt=media&token=a7d87928-4de8-4fc0-95a8-3b92cc7fc26a", "Tegnekonkurranse", "Bli med på en kreativ tegnekonkurranse for alle aldre.", 7, 50, "Paul Holmsens vei 9, 1613 Fredrikstad"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Forslag%2Fb%C3%B8ker.jpg?alt=media&token=7a66efa0-63a6-43a4-8262-9092b75dc651", "Litteraturklubb", "Diskusjoner og bokanmeldelser med en lokal bokklubb.", 18, 20, "Deichman Bjørvika, 0150 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Forslag%2Ffotball.jpg?alt=media&token=0bf782c6-7821-4110-897d-e6e00a17b807", "Fotomaraton", "Delta i et fotomaraton og fang spennende øyeblikk i byen.", 12, 100, "Markens gate 1, 4610 Kristiansand"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Forslag%2Fperson-skriver.jpg?alt=media&token=cbccf492-3809-4a64-9454-c14344b6eae5", "Gatekunstverksted", "Lær å lage gatekunst med lokale kunstnere.", 12, 30, "Bragernes Torg, 3017 Drammen"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Forslag%2Fperson-skriver.jpg?alt=media&token=cbccf492-3809-4a64-9454-c14344b6eae5", "Skrivekurs", "Et skrivekurs for de som vil forbedre sine skriveferdigheter.", 16, 15, "Hamar kulturhus, 2317 Hamar")
//    ),
//    "Klatring" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fdato_crag_climbing.jpg?alt=media&token=b78819ed-28a2-4ca5-8e94-9fb672a8f130", "Innendørs klatring", "Prøv ut innendørs klatring på vår klatrehall.", 7, 20, "Toppidrettssenteret, Sognsveien 228, 0863 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fdato_indoor_climbing.jpg?alt=media&token=5e1b34be-5607-4f07-8b05-1e8a7582ba94", "Utendørs klatretur", "Bli med på en utendørs klatretur i naturen.", 18, 10, "Svolvær, 8300 Lofoten"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fpolarismedia_image.jpg?alt=media&token=84469b0b-7653-4e1d-94ac-fdb8cb980af2", "Klatrekurs for nybegynnere", "Et kurs for de som vil lære å klatre fra bunnen av.", 12, 15, "Bergen Klatreklubb, 5005 Bergen"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fskullerudpark_climbing.jpg?alt=media&token=8bc9d9af-8945-49ff-9a82-838b50324e16", "Klatrekonkurranse", "Bli med på en spennende klatrekonkurranse.", 16, 25, "Stavanger Klatresenter, 4015 Stavanger"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fsquarespace_trekkoppfuglen.jpg?alt=media&token=2e62794b-f877-46f6-8894-ae4e2478ff48", "Fjellklatring", "Guidet fjellklatring for de som ønsker å utfordre seg selv.", 18, 8, "Trollveggen, 6320 Rauma")
//    ),
//    "Mat" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Faktivioslo_image.jpg?alt=media&token=a278dab0-476b-412f-a7f0-10d2b9599691", "Matlagingskurs", "Lær å lage lokale retter med en profesjonell kokk.", 15, 12, "Mathallen Oslo, Vulkan 5, 0178 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fimage_7.jpg?alt=media&token=86fc59d4-01c1-4405-a689-2b4fb4993670", "Gourmetfestival", "En festival for alle matelskere med smaksprøver og konkurranser.", 18, 500, "Bryggen, 5003 Bergen"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fspaniatips_image.jpg?alt=media&token=6a36ff1d-57b3-44c7-8f8f-83cfc3b4f4a9", "Bakeworkshop", "Bli med på en bakeworkshop og lær å lage fantastiske kaker.", 7, 20, "Torvet, 7011 Trondheim"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Ftbn0_image_2.jpg?alt=media&token=bdf4bedd-e0ef-4d66-9a6d-70e526e4d7f5", "Vin og mat sammenkobling", "Lær hvordan du kombinerer vin med ulike retter.", 20, 30, "Clarion Hotel, 4006 Stavanger"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fvingruppen_image.jpg?alt=media&token=bd6cfde9-4acd-485d-8395-7187987870f7", "Matmarked", "Besøk vårt matmarked med lokale produsenter.", 7, 1000, "Torvet, 4611 Kristiansand")
//    ),
//    "Reise" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fblogg_image.jpg?alt=media&token=470f0a8b-8389-4fd3-8000-9439d3b6a042", "Guidet byvandring", "Utforsk byen med en guidet byvandring.", 7, 20, "Karl Johans gate 1, 0154 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Ffjordtours_image.jpg?alt=media&token=689c9edb-7037-49e2-862f-cfc5e867537a", "Weekendtur til fjordene", "Opplev de norske fjordene på en uforglemmelig helgetur.", 10, 50, "Geirangerfjorden, 6216 Geiranger"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fpixabay_africa_image.jpg?alt=media&token=d3f1902c-ce2b-4f18-9a3f-45cf48aa638f", "Oppdagelsesferd til Nordkapp", "En eventyrlig tur til det nordligste punktet i Europa.", 18, 30, "Nordkapp, 9764 Honningsvåg"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fsimpleview_image.jpg?alt=media&token=1993ed24-6e9e-4c7b-97cd-fd1fe53aad00", "Safari i villmarken", "Bli med på safari og opplev den norske naturen.", 25, 10, "Finnmark, 9600 Hammerfest"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fthonhotels_image.jpg?alt=media&token=e87949dd-fd31-426a-ac52-1ceb43220f0b", "Oppdag Skandinavias hovedsteder", "En tur gjennom de skandinaviske hovedstedene Oslo, Stockholm og København.", 30, 40, "Rådhusplassen, 0151 Oslo")
//    ),
//    "Trening" to listOf(
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_1.jpg?alt=media&token=c6afda2b-3492-4036-84f6-9945c2ffe8bb", "Yoga i parken", "Morgentrening med yoga under åpen himmel.", 10, 30, "Frognerparken, 0268 Oslo"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_2.jpg?alt=media&token=0f6573c9-9516-4b4f-befa-47a67b587790", "Bootcamp", "En intensiv treningsøkt med profesjonelle instruktører.", 18, 50, "Byparken, 5003 Bergen"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_3.jpg?alt=media&token=9360b343-cc29-4b30-9b01-85e5f1cc3f56", "Løpetrening for nybegynnere", "Lær å løpe riktig med veiledning fra en erfaren trener.", 15, 20, "Nidarosdomen, 7013 Trondheim"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_4.jpg?alt=media&token=9d582a4b-2761-40d7-bf8d-b6d1811d90c4", "Styrketrening", "Bli sterkere med en treningsøkt tilpasset styrketrening.", 20, 15, "Stavanger Stadion, 4020 Stavanger"),
//        Activity2("https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_5.jpg?alt=media&token=5a2010ba-2db0-4910-94a9-f1c5273196d7", "Sykkeltur", "Utforsk byen på sykkel med en guide.", 12, 20, "Sognsvann, 0863 Oslo")
//    )
//)
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
//


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

