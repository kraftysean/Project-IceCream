package com.tasty;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

class StripeWebServiceTask extends AsyncTask<Object, Void, String> {

    CheckoutActivity callerActivity;

    private static final String TAG = StripeWebServiceTask.class.getSimpleName();

    @Override
    protected String doInBackground(Object... params) {
        callerActivity = (CheckoutActivity) params[0];
        String hostUrl = (String) params[1];
        JSONObject stripeParams = (JSONObject) params[2];
        Log.d(TAG, stripeParams.toString());
        try {
            return authorizeStripePayment(hostUrl, stripeParams);
        } catch (JSONException | IOException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(callerActivity, result, Toast.LENGTH_LONG).show();
        Intent i = new Intent(callerActivity, ResultActivity.class);
        i.putExtra("http-result", result);
        callerActivity.startActivity(i);
        callerActivity.finish();
    }

    private String authorizeStripePayment(String hostUrl, JSONObject stripeParams) throws IOException, JSONException {
        InputStream is = null;
        StringBuilder httpResponse = new StringBuilder();
        try {
            URL url = new URL(hostUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            // Start the query
            conn.connect();
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(stripeParams.toString().getBytes("UTF-8"));
            dos.flush();
            dos.close();


            int httpStatus = conn.getResponseCode();
            Log.d(TAG, "HTTP Status: " + httpStatus);

            BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String strLine;
            while ((strLine = input.readLine()) != null)
            {
                httpResponse.append(strLine);
            }
            input.close();
            Log.d(TAG, "HTTP Response: " + httpResponse.toString());
        } finally {
            if (is != null)
                is.close();
        }
        return httpResponse.toString();
    }
}
