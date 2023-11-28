package com.example.assignment5;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryActivity extends AppCompatActivity implements ListAdapterCheck.CheckboxListener{

    SQLiteDatabase mydb;
    SQLiteDatabase mydbcam;
    ArrayList<ListItem> listall;
    ArrayList<ListItem> listcam;
    ArrayList<ListItem> listsket;
    String[] selectedTags;
    List<String> selected;
    ArrayList<ListItem> findlist;
    CheckBox myCheckBox;
    Boolean showSket;

    String url = "https://api.textcortex.com/v1/texts/social-media-posts";
    String API_Key = "gAAAAABlTUaktVU7qPMwQ-tS2WiwbSSz-t2PbqinT142EnXQVwE8P8bscpCONMYN2nyJNvcFKykzPQyQrlAgQmZOEb7LK8sqcek0-bQ6HW5OqM4Va_UV3nf9_0BrzPxLxEbCHFVkTfEF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_board);

        listall = new ArrayList<>();
        listsket = new ArrayList<>();
        listcam = new ArrayList<>();
        selected = new ArrayList<>();
        showSket = true;

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean isDatabaseInitialized = prefs.getBoolean("camdb_initialized", false);

        if (!isDatabaseInitialized) {
            // Initialize the database and set the flag to true
            mydbcam = this.openOrCreateDatabase("mydbcam", Context.MODE_PRIVATE, null);
            mydbcam.execSQL("DROP TABLE if exists DATA");
            mydbcam.execSQL("create table DATA (ID INT PRIMARY KEY, image BLOB, Date TEXT, TAG TEXT)");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("camdb_initialized", true);
            editor.apply();
        }

        SharedPreferences sketprefs = getSharedPreferences("MyAppSket", Context.MODE_PRIVATE);
        boolean isDatabaseSketInitialized = sketprefs.getBoolean("db_initialized", false);

        if (!isDatabaseSketInitialized) {
            // Initialize the database and set the flag to true
            mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
            mydb.execSQL("DROP TABLE if exists DATA");
            mydb.execSQL("create table DATA (ID INT PRIMARY KEY, image BLOB, Date TEXT, TAG TEXT)");
            SharedPreferences.Editor editor = sketprefs.edit();
            editor.putBoolean("db_initialized", true);
            editor.apply();
        }

        myCheckBox = findViewById(R.id.myCheckBox);
        myCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Call your custom onChecked method
                onChecked(myCheckBox);
            }
        });

        databaseWork();
    }

    private void databaseWork() {
        mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
        mydbcam = this.openOrCreateDatabase("mydbcam", Context.MODE_PRIVATE, null);

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

            listsket.add(new ListItem(bitmap, date, tagText));
        }
        cursor.close();

        cursor = mydbcam.rawQuery("SELECT DISTINCT * FROM DATA order by date desc", null);

        while (cursor.moveToNext()) {
            id = cursor.getInt(0);
            imageBlob = cursor.getBlob(1);
            date = cursor.getString(2);
            tagText = cursor.getString(3);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

            listcam.add(new ListItem(bitmap, date, tagText));
        }
        cursor.close();

        listall.addAll(listcam);
        listall.addAll(listsket);
        Collections.sort(listall, (a, b) -> a.date.compareTo(b.date));

        if(listall.size() == 0) listall.add(new ListItem(null, "Unavailable", ""));
        ListAdapterCheck la = new ListAdapterCheck(StoryActivity.this, R.layout.checked_list_item, listall, this);
        ListView lv = findViewById(R.id.mylistchecked);
        lv.setAdapter(la);
    }

    public void onChecked(CheckBox myCheckBox){

        if (myCheckBox.isChecked()) {
            /*if(listall.size() == 0) listall.add(new ListItem(null, "Unavailable", ""));
            ListAdapterCheck la = new ListAdapterCheck(StoryActivity.this, R.layout.checked_list_item, listall, this);
            ListView lv = findViewById(R.id.mylistchecked);
            lv.setAdapter(la);*/
            showSket = true;
            findtag(findViewById(R.id.find));
        } else {
            /*if(listcam.size() == 0) listcam.add(new ListItem(null, "Unavailable", ""));
            ListAdapterCheck la = new ListAdapterCheck(StoryActivity.this, R.layout.checked_list_item, listcam, this);
            ListView lv = findViewById(R.id.mylistchecked);
            lv.setAdapter(la);*/
            showSket = false;
            findtag(findViewById(R.id.find));
        }
    }

    /*public void onCheckedList(CheckBox myCheckBox){
        TextView tag = findViewById(R.id.ltag);
        String temp = tag.toString();
        TextView stext = findViewById(R.id.selected);
        if (myCheckBox.isChecked()) {
            String[] tags = temp.split(",");
            for(String s: tags) {
                if(!selected.contains(s)) selected.add(s);
            }
            StringBuilder selectedText = new StringBuilder("Selected:");
            for (String element : selected) {
                selectedText.append(",").append(element);
            }
            stext.setText(selectedText);
        } else {
            String[] tags = temp.split(",");
            for(String s: tags) {
                if(selected.contains(s)) selected.remove(s);
            }
            StringBuilder selectedText = new StringBuilder("Selected:");
            for (String element : selected) {
                selectedText.append(",").append(element);
            }
            stext.setText(selectedText);
        }
    }*/

    public void updateChecked(){
        TextView stext = findViewById(R.id.selected);
        List<String> checkedItems = selected;

        // Build a string with the checked items
        StringBuilder selectedText = new StringBuilder("Selected: ");
        for (String item : checkedItems) {
            selectedText.append(item).append(", ");
        }

        // Remove the trailing comma and space
        if (selectedText.length() > 0) {
            selectedText.setLength(selectedText.length() - 2);
        }

        // Set the text for the TextView
        stext.setText(selectedText.toString());
    }

    @Override
    public void onCheckboxChanged(String item, boolean isChecked) {
        String[] tags = item.split(",");
        if (isChecked) {
            for(String s: tags) {
                if (!selected.contains(s)) selected.add(s);
            }
        } else {
            for(String s: tags) {
                if (selected.contains(s)) selected.remove(s);
            }
        }

        updateChecked();
    }

    public void makeHttpRequest(View view) throws JSONException {
        JSONObject data = new JSONObject();
        String contxt = "story";
        //EditText keyWords = findViewById(R.id.keywords);
        if(selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one image.", Toast.LENGTH_SHORT).show();
        }else {
            String[] keywords = selected.toArray(new String[0]);

            data.put("context", contxt);
            data.put("max_tokens", 100);
            data.put("mode", "twitter");
            data.put("model", "chat-sophos-1");

            //String[] keywords = keyWords.getText().toString().split(",");
            data.put("keywords", new JSONArray(keywords));

            TextView story = findViewById(R.id.story);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.v("ss", response.toString());
                    try {
                        String textValue = response
                                .getJSONObject("data")
                                .getJSONArray("outputs")
                                .getJSONObject(0)
                                .getString("text");
                        story.setText(textValue);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //story.setText("Response: " + response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    story.setText(new String(error.networkResponse.data));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + API_Key);
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }
    }

    public void findtag(View view) {
        findlist = new ArrayList<>();
        EditText et = findViewById(R.id.find);
        String userInput = et.getText().toString();
        String[] inputs = userInput.split(",");
        //Log.v("kaiinp", userInput);
        for(String input: inputs){
            input = "%" + input + "%";
        }
        //String input = "%" + userInput + "%";
        //Log.v("aqw", input);

        //for(String tag: input) {
        /*Cursor cursor;
        if (userInput.equals("")) {
            cursor = mydbcam.rawQuery("SELECT DISTINCT * FROM DATA order by date desc", null);
        } else {
            cursor = mydbcam.rawQuery("SELECT DISTINCT * FROM DATA where tag like  ?  order by date desc", new String[]{input});
        }*/

        StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT * FROM DATA WHERE ");
        List<String> placeholders = new ArrayList<>();

        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            String placeholder = "input" + i;

            if (i > 0) {
                queryBuilder.append(" AND ");
            }

            queryBuilder.append("tag LIKE ? ");
            placeholders.add("%" + input + "%");
        }

        // Combine the placeholders into a single string
        String[] placeholderArray = placeholders.toArray(new String[0]);
        String sqlQuery = queryBuilder.toString();

        if (userInput.equals("")) {
            sqlQuery = "SELECT DISTINCT * FROM DATA order by date desc";
            placeholderArray = null;
        }

        if(showSket) {
            Cursor cursor = mydbcam.rawQuery(sqlQuery, placeholderArray);
            Cursor cursor1 = mydb.rawQuery(sqlQuery, placeholderArray);

            byte[] imageBlob;
            String date;
            String tagText;
            int index = 0;
            listall = new ArrayList<>();

            while (cursor.moveToNext()) {
                //id = cursor.getInt(0);
                imageBlob = cursor.getBlob(1);
                date = cursor.getString(2);
                tagText = cursor.getString(3);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

                findlist.add(new ListItem(bitmap, date, tagText));
            }
            cursor.close();
            while (cursor1.moveToNext()) {
                //id = cursor.getInt(0);
                imageBlob = cursor1.getBlob(1);
                date = cursor1.getString(2);
                tagText = cursor1.getString(3);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                findlist.add(new ListItem(bitmap, date, tagText));
            }
            cursor1.close();

            Collections.sort(findlist, (a, b) -> a.date.compareTo(b.date));
            if (findlist.size() == 0) findlist.add(new ListItem(null, "Unavailable", ""));
            ListAdapterCheck la = new ListAdapterCheck(StoryActivity.this, R.layout.checked_list_item, findlist, this);
            ListView lv = findViewById(R.id.mylistchecked);
            lv.setAdapter(la);
        }else{
            Cursor cursor = mydbcam.rawQuery(sqlQuery, placeholderArray);

            byte[] imageBlob;
            String date;
            String tagText;
            int index = 0;
            listall = new ArrayList<>();

            while (cursor.moveToNext()) {
                //id = cursor.getInt(0);
                imageBlob = cursor.getBlob(1);
                date = cursor.getString(2);
                tagText = cursor.getString(3);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

                findlist.add(new ListItem(bitmap, date, tagText));
            }
            cursor.close();

            Collections.sort(findlist, (a, b) -> a.date.compareTo(b.date));
            if (findlist.size() == 0) findlist.add(new ListItem(null, "Unavailable", ""));
            ListAdapterCheck la = new ListAdapterCheck(StoryActivity.this, R.layout.checked_list_item, findlist, this);
            ListView lv = findViewById(R.id.mylistchecked);
            lv.setAdapter(la);
        }
    }

    public void goBackToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
