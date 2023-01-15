package hm.dev.androidservices

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log.d
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_CALL
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import kotlinx.coroutines.*


class BatteryService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
    private lateinit var wakeLock: PowerManager.WakeLock


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Do your foreground service work here

        createNotificationChannel()
        startPowerService()
        val pendingIntent2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, notificationIntent, pendingIntent2)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, pendingIntent2)
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Charging Animation is running")
            .setContentText("Go to app settings to stop the service")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(101, notification)
        d("serviceRunning", "onStartCommand: serviceRunning")


        return START_STICKY
    }

    private fun startPowerService() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryService::lock")
        wakeLock.acquire(5000)
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                launch(Dispatchers.IO) {
                    d("ForegroundService", "startPowerService: ")
                }
                delay(5000)
            }
        }


    }


    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()

//        startForeground(9999, Notification())
        d("serviceCreated", "onCreate: serviceCreated")
    }


    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        //start service on task removed
        val serviceIntent = Intent(applicationContext, BatteryService::class.java).also {
            it.setPackage(packageName)
        }

        val flagsMutabilityIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

        val restartServiceIntent: PendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) PendingIntent.getForegroundService(
                applicationContext,
                101,
                serviceIntent,
                flagsMutabilityIntent
            )
            else PendingIntent.getService(
                applicationContext,
                101,
                serviceIntent,
                flagsMutabilityIntent
            )


        val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val triggerTime = SystemClock.elapsedRealtime() + 5000 // 5 seconds

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                restartServiceIntent

            )
        } else {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                restartServiceIntent
            )
        }

    }


}
