package com.example.test2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    Button button;
    TextView text;

    private static final String TAG = "POSTRequest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        text = findViewById(R.id.textView);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                makePostRequest();
                text.setText("1");
            }
        });
    }

    private void makePostRequest() {
        String urlString = "http://192.168.100.32:9001/course-data";

        try {
            File tempFile = createTempFileFromAssets("nilai.json");

            RequestBody fileBody = RequestBody.create(tempFile, MediaType.get("application/json"));

            // Add the file part with a name and filename
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.getName(), fileBody)
                    .build();
            Log.d(TAG, "Body: " + requestBody);

            // Build the request
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(requestBody)
                    .build();
            Log.d(TAG, "Request: " + request);

            // Send the request in a background thread
            new Thread(() -> {
                OkHttpClient client = new OkHttpClient();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        final String responseData = response.body().string();
                        runOnUiThread(() -> {
                            text.setText("Response: " + responseData);
                            Toast.makeText(MainActivity.this, "Request successful", Toast.LENGTH_LONG).show();
                        });
                    } else {
                        Log.e(TAG, "Request failed: " + response.code());
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Request failed: " + response.code(), Toast.LENGTH_LONG).show());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error: " + e.getMessage(), e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        } catch (IOException e) {
            Log.e(TAG, "Error preparing file for upload: " + e.getMessage(), e);
        }
    }

    // Helper method to create a temporary file from an asset
    private File createTempFileFromAssets(String assetFilename) throws IOException {
        File tempFile = new File(getCacheDir(), assetFilename);
        try (InputStream is = getAssets().open(assetFilename);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
        return tempFile;
    }

}