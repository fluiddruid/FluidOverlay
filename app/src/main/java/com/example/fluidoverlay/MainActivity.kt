package com.example.fluidoverlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fluidoverlay.ui.theme.FluidOverlayTheme

class MainActivity : ComponentActivity() {
    private val serviceRunning = mutableStateOf(OverlayService.isRunning)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluidOverlayTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OverlayControls(
                        serviceRunning = serviceRunning,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceRunning.value = OverlayService.isRunning
    }
}

@Composable
fun OverlayControls(serviceRunning: MutableState<Boolean>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var serviceRunningState by serviceRunning

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasPermission = Settings.canDrawOverlays(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (!hasPermission) {
            Text(
                text = stringResource(R.string.permission_required),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                permissionLauncher.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }) {
                Text(stringResource(R.string.grant_permission))
            }
        } else if (!serviceRunningState) {
            Button(onClick = {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, OverlayService::class.java)
                )
                serviceRunningState = true
            }) {
                Text(stringResource(R.string.start_overlay))
            }
        } else {
            Text(
                text = stringResource(R.string.overlay_active),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = {
                context.stopService(Intent(context, OverlayService::class.java))
                serviceRunningState = false
            }) {
                Text(stringResource(R.string.stop_overlay))
            }
        }
    }
}
