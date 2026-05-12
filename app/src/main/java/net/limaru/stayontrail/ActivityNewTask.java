package net.limaru.stayontrail;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityNewTask extends ComponentTaskbox {


  @Override
  public int layout() {
    return R.layout.activity_new_task;
  }

  @Override
  public int confirmButton() {
    return R.id.new_task_set;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    findViewById(R.id.ai_prompt_btn).setOnClickListener(v -> {
      Intent intent = new Intent(ActivityNewTask.this, ActivityAiPrompt.class);
      startActivity(intent);
    });

    if (getIntent().getExtras() != null) {
      this.titleView.setText(getIntent().getStringExtra("title"));
      this.descriptionView.setText(getIntent().getStringExtra("description"));

      int day   = getIntent().getIntExtra("day", 0);
      int month = getIntent().getIntExtra("month", 0);
      int year  = getIntent().getIntExtra("year", 0);

      if (day != 0 && month != 0 && year != 0) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        this.dateTimeSecs[0] = cal.getTimeInMillis() / 1000L;

        SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        this.dateView.setText(fmt.format(new Date(this.dateTimeSecs[0] * 1000L)));
      }

      int hours   = getIntent().getIntExtra("duration_hours", 0);
      int minutes = getIntent().getIntExtra("duration_minutes", 0);
      this.durationHoursView.setText(String.valueOf(hours));
      this.durationMinutesView.setText(String.valueOf(minutes));

      setSpinnerByValue(this.priorityView, getIntent().getStringExtra("priority"));
      setSpinnerByValue(this.repeatsView, getIntent().getStringExtra("repeats"));
      setSpinnerByValue(this.groupView, getIntent().getStringExtra("group"));
    }
  }

  private void setSpinnerByValue(Spinner spinner, String value) {
    if (value == null) return;
    ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
    for (int i = 0; i < adapter.getCount(); i++) {
      if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
        spinner.setSelection(i);
        break;
      }
    }
  }

  @Override
  public void buttonOnFinal(Task task) {
    long until = task.getUntil();
    Calendar timeRef = Calendar.getInstance();
    int i = 2 << 7;
    switch (task.getRepeats()) {
      case NEVER:
        MainActivity.sqlHelper.insertTask(task);
        break;
      case DAILY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) {
            break;
          }
          MainActivity.sqlHelper.insertTask(task);
          timeRef.add(Calendar.DATE, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case WEEKLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) {
            break;
          }
          MainActivity.sqlHelper.insertTask(task);
          timeRef.add(Calendar.WEEK_OF_YEAR, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case FORTNIGHTLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) {
            break;
          }
          MainActivity.sqlHelper.insertTask(task);
          timeRef.add(Calendar.WEEK_OF_YEAR, 2);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case MONTHLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) {
            break;
          }
          MainActivity.sqlHelper.insertTask(task);
          timeRef.add(Calendar.MONTH, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
    }

    Intent intent = new Intent(ActivityNewTask.this, MainActivity.class);
    startActivity(intent);
  }
}