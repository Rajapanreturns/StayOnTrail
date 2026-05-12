package net.limaru.stayontrail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import net.limaru.stayontrail.graphutils.Graph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FragmentHome extends Fragment implements View.OnClickListener {

  //private ViewGroup container;
  @Nullable
  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    // Inflate the layout for this fragment
    //this.container = container;
    //private int a = 0;
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    // New task button
    FloatingActionButton generateTaskButton = view.findViewById(R.id.generate_task);
    generateTaskButton.setOnClickListener(v -> {
      Intent intent = new Intent(this.getActivity(), ActivityNewTask.class);
      this.startActivity(intent);
    });

    // Task list
    LinearLayout layout = view.findViewById(R.id.home_tasks);
    Graph graph = new Graph(MainActivity.sqlHelper.getAllTasks());
    // 8 hours
    List<Task> tasks = graph.suggestTasks(MainActivity.sqlHelper.getTodayTasks(), 28800);

    for (Task task : tasks) {
      View listItem = getLayoutInflater().inflate(R.layout.component_list_item, container, false);
      Button editTaskBtn = listItem.findViewById(R.id.edit_task);
      TextView title = listItem.findViewById(R.id.title);
      TextView priority = listItem.findViewById(R.id.priority);
      View grouppe = listItem.findViewById(R.id.group_color);
      title.setText(task.getTitle());
      priority.setText(Util.toTitleCase(task.getPriority().name()));
      grouppe.setBackgroundColor(task.getGroup().color);
      editTaskBtn.setOnClickListener(v -> {
        Intent intent = new Intent(this.getActivity(), ActivityEditTask.class);
        intent.putExtra("taskId", MainActivity.sqlHelper.getId(task));
        startActivity(intent);
      });
      Button startbtn = listItem.findViewById(R.id.start_task);
      startbtn.setOnClickListener(v -> {
        setCurrentFragment(new FragmentTimer(task.getDuration(), task.getId()));
      });
      layout.addView(listItem);
    }
    return view;
  }

  @Override
  public void onClick (View v) {
    Intent intent = new Intent(this.getActivity(), ActivityNewTask.class);
    this.startActivity(intent);
  }

  private void setCurrentFragment(Fragment fragment) {
    this.requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.frame_layout_fragment, fragment)
        .commit();
  }

}
