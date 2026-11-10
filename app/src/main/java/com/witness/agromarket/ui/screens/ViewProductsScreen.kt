package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.model.Order
import com.witness.agromarket.data.model.Product
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ViewProductsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val loggedInPhone = remember { sessionManager.getLoggedInPhone() }
    val products by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val currentUser by db.userDao().observeUserByPhone(loggedInPhone).collectAsState(initial = null)

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var orderQuantityInput by remember { mutableStateOf("1") }
    var buyerNameInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        if (buyerNameInput.isBlank() && currentUser != null) {
            buyerNameInput = currentUser!!.fullName
        }
    }

    val filteredProducts = products.filter {
        it.productName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Products",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search product or category...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedProduct != null) {
            val product = selectedProduct!!
            val unitPrice = product.price.toDoubleOrNull() ?: 0.0
            val qtyNumber = orderQuantityInput.toDoubleOrNull() ?: 1.0
            val calculatedTotal = unitPrice * qtyNumber

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order: ${product.productName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Seller: ${product.sellerName} (${product.sellerPhone})")
                    Text("Unit Price: ZMW ${product.price}")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = orderQuantityInput,
                        onValueChange = { orderQuantityInput = it },
                        label = { Text("Quantity to Buy") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buyerNameInput,
                        onValueChange = { buyerNameInput = it },
                        label = { Text("Buyer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total Cost: ZMW ${"%.2f".format(calculatedTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                selectedProduct = null
                                orderQuantityInput = "1"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (orderQuantityInput.isNotBlank() && buyerNameInput.isNotBlank()) {
                                    scope.launch {
                                        db.orderDao().insertOrder(
                                            Order(
                                                productName = product.productName,
                                                quantity = orderQuantityInput.trim(),
                                                totalPriceZmw = calculatedTotal,
                                                status = "Pending",
                                                buyerOrFarmerName = buyerNameInput.trim(),
                                                buyerPhone = currentUser?.phone ?: loggedInPhone,
                                                sellerPhone = product.sellerPhone,
                                                sellerName = product.sellerName,
                                                paymentStatus = "Unpaid"
                                            )
                                        )
                                        statusMessage = "Order placed! Pay via your AgroMarket Wallet to complete."
                                        selectedProduct = null
                                        orderQuantityInput = "1"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Order")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (statusMessage.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = statusMessage,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { navController.navigate("payments") }) {
                        Text("Go to AgroMarket Wallet")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filteredProducts.isEmpty()) {
            Text("No products available yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredProducts) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Category: ${product.category}")
                            Text("Price: ZMW ${product.price}")
                            Text("Available Stock: ${product.quantity}")
                            if (product.sellerName.isNotBlank()) {
                                Text("Farmer: ${product.sellerName}")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    selectedProduct = product
                                    statusMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Buy / Order Now")
                            }
                        }
                    }
                }
            }
        }
    }
}
