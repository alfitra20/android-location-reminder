package com.example.alfitrarahman.lab4

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var client:FusedLocationProviderClient
    private lateinit var locationManager : LocationManager

    private val locationListener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationChanged(location: Location?) {
            val context:Context = applicationContext
            if (location != null){

                longitudeText.text = location.longitude.toString()
                latitudeText.text = location.latitude.toString()

                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val longitude = preferences.getString("longitude", "")
                val latitude = preferences.getString("latitude", "")
                val messageTitle = preferences.getString("messageTitle","")
                val messageContent = preferences.getString("messageContent","")
                val id = 1
                if (longitude != "") {
                    var newSavedLocation = Location("newSavedLocation")
                    newSavedLocation.longitude = longitude.toDouble()
                    newSavedLocation.latitude = latitude.toDouble()
                    var distance = location.distanceTo(newSavedLocation).toDouble()

                    if (distance<10) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            val name = "my Channel"
                            val description = "Description"
                            val importance = NotificationManager.IMPORTANCE_LOW
                            val mChannel = NotificationChannel(id.toString(), name, importance)
                            mChannel.description = description
                            mChannel.enableLights(true)
                            mChannel.lightColor = Color.RED
                            mChannel.enableVibration(true)
                            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

                            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mNotificationManager.createNotificationChannel(mChannel)


                            val mBuilder = Notification.Builder(context)
                                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                                    .setContentTitle(messageTitle)
                                    .setContentText(messageContent)
                                    .setChannelId(id.toString())

                            val resultIntent = Intent(context, MainActivity::class.java)
                            val stackBuilder = TaskStackBuilder.create(context)
                            stackBuilder.addParentStack(MainActivity::class.java)
                            stackBuilder.addNextIntent(resultIntent)
                            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                            mBuilder.setContentIntent(resultPendingIntent)

                            //var notifications = mNotificationManager.activeNotifications
                            //notifications.forEach { notif -> run {
                            //    if (notif.id != 1) {
                                    mNotificationManager.notify(id, mBuilder.build())
                            //    }
                            //}}
                        } else {
                            val CHANNEL_ID = "my_channel_01"
                            var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) as Uri

                            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.id.icon)
                                    .setContentTitle(messageTitle)
                                    .setContentText(messageContent)
                                    .setSound(uri)

                            val resultIntent = Intent(context, MainActivity::class.java)
                            val stackBuilder = TaskStackBuilder.create(context)
                            stackBuilder.addParentStack(MainActivity::class.java)
                            stackBuilder.addNextIntent(resultIntent)
                            val resultPendingIntent = stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            mBuilder.setContentIntent(resultPendingIntent)
                            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mNotificationManager.notify(id, mBuilder.build())
                        }
                    }
                    else {
                        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        mNotificationManager.cancel(id)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        var longitude = ""
        var latitude = ""
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        client = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            longToast("Permission Denied")
            finish()
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        } catch(ex: SecurityException) {
            Log.d("myTag", "Security Exception, no location available")
        }
        client.lastLocation.addOnSuccessListener(this, object: OnSuccessListener<Location> {
            override fun onSuccess(location: Location) {
                if (location != null) {
                    longitude = location.longitude.toString()
                    latitude = location.latitude.toString()
                    longitudeText.text = location.longitude.toString()
                    latitudeText.text = location.latitude.toString()
                }
            }
        })
        addMessageButton.setOnClickListener{
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                        preferences.edit()
                                .putString("longitude", longitude)
                                .putString("latitude", latitude)
                                .putString("messageTitle", messageTitleText.text.toString())
                                .putString("messageContent", messageContextText.text.toString())
                                .apply()
        }
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf<String>(ACCESS_FINE_LOCATION),1)
    }

    override fun onDestroy() {
        super.onDestroy()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit()
                .putString("longitude", "")
                .putString("latitude", "")
                .putString("messageTitle", "")
                .putString("messageContent", "")
                .apply()
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(1)
    }

    override fun onStop() {
        super.onStop()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit()
                .putString("longitude", "")
                .putString("latitude", "")
                .putString("messageTitle", "")
                .putString("messageContent", "")
                .apply()
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(1)
    }
}
