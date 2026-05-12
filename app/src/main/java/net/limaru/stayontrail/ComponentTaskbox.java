package net.limaru.stayontrail;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public abstract class ComponentTaskbox
    extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

  public abstract int layout ();
  public abstract int confirmButton ();
  public abstract void buttonOnFinal (Task task);

  EditText titleView;
  EditText descriptionView;
  Button dateView;
  EditText durationHoursView;
  EditText durationMinutesView;
  Spinner repeatsView;
  Button untilView;
  Spinner groupView;
  Spinner priorityView;
  long[] dateTimeSecs = new long[]{System.currentTimeMillis() / 1000L, 0};
  TextView prerequisitesView;

  // for prerequisites
  List<Task> prerequisiteTasks = MainActivity.sqlHelper.getAllTasks();
  boolean[] selectedTasks = new boolean[prerequisiteTasks.size()];
  ArrayList<Integer> selectedTaskList = new ArrayList<>();
  List<Long> selectedTaskIds = new ArrayList<>();

  public void onCreate (@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(layout());

    // buttons and spinners
    Button setTaskBtn = this.findViewById(confirmButton());
    this.repeatsView = this.findViewById(R.id.editRepeats);
    this.groupView = this.findViewById(R.id.editGroup);
    this.priorityView = this.findViewById(R.id.editPriority);

    setOptions(Stream.of(SqlHelper.ERepeats.values()).map(e -> Util.toTitleCase(e.name())).toArray(String[]::new), repeatsView);
    setOptions(Stream.of(SqlHelper.EGroup.values()).map(e -> Util.toTitleCase(e.name())).toArray(String[]::new), groupView);
    setOptions(Stream.of(SqlHelper.EPriority.values()).map(e -> Util.toTitleCase(e.name())).toArray(String[]::new), priorityView);

    // sets priorityView to "Normal"
    this.priorityView.setSelection(2);

    this.titleView = this.findViewById(R.id.editTitle);
    this.descriptionView = this.findViewById(R.id.editDescrption);
    this.dateView = this.findViewById(R.id.editDate);
    this.durationHoursView = this.findViewById(R.id.editDurationHours);
    this.durationMinutesView = this.findViewById(R.id.editDurationMinutes);
    this.untilView = this.findViewById(R.id.repeatUntil);

    // textViews
    this.prerequisitesView = this.findViewById(R.id.editPrerequisites);

    // for date operations; methods below require the use of an array to counteract pointer shenanigans

    // dateView date picker
    this.dateView.setOnClickListener(v -> {
      Calendar now = Calendar.getInstance();
      new DatePickerDialog(this, (datePicker, year, month, day) -> {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        dateTimeSecs[0] = cal.getTimeInMillis() / 1000L;

        SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        this.dateView.setText(fmt.format(cal.getTime()));
      }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    });

    // untilView date picker
    this.untilView.setOnClickListener(v -> {
      Calendar now = Calendar.getInstance();
      new DatePickerDialog(this, (datePicker, year, month, day) -> {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        dateTimeSecs[1] = cal.getTimeInMillis() / 1000L;

        SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        this.untilView.setText(fmt.format(cal.getTime()));
      }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    });

    // prerequisite menu
    this.prerequisitesView.setOnClickListener(v -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Select prerequisites");
      String[] taskArray = new String[prerequisiteTasks.size()];
      for (int i = 0; i < prerequisiteTasks.size(); i++) {
        taskArray[i] = prerequisiteTasks.get(i).getTitle();
      }
      builder.setMultiChoiceItems(taskArray, selectedTasks, (dialogInterface, i, b) -> {
        // check condition
        if (b) {
          // when checkbox selected
          // Add position  in lang list
          selectedTaskList.add(i);
          // Sort array list
          Collections.sort(selectedTaskList);
        } else {
          // when checkbox unselected
          // Remove position from langList
          selectedTaskList.remove(Integer.valueOf(i));
        }
      });
      builder.setPositiveButton("OK", (dialogInterface, i) -> {
        // Initialize string builder
        StringBuilder stringBuilder = new StringBuilder();
        // use for loop
        for (int j = 0; j < selectedTaskList.size(); j++) {
          // concat array value
          stringBuilder.append(taskArray[selectedTaskList.get(j)]);
          selectedTaskIds.add(prerequisiteTasks.get(selectedTaskList.get(j)).getId());
          // check condition
          if (j != selectedTaskList.size() - 1) {
            // When j value  not equal
            // to lang list size - 1
            // add comma
            stringBuilder.append(", ");
          }
        }
        // set text on textView
        prerequisitesView.setText(stringBuilder.toString());
      });

      builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
        // dismiss dialog
        dialogInterface.dismiss();
      });
      builder.setNeutralButton("Clear All", (dialogInterface, i) -> {
        // use for loop
        for (int j = 0; j < selectedTasks.length; j++) {
          // remove all selection
          selectedTasks[j] = false;
          // clear language list
          selectedTaskList.clear();
          // clear text view value
          prerequisitesView.setText("");
        }
      });
      // show dialog
      builder.show();
    });

    // set task button operations
    setTaskBtn.setOnClickListener((v) -> {
      String title = this.titleView.getText().toString();
      String description = this.descriptionView.getText().toString();
      String hoursStr = this.durationHoursView.getText().toString();
      String minutesStr = this.durationMinutesView.getText().toString();
      long hours = hoursStr.isEmpty() ? 0 : Long.parseLong(hoursStr);
      long minutes = minutesStr.isEmpty() ? 0 : Long.parseLong(minutesStr);
      long duration = (hours * 3600L) + (minutes * 60L);

      SqlHelper.ERepeats repeats = SqlHelper.ERepeats.valueOf(
          repeatsView.getSelectedItem().toString().toUpperCase(Locale.US));
      SqlHelper.EGroup group = SqlHelper.EGroup.valueOf(
          groupView.getSelectedItem().toString().toUpperCase(Locale.US));
      SqlHelper.EPriority priority = SqlHelper.EPriority.valueOf(
          priorityView.getSelectedItem().toString().toUpperCase(Locale.US));

      Task task = new Task(
          title,
          description,
          dateTimeSecs[0],
          duration,
          repeats,
          group,
          priority,
          dateTimeSecs[1]
      );

      task.setPrerequisites(selectedTaskIds);
      buttonOnFinal(task);
    });
  }

  private void setOptions (String[] options, Spinner dropdown) {
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_dropdown_item, options);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    dropdown.setAdapter(adapter);
    dropdown.setOnItemSelectedListener(this);
  }

  @Override
  public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
  }

  @Override
  public void onNothingSelected (AdapterView<?> parent) {}

}