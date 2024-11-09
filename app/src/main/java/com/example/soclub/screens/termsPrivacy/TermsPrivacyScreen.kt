package com.example.soclub.screens.termsPrivacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun TermsPrivacyScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        item {
            Text(
                text = "Disse vilkårene regulerer din bruk av applikasjonen vår. Ved å bruke appen, godtar du å overholde disse vilkårene.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Text(
                text = "1. Bruk av Tjenesten",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Du må bruke tjenesten i samsvar med gjeldende lover og regler.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "2. Personvern",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vi samler inn og behandler personopplysninger i henhold til vår personvernerklæring.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "3. Ansvarsfraskrivelse",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tjenesten tilbys \"som den er\", og vi fraskriver oss ethvert ansvar for eventuelle feil eller mangler.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "4. Innhold og Eiendomsrett",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Alt innhold i applikasjonen er beskyttet av opphavsrett og andre eiendomsrettigheter. Reproduksjon eller distribusjon av innholdet uten tillatelse er ikke tillatt.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "5. Endringer i Vilkår",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vi forbeholder oss retten til å endre vilkårene når som helst. Brukere vil bli varslet om vesentlige endringer.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "6. Brukerrettigheter",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Du har rett til å be om innsyn, korrigering eller sletting av dine personopplysninger i henhold til vår personvernerklæring.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "7. Samtykke",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ved å bruke applikasjonen gir du ditt samtykke til at vi behandler dine personopplysninger i tråd med vår personvernerklæring.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "8. Databehandlingsansvarlig",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Databehandlingsansvarlig for denne applikasjonen er Firebase (Google). Kontakt oss ved spørsmål om personvern.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "9. Overføring til Tredjeland",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personopplysninger overføres ikke til tredjeland uten tilstrekkelige sikkerhetstiltak i samsvar med GDPR.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "10. Datalagring",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vi lagrer personopplysninger så lenge det er nødvendig for å oppfylle formålet med behandlingen og i henhold til gjeldende lover.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "11. Klagerett",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Du har rett til å sende en klage til en tilsynsmyndighet, som f.eks. Datatilsynet, dersom du mener at personvernrettighetene dine er brutt.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun TermsPrivacyScreenPreview() {
    TermsPrivacyScreen(rememberNavController())
}
