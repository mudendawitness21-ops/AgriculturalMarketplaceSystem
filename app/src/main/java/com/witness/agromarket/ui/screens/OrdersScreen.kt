package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

data class OrderItem(
    val orderId: String,
    val productName: String,
    val quantity: String,
    val totalPriceZmw: Double,
    val status: String,
    val buyerOrFarmerName: String
)

@Composable
fun OrdersScreen(navController: NavHostController) {
    val orders = remember { mutableStateListOf<OrderItem>() }

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var totalPriceInput by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Orders Management",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Home")
            }

            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showForm) "Hide Form" else "New Order")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Order Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalPriceInput,
                        onValueChange = { totalPriceInput = it },
                        label = { Text("Total Price (ZMW)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it },
                        label = { Text("Buyer or Farmer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                productName = ""
                                quantity = ""
                                totalPriceInput = ""
                                partyName = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = {
                                val parsedPrice = totalPriceInput.toDoubleOrNull()
                                if (productName.isBlank() || quantity.isBlank() || parsedPrice == null || partyName.isBlank()) {
                                    errorMessage = "Please fill in all fields with a valid price."
                                } else {
                                    val nextId = "ORD-${1 + orders.size}"
                                    orders.add(
                                        0,
                                        OrderItem(
                                            orderId = nextId,
                                            productName = productName.trim(),
                                            quantity = quantity.trim(),
                                            totalPriceZmw = parsedPrice,
                                            status = "Pending",
                                            buyerOrFarmerName = partyName.trim()
                                        )
                                    )
                                    productName = ""
                                    quantity = ""
                                    totalPriceInput = ""
                                    partyName = ""
                                    errorMessage = ""
                                    showForm = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Order")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (orders.isEmpty()) {
            Text(
                text = "No orders recorded yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = order.orderId, fontWeight = FontWeight.Bold)
                                Text(
                                    text = order.status,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Product: ${order.productName}")
                            Text(text = "Quantity: ${order.quantity}")
                            Text(text = "Total: ZMW ${order.totalPriceZmw}")
                            Text(text = "Party: ${order.buyerOrFarmerName}")

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (order.status != "Delivered") {
                                    Button(
                                        onClick = {
                                            val index = orders.indexOf(order)
                                            if (index != -1) {
                                                val nextStatus = when (order.status) {
                                                    "Pending" -> "Confirmed"
                                                    "Confirmed" -> "In Transit"
                                                    else -> "Delivered"
                                                }
                                                orders[index] = order.copy(status = nextStatus)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Advance Status")
                                    }
                                }

                                OutlinedButton(
                                    onClick = { orders.remove(order) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Delete Order")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
