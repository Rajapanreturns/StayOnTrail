package net.limaru.stayontrail.streakutils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;

public class ReminderScheduler {

  public static final String ACTION_REMINDER = "net.limaru.stayontrail.streakreminder";

  private static final int[] REMINDER_HOURS = {6, 18};

  /**
   * Schedules an alarm for the next upcoming 6am or 6pm.
   * Call this on app open and after each alarm fires.
   */
  public static void scheduleNext(Context context) {
    long nextTrigger = getNextReminderTime();
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    PendingIntent pendingIntent = buildPendingIntent(context);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (!alarmManager.canScheduleExactAlarms()) {
        // Exact alarms require user permission on Android 12+.
        // Fall back to inexact, or prompt the user in settings.
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
        return;
      }
    }

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
  }

  /** Cancels any pending reminder alarm. */
  public static void cancel(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(buildPendingIntent(context));
  }

  /**
   * Returns the millisecond timestamp of the next 6am or 6pm,
   * whichever comes first from now.
   */
  static long getNextReminderTime() {
    Calendar now = Calendar.getInstance();
    Calendar best = null;

    for (int hour : REMINDER_HOURS) {
      Calendar candidate = Calendar.getInstance();
      candidate.set(Calendar.HOUR_OF_DAY, hour);
      candidate.set(Calendar.MINUTE, 0);
      candidate.set(Calendar.SECOND, 0);
      candidate.set(Calendar.MILLISECOND, 0);

      // If this time has already passed today, target tomorrow.
      if (!candidate.after(now)) {
        candidate.add(Calendar.DAY_OF_YEAR, 1);
      }

      if (best == null || candidate.before(best)) {
        best = candidate;
      }
    }

    return best.getTimeInMillis();
  }

  private static PendingIntent buildPendingIntent(Context context) {
    Intent intent = new Intent(context, ReminderReceiver.class);
    intent.setAction(ACTION_REMINDER);
    return PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
  }
}
