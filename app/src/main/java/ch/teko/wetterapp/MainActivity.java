package ch.teko.wetterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    NotificationManagerCompat notificationManagerCompat;

    NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    private void createNotificationText(String notificationContent) {
        builder = new NotificationCompat.Builder(this, "YOLO")
                .setSmallIcon(R.drawable.alert)
                .setContentTitle("Wetterkanal")
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Wetterkanal";
            String description = "Eine Aufgabe zum Ende des Studiums";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("YOLO", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void getTemperature(View view) throws InterruptedException {
        TextView status = findViewById(R.id.status);
        status.setText("SERVICE IS RUNNING!");
        final String url = "https://tecdottir.herokuapp.com/measurements/tiefenbrunnen?startDate=2021-09-20&endDate=2021-09-20";
        makeHTTPRequest(url, Request.Method.GET);
    }

    public void stopService(View view) {
        TextView status = findViewById(R.id.status);
        status.setText("SERVICE STOPPED!");
    }

    public void compare(){
        try {
            String resultJSON = getIntent().getStringExtra("resultJSON");
            if (!resultJSON.isEmpty() && resultJSON != null) {
                final JSONObject resultJsonObject = new JSONObject(getIntent().getStringExtra("resultJSON"));
                final JSONArray resultJsonArray = resultJsonObject.getJSONArray("result");
                JSONObject latestResult = resultJsonArray.getJSONObject(resultJsonArray.length() - 1);
                JSONObject secondLatestResult = resultJsonArray.getJSONObject(resultJsonArray.length() - 2);
                Object latestValue = latestResult.getJSONObject("values").getJSONObject("air_temperature").get("value");
                Object secondLatestValue = secondLatestResult.getJSONObject("values").getJSONObject("air_temperature").get("value");
                double latestValueFloat = latestValue instanceof Integer ? ((Integer) latestValue).doubleValue() : (double) latestValue;
                double secondLatestValueFloat = secondLatestValue instanceof Integer ? ((Integer) secondLatestValue).doubleValue() : (double) secondLatestValue;

                TextView temperatureDifferenceView = findViewById(R.id.temperature);
                String temperatureDifference = temperatureDifferenceView.getText().toString();
                temperatureDifference = temperatureDifference.isEmpty() ? "0.0" : temperatureDifference;

                if ((latestValueFloat - secondLatestValueFloat) >= Double.parseDouble(temperatureDifference) || (latestValueFloat - secondLatestValueFloat) >= Double.parseDouble(temperatureDifference)) {
                    createNotificationText(
                            "Latest Temp: " + latestValue + ", Previous Temp: " + secondLatestValue + ", Threshold: " + temperatureDifference
                    );
                    notificationManagerCompat.notify(100, builder.build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeHTTPRequest(final String url, final int requestMethod) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(requestMethod, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject resultJsonObject = new JSONObject(response);
                            getIntent().putExtra("result", response);
                            getIntent().putExtra("resultJSON", resultJsonObject.toString());
                            compare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERROR: " + error);
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}