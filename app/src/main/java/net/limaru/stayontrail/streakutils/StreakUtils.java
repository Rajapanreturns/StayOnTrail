package net.limaru.stayontrail.streakutils;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;

public class StreakUtils {

  private static final String PREFS_NAME = "app_open_tracker";
  private static final String KEY_LAST_OPEN = "last_open_timestamp";

  private static final int[] WINDOW_HOURS = {0, 6, 18}; // boundaries of each day window

  public static void recordOpen(Context context) {
    getPrefs(context).edit()
        .putLong(KEY_LAST_OPEN, System.currentTimeMillis())
        .apply();
  }

  public static long getLastOpenTime(Context context) {
    return getPrefs(context).getLong(KEY_LAST_OPEN, -1L);
  }

  /**
   * Returns true if the user has NOT opened the app during the current reminder window.
   * Windows are: midnight–6am, 6am–6pm, 6pm–midnight.
   */
  public static boolean hasBeenClosedForCurrentWindow(Context context) {
    long lastOpen = getLastOpenTime(context);
    if (lastOpen == -1L) {
      return true;
    }

    long windowStart = getCurrentWindowStart();
    return lastOpen < windowStart;
  }

  /**
   * Returns the timestamp (ms) of the start of the current window,
   * based on the WINDOW_HOURS boundaries.
   */
  private static long getCurrentWindowStart() {
    Calendar now = Calendar.getInstance();
    int currentHour = now.get(Calendar.HOUR_OF_DAY);

    // Find the latest boundary that is <= current hour.
    int windowHour = WINDOW_HOURS[0];
    for (int boundary : WINDOW_HOURS) {
      if (currentHour >= boundary) {
        windowHour = boundary;
      }
    }

    Calendar windowStart = Calendar.getInstance();
    windowStart.set(Calendar.HOUR_OF_DAY, windowHour);
    windowStart.set(Calendar.MINUTE, 0);
    windowStart.set(Calendar.SECOND, 0);
    windowStart.set(Calendar.MILLISECOND, 0);
    return windowStart.getTimeInMillis();
  }

  private static SharedPreferences getPrefs(Context context) {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
  }
}
