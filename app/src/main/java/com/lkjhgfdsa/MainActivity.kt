package com.lkjhgfdsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lkjhgfdsa.ui.pages.HomePage
import com.lkjhgfdsa.ui.pages.OptionEditPage
import com.lkjhgfdsa.ui.pages.OptionListPage
import com.lkjhgfdsa.ui.theme.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Main() }
    }
}

@Composable
fun Main() {
    Theme {
        val navController = rememberNavController()
        val goBack = goBack@{
            navController.popBackStack()
            return@goBack
        }
        val goTo = goTo@{ route: String ->
            navController.navigate(route) {
                popUpTo(route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            return@goTo
        }
        NavHost(
            navController = navController,
            startDestination = "/home",
            enterTransition = {
                val init = initialState.destination.route ?: ""
                val target = targetState.destination.route ?: ""
                if (target.startsWith(init)) {
                    slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it })
                } else {
                    fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val init = initialState.destination.route ?: ""
                val target = targetState.destination.route ?: ""
                if (target.startsWith(init)) {
                    fadeOut(animationSpec = tween(300))
                } else {
                    slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it })
                }
            }
        ) {
            composable(
                route = "/home"
            ) {
                HomePage(goTo = goTo)
            }
            composable(
                route = "/home/option-list"
            ) {
                OptionListPage(goBack = goBack, goTo = goTo)
            }
            composable(
                route = "/home/option-list/option-edit?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        defaultValue = "id"
                    }
                )
            ) {
                OptionEditPage(goBack = goBack)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Main()
}