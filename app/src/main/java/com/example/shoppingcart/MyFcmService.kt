package com.example.shoppingcart

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.shoppingcart.activities.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class MyFcmService : FirebaseMessagingService() {

    private companion object{
        private const val TAG = "MY_FCM_TAG"

        private const val NOTIFICATION_CHANNEL_ID = "SHOPPING_CHANNEL_ID"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = "${remoteMessage.notification?.title}"
        val body = "${remoteMessage.notification?.body}"

        val senderUid = "${remoteMessage.data["senderUid"]}"
        val notificationType = "${remoteMessage.data["notificationType"]}"

        showChatNotification(title, body, senderUid)
    }

    private fun showChatNotification(notificationTitle: String, notificationDescription: String, senderUid: String){
        //Generate random integer between 3000 to use as notification id
        val notificationId = Random().nextInt(3000)
        //init noti manager
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //function call to setup noti channel in case Android 0 and above
        setupNotificationChannel(notificationManager)
        //Intent to launch ChatActivity when noti is clicked
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("receiptUid", senderUid)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //pending intent to add in notification
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_IMMUTABLE)
        //setup noti
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.img)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        //show noti
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun setupNotificationChannel(notificationManager: NotificationManager){
        //Starting in Android 8 (Api level 26) all notification must be assigned to a channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Chat Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Show Chat Notifications"
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}