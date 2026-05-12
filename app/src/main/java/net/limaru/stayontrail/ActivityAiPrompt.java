package net.limaru.stayontrail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityAiPrompt extends AppCompatActivity {

  private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

  private static final String SYSTEM_PROMPT =
      "You are a task parser. The user will describe a task in natural language. " +
          "Extract the following fields and return ONLY a valid JSON object, no explanation, no markdown:\n" +
          "{\n" +
          "  \"title\": \"short task title\",\n" +
          "  \"description\": \"full description\",\n" +
          "  \"day\": day as integer or 0 if not mentioned,\n" +
          "  \"month\": month as integer (1-12) or 0 if not mentioned,\n" +
          "  \"year\": year as integer or 2026 if not mentioned,\n" +
          "  \"duration_hours\": 0,\n" +
          "  \"duration_minutes\": 0,\n" +
          "  \"priority\": \"HIGHEST | HIGH | NORMAL | LOW | LOWEST\",\n" +
          "  \"repeats\": \"NEVER | DAILY | WEEKLY | FORTNIGHTLY | MONTHLY\",\n" +
          "  \"group\": \"RED | ORANGE | YELLOW | GREEN | BLUE | PURPLE | PINK\"\n" +
          "}\n" +
          "Rules:\n" +
          "- Today's date is " + new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date()) + "\n" +
          "- If the user says today, tomorrow, or any relative date, convert it to the actual day/month/year integers using today's date above\n" +
          "- Only set day/month/year to 0 if absolutely no date or time reference is mentioned\n" +
          "- If priority not mentioned, infer from urgency\n" +
          "- If repeats not mentioned, set to NEVER\n" +
          "- If duration not mentioned, set both to 0\n" +
          "- Set group color based on priority: HIGHEST=RED, HIGH=ORANGE, NORMAL=GREEN, LOW=BLUE, LOWEST=PURPLE\n" +
          "- Return ONLY the JSON, nothing else";

  private JSONObject result = null;
  private EditText promptInput;
  private Button sendBtn;
  private Button doneBtn;
  private TextView resultText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ai_prompt);

    promptInput = findViewById(R.id.ai_prompt_input);
    sendBtn = findViewById(R.id.ai_send_btn);
    doneBtn = findViewById(R.id.ai_done_btn);
    resultText = findViewById(R.id.ai_result_text);

    sendBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
          return;
        }

        doneBtn.setVisibility(View.GONE);
        resultText.setVisibility(View.GONE);
        sendBtn.setEnabled(false);

        if (DeepSeekHelper.isNetworkAvailable(ActivityAiPrompt.this)) {
          sendPrompt(prompt);
        } else {
          Toast.makeText(ActivityAiPrompt.this, "Error: No internet connection, try again", Toast.LENGTH_SHORT).show();
        }
      }
    });

    doneBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (result == null) {
          return;
        }
        try {
          Intent intent = new Intent(ActivityAiPrompt.this, ActivityNewTask.class);
          intent.putExtra("title",result.getString("title"));
          intent.putExtra("description",result.optString("description", ""));
          intent.putExtra("day",result.optInt("day", 0));
          intent.putExtra("month",result.optInt("month", 0));
          intent.putExtra("year",result.optInt("year", 0));
          intent.putExtra("duration_hours",result.optInt("duration_hours", 0));
          intent.putExtra("duration_minutes",result.optInt("duration_minutes", 0));
          intent.putExtra("priority",result.getString("priority"));
          intent.putExtra("repeats",result.getString("repeats"));
          intent.putExtra("group",result.getString("group"));
          startActivity(intent);
          finish();
        } catch (JSONException e) {
          Toast.makeText(ActivityAiPrompt.this, "Error: " + e.getMessage(),
              Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void sendPrompt(final String prompt) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject systemMsg = new JSONObject();
          systemMsg.put("role", "system");
          systemMsg.put("content", SYSTEM_PROMPT);

          JSONObject userMsg = new JSONObject();
          userMsg.put("role", "user");
          userMsg.put("content", prompt);

          JSONArray messages = new JSONArray();
          messages.put(systemMsg);
          messages.put(userMsg);

          JSONObject body = new JSONObject();
          body.put("model", "deepseek-chat");
          body.put("messages", messages);

          // making tge network call
          URL url = new URL(DEEPSEEK_URL);
          InputStream stream = DeepSeekHelper.getInputStream(url, body.toString());
          String json = DeepSeekHelper.convertStreamToString(stream);

          if (json == null) {
            handler.post(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(ActivityAiPrompt.this, "No response from server", Toast.LENGTH_SHORT).show();
                sendBtn.setEnabled(true);
              }
            });
            return;
          }

          JSONObject jsonResponse = new JSONObject(json);
          String content = jsonResponse
              .getJSONArray("choices")
              .getJSONObject(0)
              .getJSONObject("message")
              .getString("content");

          String cleaned = content
              .replace("```json", "")
              .replace("```", "")
              .trim();

          result = new JSONObject(cleaned);

          handler.post(new Runnable() {
            @Override
            public void run() {
              try {
                String summary =
                    "Title: " + result.getString("title") + "\n" +
                        "Date: " + result.getInt("day") + "/" +
                        result.getInt("month") + "/" + result.getInt("year") + "\n" +
                        "Duration: " + result.getInt("duration_hours") + "h " +
                        result.getInt("duration_minutes") + "m\n" +
                        "Priority: " + result.getString("priority") + "\n" +
                        "Repeats: " + result.getString("repeats");

                resultText.setText(summary);
                resultText.setVisibility(View.VISIBLE);
                doneBtn.setVisibility(View.VISIBLE);
                sendBtn.setEnabled(true);
              } catch (JSONException e) {
                Toast.makeText(ActivityAiPrompt.this,
                    "Failed to parse response", Toast.LENGTH_SHORT).show();
                sendBtn.setEnabled(true);
              }
            }
          });

        } catch (JSONException e) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(ActivityAiPrompt.this,
                  "Failed to generate output, try again", Toast.LENGTH_SHORT).show();
              sendBtn.setEnabled(true);
            }
          });
        } catch (MalformedURLException e) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(ActivityAiPrompt.this,
                  "Failed to generate output, try again", Toast.LENGTH_SHORT).show();
              sendBtn.setEnabled(true);
            }
          });
        }
      }
    });
  }
}