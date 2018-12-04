package com.example.junmung.studyhelper.drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.junmung.studyhelper.R;

import java.util.ArrayList;

public class DrawerItemAdapter extends BaseAdapter{
    private ArrayList<DrawerItem> drawerItems;

    public DrawerItemAdapter() {
        drawerItems = new ArrayList<>();
        drawerItems.add(new DrawerItem(R.drawable.account_24dp, "StudyHelper"));
        drawerItems.add(new DrawerItem(R.drawable.report_24dp, "공부시간 보고"));
        drawerItems.add(new DrawerItem(R.drawable.chart_24dp, "통계"));
        drawerItems.add(new DrawerItem(R.drawable.email_24dp, "문의하기"));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_menu, parent, false);
        }


        ImageView img = convertView.findViewById(R.id.item_menu_ImageView);
        TextView txt = convertView.findViewById(R.id.item_menu_TextView);

        if(position == 0)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

        DrawerItem item = drawerItems.get(position);
        img.setImageResource(item.getImg());
        txt.setText(item.getName());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return drawerItems.get(position);
    }

    @Override
    public int getCount() {
        return drawerItems.size();
    }

}
