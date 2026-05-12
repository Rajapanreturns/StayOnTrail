package net.limaru.stayontrail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WeekdayHeaderAdapter extends
    RecyclerView.Adapter<WeekdayHeaderAdapter.WeekdayViewHolder> {

  private final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

  @NonNull
  @Override
  public WeekdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_calendar_weekday, parent, false);
    return new WeekdayViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull WeekdayViewHolder holder, int position) {
    holder.weekdayText.setText(days[position]);
  }

  @Override
  public int getItemCount() {
    return days.length;
  }

  static class WeekdayViewHolder extends RecyclerView.ViewHolder {
    TextView weekdayText;

    public WeekdayViewHolder(@NonNull View itemView) {
      super(itemView);
      weekdayText = itemView.findViewById(R.id.weekday_text);
    }
  }
}