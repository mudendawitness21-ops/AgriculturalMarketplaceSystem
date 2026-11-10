package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.repository.AgroMarketDatabase

@Composable
fun TrackGoodsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val orders by db.orderDao().getAllOrders().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Track Purchased Goods",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Text("No goods to track yet. Place an order first.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { order ->
                    val progress = when (order.status) {
                        "Pending" -> 0.25f
                        "Confirmed" -> 0.50f
                        "In Transit" -> 0.75f
                        "Delivered" -> 1.0f
                        else -> 0.1f
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ORD-${order.id} • ${order.productName}", fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (order.paymentStatus == "Paid") "PAYMENT DONE" else "UNPAID",
                                    color = if (order.paymentStatus == "Paid") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Quantity: ${order.quantity} | Total: ZMW ${order.totalPriceZmw}")
                            if (order.paymentStatus == "Paid") {
                                Text("Payment Method: ${order.paymentMethod}")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Delivery Status: ${order.status}", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
