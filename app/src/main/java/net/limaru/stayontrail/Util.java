package net.limaru.stayontrail;
import android.app.Activity;
import android.widget.TextView;
import static net.limaru.stayontrail.MainActivity.sqlHelper;

public class Util {
  public static String toTitleCase (String s) {
    if (s == null || s.isEmpty()) {
      return s;
    }
    StringBuilder converted = new StringBuilder();

    boolean convertNext = true;
    for (char c : s.toCharArray()) {
      if (Character.isSpaceChar(c)) {
        convertNext = true;
      } else if (convertNext) {
        c = Character.toTitleCase(c);
        convertNext = false;
      } else {
        c = Character.toLowerCase(c);
      }
      converted.append(c);
    }

    return converted.toString();
  }

  public static void reloadStreak (Activity activity) {
    int streakDays = sqlHelper.getStreakDays();
    int streakTasks = sqlHelper.getStreakTasks();

    TextView daysView = activity.findViewById(R.id.day_count);
    daysView.setText(String.format("\uD83D\uDD25 %d", streakDays));
    TextView tasksView = activity.findViewById(R.id.task_count);
    tasksView.setText(String.format("🚩 %d", streakTasks));
  }
}
