package com.example.soclub.screens.termsPrivacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.soclub.R

//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.soclub.R


@Composable
fun TermsPrivacyScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        item {
            Text(
                text = stringResource(R.string.this_terms),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Text(
                text = stringResource(R.string.use_of_the_Service),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.fair_use),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.privacy),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.processes_personal_data),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.the_service_is_offered),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.Content_Property),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.in_app),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.changes_Terms ),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.change_terms_any_time),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.user_rights),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.se_delet),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource( R.string.consent) ,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.you_give_your_consent),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.data_processing),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.data_processing_in_app),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.transfer_third_country) ,//"",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.no_transfer_third_country),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.data_storage),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.we_store_personal_data_as_long_as_necessary_accordance_with_the_law),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = stringResource(R.string.the_complaint),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.you_have_the_right_to_file_a_complaint),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}