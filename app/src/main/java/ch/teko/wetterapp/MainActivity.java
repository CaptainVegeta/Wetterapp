package ch.teko.wetterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getTemperature(View view) throws InterruptedException {
        final String url = "https://tecdottir.herokuapp.com/measurements/tiefenbrunnen?startDate=2021-09-20&endDate=2021-09-20";
        makeHTTPRequest(url, Request.Method.GET);
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