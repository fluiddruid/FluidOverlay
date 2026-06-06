package com.example.fluidoverlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fluidoverlay.ui.theme.FluidOverlayTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun noPermission_showsPermissionError_andGrantButton() {
        composeTestRule.setContent {
            FluidOverlayTheme {
                OverlayControls(
                    serviceRunning = mutableStateOf(false),
                    hasPermission = false,
                    onRequestPermission = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Overlay permission required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant Overlay Permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Overlay").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Stop Overlay").assertIsNotDisplayed()
    }

    @Test
    fun hasPermission_serviceNotRunning_showsStartButton() {
        composeTestRule.setContent {
            FluidOverlayTheme {
                OverlayControls(
                    serviceRunning = mutableStateOf(false),
                    hasPermission = true,
                    onRequestPermission = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Start Overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant Overlay Permission").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Stop Overlay").assertIsNotDisplayed()
    }

    @Test
    fun hasPermission_serviceRunning_showsActiveState() {
        composeTestRule.setContent {
            FluidOverlayTheme {
                OverlayControls(
                    serviceRunning = mutableStateOf(true),
                    hasPermission = true,
                    onRequestPermission = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Overlay is active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop Overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Overlay").assertIsNotDisplayed()
    }

    @Test
    fun appTitle_alwaysVisible() {
        composeTestRule.setContent {
            FluidOverlayTheme {
                OverlayControls(
                    serviceRunning = mutableStateOf(false),
                    hasPermission = false,
                    onRequestPermission = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Fluid Overlay").assertIsDisplayed()
    }
}
