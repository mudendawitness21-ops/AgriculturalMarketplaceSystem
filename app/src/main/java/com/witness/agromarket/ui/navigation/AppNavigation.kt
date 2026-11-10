package com.witness.agromarket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.witness.agromarket.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("addProduct") { AddProductScreen(navController) }
        composable("viewProducts") { ViewProductsScreen(navController) }
        composable("orders") { OrdersScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("payments") { PaymentsScreen(navController) }
        composable("trackGoods") { TrackGoodsScreen(navController) }
    }
}
