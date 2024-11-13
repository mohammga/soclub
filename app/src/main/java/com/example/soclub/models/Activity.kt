package com.example.soclub.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

data class Activity(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val ageGroup: Int = 0,
    val maxParticipants: Int = 0,
    val location: String = "",
    val restOfAddress: String = "",
    val date: Timestamp? = null,
    val creatorId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val startTime: String = "",
    val category: String? = null,
    val lastUpdated: Timestamp = Timestamp.now()
)

// Denne koden hjelper oss til å fylle databasen vår med data for testing
//
//data class Activity2(
//    val imageUrl: String = "",
//    val title: String = "",
//    val description: String = "",
//    val ageGroup: Int = 0,
//    val maxParticipants: Int = 0,
//    val location: String = "",
//    val creatorId: String = "",
//    val date: Timestamp? = null,
//    val createdAt: Timestamp = Timestamp.now(),
//    val startTime: String = "",
//    val lastUpdated: Timestamp = Timestamp.now()
//
//)
//
//fun sendAllCategoriesAndActivitiesToFirestore() {
//    val db = FirebaseFirestore.getInstance()
//
//val categoryActivities = mapOf(
//
//        "Festivaler" to listOf(
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Fencrypted_tbn0_image_3.jpg?alt=media&token=7f1c43fd-98eb-4c1e-9d5e-de3c958c054d",
//                title = "Sommerfestival",
//                description = "Sommerfestivalen er en energisk og spennende festival midt i Oslo sentrum, fylt med konserter, matboder fra hele verden og spennende aktiviteter for hele familien. Dagen starter med lokal musikk fra kjente band, mens kvelden byr på internasjonale artister. Det er også en rekke workshops, blant annet i dans og kunst, og aktiviteter for barn og unge som tegneverksted og minikonserter.",
//                ageGroup = 7,
//                maxParticipants = 500,
//                location = "Oslo sentrum, 0150 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 7, 20)),
//                createdAt = Timestamp.now(),
//                startTime = "18:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Frosenheime_sommerfestival_2023.jpg?alt=media&token=4934dacb-25e2-454c-a0bc-c5516f056a5a",
//                title = "Vinterfest",
//                description = "Vinterfesten i Bergen sentrum byr på en unik opplevelse med vakre is-skulpturer, lysshow og koselige boder. Denne festivalen tar deg inn i en magisk vinterverden der du kan vandre mellom snøfigurer og oppleve spennende kunstinstallasjoner. Om kveldene fylles sentrum med musikk og opptredener fra lokale band, og det arrangeres matopplevelser med typiske vinterretter og varm drikke.",
//                ageGroup = 10,
//                maxParticipants = 300,
//                location = "Bergen sentrum, 5003 Bergen",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 12, 10)),
//                createdAt = Timestamp.now(),
//                startTime = "17:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Fstockholm_film_festival_2023.jpg?alt=media&token=8af8979f-f1b6-42de-b16a-216eb019e845",
//                title = "Kulturfestival",
//                description = "På Kulturfestivalen i Trondheim kan besøkende oppleve en rik blanding av kulturuttrykk fra hele verden. Festivalen tilbyr alt fra tradisjonelle norske folkedanser til moderne kunstutstillinger, med en rekke boder som selger håndverk og delikatesser. I tillegg vil det være spennende foredrag fra kjente kunstnere, og ulike aktiviteter som malekurs og teaterforestillinger for barn.",
//                ageGroup = 12,
//                maxParticipants = 1000,
//                location = "Trondheim Torg, 7013 Trondheim",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 8, 25)),
//                createdAt = Timestamp.now(),
//                startTime = "13:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Funderground_gallery_streetart.jpg?alt=media&token=4cda0c1b-cbff-4031-8fcc-8da5e7f2fd41",
//                title = "Musikkfest",
//                description = "Musikkfesten i Stavanger Konserthus er en storslagen musikkfestival med fokus på både nasjonale og internasjonale artister. Gjennom festivaldagene kan man oppleve alt fra rock og pop til klassisk musikk, med et bredt spekter av artister. Festivalen har også matvogner og boder som serverer lokale spesialiteter og spennende retter fra hele verden. Perfekt for musikkelskere som ønsker en unik konsertopplevelse.",
//                ageGroup = 18,
//                maxParticipants = 2000,
//                location = "Stavanger konserthus, 4005 Stavanger",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 9, 15)),
//                createdAt = Timestamp.now(),
//                startTime = "19:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Festivaler%2Funderground_gallery_streetart.jpg?alt=media&token=4cda0c1b-cbff-4031-8fcc-8da5e7f2fd41",
//                title = "Filmfestival",
//                description = "Filmfestivalen i Tromsø tilbyr en fantastisk opplevelse for filmelskere, med premierer, visninger og diskusjoner med regissører og skuespillere. Festivalen viser alt fra nye filmer til klassikere og dokumentarer, med fokus på nordisk og internasjonal film. Det vil også være prisutdelinger, og publikum får mulighet til å stemme på sine favoritter. En flott arena for alle som er interessert i filmkunst og ønsker å oppdage nye filmer.",
//                ageGroup = 15,
//                maxParticipants = 400,
//                location = "Tromsø sentrum, 9008 Tromsø",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 10, 5)),
//                createdAt = Timestamp.now(),
//                startTime = "16:30",
//                lastUpdated = Timestamp.now()
//            )
//        ),
//            "Klatring" to listOf(
//                Activity2(
//                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fdato_crag_climbing.jpg?alt=media&token=b78819ed-28a2-4ca5-8e94-9fb672a8f130",
//                    title = "Innendørs klatring",
//                    description = "Opplev utfordrende klatrevegger og tekniske ruter i vår moderne klatrehall på Toppidrettssenteret. Dette kurset gir deg muligheten til å trene teknikk, styrke og utholdenhet under trygge forhold, med veiledning fra profesjonelle instruktører. Passer for både nybegynnere og viderekomne.",
//                    ageGroup = 7,
//                    maxParticipants = 20,
//                    location = "Toppidrettssenteret, Sognsveien 228, 0863 Oslo",
//                    creatorId = "5mBk1m2CamesBjhz3tyg",
//                    date = Timestamp(Date(2024, 5, 14)),
//                    createdAt = Timestamp.now(),
//                    startTime = "14:00",
//                    lastUpdated = Timestamp.now()
//                ),
//                Activity2(
//                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fdato_indoor_climbing.jpg?alt=media&token=5e1b34be-5607-4f07-8b05-1e8a7582ba94",
//                    title = "Utendørs klatretur",
//                    description = "Bli med på en spektakulær utendørs klatretur i den vakre naturen rundt Svolvær. Her får du oppleve ren klatreopplevelse i norsk natur, med utfordrende ruter og spektakulær utsikt. Passer for de som har litt erfaring med klatring og ønsker å teste grensene i friluft.",
//                    ageGroup = 18,
//                    maxParticipants = 10,
//                    location = "Svolvær, 8300 Lofoten",
//                    creatorId = "5mBk1m2CamesBjhz3tyg",
//                    date = Timestamp(Date(2024, 6, 20)),
//                    createdAt = Timestamp.now(),
//                    startTime = "10:00",
//                    lastUpdated = Timestamp.now()
//                ),
//                Activity2(
//                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fpolarismedia_image.jpg?alt=media&token=84469b0b-7653-4e1d-94ac-fdb8cb980af2",
//                    title = "Klatrekurs for nybegynnere",
//                    description = "Dette nybegynnerkurset er ideelt for de som vil lære grunnleggende klatreteknikker. Under veiledning av erfarne instruktører vil du få opplæring i sikring, grepsteknikker og klatringens grunnprinsipper. Kurset inkluderer alt nødvendig utstyr og krever ingen tidligere erfaring.",
//                    ageGroup = 12,
//                    maxParticipants = 15,
//                    location = "Bergen Klatreklubb, 5005 Bergen",
//                    creatorId = "5mBk1m2CamesBjhz3tyg",
//                    date = Timestamp(Date(2024, 7, 5)),
//                    createdAt = Timestamp.now(),
//                    startTime = "12:00",
//                    lastUpdated = Timestamp.now()
//                ),
//                Activity2(
//                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fskullerudpark_climbing.jpg?alt=media&token=8bc9d9af-8945-49ff-9a82-838b50324e16",
//                    title = "Klatrekonkurranse",
//                    description = "Bli med på en spennende klatrekonkurranse hvor klatrere fra hele landet møtes for å konkurrere i hurtighet og teknikk. Konkurransen er åpen for både amatører og profesjonelle, og det vil være ulike ruter og nivåer. Premieutdeling finner sted på slutten av dagen, etterfulgt av en sosial samling for deltakerne.",
//                    ageGroup = 16,
//                    maxParticipants = 25,
//                    location = "Stavanger Klatresenter, 4015 Stavanger",
//                    creatorId = "5mBk1m2CamesBjhz3tyg",
//                    date = Timestamp(Date(2024, 8, 18)),
//                    createdAt = Timestamp.now(),
//                    startTime = "09:00",
//                    lastUpdated = Timestamp.now()
//                ),
//                Activity2(
//                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Klatring%2Fsquarespace_trekkoppfuglen.jpg?alt=media&token=2e62794b-f877-46f6-8894-ae4e2478ff48",
//                    title = "Fjellklatring",
//                    description = "Guidet fjellklatring for erfarne klatrere som ønsker å utfordre seg selv i den spektakulære Trollveggen. Denne turen er for deg som har solid klatreerfaring og ønsker å oppleve Norges mest ikoniske klatreområde. Du vil bli veiledet av erfarne fjellklatrere som kjenner området godt og sørger for en sikker og utfordrende opplevelse.",
//                    ageGroup = 18,
//                    maxParticipants = 8,
//                    location = "Trollveggen, 6320 Rauma",
//                    creatorId = "5mBk1m2CamesBjhz3tyg",
//                    date = Timestamp(Date(2024, 9, 30)),
//                    createdAt = Timestamp.now(),
//                    startTime = "08:30",
//                    lastUpdated = Timestamp.now()
//                )
//            ),
//
//        "Mat" to listOf(
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Faktivioslo_image.jpg?alt=media&token=a278dab0-476b-412f-a7f0-10d2b9599691",
//                title = "Matlagingskurs",
//                description = "Lær å lage lokale retter fra det norske kjøkken under veiledning av en profesjonell kokk. Kurset dekker teknikker for å tilberede tradisjonelle retter med fokus på lokale råvarer, samt tips for smakstilpasning. Perfekt for de som vil utvide kunnskapen sin om nordisk mat og lære å lage autentiske retter.",
//                ageGroup = 15,
//                maxParticipants = 12,
//                location = "Mathallen Oslo, Vulkan 5, 0178 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 7, 10)),
//                createdAt = Timestamp.now(),
//                startTime = "17:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fimage_7.jpg?alt=media&token=86fc59d4-01c1-4405-a689-2b4fb4993670",
//                title = "Gourmetfestival",
//                description = "Denne gourmetfestivalen i Bergen er et mekka for matentusiaster. Festivalen inkluderer smaksprøver fra landets beste restauranter, kokkekonkurranser og mesterklasser. Et bredt spekter av norske og internasjonale retter tilbys, og deltakerne kan delta på workshops og lære teknikker fra prisvinnende kokker.",
//                ageGroup = 18,
//                maxParticipants = 500,
//                location = "Bryggen, 5003 Bergen",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 8, 15)),
//                createdAt = Timestamp.now(),
//                startTime = "12:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fspaniatips_image.jpg?alt=media&token=6a36ff1d-57b3-44c7-8f8f-83cfc3b4f4a9",
//                title = "Bakeworkshop",
//                description = "Denne bakeworkshoppen tar deg gjennom kunsten å lage tradisjonelle norske kaker og brød. Under kyndig veiledning vil du lære grunnleggende baketeknikker og få tips til hvordan du kan eksperimentere med nye smaker og dekorasjoner. Workshoppen passer for alle ferdighetsnivåer, og alle får med seg sine egne kreasjoner hjem.",
//                ageGroup = 7,
//                maxParticipants = 20,
//                location = "Torvet, 7011 Trondheim",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 9, 20)),
//                createdAt = Timestamp.now(),
//                startTime = "14:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Ftbn0_image_2.jpg?alt=media&token=bdf4bedd-e0ef-4d66-9a6d-70e526e4d7f5",
//                title = "Vin og mat sammenkobling",
//                description = "Utforsk kunsten å matche vin med ulike retter i en unik smaksopplevelse. Kveldens vert, en erfaren sommelier, vil gi deg innsikt i hvordan forskjellige smaker kan kombineres og forsterkes. Perfekt for vinelskere og de som ønsker å forstå hvordan vin kan fremheve ulike smaker i mat.",
//                ageGroup = 20,
//                maxParticipants = 30,
//                location = "Clarion Hotel, 4006 Stavanger",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 10, 5)),
//                createdAt = Timestamp.now(),
//                startTime = "19:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Mat%2Fvingruppen_image.jpg?alt=media&token=bd6cfde9-4acd-485d-8395-7187987870f7",
//                title = "Matmarked",
//                description = "Oppdag et levende matmarked fylt med lokale produsenter og autentiske delikatesser. Dette markedet er ideelt for de som ønsker å oppleve tradisjonelle, håndlagde produkter og smake på alt fra ferske oster til hjemmelagde syltetøy. Arrangementet byr også på live-musikk og aktiviteter for hele familien.",
//                ageGroup = 7,
//                maxParticipants = 1000,
//                location = "Torvet, 4611 Kristiansand",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 11, 25)),
//                createdAt = Timestamp.now(),
//                startTime = "10:00",
//                lastUpdated = Timestamp.now()
//            )
//        ),
//
//
//        "Reise" to listOf(
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fblogg_image.jpg?alt=media&token=470f0a8b-8389-4fd3-8000-9439d3b6a042",
//                title = "Guidet byvandring",
//                description = "Oppdag Oslo fra en lokal ekspert sitt perspektiv med en guidet byvandring gjennom byens historiske landemerker. Turen inkluderer besøk til det ikoniske slottet, Stortinget og andre viktige kulturelle severdigheter, mens guiden deler fascinerende historier om byens utvikling og kultur.",
//                ageGroup = 7,
//                maxParticipants = 20,
//                location = "Karl Johans gate 1, 0154 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 5, 18)),
//                createdAt = Timestamp.now(),
//                startTime = "10:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Ffjordtours_image.jpg?alt=media&token=689c9edb-7037-49e2-862f-cfc5e867537a",
//                title = "Weekendtur til fjordene",
//                description = "En uforglemmelig weekendtur til de majestetiske norske fjordene, hvor du får oppleve naturens storslåtte skjønnhet og delta i aktiviteter som fjordcruise, fjellturer og besøk til tradisjonelle norske bygder. Denne turen inkluderer overnatting, måltider og guide.",
//                ageGroup = 10,
//                maxParticipants = 50,
//                location = "Geirangerfjorden, 6216 Geiranger",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 6, 10)),
//                createdAt = Timestamp.now(),
//                startTime = "08:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fpixabay_africa_image.jpg?alt=media&token=d3f1902c-ce2b-4f18-9a3f-45cf48aa638f",
//                title = "Oppdagelsesferd til Nordkapp",
//                description = "Reis til Europas nordligste punkt, Nordkapp, og opplev midnattssolen i all sin prakt. Turen inkluderer guidet kjøring gjennom det arktiske landskapet, stopp ved naturskjønne utsiktspunkter og besøk til Nordkapphallen. En opplevelse som gir minner for livet.",
//                ageGroup = 18,
//                maxParticipants = 30,
//                location = "Nordkapp, 9764 Honningsvåg",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 7, 25)),
//                createdAt = Timestamp.now(),
//                startTime = "18:30",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fsimpleview_image.jpg?alt=media&token=1993ed24-6e9e-4c7b-97cd-fd1fe53aad00",
//                title = "Safari i villmarken",
//                description = "Bli med på en eksotisk safari i Finnmarks villmark, hvor du kan oppleve det rike dyrelivet og de vakre landskapene nord i Norge. Turen inkluderer guidet kjøring gjennom skogene, stopp ved naturskjønne innsjøer, og mulighet til å se reinsdyr, ørn og andre ville dyr i sitt naturlige miljø.",
//                ageGroup = 25,
//                maxParticipants = 10,
//                location = "Finnmark, 9600 Hammerfest",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 8, 30)),
//                createdAt = Timestamp.now(),
//                startTime = "09:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Reise%2Fthonhotels_image.jpg?alt=media&token=e87949dd-fd31-426a-ac52-1ceb43220f0b",
//                title = "Oppdag Skandinavias hovedsteder",
//                description = "Utforsk de skandinaviske hovedstedene Oslo, Stockholm og København på en spennende tur som dekker kulturen, historien og matopplevelsene i disse ikoniske byene. Turen inkluderer besøk til nasjonale landemerker, guidede byvandringer, og tid til å utforske byene på egen hånd.",
//                ageGroup = 30,
//                maxParticipants = 40,
//                location = "Rådhusplassen, 0151 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 9, 12)),
//                createdAt = Timestamp.now(),
//                startTime = "11:00",
//                lastUpdated = Timestamp.now()
//            )
//        ),
//
//        "Trening" to listOf(
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_1.jpg?alt=media&token=c6afda2b-3492-4036-84f6-9945c2ffe8bb",
//                title = "Yoga i parken",
//                description = "Start dagen med ro og energi i vår yogaøkt under åpen himmel i Frognerparken. Økten er tilpasset alle nivåer og ledes av en erfaren yogainstruktør som vil veilede deg gjennom pust, meditasjon og ulike yogaposisjoner. Ta med egen matte og opplev naturens fred i denne beroligende økten.",
//                ageGroup = 10,
//                maxParticipants = 30,
//                location = "Frognerparken, 0268 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 5, 25)),
//                createdAt = Timestamp.now(),
//                startTime = "08:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_2.jpg?alt=media&token=0f6573c9-9516-4b4f-befa-47a67b587790",
//                title = "Bootcamp",
//                description = "Kast deg inn i en intens bootcamp-økt i Byparken i Bergen, designet for å utfordre hele kroppen. Her vil profesjonelle instruktører guide deg gjennom styrke-, kondisjons- og utholdenhetsøvelser i et høyt tempo. Økten er perfekt for deg som ønsker en fullkroppstrening ute i naturen.",
//                ageGroup = 18,
//                maxParticipants = 50,
//                location = "Byparken, 5003 Bergen",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 6, 10)),
//                createdAt = Timestamp.now(),
//                startTime = "18:30",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_3.jpg?alt=media&token=9360b343-cc29-4b30-9b01-85e5f1cc3f56",
//                title = "Løpetrening for nybegynnere",
//                description = "En nybegynner-vennlig løpetrening utenfor Nidarosdomen i Trondheim, hvor du vil lære grunnleggende løpsteknikker og få tips om hvordan du kan forbedre utholdenheten din. Med veiledning fra en erfaren løpetrener er dette en perfekt start for deg som vil komme i gang med løping på en trygg måte.",
//                ageGroup = 15,
//                maxParticipants = 20,
//                location = "Nidarosdomen, 7013 Trondheim",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 7, 5)),
//                createdAt = Timestamp.now(),
//                startTime = "09:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_4.jpg?alt=media&token=9d582a4b-2761-40d7-bf8d-b6d1811d90c4",
//                title = "Styrketrening",
//                description = "Bli sterkere og forbedre teknikken din med vår styrketreningsøkt på Stavanger Stadion. Her vil du få veiledning i ulike styrkeøvelser for hele kroppen, inkludert vektløfting og funksjonelle øvelser. Økten er åpen for alle nivåer og fokuserer på sikkerhet og effektivitet.",
//                ageGroup = 20,
//                maxParticipants = 15,
//                location = "Stavanger Stadion, 4020 Stavanger",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 8, 15)),
//                createdAt = Timestamp.now(),
//                startTime = "17:00",
//                lastUpdated = Timestamp.now()
//            ),
//            Activity2(
//                imageUrl = "https://firebasestorage.googleapis.com/v0/b/soclub-af7ad.appspot.com/o/Trening%2Fimage_5.jpg?alt=media&token=5a2010ba-2db0-4910-94a9-f1c5273196d7",
//                title = "Sykkeltur",
//                description = "Utforsk Oslos natur på sykkel sammen med en erfaren guide på denne turen rundt Sognsvann. Turen er egnet for alle, uansett erfaring, og tar deg gjennom vakre stier og rolige områder. En flott måte å oppleve byen på, samtidig som du får god mosjon.",
//                ageGroup = 12,
//                maxParticipants = 20,
//                location = "Sognsvann, 0863 Oslo",
//                creatorId = "5mBk1m2CamesBjhz3tyg",
//                date = Timestamp(Date(2024, 9, 20)),
//                createdAt = Timestamp.now(),
//                startTime = "13:00",
//                lastUpdated = Timestamp.now()
//            )
//        ),
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














// denne koden sletter alle dataene i databasen vår.

//fun deleteAllActivitiesFromCategories() {
//    // Firestore referanse
//    val db = FirebaseFirestore.getInstance()
//
//    // Definer kategoriene som skal slettes
//    val categories = listOf("Festivaler", "Klatring", "Mat", "Reise", "Trening")
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

