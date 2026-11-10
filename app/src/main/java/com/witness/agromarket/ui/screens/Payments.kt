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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.model.Order
import com.witness.agromarket.data.model.WalletTransaction
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun PaymentsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AgroMarketDatabase.getDatabase(context) }
    val orderDao = remember { db.orderDao() }
    val userDao = remember { db.userDao() }
    val walletDao = remember { db.walletDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val loggedInPhone = remember { sessionManager.getLoggedInPhone() }
    val currentUser by userDao.observeUserByPhone(loggedInPhone).collectAsState(initial = null)
    val allOrders by orderDao.getAllOrders().collectAsState(initial = emptyList())
    val transactions by walletDao.getTransactionsForUser(loggedInPhone).collectAsState(initial = emptyList())

    val walletBalance = currentUser?.walletBalanceZmw ?: 0.0

    val userOrders = allOrders.filter {
        it.buyerPhone == loggedInPhone || it.sellerPhone == loggedInPhone || (it.buyerPhone.isBlank() && it.sellerPhone.isBlank())
    }

    var activeFormMode by remember { mutableStateOf("NONE") }
    var selectedProvider by remember { mutableStateOf("") }
    var externalAccountNo by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }

    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var feedbackMessage by remember { mutableStateOf("") }
    var isErrorFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (externalAccountNo.isBlank() && currentUser != null) {
            externalAccountNo = currentUser!!.linkedAccountNumber.ifBlank { currentUser!!.phone }
            selectedProvider = currentUser!!.linkedProvider
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "AgroMarket Online Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }

        // 1. Individual Online Money Account Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${currentUser?.fullName ?: "Account Holder"} (${currentUser?.role ?: "User"})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Wallet ID / Phone: ${currentUser?.phone ?: "Not Logged In"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Balance: ZMW ${"%.2f".format(walletBalance)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                activeFormMode = if (activeFormMode == "DEPOSIT") "NONE" else "DEPOSIT"
                                feedbackMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deposit Funds")
                        }

                        OutlinedButton(
                            onClick = {
                                activeFormMode = if (activeFormMode == "WITHDRAW") "NONE" else "WITHDRAW"
                                feedbackMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Withdraw")
                        }
                    }
                }
            }
        }

        // 2. Deposit or Withdraw Form
        if (activeFormMode != "NONE") {
            val isDeposit = activeFormMode == "DEPOSIT"
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isDeposit) "Deposit from Mobile Money / Bank" else "Withdraw to Mobile Money / Bank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Provider:", fontWeight = FontWeight.Medium)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedProvider == "MTN MoMo",
                                onClick = { selectedProvider = "MTN MoMo" },
                                label = { Text("MTN MoMo") }
                            )
                            FilterChip(
                                selected = selectedProvider == "Airtel Money",
                                onClick = { selectedProvider = "Airtel Money" },
                                label = { Text("Airtel Money") }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedProvider == "Zamtel Kwacha",
                                onClick = { selectedProvider = "Zamtel Kwacha" },
                                label = { Text("Zamtel") }
                            )
                            FilterChip(
                                selected = selectedProvider == "Bank Account",
                                onClick = { selectedProvider = "Bank Account" },
                                label = { Text("Bank Account") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = externalAccountNo,
                            onValueChange = { externalAccountNo = it },
                            label = { Text("Mobile Money Number or Bank Account No.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text(if (isDeposit) "Deposit Amount (ZMW)" else "Withdraw Amount (ZMW)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("4-Digit PIN Authorization") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    activeFormMode = "NONE"
                                    amountInput = ""
                                    pinInput = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    val amount = amountInput.toDoubleOrNull()
                                    val user = currentUser
                                    if (user == null) {
                                        isErrorFeedback = true
                                        feedbackMessage = "Please login or register an account before transacting."
                                    } else if (selectedProvider.isBlank() || externalAccountNo.length < 6 || amount == null || amount <= 0.0 || pinInput.length < 4) {
                                        isErrorFeedback = true
                                        feedbackMessage = "Enter provider, valid account number, amount, and 4-digit PIN."
                                    } else if (!isDeposit && amount > user.walletBalanceZmw) {
                                        isErrorFeedback = true
                                        feedbackMessage = "Withdrawal Declined: Insufficient balance (ZMW ${"%.2f".format(user.walletBalanceZmw)})."
                                    } else {
                                        scope.launch {
                                            val ref = "${if (isDeposit) "DEP" else "WDR"}-${System.currentTimeMillis().toString().takeLast(6)}"
                                            val newBalance = if (isDeposit) {
                                                user.walletBalanceZmw + amount
                                            } else {
                                                user.walletBalanceZmw - amount
                                            }
                                            userDao.insertOrUpdateUser(
                                                user.copy(
                                                    walletBalanceZmw = newBalance,
                                                    linkedProvider = selectedProvider,
                                                    linkedAccountNumber = externalAccountNo.trim()
                                                )
                                            )
                                            walletDao.insertTransaction(
                                                WalletTransaction(
                                                    referenceId = ref,
                                                    userPhone = user.phone,
                                                    type = if (isDeposit) "DEPOSIT" else "WITHDRAWAL",
                                                    channel = selectedProvider,
                                                    accountNumber = externalAccountNo.trim(),
                                                    amountZmw = amount,
                                                    status = "SUCCESS",
                                                    description = if (isDeposit) {
                                                        "Deposited from $selectedProvider ($externalAccountNo)"
                                                    } else {
                                                        "Withdrawn to $selectedProvider ($externalAccountNo)"
                                                    }
                                                )
                                            )
                                            isErrorFeedback = false
                                            feedbackMessage = if (isDeposit) {
                                                "Deposited ZMW ${"%.2f".format(amount)} into AgroMarket Account (Ref: $ref)."
                                            } else {
                                                "Withdrawn ZMW ${"%.2f".format(amount)} to $selectedProvider $externalAccountNo (Ref: $ref)."
                                            }
                                            amountInput = ""
                                            pinInput = ""
                                            activeFormMode = "NONE"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isDeposit) "Confirm Deposit" else "Confirm Withdrawal")
                            }
                        }
                    }
                }
            }
        }
        if (feedbackMessage.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isErrorFeedback) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = feedbackMessage,
                        color = if (isErrorFeedback) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
