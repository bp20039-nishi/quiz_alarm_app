
package com.example.alarm1


/*https://pg.akihiro-takeda.com/android-alarm/#toc7*/
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val toast = Toast.makeText(context, "アラームによる処理が実行されました。", Toast.LENGTH_SHORT)
        toast.show()

        val intent2 = Intent(context, QuizActivity::class.java)
        // Activity以外からActivityを呼び出すためのフラグを設定
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent2)


        /*  通知使用時（未完成）
        val fullScreenIntent = Intent(context, QuizActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Incoming call")
                .setContentText("(919) 555-1234")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)

        val incomingCallNotification = notificationBuilder.build()

         */
    }
}

