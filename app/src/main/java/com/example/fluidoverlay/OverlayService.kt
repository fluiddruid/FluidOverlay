package com.example.fluidoverlay

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OverlayService : Service() {

    companion object {
        @Volatile var isRunning = false
        private const val CHANNEL_ID = "overlay_channel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var batteryIcon: ImageView
    private lateinit var batteryText: TextView
    private lateinit var clockText: TextView
    private lateinit var params: WindowManager.LayoutParams
    private var overlayAdded = false
    private var lastCharging: Boolean? = null
    private var lastPct: Int = -1

    private fun showOverlay() {
        if (!overlayAdded) {
            windowManager.addView(overlayView, params)
            overlayAdded = true
        }
    }

    private fun hideOverlay() {
        if (overlayAdded) {
            windowManager.removeView(overlayView)
            overlayAdded = false
        }
    }

    private val pinHandler = Handler(Looper.getMainLooper())
    private val pinPoller = object : Runnable {
        override fun run() {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_PINNED) showOverlay()
            else hideOverlay()
            pinHandler.postDelayed(this, 1000)
        }
    }

    private fun batteryDrawable(pct: Int, charging: Boolean) = when {
        pct == 100 -> R.drawable.ic_battery_full
        charging   -> R.drawable.ic_battery_charging
        pct > 85   -> R.drawable.ic_battery_6_bar
        pct > 71 -> R.drawable.ic_battery_5_bar
        pct > 57 -> R.drawable.ic_battery_4_bar
        pct > 42 -> R.drawable.ic_battery_3_bar
        pct > 28 -> R.drawable.ic_battery_2_bar
        pct > 14 -> R.drawable.ic_battery_1_bar
        else     -> R.drawable.ic_battery_0_bar
    }

    private val clockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = updateClock()
    }

    private fun updateClock() {
        val pattern = if (DateFormat.is24HourFormat(this)) "HH:mm" else "h:mm"
        clockText.text = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level < 0 || scale <= 0) return
            val pct = (level * 100 / scale.toFloat()).toInt()
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            if (charging != lastCharging || pct != lastPct) {
                lastCharging = charging
                lastPct = pct
                batteryIcon.setImageResource(batteryDrawable(pct, charging))
            }
            batteryText.text = "$pct%"
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForegroundCompat()
        setupOverlay()
        pinHandler.post(pinPoller)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val clockFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(clockReceiver, clockFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        isRunning = false
        hideOverlay()
        pinHandler.removeCallbacks(pinPoller)
        try { unregisterReceiver(batteryReceiver) } catch (_: IllegalArgumentException) {}
        try { unregisterReceiver(clockReceiver) } catch (_: IllegalArgumentException) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupOverlay() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        batteryIcon = overlayView.findViewById(R.id.battery_icon)
        batteryText = overlayView.findViewById(R.id.battery_text)
        clockText = overlayView.findViewById(R.id.clock)
        updateClock()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 4
            y = 5
        }

    }
}