// 3. Buyer-to-Seller Wallet Checkout Confirmation
        if (selectedOrder != null) {
            val order = selectedOrder!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pay Order ORD-${order.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Product: ${order.productName} (${order.quantity})")
                        Text("Receiver (Farmer): ${order.sellerName.ifBlank { "AgroMarket Seller" }} (${order.sellerPhone})")
                        Text("Amount to Transfer: ZMW ${"%.2f".format(order.totalPriceZmw)}")
                        Text("Your Available Balance: ZMW ${"%.2f".format(walletBalance)}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { selectedOrder = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val buyer = currentUser
                                    if (buyer == null || walletBalance < order.totalPriceZmw) {
                                        val needed = order.totalPriceZmw - walletBalance
                                        isErrorFeedback = true
                                        feedbackMessage = "Payment Failed: Insufficient AgroMarket balance. Deposit ZMW ${"%.2f".format(needed)} first."
                                        activeFormMode = "DEPOSIT"
                                        selectedOrder = null
                                    } else {
                                        scope.launch {
                                            val txnRef = "TXN-${System.currentTimeMillis().toString().takeLast(6)}"
// 1. Deduct from Buyer's AgroMarket Account
                                            val updatedBuyerBalance = buyer.walletBalanceZmw - order.totalPriceZmw
                                            userDao.insertOrUpdateUser(buyer.copy(walletBalanceZmw = updatedBuyerBalance))
                                            walletDao.insertTransaction(
                                                WalletTransaction(
                                                    referenceId = txnRef,
                                                    userPhone = buyer.phone,
                                                    type = "PAYMENT_SENT",
                                                    channel = "AgroMarket Wallet",
                                                    accountNumber = order.sellerPhone,
                                                    amountZmw = order.totalPriceZmw,
                                                    status = "SUCCESS",
                                                    description = "Paid ORD-${order.id} (${order.productName}) to ${order.sellerName}"
                                                )
                                            )
// 2. Credit Receiver's (Farmer's) AgroMarket Account
                                            if (order.sellerPhone.isNotBlank()) {
                                                val seller = userDao.getUserByPhone(order.sellerPhone)
                                                if (seller != null) {
                                                    userDao.insertOrUpdateUser(
                                                        seller.copy(walletBalanceZmw = seller.walletBalanceZmw + order.totalPriceZmw)
                                                    )
                                                    walletDao.insertTransaction(
                                                        WalletTransaction(
                                                            referenceId = txnRef,
                                                            userPhone = seller.phone,
                                                            type = "PAYMENT_RECEIVED",
                                                            channel = "AgroMarket Escrow",
                                                            accountNumber = buyer.phone,
                                                            amountZmw = order.totalPriceZmw,
                                                            status = "SUCCESS",
                                                            description = "Received payment for ORD-${order.id} (${order.productName}) from ${buyer.fullName}"
                                                        )
                                                    )
                                                }
                                            }
// 3. Mark Order Paid
                                            orderDao.updateOrder(
                                                order.copy(
                                                    paymentStatus = "Paid",
                                                    paymentMethod = "AgroMarket Wallet (Ref: $txnRef)"
                                                )
                                            )
                                            isErrorFeedback = false
                                            feedbackMessage = "Transfer of ZMW ${"%.2f".format(order.totalPriceZmw)} sent to ${order.sellerName.ifBlank { "Seller" }} (Ref: $txnRef)."
                                            selectedOrder = null
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirm Transfer")
                            }
                        }
                    }
                }
            }
        }
// 4. Orders List
        item {
            Text(
                text = "Orders & Payment Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (userOrders.isEmpty()) {
            item { Text("No orders found for your account.") }
        } else {
            items(userOrders) { order ->
                val isSeller = order.sellerPhone == loggedInPhone && order.buyerPhone != loggedInPhone
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ORD-${order.id}: ${order.productName}", fontWeight = FontWeight.Bold)
                            Text(
                                text = order.paymentStatus,
                                color = if (order.paymentStatus == "Paid") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Quantity: ${order.quantity} | Amount: ZMW ${"%.2f".format(order.totalPriceZmw)}")
                        if (isSeller) {
                            Text("Buyer: ${order.buyerOrFarmerName} (${order.buyerPhone})")
                        } else if (order.sellerName.isNotBlank()) {
                            Text("Receiver: ${order.sellerName}")
                        }
                        if (order.paymentStatus == "Paid") {
                            Text("Receipt: ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                        } else if (!isSeller) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                selectedOrder = order
                                feedbackMessage = ""
                            }) {
                                Text("Pay from AgroMarket Account")
                            }
                        }
                    }
                }
            }
        }
// 5. Individual Account Statement
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Account Statement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (transactions.isEmpty()) {
            item { Text("No transactions yet.") }
        } else {
            items(transactions) { txn ->
                val isCredit = txn.type == "DEPOSIT" || txn.type == "PAYMENT_RECEIVED"
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${txn.type} • ${txn.referenceId}", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${if (isCredit) "+" else "-"} ZMW ${"%.2f".format(txn.amountZmw)}",
                                color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(txn.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}