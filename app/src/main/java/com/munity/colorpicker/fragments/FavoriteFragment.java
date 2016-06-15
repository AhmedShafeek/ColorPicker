package com.munity.colorpicker.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.munity.colorpicker.FavoriteClass;
import com.munity.colorpicker.R;

import java.util.ArrayList;

/**
 * Created by shafeek on 15/06/16.
 */
public class FavoriteFragment extends Fragment {

    View view;
    ListView favoriteListView;
    TextView favoriteEmpty;
    FavoriteAdapter FA;
    FavoriteClass FC;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.favorite_fragment, container, false);
        favoriteListView = (ListView) view.findViewById(R.id.favoriteListView);
        favoriteEmpty = (TextView) view.findViewById(R.id.favoriteEmpty);
        FA = new FavoriteAdapter();
        FC = new FavoriteClass(getActivity());

        if(FC.getListString("rgb").isEmpty()){
            favoriteEmpty.setVisibility(View.VISIBLE);
        }
        favoriteListView.setAdapter(FA);

        return view;
    }

    public class FavoriteAdapter extends BaseAdapter {
        ImageView colorImageView, shareButton, deleteButton;
        TextView rgbTextView, hexTextView;
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.list_favorite_custom, parent, false);
            colorImageView = (ImageView) convertView.findViewById(R.id.colorImageView);
            shareButton = (ImageView) convertView.findViewById(R.id.shareButton);
            deleteButton = (ImageView) convertView.findViewById(R.id.deleteButton);
            rgbTextView = (TextView) convertView.findViewById(R.id.rgbTextView);
            hexTextView = (TextView) convertView.findViewById(R.id.hexTextView);

            colorImageView.setColorFilter(Color.parseColor(FC.getListString("hex").get(position)));
            rgbTextView.setText("RGB    " + FC.getListString("rgb").get(position));
            hexTextView.setText("HEX    " + FC.getListString("hex").get(position));
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "RGB    " + FC.getListString("rgb").get(position) +"     " + "HEX    " + FC.getListString("hex").get(position);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> rgb = new ArrayList<String>();
                    ArrayList<String> hex = new ArrayList<String>();
                    rgb.addAll(FC.getListString("rgb"));
                    hex.addAll(FC.getListString("hex"));
                    rgb.remove(position);
                    hex.remove(position);
                    FC.putListString("rgb", rgb);
                    FC.putListString("hex", hex);
                    FA.notifyDataSetChanged();
                    Snackbar.make(view, "Color Deleted", Snackbar.LENGTH_LONG).setActionTextColor(Color.WHITE)
                            .setAction("Action", null).show();
                }
            });
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return FC.getListString("rgb").size();
        }
    }
}
