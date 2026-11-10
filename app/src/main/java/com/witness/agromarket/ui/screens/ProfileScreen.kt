package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.model.User
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val userDao = remember { db.userDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val loggedInPhone = remember { sessionManager.getLoggedInPhone() }
    val savedUser by userDao.observeUserByPhone(loggedInPhone).collectAsState(initial = null)

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var farmOrBusinessName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(savedUser) {
        savedUser?.let { user ->
            fullName = user.fullName
            phone = user.phone
            location = user.location
            farmOrBusinessName = user.farmOrBusinessName
            role = user.role
            isEditing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditing) {
                    Text(
                        text = "Enter Your Profile Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select Role:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
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
                        value = farmOrBusinessName,
                        onValueChange = { farmOrBusinessName = it },
                        label = { Text("Farm or Business Name") },
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

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text(text = "Name: $fullName", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Role: $role")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Farm / Business: $farmOrBusinessName")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Phone: $phone")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Location: $location")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Wallet Balance: ZMW ${"%.2f".format(savedUser?.walletBalanceZmw ?: 0.0)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                OutlinedButton(
                    onClick = {
                        fullName = ""
                        phone = ""
                        location = ""
                        farmOrBusinessName = ""
                        role = ""
                        errorMessage = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            Button(
                onClick = {
                    if (isEditing) {
                        if (fullName.isBlank() || role.isBlank() || farmOrBusinessName.isBlank() || phone.isBlank() || location.isBlank()) {
                            errorMessage = "Please fill in all fields and select a role."
                        } else {
                            scope.launch {
                                val existing = savedUser
                                userDao.insertOrUpdateUser(
                                    User(
                                        id = existing?.id ?: 0,
                                        fullName = fullName.trim(),
                                        phone = phone.trim(),
                                        location = location.trim(),
                                        role = role,
                                        farmOrBusinessName = farmOrBusinessName.trim(),
                                        password = existing?.password ?: "",
                                        walletBalanceZmw = existing?.walletBalanceZmw ?: 0.0,
                                        linkedProvider = existing?.linkedProvider ?: "",
                                        linkedAccountNumber = existing?.linkedAccountNumber ?: ""
                                    )
                                )
                                sessionManager.saveLoggedInPhone(phone.trim())
                                errorMessage = ""
                                isEditing = false
                            }
                        }
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEditing) "Save Profile" else "Edit Profile")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                sessionManager.clearSession()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}