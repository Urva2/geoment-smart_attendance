package com.example.attendance;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.*;

public class CloudinaryHelper {
    private static final String CLOUD_NAME = "ds12ivy94";
    private static final String API_KEY = "356955384385763";
    private static final String API_SECRET = "E0-zZv_zjqEeuq4nJY1pRmxbFlE";
    private static final String UPLOAD_PRESET = "student_leave_pdfs";

    public interface UploadCallback {
        void onSuccess(String fileUrl);
        void onFailure(String errorMessage);
    }

    public static void uploadPDF(Context context, Uri pdfUri, UploadCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            byte[] pdfBytes = new byte[inputStream.available()];
            inputStream.read(pdfBytes);
            inputStream.close();

            String encodedPDF = Base64.encodeToString(pdfBytes, Base64.DEFAULT);
            String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/upload";

            RequestBody requestBody = new FormBody.Builder()
                    .add("file", "data:application/pdf;base64," + encodedPDF)
                    .add("upload_preset", UPLOAD_PRESET)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Upload Failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onFailure("Upload Failed!");
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String fileUrl = jsonObject.getString("secure_url");
                        callback.onSuccess(fileUrl);
                    } catch (Exception e) {
                        callback.onFailure("Error parsing response");
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
 }
}
}
