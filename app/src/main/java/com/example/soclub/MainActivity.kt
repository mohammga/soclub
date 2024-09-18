package com.example.soclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.soclub.ui.navigation.AppNavigation
import com.example.soclub.ui.theme.SoclubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoclubTheme  {
                AppNavigation()
            }
        }
    }
}

