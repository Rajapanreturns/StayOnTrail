package net.limaru.stayontrail;

import java.util.List;

public class CalendarDayCell {
  private final int year;
  private final int month;
  private final int dayOfMonth;
  private final boolean currentMonth;
  private final boolean selected;
  private final boolean today;
  private final List<Integer> markerColors;

  public CalendarDayCell(int year, int month, int dayOfMonth, boolean currentMonth,
                         boolean selected, boolean today, List<Integer> markerColors) {
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
    this.currentMonth = currentMonth;
    this.selected = selected;
    this.today = today;
    this.markerColors = markerColors;
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public boolean isCurrentMonth() {
    return currentMonth;
  }

  public boolean isSelected() {
    return selected;
  }

  public boolean isToday() {
    return today;
  }

  public List<Integer> getMarkerColors() {
    return markerColors;
  }
}