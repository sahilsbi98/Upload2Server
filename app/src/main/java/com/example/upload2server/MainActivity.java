package com.example.upload2server;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    int PICK_IMAGE = 1;
    String filePath = "";
    private String upload_path="";
    ProgressDialog pDialog;

    int serverResponseCode = 0;
    private FileInputStream fileInputStream;
    private static final String SERVER = "http://10.0.2.2:3000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pDialog = new ProgressDialog(this);

        Button selectButton = findViewById(R.id.selectImageButton);
        Button uploadButton = findViewById(R.id.uploadImageButton);

        Log.e("PATH", "hello");

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                Log.e("PATH1", upload_path);
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
                Log.e("PATH2", upload_path);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                uploadImage();
                Log.e("PATH3", upload_path);
                upload();
                Log.e("PATH4", upload_path);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            try{
                Uri path = data.getData();
                filePath = getGalleryPath(path, this);
                upload_path = getGalleryPath(path, this);
                Log.e("FILEPath", upload_path+"is path");
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getGalleryPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void upload() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new UploadFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SERVER);
            } else {
                new UploadFile().execute(SERVER);
            }
        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class UploadFile extends AsyncTask<String, Void, Void> {
        String fileName = upload_path;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(upload_path);
        private String Content;
        private String Error = null;

        protected void onPreExecute() {
            pDialog.show();
        }

        protected Void doInBackground(String... urls) {
            BufferedReader reader = null;
            if (!sourceFile.isFile()) {
                pDialog.dismiss();
                Log.e("uploadFile", "Source File not exist");
            } else {
                try {
                    fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(urls[0]);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    serverResponseCode = conn.getResponseCode();
                    Content = conn.getResponseMessage();


                } catch (Exception ex) {
                    Error = ex.getMessage();
                } finally {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            pDialog.dismiss();
            try {
                if (Content != null) {
                    Toast.makeText(getApplicationContext(), Content, Toast.LENGTH_SHORT).show();
                    if (Content.equalsIgnoreCase("OK")) {
                        Toast.makeText(getApplicationContext(), "Your file uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
//    public void uploadImage(){
//        File file = new File(filePath);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
//        MultipartBody.Part part = MultipartBody.Part.createFormData("newimage", file.getName(), requestBody);
//
//        RequestBody somedata = RequestBody.create(MediaType.parse("text/plain"), "This is a new image");
//
//        Retrofit retrofit = NetworkClient.getRetrofit();
//        UploadApis uploadApis = retrofit.create(UploadApis.class);
//        Call call = uploadApis.uploadImage(part, somedata);
//        call.enqueue(new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) {
//                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t) {
//                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
