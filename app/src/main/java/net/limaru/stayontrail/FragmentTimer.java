package net.limaru.stayontrail;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import org.jetbrains.annotations.NotNull;

public class FragmentTimer extends Fragment {
  private CountDownTimer countDownTimer;
  private long timeRemaining;
  private final long startTime;
  private boolean isPaused = false;
  private final long id;

  public FragmentTimer (long duration, long id) {
    this.startTime = duration*1000;
    this.id = id;
  }

  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    View view = inflater.inflate(R.layout.fragment_timer, container, false);
    TextView timerText = view.findViewById(R.id.timerText);
    Button startButton = view.findViewById(R.id.startButton);
    Button pauseButton = view.findViewById(R.id.pauseButton);
    ImageView TreeImage = view.findViewById(R.id.TreeImage);

    TreeImage.setScaleX(0.3f);
    TreeImage.setScaleY(0.3f);
    // QOL change
    long hours = startTime / 3600000;
    long minutes = (startTime % 3600000) / 60000;
    long seconds = (startTime % 60000)/1000;
    timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TreeImage.setScaleX(0.3f);
        TreeImage.setScaleY(0.3f);
        timeRemaining = startTime;
        startTimer(timerText, TreeImage, pauseButton, startButton);
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
      }
    });

    pauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isPaused) {
          // Pause
          countDownTimer.cancel();
          isPaused = true;
          pauseButton.setText("Unpause");
        } else {
          // Unpause
          startTimer(timerText, TreeImage, pauseButton, startButton);
          isPaused = false;
          pauseButton.setText("Pause");
        }
      }
    });

    return view;
  }

  private void startTimer(TextView timerText, ImageView TreeImage, Button pauseButton, Button startButton) {
    countDownTimer = new CountDownTimer(timeRemaining, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {

        timeRemaining = millisUntilFinished; // keep updating remaining time
        float progress = 1.0f - ((float) millisUntilFinished / (float) startTime);
        float scale = 0.3f + (progress * 0.7f);

        TreeImage.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(900)
            .start();

        if (timeRemaining <= startTime/2) {
          TreeImage.setImageResource(R.drawable.tree2);
        }
        if (timeRemaining > startTime/2) {
          TreeImage.setImageResource(R.drawable.tree1);
        }
        long hours = millisUntilFinished / 3600000;
        long minutes = (millisUntilFinished % 3600000) / 60000;
        long seconds = (millisUntilFinished % 60000) / 1000;
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
      }

      @Override
      public void onFinish() {
        timerText.setText("Done :)");
        TreeImage.setImageResource(R.drawable.tree3);

        TreeImage.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(500)
            .start();

        startButton.setEnabled(true);
        startButton.setText("Restart");
        pauseButton.setEnabled(false);
        MainActivity.sqlHelper.deleteTask(id);
        MainActivity.sqlHelper.incrementStreakTasks();

        long lastTaskDone = MainActivity.sqlHelper.getLastTaskDone();

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long startOfYesterday = calendar.getTimeInMillis() / 1000;

        if (lastTaskDone < startOfToday && (lastTaskDone - startOfYesterday < 86400 || lastTaskDone == 0)) {
          MainActivity.sqlHelper.incrementStreakDays();
        }

        MainActivity.sqlHelper.setLastTaskDone(now / 1000);
        reloadStreak();
      }
    }.start();

  }

  private void reloadStreak () {
    Util.reloadStreak(this.requireActivity());
  }
}