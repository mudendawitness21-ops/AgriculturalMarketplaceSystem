package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.model.User
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val userDao = remember { db.userDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var farmOrBusinessName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create AgroMarket Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("District / Town") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = farmOrBusinessName,
            onValueChange = { farmOrBusinessName = it },
            label = { Text("Farm or Business Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = role == "Farmer",
                onClick = { role = "Farmer" },
                label = { Text("Farmer") }
            )
            FilterChip(
                selected = role == "Buyer",
                onClick = { role = "Buyer" },
                label = { Text("Buyer") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (fullName.isBlank() || phone.isBlank() || location.isBlank() || role.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all required fields and select a role."
                } else {
                    scope.launch {
                        val existingUser = userDao.getUserByPhone(phone.trim())
                        if (existingUser != null) {
                            errorMessage = "An account with this phone number already exists. Please login."
                        } else {
                            userDao.insertOrUpdateUser(
                                User(
                                    fullName = fullName.trim(),
                                    phone = phone.trim(),
                                    location = location.trim(),
                                    role = role,
                                    farmOrBusinessName = farmOrBusinessName.trim(),
                                    password = password.trim(),
                                    walletBalanceZmw = 0.0
                                )
                            )
                            sessionManager.saveLoggedInPhone(phone.trim())
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }
    }
}
