package com.witness.agromarket.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.witness.agromarket.ui.screens.LoginScreen
import com.witness.agromarket.ui.screens.RegisterScreen
import com.witness.agromarket.ui.screens.HomeScreen
import com.witness.agromarket.ui.screens.AddProductScreen
import com.witness.agromarket.ui.screens.ViewProductsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("addProduct") {
            AddProductScreen()
        }

        composable("viewProducts") {
            ViewProductsScreen()
        }
    }
}