package com.ot.ftlab;

import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class CoreSDP_APIClient {

    private static String accessToken;
    private static Instant tokenExpiry;
    private static final OkHttpClient client = new OkHttpClient();

    // *************************************
    // Initiate the constants with your data
    // *************************************

    private static String BASE_URL = "http://<SERVER>:8080/rest/";
    private static final String CLIENT_ID = "<CLIENT_ID>";
    private static final String SECRET = "<SECRET>>";
    private static final String TENANT = "<TENANT>";

    // ************************************
    // APIs end-points
    // ************************************

    private static final String ENDPOINT_CLIENT_TOKEN = "oauth2/token";
    private static final String ENDPOINT_CLIENT_DEVICES = "deviceContent";
    private static final String ENDPOINT_CLIENT_APPS = "apps";
    private static final String ENDPOINT_CLIENT_UPLOAD_APPS = "apps/upload?enforceUpload=true";
    private static final String ENDPOINT_CLIENT_USERS = "v2/users";

    // ************************************
    // Path to app (IPA or APK) for upload
    // ************************************

    @SuppressWarnings("unused")
    private static String APP = "C:\\PATH\\TO\\APP\\FILE.ipa|apk";

    public static synchronized String getAccessToken() throws IOException {
        if (accessToken == null || Instant.now().isAfter(tokenExpiry)) {
            refreshToken();
        }
        return accessToken;
    }

    private static void refreshToken() throws IOException {
        RequestBody body = RequestBody.create(
                MediaType.get("application/json"),
                "{\"clientId\":\"" + CLIENT_ID + "\",\"secret\":\"" + SECRET + "\",\"tenant\":\"" + TENANT + "\"}"
        );

        Request request = new Request.Builder()
                .url(BASE_URL + ENDPOINT_CLIENT_TOKEN)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                accessToken = json.getString("access_token");
                tokenExpiry = Instant.now().plusSeconds(json.getInt("expires_in"));
            } else {
                throw new RuntimeException("Failed to refresh token: " + (response.body() != null ? response.body().string() : "Unknown error"));
            }
        }
    }

    public static String callGET(String endpoint) throws IOException {
        String token = getAccessToken();
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("tenant-id", TENANT)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return "No response body";
        }
    }

    @SuppressWarnings("unused")
    private static void uploadApp(String filename) throws IOException {
        String token = getAccessToken();
        String[] parts = filename.split("\\\\");
        System.out.println("Uploading and preparing the app... ");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", parts[parts.length - 1],
                        RequestBody.create(MediaType.parse("application/vnd.android.package-archive"), new File(filename)))
                .build();

        Request request = new Request.Builder()
                .addHeader("content-type", "multipart/form-data")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("tenant-id", TENANT)
                .url(BASE_URL + ENDPOINT_CLIENT_UPLOAD_APPS)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Done!");
                System.out.println(response.toString());
                ResponseBody body = response.body();
                if (body != null) {
                    System.out.println(body.string());
                }
            } else {
                throw new IOException("Unexpected code " + response);
            }
            response.close();

        }

    }

    public static void main(String[] args) {
        try {
            System.out.println(callGET(ENDPOINT_CLIENT_DEVICES));
            System.out.println(callGET(ENDPOINT_CLIENT_APPS));
            System.out.println(callGET(ENDPOINT_CLIENT_USERS));

            //uploadApp(APP);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
