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
import com.witness.agromarket.data.model.Product
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AddProductScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val loggedInPhone = remember { sessionManager.getLoggedInPhone() }
    val currentUser by db.userDao().observeUserByPhone(loggedInPhone).collectAsState(initial = null)

    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add Agricultural Product",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (ZMW)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (productName.isBlank() || category.isBlank() || price.isBlank() || quantity.isBlank()) {
                    errorMessage = "Please fill in all fields."
                } else {
                    scope.launch {
                        db.productDao().insertProduct(
                            Product(
                                productName = productName.trim(),
                                category = category.trim(),
                                price = price.trim(),
                                quantity = quantity.trim(),
                                sellerPhone = currentUser?.phone ?: loggedInPhone,
                                sellerName = currentUser?.fullName ?: "Farmer"
                            )
                        )
                        navController.navigate("viewProducts")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Product")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}
