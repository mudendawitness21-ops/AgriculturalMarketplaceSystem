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
import com.witness.agromarket.data.model.WalletTransaction
import com.witness.agromarket.data.network.DepositRequest
import com.witness.agromarket.data.network.PaymentApiService
import com.witness.agromarket.data.network.WithdrawRequest
import com.witness.agromarket.data.repository.AgroMarketDatabase
import com.witness.agromarket.data.repository.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val database = remember { AgroMarketDatabase.getDatabase(context) }
    val userDao = remember { database.userDao() }
    val walletDao = remember { database.walletDao() }
    val orderDao = remember { database.orderDao() }
    val sessionManager = remember { SessionManager(context) }
    val paymentApi = remember { PaymentApiService.create() }
    val scope = rememberCoroutineScope()

    val phone = remember { sessionManager.getLoggedInPhone() }
    val currentUser by userDao.observeUserByPhone(phone).collectAsState(initial = null)
    val transactions by walletDao.getTransactionsForUser(phone).collectAsState(initial = emptyList())
    val allOrders by orderDao.getAllOrders().collectAsState(initial = emptyList())

    val walletBalance = currentUser?.walletBalanceZmw ?: 0.0

    val userOrders = allOrders.filter {
        it.buyerPhone == phone || it.sellerPhone == phone || (it.buyerPhone.isBlank() && it.sellerPhone.isBlank())
    }

    var amountInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf("MTN MoMo") }
    var accountNumber by remember { mutableStateOf("") }
    var depositMode by remember { mutableStateOf(true) }
    var isProcessingNetwork by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(currentUser) {
        if (accountNumber.isBlank() && currentUser != null) {
            accountNumber = currentUser!!.linkedAccountNumber.ifBlank { currentUser!!.phone }
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
                text = "AgroMarket Wallet",
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

            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = currentUser?.fullName ?: "Guest", fontWeight = FontWeight.Bold)
                    Text(text = currentUser?.phone ?: "")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Balance", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "ZMW ${"%.2f".format(walletBalance)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        depositMode = true
                        feedbackMessage = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Deposit")
                }

                OutlinedButton(
                    onClick = {
                        depositMode = false
                        feedbackMessage = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Withdraw")
                }
            }
        }

        item {
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = {
                    Text(
                        if (selectedProvider == "Bank Account") "Bank Account Number"
                        else "Mobile Money Number (e.g. 097... / 096...)"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount (ZMW)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column {
                Text(text = "Select Provider", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedProvider == "MTN MoMo",
                        onClick = { selectedProvider = "MTN MoMo" },
                        label = { Text("MTN") }
                    )
                    FilterChip(
                        selected = selectedProvider == "Airtel Money",
                        onClick = { selectedProvider = "Airtel Money" },
                        label = { Text("Airtel") }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedProvider == "Zamtel Kwacha",
                        onClick = { selectedProvider = "Zamtel Kwacha" },
                        label = { Text("Zamtel") }
                    )
                    FilterChip(
                        selected = selectedProvider == "Bank Account",
                        onClick = { selectedProvider = "Bank Account" },
                        label = { Text("Bank") }
                    )
                }
            }
        }

        if (feedbackMessage.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = feedbackMessage,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                enabled = !isProcessingNetwork,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val amount = amountInput.toDoubleOrNull()
                    val user = currentUser

                    if (user == null) {
                        isError = true
                        feedbackMessage = "Please log in first."
                        return@Button
                    }

                    if (accountNumber.trim().length < 9) {
                        isError = true
                        feedbackMessage = "Enter a valid Mobile Money or account number."
                        return@Button
                    }

                    if (amount == null || amount <= 0) {
                        isError = true
                        feedbackMessage = "Enter a valid amount."
                        return@Button
                    }

                    if (!depositMode && amount > user.walletBalanceZmw) {
                        isError = true
                        feedbackMessage = "Insufficient balance."
                        return@Button
                    }

                    scope.launch {
                        isProcessingNetwork = true
                        try {
                            if (depositMode) {
                                val ref = "DEP-${System.currentTimeMillis()}"
                                val initRes = paymentApi.initiateDeposit(
                                    DepositRequest(
                                        phone = accountNumber.trim(),
                                        amount = amount,
                                        provider = selectedProvider,
                                        reference = ref
                                    )
                                )
                                if (!initRes.status) {
                                    isError = true
                                    feedbackMessage = initRes.message
                                } else {
                                    isError = false
                                    feedbackMessage = "USSD prompt sent to $accountNumber. Enter your SIM PIN on your phone..."
                                    var finalStatus = initRes.paymentStatus ?: "pending"
                                    var attempts = 0
                                    while (finalStatus != "successful" && finalStatus != "failed" && attempts < 12) {
                                        delay(5000)
                                        val verifyRes = paymentApi.verifyDeposit(ref)
                                        finalStatus = verifyRes.paymentStatus ?: "pending"
                                        attempts++
                                    }
                                    if (finalStatus == "successful") {
                                        userDao.insertOrUpdateUser(
                                            user.copy(
                                                walletBalanceZmw = user.walletBalanceZmw + amount,
                                                linkedProvider = selectedProvider,
                                                linkedAccountNumber = accountNumber.trim()
                                            )
                                        )
                                        walletDao.insertTransaction(
                                            WalletTransaction(
                                                referenceId = ref,
                                                userPhone = user.phone,
                                                type = "DEPOSIT",
                                                channel = selectedProvider,
                                                accountNumber = accountNumber.trim(),
                                                amountZmw = amount,
                                                status = "SUCCESS",
                                                description = "Verified deposit via $selectedProvider ($accountNumber)"
                                            )
                                        )
                                        isError = false
                                        feedbackMessage = "Deposit of ZMW ${"%.2f".format(amount)} confirmed! (Ref: $ref)"
                                        amountInput = ""
                                    } else {
                                        isError = true
                                        feedbackMessage = "Transaction not completed (Status: $finalStatus). Wallet was not credited."
                                    }
                                }
                            } else {
                                val ref = "WDR-${System.currentTimeMillis()}"
                                val withdrawRes = paymentApi.initiateWithdraw(
                                    WithdrawRequest(
                                        phone = accountNumber.trim(),
                                        amount = amount,
                                        provider = selectedProvider,
                                        accountName = user.fullName,
                                        reference = ref
                                    )
                                )
                                if (withdrawRes.status) {
                                    userDao.insertOrUpdateUser(
                                        user.copy(
                                            walletBalanceZmw = user.walletBalanceZmw - amount,
                                            linkedProvider = selectedProvider,
                                            linkedAccountNumber = accountNumber.trim()
                                        )
                                    )
                                    walletDao.insertTransaction(
                                        WalletTransaction(
                                            referenceId = ref,
                                            userPhone = user.phone,
                                            type = "WITHDRAWAL",
                                            channel = selectedProvider,
                                            accountNumber = accountNumber.trim(),
                                            amountZmw = amount,
                                            status = "SUCCESS",
                                            description = "Withdrawn to $selectedProvider ($accountNumber)"
                                        )
                                    )
                                    isError = false
                                    feedbackMessage = "ZMW ${"%.2f".format(amount)} sent to $selectedProvider $accountNumber (Ref: $ref)."
                                    amountInput = ""
                                } else {
                                    isError = true
                                    feedbackMessage = withdrawRes.message
                                }
                            }
                        } catch (e: Exception) {
                            isError = true
                            feedbackMessage = "Server Unreachable: Check that node server.js is running (${e.localizedMessage})."
                        } finally {
                            isProcessingNetwork = false
                        }
                    }
                }
            ) {
                Text(
                    if (isProcessingNetwork) "Waiting for PIN..."
                    else if (depositMode) "Send USSD Deposit"
                    else "Withdraw Funds"
                )
            }
        }
