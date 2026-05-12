package net.limaru.stayontrail;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarDayAdapter extends
    RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder> {

  public interface OnDateClickListener {
    void onDateClicked(@NonNull CalendarDayCell cell);
  }

  private List<CalendarDayCell> items;
  private final OnDateClickListener listener;

  public CalendarDayAdapter(List<CalendarDayCell> items, OnDateClickListener listener) {
    this.items = items;
    this.listener = listener;
  }

  public void submitList(List<CalendarDayCell> newItems) {
    this.items = newItems;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public CalendarDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_calendar_day, parent, false);
    return new CalendarDayViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CalendarDayViewHolder holder, int position) {
    CalendarDayCell cell = items.get(position);
    holder.dayNumber.setText(String.valueOf(cell.getDayOfMonth()));

    if (cell.isCurrentMonth()) {
      holder.dayNumber.setAlpha(1f);
      holder.markersContainer.setAlpha(1f);
    } else {
      holder.dayNumber.setAlpha(0.35f);
      holder.markersContainer.setAlpha(0.35f);
    }

    holder.dayNumber.setBackground(null);
    holder.dayNumber.setTextColor(Color.BLACK);

    if (cell.isToday()) {
      holder.dayNumber.setBackgroundResource(R.drawable.rounded_calendar_today_bg);
    }

    if (cell.isSelected()) {
      holder.dayNumber.setBackgroundResource(R.drawable.rounded_calendar_selected_bg);
      holder.dayNumber.setTextColor(Color.WHITE);
    }

    holder.markersContainer.removeAllViews();
    List<Integer> colors = cell.getMarkerColors();

    if (colors != null && !colors.isEmpty()) {
      for (int i = 0; i < colors.size() && i < 3; i++) {
        View marker = new View(holder.itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(holder.itemView, 3)
        );
        if (i > 0) {
          params.topMargin = dpToPx(holder.itemView, 2);
        }
        marker.setLayoutParams(params);
        marker.setBackgroundColor(colors.get(i));
        holder.markersContainer.addView(marker);
      }
    }

    holder.itemView.setOnClickListener(v -> listener.onDateClicked(cell));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class CalendarDayViewHolder extends RecyclerView.ViewHolder {
    TextView dayNumber;
    LinearLayout markersContainer;

    public CalendarDayViewHolder(@NonNull View itemView) {
      super(itemView);
      dayNumber = itemView.findViewById(R.id.calendar_day_number);
      markersContainer = itemView.findViewById(R.id.calendar_day_markers);
    }
  }

  private int dpToPx(View view, int dp) {
    float density = view.getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }
}