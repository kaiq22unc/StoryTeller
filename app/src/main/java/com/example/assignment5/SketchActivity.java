package com.example.assignment5;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SketchActivity extends AppCompatActivity{
    ImageView iv1;
    ImageView iv2;
    ImageView iv3;
    SQLiteDatabase mydb;
    MyDrawingArea dArea;
    int id;
    private final String API_KEY = "AIzaSyDqSUrOzIE4OLvr4c2NrhtBExecqOxlcI0";
    ArrayList<ListItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sketch_lay);

        /*iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);*/
        list = new ArrayList<>();
        dArea = findViewById(R.id.MDA);

        SharedPreferences prefs = getSharedPreferences("MyAppSket", Context.MODE_PRIVATE);
        boolean isDatabaseInitialized = prefs.getBoolean("db_initialized", false);

        if (!isDatabaseInitialized) {
            // Initialize the database and set the flag to true
            mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
            mydb.execSQL("DROP TABLE if exists DATA");
            mydb.execSQL("create table DATA (ID INT PRIMARY KEY, image BLOB, Date TEXT, TAG TEXT)");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("db_initialized", true);
            editor.apply();
        }

        databaseWork();
    }

    void databaseWork() {
        mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);

        //mydb.execSQL("DROP TABLE if exists DATA");
        //mydb.execSQL("create table DATA (ID INT PRIMARY KEY, image BLOB, Date TEXT, TAG TEXT)");
        //mydb.execSQL("insert into USER Values (1, 'as', 12)");

        /*ContentValues cv = new ContentValues();
        cv.put("ID", 3);
        cv.put("Name", "bb");
        cv.put("age", 15);
        mydb.insert("USER", null, cv);
         */

        EditText et = findViewById(R.id.find);
        //String userInput = et.getText().toString();
        //String[] input = userInput.split(",");

        //for(String tag: input) {
        Cursor cursor = mydb.rawQuery("SELECT DISTINCT * FROM DATA order by date desc", null);
        int id;
        byte[] imageBlob;
        String date;
        String tagText;
        int index = 0;

        while (cursor.moveToNext()) {
            id = cursor.getInt(0);
            imageBlob = cursor.getBlob(1);
            date = cursor.getString(2);
            tagText = cursor.getString(3);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

            list.add(new ListItem(bitmap, date, tagText));
        }
        cursor.close();
        if(list.size() == 0) list.add(new ListItem(null, "Unavailable", ""));
        ListAdapter la = new ListAdapter(SketchActivity.this, R.layout.list_item, list);
        ListView lv = findViewById(R.id.mylist);
        lv.setAdapter(la);
        //}
    }

    public void findtag(View view){
        EditText et = findViewById(R.id.find);
        String userInput = et.getText().toString();
        String input = "%" + userInput + "%";
        //String[] input = userInput.split(",");
        //Log.v("kaiinp", userInput);

        //for(String tag: input) {
        Cursor cursor;
        if (userInput.equals("")) {
            cursor = mydb.rawQuery("SELECT DISTINCT * FROM DATA order by date desc", null);
        } else {
            cursor = mydb.rawQuery("SELECT DISTINCT * FROM DATA where tag like ?  order by date desc", new String[]{input});
        }

        byte[] imageBlob;
        String date;
        String tagText;
        int index = 0;
        /*TextView ivr1d = findViewById(R.id.date1);
        TextView ivr1t = findViewById(R.id.tagss1);
        TextView ivr2d = findViewById(R.id.date2);
        TextView ivr2t = findViewById(R.id.tagss2);
        TextView ivr3d = findViewById(R.id.date3);
        TextView ivr3t = findViewById(R.id.tagss3); */


        list = new ArrayList<>();
        while (cursor.moveToNext()) {
            //id = cursor.getInt(0);
            imageBlob = cursor.getBlob(1);
            date = cursor.getString(2);
            tagText = cursor.getString(3);
            //Log.v("kaidat", date);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            list.add(new ListItem(bitmap, date, tagText));
        }

        cursor.close();
        if(list.size() == 0) list.add(new ListItem(null, "Unavailable", ""));
        ListAdapter la = new ListAdapter(SketchActivity.this, R.layout.list_item, list);
        ListView lv = findViewById(R.id.mylist);
        lv.setAdapter(la);
    }

    public void clear(View view){
        dArea.clear();
    }

    public void save(View view){
        Bitmap b = dArea.getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        long currentTimeMillis = System.currentTimeMillis();
        id = (int) currentTimeMillis;

        @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        @SuppressLint({"NewApi", "LocalSuppress"}) LocalDateTime now = LocalDateTime.now();
        @SuppressLint({"NewApi", "LocalSuppress"}) String dateString = now.format(formatter);

        //List<String> res = new ArrayList<>();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> res = new ArrayList<>();
                    res = myVisionTester(b);
                    Log.v("tt", res.toString());
                    for(int i = 0; i < res.size(); i++) {
                        //Log.v("kai", input[i]);
                        ContentValues cv = new ContentValues();
                        cv.put("ID", id + i*1000000);
                        cv.put("image", imageBytes);
                        cv.put("date", dateString);
                        cv.put("tag", res.get(i));
                        mydb.insert("DATA", null, cv);
                    }*/
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Your UI-related code here
                            // This code will run on the UI thread
                            @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            @SuppressLint({"NewApi", "LocalSuppress"}) LocalDateTime now = LocalDateTime.now();
                            @SuppressLint({"NewApi", "LocalSuppress"}) String dateString = now.format(formatter);

                            EditText et = findViewById(R.id.tags);
                            String userInput = et.getText().toString();
                            String[] input = userInput.split(",");

                            for(int i = 0; i < input.length; i++) {
                                //Log.v("kai", input[i]);
                                ContentValues cv = new ContentValues();
                                cv.put("ID", id + i*1000000);
                                cv.put("image", imageBytes);
                                cv.put("date", dateString);
                                cv.put("tag", input[i]);
                                mydb.insert("DATA", null, cv);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/


        EditText et = findViewById(R.id.tags);
        String userInput = et.getText().toString();
        //String[] input = userInput.split(",");

        //for(int i = 0; i < input.length; i++) {
            //Log.v("kai", input[i]);
            ContentValues cv = new ContentValues();
            cv.put("ID", id + 1*1000000);
            cv.put("image", imageBytes);
            cv.put("date", dateString);
            cv.put("tag", userInput);
            mydb.insert("DATA", null, cv);
        //}
    }

    public void goBackToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    List<String> myVisionTester(Bitmap bitmap) throws IOException {
        //1. ENCODE image.
        //Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(drawable)).getBitmap();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());

        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(5);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);

        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer(API_KEY));
        Vision vision = builder.build();

        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();
        Log.v("MYTAG", response.toPrettyString());
        List<EntityAnnotation> annotations = response.getResponses().get(0).getLabelAnnotations();
        List<String> res = new ArrayList<>();

        if (annotations != null && !annotations.isEmpty()) {
            // Get the first description from the first annotation

            for (EntityAnnotation annotation : annotations) {
                double score = annotation.getScore();

                if (score > 0.85) {
                    String description = annotation.getDescription();
                    res.add(description);
                }
            }
            if (res.isEmpty()) res.add(annotations.get(0).getDescription());
            //String firstDescription = annotations.get(0).getDescription();
            //Log.v("fird", firstDescription);
            return res;
        } else {
            //System.out.println("No label annotations found.");
            //return "failed";
        }
        return new ArrayList<>();
    }

    public void tag(View view){
        Bitmap b = dArea.getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> res = new ArrayList<>();
                    res = myVisionTester(b);
                    Log.v("ts", res.toString());
                    List<String> finalRes = res;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Your UI-related code here
                            // This code will run on the UI thread
                            EditText ed = findViewById(R.id.tags);
                            String temp = "";
                            for(int i = 0; i < finalRes.size(); i++){
                                if(i == finalRes.size() - 1) temp += finalRes.get(i);
                                else temp += finalRes.get(i) + ",";
                            }
                            Log.v("kk", temp);
                            ed.setText(temp);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