// Order Payment Confirmation Card
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
                        Text("Receiver: ${order.sellerName.ifBlank { "Seller" }}")
                        Text("Amount: ZMW ${"%.2f".format(order.totalPriceZmw)}")
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
                                        isError = true
                                        feedbackMessage = "Insufficient wallet balance. Deposit funds first."
                                        selectedOrder = null
                                    } else {
                                        scope.launch {
                                            val txnRef = "TXN-${System.currentTimeMillis().toString().takeLast(6)}"
                                            userDao.insertOrUpdateUser(
                                                buyer.copy(walletBalanceZmw = buyer.walletBalanceZmw - order.totalPriceZmw)
                                            )
                                            walletDao.insertTransaction(
                                                WalletTransaction(
                                                    referenceId = txnRef,
                                                    userPhone = buyer.phone,
                                                    type = "PAYMENT_SENT",
                                                    channel = "AgroMarket Wallet",
                                                    accountNumber = order.sellerPhone,
                                                    amountZmw = order.totalPriceZmw,
                                                    status = "SUCCESS",
                                                    description = "Paid ORD-${order.id} (${order.productName})"
                                                )
                                            )
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
                                                            description = "Received payment for ORD-${order.id} (${order.productName})"
                                                        )
                                                    )
                                                }
                                            }
                                            orderDao.updateOrder(
                                                order.copy(
                                                    paymentStatus = "Paid",
                                                    paymentMethod = "AgroMarket Wallet (Ref: $txnRef)"
                                                )
                                            )
                                            isError = false
                                            feedbackMessage = "Order ORD-${order.id} paid! Receipt: $txnRef"
                                            selectedOrder = null
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirm Payment")
                            }
                        }
                    }
                }
            }
        }
// Orders & Payment Status
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Orders & Payment Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (userOrders.isEmpty()) {
            item { Text("No orders found.") }
        } else {
            items(userOrders) { order ->
                val isSeller = order.sellerPhone == phone && order.buyerPhone != phone
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
                        if (order.paymentStatus == "Paid") {
                            Text("Receipt: ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                        } else if (!isSeller) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                selectedOrder = order
                                feedbackMessage = ""
                            }) {
                                Text("Pay from Wallet")
                            }
                        }
                    }
                }
            }
        }
// Account Statement
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