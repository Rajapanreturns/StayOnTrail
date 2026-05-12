package net.limaru.stayontrail;

import android.database.sqlite.SQLiteException;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import net.limaru.stayontrail.streakutils.ReminderScheduler;
import net.limaru.stayontrail.streakutils.StreakUtils;

public class MainActivity extends AppCompatActivity {

  public static SqlHelper sqlHelper;

  @Override
  protected void onResume() {
    super.onResume();
    StreakUtils.recordOpen(this);
    ReminderScheduler.scheduleNext(this); // re-arms the next 6am/6pm alarm
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

    Fragment fragmentHome = new FragmentHome();
    Fragment fragmentTask = new FragmentTask();
    Fragment fragmentReward = new FragmentReward();

    setCurrentFragment(fragmentHome);
    try	(SqlHelper temp = new SqlHelper(this)) {
      sqlHelper = temp;
    }
    catch (SQLiteException ignored) {}

    bottomNavigationView.setOnItemSelectedListener(item -> {
      if (item.getItemId() == R.id.page_home) {
        setCurrentFragment(fragmentHome);
      }
      else if (item.getItemId() == R.id.page_task) {
        setCurrentFragment(fragmentTask);
      }
      else if (item.getItemId() == R.id.page_reward) {
        setCurrentFragment(fragmentReward);
      }
      return true;
    });

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.add(Calendar.DAY_OF_YEAR, -1);
    long startOfYesterday = calendar.getTimeInMillis() / 1000;
    Log.i("MainActivity", "lastTaskDone: "+sqlHelper.getLastTaskDone());
    Log.i("MainActivity", "startOfYesterday: "+startOfYesterday);
    if (sqlHelper.getLastTaskDone() < startOfYesterday) {
      sqlHelper.setStreakDays(0);
    }
    Util.reloadStreak(this);
  }

  private void setCurrentFragment(Fragment fragment) {
    this.getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.frame_layout_fragment, fragment)
        .commit();
  }
}