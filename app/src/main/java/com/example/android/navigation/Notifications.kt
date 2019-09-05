package com.example.android.navigation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android.navigation.util.Constants
import com.google.firebase.firestore.FirebaseFirestore


class Notifications : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("Tag.Notifications", "From: " + remoteMessage.from!!)
        if (remoteMessage.data.size > 0) {
            Log.d("Tag.Notifications", "Message data payload: " + remoteMessage.data)
        }
        if (remoteMessage.notification != null) {
            Log.d("Tag.Notifications", "Message Notification Body: " + remoteMessage.notification!!.body!!)
            sendNotification(remoteMessage)
        }

    }
//    override fun onNewToken(token: String) {
//        Log.d("Tag.Notifications", "Refreshed token: " + token!!)
//        //to shared pref and to db
//        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        prefs.edit().putString(Constants().FCM_TOKEN, token).apply()
//        val fire: FirebaseFirestore = FirebaseFirestore.getInstance()
//        fire.collection("users").document(prefs.getString(Constants().UID,"notavaiduid")).update("tokenFcm",token)
//    }

    private fun sendNotification(msg: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("type", msg.messageType)
        intent.putExtra("message", msg.data.toString())
        intent.putExtra("id", msg.messageId)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = msg.notification!!.channelId?:"notification"
        val idCurrentTime = System.currentTimeMillis().toInt()
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(msg.notification!!.title)
                .setSmallIcon(R.drawable.android)
                .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,R.mipmap.ic_launcher))
                .setContentText(msg.notification!!.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channelName: String = "Daily notification"
            if(channelId.equals("notification")) channelName = channelId
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(idCurrentTime, notificationBuilder.build())
    }

}
