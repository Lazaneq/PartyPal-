package com.party.partypal;

import static android.view.LayoutInflater.*;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<cards> {

    Context context;
    public arrayAdapter(Context context, int resourceId, List<cards> items){
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        cards card_item = getItem(position);

        if(convertView == null) {
            convertView = from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        //ImageView image = (ImageView) convertView.findViewById(R.id.);
        name.setText(card_item.getName());
        //image.setImageResource(R.drawable.icon);

        return convertView;
    }
}
