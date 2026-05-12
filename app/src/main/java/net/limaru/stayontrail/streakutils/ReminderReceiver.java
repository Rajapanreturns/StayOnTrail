package net.limaru.stayontrail.streakutils;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import net.limaru.stayontrail.MainActivity;
import net.limaru.stayontrail.R;
import static net.limaru.stayontrail.streakutils.ReminderScheduler.ACTION_REMINDER;

public class ReminderReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!ACTION_REMINDER.equals(intent.getAction())) return;

    // Only notify if the user hasn't opened the app since the start of today's alarm window.
    if (!StreakUtils.hasBeenClosedForCurrentWindow(context)) {
      // User already opened the app during this window — skip, but still reschedule.
      ReminderScheduler.scheduleNext(context);
      return;
    }

    createNotificationChannel(context);
    showNotification(context);
    ReminderScheduler.scheduleNext(context); // arm the next alarm (6am or 6pm)
  }

  private void showNotification(Context context) {
    Intent openIntent = new Intent(context, MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(
        context, 0, openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    Notification notification = new NotificationCompat.Builder(context, "reminder_channel")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle("Time to check in!")
        .setContentText("Open the app to stay on track.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build();

    NotificationManagerCompat manager = NotificationManagerCompat.from(context);
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        == PackageManager.PERMISSION_GRANTED) {
      manager.notify(1001, notification);
    }
  }

  private void createNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
          "reminder_channel", "Reminders", NotificationManager.IMPORTANCE_DEFAULT
      );
      NotificationManager manager = context.getSystemService(NotificationManager.class);
      manager.createNotificationChannel(channel);
    }
  }
}
