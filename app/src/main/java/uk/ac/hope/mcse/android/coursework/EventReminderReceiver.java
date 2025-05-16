package uk.ac.hope.mcse.android.coursework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class EventReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("EventReminder", "Notification triggered for event");

        String eventName = intent.getStringExtra("event_name");
        String eventTime = intent.getStringExtra("event_time");
        
        if (eventName == null || eventTime == null) {
            Log.e("EventReminder", "Missing event data in intent");
            return;
        }

        // Create an intent for the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        // Generate unique notification ID based on event name and time
        int notificationId = (eventName + eventTime).hashCode();

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Event")
            .setContentText("Your event " + eventName + " starts soon!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }
} 