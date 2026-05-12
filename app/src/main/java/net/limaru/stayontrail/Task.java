package net.limaru.stayontrail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
  public Task (
      String title,
      String description,
      long date,
      long duration,
      SqlHelper.ERepeats repeats,
      SqlHelper.EGroup group,
      SqlHelper.EPriority priority,
      long until
  ) {
    this.title = title;
    this.description = description;
    this.date = date;
    this.duration = duration;
    this.repeats = repeats;
    this.until = until;
    this.group = group;
    this.priority = priority;
  }

  private long id = -1;
  private String title;
  private String description;
  private long date;
  private long duration;
  private SqlHelper.ERepeats repeats;
  private long until;
  private SqlHelper.EGroup group;
  private SqlHelper.EPriority priority;
  private long[] prerequisites = new long[0];

  private Task(Builder builder) {
    this.id = builder.id;
    this.title = builder.title;
    this.description = builder.description;
    this.date = builder.date;
    this.duration = builder.duration;
    this.repeats = builder.repeats;
    this.group = builder.group;
    this.priority = builder.priority;
    this.until = builder.until;

    if (builder.prerequisites != null) {
      this.prerequisites = new long[builder.prerequisites.size()];
      for (int i = 0; i < builder.prerequisites.size(); i++) {
        this.prerequisites[i] = builder.prerequisites.get(i);
      }
    }
  }

  public static class Builder {
    private long id;
    private String title;
    private String description;
    private long date;
    private long duration;
    private SqlHelper.ERepeats repeats;
    private long until;
    private SqlHelper.EGroup group;
    private SqlHelper.EPriority priority;
    private ArrayList<Long> prerequisites;

    public Builder id (long id) {
      this.id = id;
      return this;
    }

    public Builder title (String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder date(long date) {
      this.date = date;
      return this;
    }

    public Builder duration(long duration) {
      this.duration = duration;
      return this;
    }

    public Builder repeats(SqlHelper.ERepeats repeats) {
      this.repeats = repeats;
      return this;
    }

    public Builder until(long until) {
      this.until = until;
      return this;
    }

    public Builder group(SqlHelper.EGroup group) {
      this.group = group;
      return this;
    }

    public Builder priority(SqlHelper.EPriority priority) {
      this.priority = priority;
      return this;
    }

    public Builder prerequisites(ArrayList<Long> prerequisites) {
      this.prerequisites = prerequisites;
      return this;
    }

    public Builder addPrerequisite(long prerequisite) {
      if (this.prerequisites == null) {
        this.prerequisites = new ArrayList<>();
      }
      this.prerequisites.add(prerequisite);
      return this;
    }

    public Task build() {
      return new Task(this);
    }
  }

  public String getTitle () {
    return title;
  }

  public long getId () {
    return id;
  }

  public void setId (long id) {
    this.id = id;
  }

  public void setTitle (String title) {
    this.title = title;
  }

  public String getDescription () {
    return description;
  }

  public void setDescription (String description) {
    this.description = description;
  }

  public long getDate () {
    return date;
  }

  public void setDate (long date) {
    this.date = date;
  }

  public long getDuration () {
    return duration;
  }

  public void setDuration (long duration) {
    this.duration = duration;
  }

  public SqlHelper.ERepeats getRepeats () {
    return repeats;
  }

  public void setRepeats (SqlHelper.ERepeats repeats) {
    this.repeats = repeats;
  }

  public long getUntil () {
    return until;
  }

  public void setUntil (long until) {
    this.until = until;
  }

  public SqlHelper.EGroup getGroup () {
    return group;
  }

  public void setGroup (SqlHelper.EGroup group) {
    this.group = group;
  }

  public SqlHelper.EPriority getPriority () {
    return priority;
  }

  public void setPriority (SqlHelper.EPriority priority) {
    this.priority = priority;
  }

  public long[] getPrerequisites () {return prerequisites;}
  public List<Long> getPrerequisitesAsList () {
    return Arrays.stream(prerequisites).boxed().collect(Collectors.toList());
  }

  public void setPrerequisites (List<Long> prerequisites) {
    if (prerequisites != null) {
      this.prerequisites = new long[prerequisites.size()];
      for (int i = 0; i < prerequisites.size(); i++) {
        this.prerequisites[i] = prerequisites.get(i);
      }
    }
  }

  public void setPrerequisites (long[] prerequisites) {
    this.prerequisites = prerequisites;
  }
}
