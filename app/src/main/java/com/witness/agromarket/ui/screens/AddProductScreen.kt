package com.witness.agromarket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.witness.agromarket.data.model.Product
import com.witness.agromarket.data.repository.ProductRepository

@Composable
fun AddProductScreen(
    navController: NavHostController
) {

    var productName by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var price by remember {
        mutableStateOf("")
    }

    var quantity by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Add Product",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = {
                productName = it
            },
            label = {
                Text("Product Name")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
            },
            label = {
                Text("Category")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
            },
            label = {
                Text("Price")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it
            },
            label = {
                Text("Quantity")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (
                    productName.isBlank() ||
                    category.isBlank() ||
                    price.isBlank() ||
                    quantity.isBlank()
                ) {

                    errorMessage = "Please fill in all fields"

                } else {

                    ProductRepository.products.add(
                        Product(
                            productName,
                            category,
                            price,
                            quantity
                        )
                    )

                    errorMessage = "Product Saved Successfully"

                    productName = ""
                    category = ""
                    price = ""
                    quantity = ""
                }
            }
        ) {
            Text("Save Product")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("home")
            }

        ) {
            Text("Back")
        }
    }
}