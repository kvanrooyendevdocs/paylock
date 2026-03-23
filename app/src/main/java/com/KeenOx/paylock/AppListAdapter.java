package com.KeenOx.paylock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends ArrayAdapter<AppItem> {

    public AppListAdapter(Context context, List<AppItem> appItems) {
        super(context, 0, appItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);
        }

        AppItem appItem = getItem(position);

        ImageView ivAppIcon = view.findViewById(R.id.ivAppIcon);
        TextView tvAppName = view.findViewById(R.id.tvAppName);
        CheckBox checkBoxApp = view.findViewById(R.id.checkBoxApp);

        if (appItem != null) {
            ivAppIcon.setImageDrawable(appItem.getAppIcon());
            tvAppName.setText(appItem.getAppName());

            ListViewWithCheckState listView = (ListViewWithCheckState) parent;
            checkBoxApp.setChecked(listView.isItemChecked(position));
        }

        return view;
    }
}