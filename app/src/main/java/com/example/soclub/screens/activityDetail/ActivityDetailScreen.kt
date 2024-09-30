package com.example.soclub.screens.activityDetail

import android.provider.CalendarContract.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColor
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soclub.R

@Composable
fun ActivityDetailScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Generell padding for hele layouten
        horizontalAlignment = Alignment.Start
    ) {
        // Toppbilde
        item {
            Image(
                painter = painterResource(id = R.drawable.yoga),
                contentDescription = "Welcome Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Hovedtittel
        item {
            Text(
                text = "Yoga ved Tunevannet",

                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp)


            )
        }

        // Dato
        item {
            Text(
                text = "Tirsdag. 28. august 2024",
                modifier = Modifier
                    .padding(vertical = 4.dp),

            )
        }

        // Første informasjonsrad
        item {
            InfoRow("Tunevannet", "Sarpsborg")
        }

        // Andre informasjonsrad
        item {
            InfoRow("Maks 10 personer", "Aldersgruppe: Alle")
        }

        // Beskrivelse
        item {
            Text(
                text = "Bli med på sosialisering ved Tunevannet. Vi vil ha yoga og lek. Dette er en aktivitet for folk som liker å være sammen med andre mennesker.",
                modifier = Modifier
                    .padding(vertical = 16.dp),

            )
        }

        // GPS-bilde
        item {
            Image(
                painter = painterResource(R.drawable.gpsbilde1),
                contentDescription = "GPS-bilde",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Justert for bedre visning
                    .padding(vertical = 8.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Knapp
        item {
            Button(
                onClick = { /* Legg til handling for knappen */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "Meld deg",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun InfoRow(mainText: String, subText: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
        ) {
            ElevatedCardExample()
        }

        Column {
            Text(
                text = mainText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            )
            Text(
                text = subText,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ElevatedCardExample() {
    ElevatedCard(
        modifier = Modifier
            .size(50.dp), // Juster størrelse på kortet
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.LightGray
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_action_name),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityDetailScreenPreview() {
    ActivityDetailScreen(rememberNavController())
}

