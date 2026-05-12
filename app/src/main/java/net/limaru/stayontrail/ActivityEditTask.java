package net.limaru.stayontrail;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class ActivityEditTask extends ComponentTaskbox {

  private long id = 0;

  @Override
  public int layout () {
    return R.layout.activity_edit_task;
  }

  @Override
  public int confirmButton () {
    return R.id.edit_task_set;
  }

  @Override
  public void onCreate (@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      this.id = extras.getLong("taskId");
      ArrayList<Long> dependents = MainActivity.sqlHelper.getDependents(this.id);
      // remove self and dependencies from prerequisite list to force graph to be a DAG
      this.prerequisiteTasks.removeIf((Task t) ->
          t.getId() == this.id || dependents.contains(t.getId()));
    }
    // Get task object to populate fields
    SqlHelper sqlHelper = MainActivity.sqlHelper;
    Task task = sqlHelper.getTask(this.id);
    if (task != null) {
      this.titleView.setText(task.getTitle());
      this.descriptionView.setText(task.getDescription());
      this.durationHoursView.setText(String.valueOf(task.getDuration() / 3600));
      this.durationMinutesView.setText(String.valueOf((task.getDuration() % 3600) / 60));
      this.groupView.setSelection(task.getGroup().ordinal());
      this.priorityView.setSelection(task.getPriority().ordinal());
      StringBuilder builder = new StringBuilder();
      this.selectedTaskIds = task.getPrerequisitesAsList();
      for (int i = 0; i < selectedTaskIds.size(); i++) {
        long prerequisite = selectedTaskIds.get(i);
        Task p = sqlHelper.getTask(prerequisite);
        if (p == null) {
          continue;
        }
        builder.append(p.getTitle());
        if (i < selectedTaskIds.size()-1) {
          builder.append(", ");
        }
      }
      this.prerequisitesView.setText(builder.toString());
      this.dateTimeSecs = new long[]{task.getDate(), task.getUntil()};
    }

    Button deleteBtn = this.findViewById(R.id.edit_task_delete);
    deleteBtn.setOnClickListener(v -> {
      MainActivity.sqlHelper.deleteTask(this.id);
      Intent intent = new Intent(ActivityEditTask.this, MainActivity.class);
      startActivity(intent);
    });
  }

  @Override
  public void buttonOnFinal (Task task) {
    long until = task.getUntil();
    Calendar timeRef = Calendar.getInstance();
    int i = 2 << 7;  // Limit task insertion to 2 << 7 to prevent crashes
    switch (task.getRepeats()) {
      case NEVER:
        MainActivity.sqlHelper.updateTask(this.id, task);
        break;
      case DAILY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) break;
          MainActivity.sqlHelper.updateTask(this.id, task);
          timeRef.add(Calendar.DATE, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case WEEKLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) break;
          MainActivity.sqlHelper.updateTask(this.id, task);
          timeRef.add(Calendar.WEEK_OF_YEAR, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case FORTNIGHTLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) break;
          MainActivity.sqlHelper.updateTask(this.id, task);
          timeRef.add(Calendar.WEEK_OF_YEAR, 2);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
      case MONTHLY:
        task.setRepeats(SqlHelper.ERepeats.NEVER);
        while (timeRef.getTimeInMillis() / 1000 <= until) {
          if (--i < 0) break;
          MainActivity.sqlHelper.updateTask(this.id, task);
          timeRef.add(Calendar.MONTH, 1);
          task.setDate(timeRef.getTimeInMillis() / 1000);
        }
        break;
    }

    Intent intent = new Intent(ActivityEditTask.this, MainActivity.class);
    startActivity(intent);
  }
}
