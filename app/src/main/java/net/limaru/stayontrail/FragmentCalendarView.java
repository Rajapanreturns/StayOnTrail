package net.limaru.stayontrail;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

public class FragmentCalendarView
    extends Fragment implements CalendarDayAdapter.OnDateClickListener {

  private TextView monthTitle;
  private TextView selectedDateTitle;
  private LinearLayout selectedDateTasks;
  private RecyclerView weekdayRecyclerView;
  private RecyclerView monthRecyclerView;
  private CalendarDayAdapter calendarDayAdapter;

  private final Calendar visibleMonth = Calendar.getInstance();
  private final Calendar selectedDate = Calendar.getInstance();

  private List<Task> allTasks = new ArrayList<>();
  private final Map<Long, List<Task>> tasksByDay = new LinkedHashMap<>();

  @Nullable
  @Override
  public View onCreateView(@NotNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState
  ) {
    View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);

    android.util.Log.e("TEST123", "FragmentCalendarView opened");

    monthTitle = view.findViewById(R.id.calendar_month_title);
    selectedDateTitle = view.findViewById(R.id.selected_date_title);
    selectedDateTasks = view.findViewById(R.id.selected_date_tasks);
    weekdayRecyclerView = view.findViewById(R.id.calendar_weekday_header);
    monthRecyclerView = view.findViewById(R.id.calendar_month_grid);

    ImageButton previousMonthBtn = view.findViewById(R.id.button_previous_month);
    ImageButton nextMonthBtn = view.findViewById(R.id.button_next_month);

    weekdayRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
    weekdayRecyclerView.setAdapter(new WeekdayHeaderAdapter());

    monthRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
    monthRecyclerView.setNestedScrollingEnabled(false);
    calendarDayAdapter = new CalendarDayAdapter(new ArrayList<>(), this);
    monthRecyclerView.setAdapter(calendarDayAdapter);

    loadTasks();
    normalizeSelectedDate(selectedDate);
    normalizeToMonthStart(visibleMonth);

    previousMonthBtn.setOnClickListener(v -> {
      visibleMonth.add(Calendar.MONTH, -1);
      normalizeToMonthStart(visibleMonth);
      bindMonth();
    });

    nextMonthBtn.setOnClickListener(v -> {
      visibleMonth.add(Calendar.MONTH, 1);
      normalizeToMonthStart(visibleMonth);
      bindMonth();
    });

    bindMonth();
    renderTasksForSelectedDate();

    return view;
  }

  private void loadTasks() {
    allTasks = MainActivity.sqlHelper.getAllTasks();
    tasksByDay.clear();

    for (Task task : allTasks) {
      long dayKey = toDayStartMillis(task.getDate() * 1000L);

      android.util.Log.d("CalendarDebug",
          "LOAD task=" + task.getTitle()
              + ", rawDate=" + task.getDate()
              + ", millis=" + (task.getDate() * 1000L)
              + ", dayKey=" + dayKey
              + ", group=" + task.getGroup().name()
              + ", color=" + task.getGroup().color);

      if (!tasksByDay.containsKey(dayKey)) {
        tasksByDay.put(dayKey, new ArrayList<>());
      }
      tasksByDay.get(dayKey).add(task);
    }
  }

  private void bindMonth() {
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    monthTitle.setText(monthFormat.format(new Date(visibleMonth.getTimeInMillis())));

    List<CalendarDayCell> cells = buildMonthCells();
    calendarDayAdapter.submitList(cells);
  }

  private List<CalendarDayCell> buildMonthCells() {
    List<CalendarDayCell> cells = new ArrayList<>();

    Calendar firstCell = (Calendar) visibleMonth.clone();
    int firstDayOfWeek = firstCell.get(Calendar.DAY_OF_WEEK);
    int offset = (firstDayOfWeek + 6) % 7;
    firstCell.add(Calendar.DAY_OF_MONTH, -offset);

    Calendar today = Calendar.getInstance();
    normalizeSelectedDate(today);

    for (int i = 0; i < 42; i++) {
      Calendar cellDate = (Calendar) firstCell.clone();
      cellDate.add(Calendar.DAY_OF_MONTH, i);

      long cellDayMillis = toDayStartMillis(cellDate.getTimeInMillis());
      List<Task> tasksForDay = tasksByDay.get(cellDayMillis);

      ArrayList<Integer> markerColors = new ArrayList<>();
      if (tasksForDay != null) {
        for (int j = 0; j < tasksForDay.size() && j < 3; j++) {
          markerColors.add(tasksForDay.get(j).getGroup().color);
        }
      }

      android.util.Log.d("CalendarDebug",
          "CELL date=" + new Date(cellDate.getTimeInMillis())
              + ", cellDayMillis=" + cellDayMillis
              + ", tasksForDay=" + (tasksForDay == null ? 0 : tasksForDay.size())
              + ", markerColors=" + markerColors);

      boolean isCurrentMonth =
          cellDate.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH) &&
              cellDate.get(Calendar.YEAR) == visibleMonth.get(Calendar.YEAR);

      boolean isSelected = sameDay(cellDate, selectedDate);
      boolean isToday = sameDay(cellDate, today);

      cells.add(new CalendarDayCell(
          cellDate.get(Calendar.YEAR),
          cellDate.get(Calendar.MONTH),
          cellDate.get(Calendar.DAY_OF_MONTH),
          isCurrentMonth,
          isSelected,
          isToday,
          markerColors
      ));
    }

    return cells;
  }

  @Override
  public void onDateClicked(@NonNull CalendarDayCell cell) {
    selectedDate.set(Calendar.YEAR, cell.getYear());
    selectedDate.set(Calendar.MONTH, cell.getMonth());
    selectedDate.set(Calendar.DAY_OF_MONTH, cell.getDayOfMonth());
    normalizeSelectedDate(selectedDate);

    if (selectedDate.get(Calendar.MONTH) != visibleMonth.get(Calendar.MONTH)
        || selectedDate.get(Calendar.YEAR) != visibleMonth.get(Calendar.YEAR)) {
      visibleMonth.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
      visibleMonth.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
      normalizeToMonthStart(visibleMonth);
    }

    bindMonth();
    renderTasksForSelectedDate();
  }

  private void renderTasksForSelectedDate() {
    if (getContext() == null) {
      return;
    }

    selectedDateTasks.removeAllViews();

    Calendar today = Calendar.getInstance();
    normalizeSelectedDate(today);

    if (sameDay(selectedDate, today)) {
      selectedDateTitle.setText("Today");
    } else {
      SimpleDateFormat fmt = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
      selectedDateTitle.setText(fmt.format(new Date(selectedDate.getTimeInMillis())));
    }

    long selectedDayMillis = toDayStartMillis(selectedDate.getTimeInMillis());
    List<Task> filteredTasks = tasksByDay.get(selectedDayMillis);

    if (filteredTasks == null || filteredTasks.isEmpty()) {
      TextView emptyView = new TextView(getContext());
      emptyView.setText("No tasks for this date");
      emptyView.setTextSize(16f);
      emptyView.setPadding(8, 24, 8, 8);
      selectedDateTasks.addView(emptyView);
      return;
    }

    LayoutInflater inflater = getLayoutInflater();

    for (Task task : filteredTasks) {
      View listItem = inflater.inflate(R.layout.component_list_item, selectedDateTasks, false);

      TextView title = listItem.findViewById(R.id.title);
      TextView priority = listItem.findViewById(R.id.priority);
      View groupColor = listItem.findViewById(R.id.group_color);
      Button editTaskBtn = listItem.findViewById(R.id.edit_task);
      Button startBtn = listItem.findViewById(R.id.start_task);

      title.setText(task.getTitle());
      priority.setText(Util.toTitleCase(task.getPriority().name()));
      groupColor.setBackgroundColor(task.getGroup().color);

      editTaskBtn.setOnClickListener(v -> {
        Intent intent = new Intent(this.getActivity(), ActivityEditTask.class);
        intent.putExtra("taskId", task.getId());
        startActivity(intent);
      });

      startBtn.setOnClickListener(v ->
          setCurrentFragment(new FragmentTimer(task.getDuration(), task.getId()))
      );

      selectedDateTasks.addView(listItem);
    }
  }

  private void setCurrentFragment(Fragment fragment) {
    requireParentFragment().getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.task_view_include, fragment)
        .addToBackStack(null)
        .commit();
  }

  private boolean sameDay(Calendar a, Calendar b) {
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
        && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
  }

  private void normalizeSelectedDate(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private void normalizeToMonthStart(Calendar calendar) {
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    normalizeSelectedDate(calendar);
  }

  private long toDayStartMillis(long millis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(millis);
    normalizeSelectedDate(cal);
    return cal.getTimeInMillis();
  }
}