package com.example.soclub.ui.screens.activityDetail

import android.provider.CalendarContract.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top  // Innholdet er fortsatt plassert øverst
    ) {
        // Image at the top
        Image(
            painter = painterResource(id = R.drawable.yoga),  // Bytt ut med riktig bilde
            contentDescription = "Welcome Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Yoga ved Tunevannet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp)
                .padding(5.dp, 15.dp)
                .align(Alignment.Start)
        )

        Text(
            text = "Tirsdag.28.august 2024",
            modifier = Modifier
                .padding(start = 16.dp)
                .padding(5.dp, 2.dp)
                .align(Alignment.Start)
        )




        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically // Justerer vertikalt i midten
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp) // Gir litt avstand til teksten
            ) {
                ElevatedCardExample()
            }

            Column(
                modifier = Modifier.padding(5.dp) // Liten padding rundt tekstene
            ) {
                Text(
                    text = "Tunevannet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(
                    text = "Sarpsborg",
                    color = Color.Gray

                )
            }
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically // Justerer vertikalt i midten
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp) // Gir litt avstand til teksten
            ) {
                ElevatedCardExample()
            }

            Column(
                modifier = Modifier.padding(5.dp) // Liten padding rundt tekstene
            ) {
                Text(
                    text = "Maks 10 personer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,

                    )
                Text(
                    text = "Aldersgruppe:Alle",
                    color = Color.Gray


                    )


            }


        }

        Text(
            text = "Bli med på sosialisering ved Tunevannet. Vi vil ha yoga og lek. Dette er en aktivitet for folk som liker å være sammen med andre mennesker.",
            modifier = Modifier
            .align(Alignment.Start)
                .padding(start = 16.dp)
                .padding(5.dp, 10.dp),
        )

        Image(
            painter = painterResource(R.drawable.gpsbilde1),
            contentDescription = "Profile Picture",
            modifier = Modifier
                    .size(400.dp)
                    .padding(start = 16.dp)
                    .padding(5.dp, 10.dp),
        )

        Button(
            onClick = {

            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Meld deg",
                color = Color.White
            )
        }


    }
}

@Composable
fun ElevatedCardExample() {
    ElevatedCard(
        modifier = Modifier
            .size(width = 50.dp, height = 50.dp),
        colors = CardDefaults.elevatedCardColors( // Endrer bakgrunnsfargen til hvit
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
    ElevatedCard(onClick = { /*TODO*/ }) {

    }
}
