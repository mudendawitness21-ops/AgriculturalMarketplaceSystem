package com.witness.agromarket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.witness.agromarket.ui.screens.AddProductScreen
import com.witness.agromarket.ui.screens.HomeScreen
import com.witness.agromarket.ui.screens.LoginScreen
import com.witness.agromarket.ui.screens.OrdersScreen
import com.witness.agromarket.ui.screens.ProfileScreen
import com.witness.agromarket.ui.screens.RegisterScreen
import com.witness.agromarket.ui.screens.ViewProductsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            LoginScreen(navController)
        }
        composable(route = "register") {
            RegisterScreen(navController)
        }
        composable(route = "home") {
            HomeScreen(navController)
        }
        composable(route = "addProduct") {
            AddProductScreen(navController)
        }
        composable(route = "viewProducts") {
            ViewProductsScreen(navController)
        }
        composable(route = "orders") {
            OrdersScreen(navController)
        }
        composable(route = "profile") {
            ProfileScreen(navController)
        }
    }
}
