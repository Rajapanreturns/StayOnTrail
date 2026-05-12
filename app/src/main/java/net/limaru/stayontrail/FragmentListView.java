package net.limaru.stayontrail;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentListView extends Fragment {

  @Nullable @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater,
      @org.jetbrains.annotations.Nullable ViewGroup container,
      @org.jetbrains.annotations.Nullable Bundle savedInstanceState
  ) {
    View view = inflater.inflate(R.layout.fragment_list_view, container, false);

    LinearLayout layout = view.findViewById(R.id.list_content);
    List<Task> tasks = MainActivity.sqlHelper.getAllTasks();

    tasks.sort(Comparator.comparingLong(Task::getDate));
    SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
    long lastDate = -1;

    int i = 2 << 10;
    for (Task task : tasks) {
      if (--i < 0) {
        break;
      }
      long date = task.getDate();
      if (date != lastDate) {
        TextView header = new TextView(getContext());
        header.setText(date == 0 ? "No date" : fmt.format(new Date(date * 1000L)));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(0, 32, 0, 8);
        layout.addView(header);
        lastDate = date;
      }

      View listItem = getLayoutInflater().inflate(R.layout.component_list_item, container, false);
      TextView title = listItem.findViewById(R.id.title);
      TextView priority = listItem.findViewById(R.id.priority);
      View grouppe = listItem.findViewById(R.id.group_color);
      Button editTaskBtn = listItem.findViewById(R.id.edit_task);
      title.setText(task.getTitle());
      priority.setText(Util.toTitleCase(task.getPriority().name()));
      grouppe.setBackgroundColor(task.getGroup().color);
      editTaskBtn.setOnClickListener(v -> {
        Intent intent = new Intent(this.getActivity(), ActivityEditTask.class);
        intent.putExtra("taskId", MainActivity.sqlHelper.getId(task));
        startActivity(intent);
      });
      Button startBtn = listItem.findViewById(R.id.start_task);
      startBtn.setOnClickListener(v -> setCurrentFragment(new FragmentTimer(task.getDuration(), task.getId())));
      layout.addView(listItem);
    }
    return view;
  }

  private void setCurrentFragment(Fragment fragment) {
    this.requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.frame_layout_fragment, fragment)
        .commit();
  }
}
