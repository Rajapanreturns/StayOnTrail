package net.limaru.stayontrail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class DeepSeekHelper {

  private static final String TAG = "DeepSeekHelper";
  private static final String API_KEY = "sk-XXXXXXXXXXXXXXXX";

  /**
   * Establishes an HTTP POST connection and returns an InputStream
   *
   * @param url      a URL object
   * @param jsonBody the JSON body to send
   * @return an InputStream object
   */
  public static InputStream getInputStream(URL url, String jsonBody) {
    HttpURLConnection urlConnection = null;
    InputStream inputStream = null;

    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setRequestProperty("Authorization", "Bearer " + API_KEY);
      urlConnection.setRequestProperty("Content-Type", "application/json");
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setUseCaches(false);
      Log.i(TAG, "Connecting...");

      // write JSON body
      OutputStream os = urlConnection.getOutputStream();
      os.write(jsonBody.getBytes());
      os.flush();
      os.close();

      urlConnection.connect();
      Log.i(TAG, "Connected");
      inputStream = urlConnection.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      inputStream = null;
    }

    return inputStream;
  }

  /**
   * Read the String returned from an InputStream
   *
   * @param inputStream an InputStream object
   * @return a String
   */
  public static String convertStreamToString(InputStream inputStream) {
    StringBuffer buffer = new StringBuffer();

    if (inputStream == null) {
      return null;
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;

    try {
      while ((line = reader.readLine()) != null) {
        buffer.append(line + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (buffer.length() == 0) {
      return null;
    }

    String outString = buffer.toString();
    Log.i(TAG, outString);
    return outString;
  }

  /**
   * Checks if network is available
   *
   * @param context a Context object
   * @return boolean
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager
        = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    boolean haveNetwork = activeNetworkInfo != null && activeNetworkInfo.isConnected();
    Log.i(TAG, "Active Network: " + haveNetwork);
    return haveNetwork;
  }
}
