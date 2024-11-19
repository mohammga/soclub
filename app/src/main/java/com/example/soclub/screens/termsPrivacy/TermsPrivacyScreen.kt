package com.example.soclub.screens.termsPrivacy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.soclub.R

/**
 * Composable function representing the Terms and Privacy screen.
 *
 * This screen displays the application's Terms of Service and Privacy Policy.
 * It organizes the content into sections with titles and corresponding descriptions.
 */
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
            SectionTitleAndContent(
                title = R.string.use_of_the_Service,
                content = R.string.fair_use
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.privacy,
                content = R.string.processes_personal_data
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.disclaimer,
                content = R.string.the_service_is_offered
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.Content_Property,
                content = R.string.in_app
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.changes_Terms,
                content = R.string.change_terms_any_time
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.user_rights,
                content = R.string.se_delet
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.consent,
                content = R.string.you_give_your_consent
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.data_processing,
                content = R.string.data_processing_in_app
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.transfer_third_country,
                content = R.string.no_transfer_third_country
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.data_storage,
                content = R.string.we_store_personal_data_as_long_as_necessary_accordance_with_the_law
            )
        }
        item {
            SectionTitleAndContent(
                title = R.string.the_complaint,
                content = R.string.you_have_the_right_to_file_a_complaint
            )
        }
    }
}

/**
 * Composable function for displaying a section with a title and corresponding content.
 *
 * @param title The string resource ID for the section title.
 * @param content The string resource ID for the section content.
 */
@Composable
fun SectionTitleAndContent(@StringRes title: Int, @StringRes content: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(content),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


