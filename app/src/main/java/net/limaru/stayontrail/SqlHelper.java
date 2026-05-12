package net.limaru.stayontrail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "No1DesignAI.db";
  private static final int DB_VERSION = 2;

  public SqlHelper (Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  /**
   * Called when database is created. This method SHOULD NOT be called directly in code, Android does it automatically for you.
   * @param db The database.
   */
  @Override
  public void onCreate (SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS tasks (" +
            "id INTEGER PRIMARY KEY NOT NULL," +
            "title TEXT NOT NULL," +
            "description TEXT," +
            "date BIGINT," +
            "duration BIGINT," +
            "repeats TEXT," +
            "until BIGINT," +
            "grouppe TEXT," +
            "priority TEXT)"
    );

    db.execSQL(
        "CREATE TABLE IF NOT EXISTS prerequisites(" +
            "id INTEGER NOT NULL," +
            "prerequisite INTEGER, " +
            "PRIMARY KEY (id, prerequisite), "+
            "FOREIGN KEY(id, prerequisite) REFERENCES tasks(id, id)" +
            "ON UPDATE CASCADE)"
    );

    db.execSQL("CREATE TABLE IF NOT EXISTS RepeatType (type TEXT UNIQUE)");
    db.execSQL("CREATE TABLE IF NOT EXISTS GroupType (type TEXT UNIQUE)");
    db.execSQL("CREATE TABLE IF NOT EXISTS PriorityType (type TEXT UNIQUE)");
    db.execSQL("CREATE TABLE IF NOT EXISTS streak (" +
        "id INTEGER PRIMARY KEY CHECK (id = 1)," +
        "days INTEGER NOT NULL DEFAULT 0," +
        "tasks INTEGER NOT NULL DEFAULT 0," +
        "lastTaskDone BIGINT NOT NULL DEFAULT 0)"
    );

    // Insert values
    for (ERepeats item : ERepeats.values()) {
      ContentValues content = new ContentValues();
      content.put("type", item.name());
      db.insert("RepeatType", null, content);
    }
    for (EGroup item : EGroup.values()) {
      ContentValues content = new ContentValues();
      content.put("type", item.name());
      db.insert("GroupType", null, content);
    }
    for (EPriority item : EPriority.values()) {
      ContentValues content = new ContentValues();
      content.put("type", item.name());
      db.insert("PriorityType", null, content);
    }
    { // First time I found a use for code blocks, woohoo!
      ContentValues content = new ContentValues();
      content.put("id", 1);
      content.put("days", 0);
      content.put("tasks", 0);
      db.insertWithOnConflict("streak", null, content, SQLiteDatabase.CONFLICT_IGNORE);
    }
  }

  /**
   * Called when database is updated. This method SHOULD NOT be called directly in code, Android does it automatically for you.
   * @param db The database.
   * @param oldVersion The old database version.
   * @param newVersion The new database version.
   */
  @Override
  public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    // Simple strategy for now
    db.execSQL("DROP TABLE IF EXISTS tasks");
    db.execSQL("DROP TABLE IF EXISTS streak");
    onCreate(db);
  }

  /**
   * Values for repeating tasks
   */
  public enum ERepeats {
    NEVER, DAILY, WEEKLY, FORTNIGHTLY, MONTHLY
  }

  /**
   * Values for groups
   */
  public enum EGroup {
    RED(0xFFff0000), ORANGE(0xFFFFA500), YELLOW(0xFFFFFF00), GREEN(0xFF00FF00), BLUE(0xFF0000FF), PURPLE(0xFFA020F0), PINK(0xFFFFC0CB);
    public final int color;
    EGroup (int color) {this.color = color;}
  }

  /**
   * Values for priority
   */
  public enum EPriority {
    HIGHEST(16), HIGH(8), NORMAL(4), LOW(2), LOWEST(1);
    public final int weight;
    EPriority (int weight) {this.weight = weight;}
  }

  /**
   * Gets the ID of a task.
   * IDs are calculated using `title.hashCode() ^ date ^ duration`,
   *   which (hopefully) is good enough for hashing.
   * @param task The task
   * @return The ID
   */
  public long getId(Task task) {
    String title = task.getTitle();
    long date = task.getDate();
    long duration = task.getDuration();
    return title.hashCode() ^ date ^ duration;
  }

  /**
   * Inserts a task into the database
   * @param task The task
   */
  public void insertTask (Task task) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put("id", getId(task));
    values.put("title", task.getTitle());
    values.put("description", task.getDescription());
    values.put("date", task.getDate());
    values.put("duration", task.getDuration());
    values.put("repeats", task.getRepeats().name());
    values.put("until", task.getUntil());
    values.put("grouppe", task.getGroup().name());
    values.put("priority", task.getPriority().name());

    db.insert("tasks", null, values);

    for (int i = 0; i < task.getPrerequisites().length; i++) {
      ContentValues prValues = new ContentValues();
      prValues.put("id", getId(task));
      prValues.put("prerequisite", task.getPrerequisites()[i]);
      db.insert("prerequisites", null, prValues);
    }
  }

  /**
   * Updates a task in the database with new values.
   * Note that since the ID does not change, getId(task) for this task WILL NOT return its true ID.
   * @param id The id of the task
   * @param task The task
   */
  public void updateTask (long id, Task task) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    long newId = getId(task);

    values.put("id", newId);
    values.put("title", task.getTitle());
    values.put("description", task.getDescription());
    values.put("date", task.getDate());
    values.put("duration", task.getDuration());
    values.put("repeats", task.getRepeats().name());
    values.put("until", task.getUntil());
    values.put("grouppe", task.getGroup().name());
    values.put("priority", task.getPriority().name());

    db.update("tasks", values, "id = ?", new String[]{String.valueOf(id)});

    db.delete("prerequisites", "id = ?", new String[]{String.valueOf(id)});
    for (int i = 0; i < task.getPrerequisites().length; i++) {
      ContentValues prValues = new ContentValues();
      prValues.put("id", newId);
      prValues.put("prerequisite", task.getPrerequisites()[i]);
      db.insert("prerequisites", null, prValues);
    }

    ContentValues prValues = new ContentValues();
    prValues.put("prerequisite", newId);
    db.update("prerequisites", prValues, "prerequisite = ?",
        new String[]{String.valueOf(id)});
  }

  /**
   * Deletes a task
   * @param id The ID of the task to be deleted
   */
  public void deleteTask (long id) {
    SQLiteDatabase db = getWritableDatabase();
    db.delete("tasks", "id = ?", new String[]{String.valueOf(id)});
    db.delete("prerequisites", "id = ? OR prerequisite = ?",
        new String[]{String.valueOf(id), String.valueOf(id)});
  }

  /**
   * Gets a task using an ID
   * @param id The ID of the task
   * @return The task, if present. Otherwise returns null.
   */
  @Nullable
  public Task getTask (long id) {
    SQLiteDatabase db = getReadableDatabase();
    Task.Builder taskBuilder = new Task.Builder();

    try (Cursor cursor = db.rawQuery(
        "SELECT title, description, date, duration, repeats, until, grouppe, priority " +
            "FROM tasks WHERE id = ?", new String[]{String.valueOf(id)})
    ) {
      if (cursor.moveToFirst()) {
        String title = cursor.getString(0);
        String description = cursor.getString(1);
        long date = cursor.getLong(2);
        long duration = cursor.getLong(3);
        ERepeats repeats = ERepeats.valueOf(cursor.getString(4));
        long until = cursor.getLong(5);
        EGroup grouppe = EGroup.valueOf(cursor.getString(6));
        EPriority priority = EPriority.valueOf(cursor.getString(7));

        taskBuilder = taskBuilder
            .title(title)
            .description(description)
            .date(date)
            .duration(duration)
            .repeats(repeats)
            .group(grouppe)
            .priority(priority)
            .until(until)
            .id(id);
      }
    }

    try (Cursor cursor = db.rawQuery("SELECT prerequisite FROM prerequisites WHERE id = ?",
        new String[]{String.valueOf(id)})
    ) {
      if (cursor.moveToFirst()) {
        do {
          long prerequisite = cursor.getLong(0);
          taskBuilder.addPrerequisite(prerequisite);
        } while (cursor.moveToNext());
      }
    }
    return taskBuilder.build();
  }

  /**
   * Gets every task placed in the database.
   * @return tasks
   */
  public List<Task> getAllTasks() {
    List<Task> taskList = new ArrayList<>();
    SQLiteDatabase db = getReadableDatabase();

    try (Cursor cursor = db.rawQuery("SELECT " +
        "t.id, " +
        "title, " +
        "description, " +
        "date, " +
        "duration, " +
        "repeats, " +
        "until, " +
        "grouppe, " +
        "priority, " +
        "GROUP_CONCAT(p.prerequisite) AS dependencies " +
        "FROM tasks t " +
        "LEFT JOIN prerequisites p ON t.id = p.id " +
        "GROUP BY t.id, t.title, t.description, t.date, t.duration, t.repeats, " +
            "t.until, t.grouppe, t.priority;", null)) {
      if (cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String title = cursor.getString(1);
          String description = cursor.getString(2);
          long date = cursor.getLong(3);
          long duration = cursor.getLong(4);
          ERepeats repeats = ERepeats.valueOf(cursor.getString(5));
          long until = cursor.getLong(6);
          EGroup grouppe = EGroup.valueOf(cursor.getString(7));
          EPriority priority = EPriority.valueOf(cursor.getString(8));

          Task task = new Task(
              title, description, date, duration, repeats, grouppe, priority, until
          );
          task.setId(id);
          if (cursor.getString(9) != null) {
            long[] prerequisites = Arrays.stream(cursor.getString(9).split(","))
                .mapToLong(Long::valueOf).toArray();
            task.setPrerequisites(prerequisites);
          }
          taskList.add(task);

        } while (cursor.moveToNext());
      }
    }
    return taskList;
  }

  /**
   * Gets the tasks with today's date
   * @return tasks
   */
  public List<Task> getTodayTasks () {
    List<Task> taskList = new ArrayList<>();
    SQLiteDatabase db = getReadableDatabase();

    // Get start of today and tomorrow in seconds
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    long startOfToday = calendar.getTimeInMillis() / 1000;

    calendar.add(Calendar.DAY_OF_MONTH, 1);
    long startOfTomorrow = calendar.getTimeInMillis() / 1000;

    String query =
        "SELECT id, title, description, date, duration, repeats, until, grouppe, priority " +
        "FROM tasks WHERE date >= ? AND date < ?";

    String[] args = {
        String.valueOf(startOfToday),
        String.valueOf(startOfTomorrow)
    };

    try (Cursor cursor = db.rawQuery(query, args)) {
      if (cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String title = cursor.getString(1);
          String description = cursor.getString(2);
          long date = cursor.getLong(3);
          long duration = cursor.getLong(4);
          ERepeats repeats = ERepeats.valueOf(cursor.getString(5));
          long until = cursor.getLong(6);
          EGroup grouppe = EGroup.valueOf(cursor.getString(7));
          EPriority priority = EPriority.valueOf(cursor.getString(8));

          Task task = new Task(
              title, description, date, duration, repeats, grouppe, priority, until
          );
          task.setId(id);
          taskList.add(task);

        } while (cursor.moveToNext());
      }
    }

    return taskList;
  }

  /**
   * Sets the number of days in the streak
   */
  public void setStreakDays (int days) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("days", days);
    db.update("streak", values, "id = 1", null);
  }

  /**
   * Sets the number of tasks in the streak
   */
  public void setStreakTasks (int tasks) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("tasks", tasks);
    db.update("streak", values, "id = 1", null);
  }

  /**
   * Sets the number of tasks in the streak
   */
  public void setLastTaskDone (long lastTaskDone) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("lastTaskDone", lastTaskDone);
    db.update("streak", values, "id = 1", null);
  }

  /**
   * Adds 1 to the number of days in the streak
   */
  public void incrementStreakDays () {
    SQLiteDatabase db = getWritableDatabase();
    db.execSQL("UPDATE streak SET days = days+1 WHERE id = 1");
  }

  /**
   * Adds 1 to the number of tasks in the streak
   */
  public void incrementStreakTasks () {
    SQLiteDatabase db = getWritableDatabase();
    db.execSQL("UPDATE streak SET tasks = tasks+1 WHERE id = 1");
  }

  /**
   * Gets the number of days in the streak
   * @return days
   */
  public int getStreakDays () {
    SQLiteDatabase db = getWritableDatabase();
    try (Cursor cursor = db.rawQuery("SELECT days FROM streak WHERE id = 1", null)) {
      if (cursor.moveToFirst()) return cursor.getInt(0);
      else return 0;
    }
  }

  /**
   * Gets the number of tasks in the streak
   * @return tasks
   */
  public int getStreakTasks () {
    SQLiteDatabase db = getWritableDatabase();
    try (Cursor cursor = db.rawQuery("SELECT tasks FROM streak WHERE id = 1", null)) {
      if (cursor.moveToFirst()) return cursor.getInt(0);
      else return 0;
    }
  }

  /**
   * Gets the time of the last task
   * @return days
   */
  public long getLastTaskDone () {
    SQLiteDatabase db = getWritableDatabase();
    try (Cursor cursor = db.rawQuery("SELECT lastTaskDone FROM streak WHERE id = 1", null)) {
      if (cursor.moveToFirst()) return cursor.getInt(0);
      else return 0;
    }
  }

  /**
   * Gets the task that depends on the input task ID
   * @param id The ID of the task
   * @return The task, if present. Otherwise returns null.
   */
  @NonNull
  public ArrayList<Long> getDependents (long id) {
    SQLiteDatabase db = getReadableDatabase();
    ArrayList<Long> dependents = new ArrayList<>();

    try (Cursor cursor = db.rawQuery("SELECT id FROM prerequisites WHERE prerequisite = ?",
        new String[]{String.valueOf(id)})) {
      if (cursor.moveToFirst()) {
        do {
          long dependent = cursor.getLong(0);
          dependents.add(dependent);
        } while (cursor.moveToNext());
      }
    }
    return dependents;
  }


  /**
   * Gets the tasks with today's date
   * @return tasks
   */
  public List<Task> getOldestTasks () {
    List<Task> taskList = new ArrayList<>();
    SQLiteDatabase db = getReadableDatabase();

    String query =
        "SELECT id, title, description, date, duration, repeats, until, grouppe, priority " +
        "FROM tasks WHERE date = (SELECT MIN(date) FROM tasks)";

    try (Cursor cursor = db.rawQuery(query, null)) {
      if (cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String title = cursor.getString(1);
          String description = cursor.getString(2);
          long date = cursor.getLong(3);
          long duration = cursor.getLong(4);
          ERepeats repeats = ERepeats.valueOf(cursor.getString(5));
          long until = cursor.getLong(6);
          EGroup grouppe = EGroup.valueOf(cursor.getString(7));
          EPriority priority = EPriority.valueOf(cursor.getString(8));

          Task task = new Task(
              title, description, date, duration, repeats, grouppe, priority, until
          );
          task.setId(id);
          taskList.add(task);

        } while (cursor.moveToNext());
      }
    }

    return taskList;
  }

  public long getOldestTaskDate () {
    SQLiteDatabase db = getWritableDatabase();
    try(Cursor cursor = db.rawQuery("SELECT MIN(date) FROM tasks", null)) {
      if (cursor.moveToFirst()) return cursor.getLong(0);
      else return 0;
    }
  }
}
