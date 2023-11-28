package com.example.assignment5;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListAdapterCheck extends ArrayAdapter<ListItem> {
    //List<String> selected;
    private CheckboxListener checkboxListener;
    int[] checkCount;

    ListAdapterCheck(Context context, int resource, ArrayList<ListItem> objects, CheckboxListener listener) {
        super(context, resource, objects);
        this.checkboxListener = listener;
        checkCount = new int[]{0};
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.checked_list_item, parent, false);
        }
        CheckBox myCheckBox = convertView.findViewById(R.id.listCheck);
        View finalConvertView = convertView;
        //selected = new ArrayList<>();
        //final int[] checkCount = {0};
        myCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Call your custom onChecked method
                    TextView tag = finalConvertView.findViewById(R.id.ltag);
                    String temp = tag.getText().toString();
                    if (myCheckBox.isChecked()) {
                        String[] tags = temp.split(",");
                        if(checkCount[0] < 3) {
                            /*for (String s : tags) {
                                if (!selected.contains(s)) selected.add(s);
                            }*/
                            checkCount[0]++;
                            Log.v("kai", String.valueOf(checkCount[0]));
                        }else{
                            myCheckBox.setChecked(false);
                        }
                    } else {
                        String[] tags = temp.split(",");
                        checkCount[0]--;
                        Log.v("kai", String.valueOf(checkCount[0]));
                        /*for(String s: tags) {
                            if(selected.contains(s)) selected.remove(s);
                        }*/
                    }

                checkboxListener.onCheckboxChanged(temp, isChecked);
            }
        });
        ListItem currentItem = getItem(position);
        ImageView Image = convertView.findViewById(R.id.ListImage);
        TextView date = convertView.findViewById(R.id.ldate);
        TextView tag = convertView.findViewById(R.id.ltag);
        Image.setImageBitmap(currentItem.b);
        date.setText(currentItem.date);
        tag.setText(currentItem.tag);
        return convertView;
    }

    /*public List<String> getSelected(){
        return selected;
    }*/

    public interface CheckboxListener {
        void onCheckboxChanged(String item, boolean isChecked);
    }


}
