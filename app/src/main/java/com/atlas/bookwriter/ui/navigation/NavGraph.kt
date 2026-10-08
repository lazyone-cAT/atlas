package com.atlas.bookwriter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.atlas.bookwriter.data.Storage
import com.atlas.bookwriter.model.AppSettings
import com.atlas.bookwriter.ui.BookScreen
import com.atlas.bookwriter.ui.BookshelfScreen
import com.atlas.bookwriter.ui.EntryEditScreen
import com.atlas.bookwriter.ui.SettingsScreen

object Routes {
    const val BOOKSHELF = "bookshelf"
    const val BOOK = "book/{bookId}"
    const val ENTRY_EDIT = "entry_edit/{bookId}?entryId={entryId}"
    const val SETTINGS = "settings"

    fun book(bookId: String) = "book/$bookId"
    fun entryEdit(bookId: String, entryId: String? = null) =
        if (entryId != null) "entry_edit/$bookId?entryId=$entryId"
        else "entry_edit/$bookId"
}

@Composable
fun AtlasNavGraph(
    storage: Storage,
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onPickPfp: ((String) -> Unit) -> Unit,
    onPickEditorBg: ((String) -> Unit) -> Unit,
    onPickCover: (String, (String) -> Unit) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.BOOKSHELF) {
        composable(Routes.BOOKSHELF) {
            BookshelfScreen(
                storage = storage,
                settings = settings,
                editorBackgroundPath = settings.editorBackgroundPath,
                onSettingsChanged = onSettingsChanged,
                onOpenBook = { bookId ->
                    navController.navigate(Routes.book(bookId))
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onPickPfp = onPickPfp,
                onPickCover = onPickCover
            )
        }

        composable(
            route = Routes.BOOK,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            BookScreen(
                storage = storage,
                bookId = bookId,
                editorBackgroundPath = settings.editorBackgroundPath,
                backgroundTheme = settings.backgroundTheme,
                onBack = { navController.popBackStack() },
                onEditEntry = { bid, entryId ->
                    navController.navigate(Routes.entryEdit(bid, entryId))
                }
            )
        }

        composable(
            route = Routes.ENTRY_EDIT,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("entryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val entryId = backStackEntry.arguments?.getString("entryId")
            EntryEditScreen(
                storage = storage,
                bookId = bookId,
                entryId = entryId,
                editorBackgroundPath = settings.editorBackgroundPath,
                backgroundTheme = settings.backgroundTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                storage = storage,
                settings = settings,
                editorBackgroundPath = settings.editorBackgroundPath,
                onSettingsChanged = onSettingsChanged,
                onBack = { navController.popBackStack() },
                onPickPfp = onPickPfp,
                onPickEditorBg = onPickEditorBg
            )
        }
    }
}
