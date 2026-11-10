package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.repository.UserRepository

@Composable
fun RegisterScreen(
    navController: NavHostController
) {

    var fullName by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
            },
            label = {
                Text("Full Name")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },
            label = {
                Text("Phone Number")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                when {

                    fullName.isBlank() ||
                            email.isBlank() ||
                            phone.isBlank() ||
                            password.isBlank() -> {

                        errorMessage = "Please fill in all fields"
                    }

                    !fullName.matches(Regex("^[A-Za-z ]+$")) -> {

                        errorMessage = "Full name must contain letters only"
                    }

                    fullName.trim().split(" ").size < 2 -> {

                        errorMessage = "Enter first name and last name"
                    }

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches() -> {

                        errorMessage = "Enter a valid email address"
                    }

                    !phone.matches(Regex("^\\d{10}$")) -> {

                        errorMessage = "Phone number must contain exactly 10 digits"
                    }

                    password.length < 6 -> {

                        errorMessage = "Password must be at least 6 characters"
                    }

                    else -> {

                        UserRepository.fullName = fullName
                        UserRepository.phoneNumber = phone
                        UserRepository.registeredEmail = email
                        UserRepository.registeredPassword = password

                        errorMessage = ""

                        navController.navigate("login")
                    }
                }
            }
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            color = Color.Red
        )

        TextButton(
            onClick = {
                navController.navigate("login")
            }
        ) {
            Text("Back")
        }
    }
}