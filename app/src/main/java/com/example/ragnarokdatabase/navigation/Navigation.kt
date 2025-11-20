package com.example.ragnarokdatabase.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ragnarokdatabase.view.FilteredItemsScreen
import com.example.ragnarokdatabase.view.ItemDetailScreen
import com.example.ragnarokdatabase.view.MainScreen
import com.example.ragnarokdatabase.view.SearchScreen
import com.example.ragnarokdatabase.viewmodel.MainViewModel
import com.example.ragnarokdatabase.viewmodel.SearchViewModel

/**
 * Definition of the application's navigation routes
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Search : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=$query"
    }
    object ItemDetail : Screen("item/{itemId}") {
        fun createRoute(itemId: Int) = "item/$itemId"
    }
    object FilteredItems : Screen("filter/{itemType}") {
        fun createRoute(itemType: String) = "filter/$itemType"
    }
}

/**
 * Application's NavHost that defines all navigation
 */
@Composable
fun RagnarokNavHost(
    navController: NavHostController
) {
    // Share the MainScreen ViewModel between recompositions
    val mainViewModel: MainViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(
            route = Screen.Main.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            // Detect when navigating back to this screen
            LaunchedEffect(backStackEntry) {
                // Reload items with the currently selected period
                mainViewModel.refreshCurrentPeriod()
            }

            MainScreen(
                onItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onSearchClick = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                },
                onTypeFilterClick = { itemType ->
                    navController.navigate(Screen.FilteredItems.createRoute(itemType))
                },
                viewModel = mainViewModel
            )
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = backStackEntry)
            SearchScreen(
                initialQuery = initialQuery,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onTypeFilterClick = { type ->
                    navController.navigate(Screen.FilteredItems.createRoute(type))
                },
                onItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                viewModel = searchViewModel,
                mainViewModel = mainViewModel
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onTypeFilterClick = { type ->
                    navController.navigate(Screen.FilteredItems.createRoute(type))
                },
                mainViewModel = mainViewModel
            )
        }

        composable(
            route = Screen.FilteredItems.route,
            arguments = listOf(
                navArgument("itemType") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val itemType = backStackEntry.arguments?.getString("itemType") ?: ""
            val filterViewModel: com.example.ragnarokdatabase.viewmodel.FilterViewModel =
                viewModel(viewModelStoreOwner = backStackEntry)
            FilteredItemsScreen(
                itemType = itemType,
                onItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onTypeFilterClick = { type ->
                    navController.navigate(Screen.FilteredItems.createRoute(type)) {
                        popUpTo(Screen.FilteredItems.route) { inclusive = true }
                    }
                },
                viewModel = filterViewModel,
                mainViewModel = mainViewModel
            )
        }
    }
}

