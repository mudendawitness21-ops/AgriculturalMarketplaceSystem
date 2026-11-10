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
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val orderDao = remember { db.orderDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val loggedInPhone = remember { sessionManager.getLoggedInPhone() }
    val orders by orderDao.getAllOrders().collectAsState(initial = emptyList())
    val currentUser by db.userDao().observeUserByPhone(loggedInPhone).collectAsState(initial = null)
    val userRole = currentUser?.role ?: "Buyer"

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var totalPriceInput by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (buyerName.isBlank() && currentUser != null) {
            buyerName = currentUser!!.fullName
        }
    }

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
            Card(modifier = Modifier.fillMaxWidth()) {
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
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Buyer Full Name") },
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
                                buyerName = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = {
                                val parsedPrice = totalPriceInput.toDoubleOrNull()
                                if (productName.isBlank() || quantity.isBlank() || parsedPrice == null || buyerName.isBlank()) {
                                    errorMessage = "Please fill in all fields with a valid price."
                                } else {
                                    scope.launch {
                                        orderDao.insertOrder(
                                            Order(
                                                productName = productName.trim(),
                                                quantity = quantity.trim(),
                                                totalPriceZmw = parsedPrice,
                                                status = "Pending",
                                                buyerOrFarmerName = buyerName.trim(),
                                                buyerPhone = loggedInPhone,
                                                paymentStatus = "Unpaid"
                                            )
                                        )
                                        productName = ""
                                        quantity = ""
                                        totalPriceInput = ""
                                        errorMessage = ""
                                        showForm = false
                                    }
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
            Text(text = "No orders recorded yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "ORD-${order.id}", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Delivery: ${order.status}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Product: ${order.productName}")
                            Text(text = "Quantity: ${order.quantity}")
                            Text(text = "Total: ZMW ${"%.2f".format(order.totalPriceZmw)}")
                            Text(text = "Buyer: ${order.buyerOrFarmerName}")
                            Text(
                                text = "Payment: ${order.paymentStatus}",
                                color = if (order.paymentStatus == "Paid") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (userRole == "Farmer" && order.status != "Delivered") {
                                    Button(
                                        onClick = {
                                            val nextStatus = when (order.status) {
                                                "Pending" -> "Confirmed"
                                                "Confirmed" -> "In Transit"
                                                else -> "Delivered"
                                            }
                                            scope.launch {
                                                orderDao.updateOrder(order.copy(status = nextStatus))
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            when (order.status) {
                                                "Pending" -> "Confirm Order"
                                                "Confirmed" -> "Dispatch Goods"
                                                else -> "Mark Delivered"
                                            }
                                        )
                                    }
                                } else if (userRole == "Buyer" && order.status == "In Transit") {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                orderDao.updateOrder(order.copy(status = "Delivered"))
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirm Received")
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            orderDao.deleteOrder(order)
                                        }
                                    },
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