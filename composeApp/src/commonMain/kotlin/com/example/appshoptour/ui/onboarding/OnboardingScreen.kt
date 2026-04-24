package com.example.appshoptour.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OnboardingScreen (onContinue: ()-> Unit){
    Column {
        Text("Oubording page")

        Button(onClick = onContinue){
            Text("Continue")
        }
    }
}