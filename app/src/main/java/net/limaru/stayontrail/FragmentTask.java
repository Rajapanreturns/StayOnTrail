package net.limaru.stayontrail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentTask extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    View view = inflater.inflate(R.layout.fragment_task, container, false);

    SwitchCompat taskViewSwitch = view.findViewById(R.id.taskViewSwitch);
    FloatingActionButton generateTaskButton = view.findViewById(R.id.generate_task);

    generateTaskButton.setOnClickListener(v -> {
      Intent intent = new Intent(this.getActivity(), ActivityNewTask.class);
      startActivity(intent);
    });

    FragmentListView fragmentListView = new FragmentListView();
    FragmentCalendarView fragmentCalendarView = new FragmentCalendarView();

    taskViewSwitch.setChecked(false);
    setTaskFragment(fragmentCalendarView);

    taskViewSwitch.setOnCheckedChangeListener((buttonView, showListView) -> {
      if (showListView) {
        setTaskFragment(fragmentListView);
      } else {
        setTaskFragment(fragmentCalendarView);
      }
    });

    return view;
  }

  private void setTaskFragment(Fragment fragment) {
    this.getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.task_view_include, fragment)
        .commit();
  }
}