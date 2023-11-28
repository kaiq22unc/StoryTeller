package com.example.assignment5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<ListItem> {
    ListAdapter(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        ListItem currentItem = getItem(position);
        ImageView Image = convertView.findViewById(R.id.ListImage);
        TextView date = convertView.findViewById(R.id.ldate);
        TextView tag = convertView.findViewById(R.id.ltag);
        Image.setImageBitmap(currentItem.b);
        date.setText(currentItem.date);
        tag.setText(currentItem.tag);
        return convertView;
    }
}
