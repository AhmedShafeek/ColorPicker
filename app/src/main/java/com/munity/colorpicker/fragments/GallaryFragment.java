package com.munity.colorpicker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.munity.colorpicker.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by shafeek on 15/06/16.
 */
public class GallaryFragment extends Fragment {

    View view;
    GridView gallaryGridView;
    GallaryAdapter ga;
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    ImageLoader imageLoader;
    ArrayList<String> userPhotos, photoCreatedTime, photoOwner, photoName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.gallary_fragment, container, false);
        gallaryGridView = (GridView) view.findViewById(R.id.gallaryGridView);
        ga = new GallaryAdapter();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.color.transparnet)
                .showImageForEmptyUri(R.color.transparnet)
                .showImageOnFail(R.color.transparnet)
                .cacheInMemory()
                .cacheOnDisc()
                .displayer(new RoundedBitmapDisplayer(0))
                .build();
        config = new ImageLoaderConfiguration.Builder(getActivity())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
        userPhotos = getArguments().getStringArrayList("userPhotos");
        photoCreatedTime = getArguments().getStringArrayList("photoCreatedTime");
        photoOwner = getArguments().getStringArrayList("photoOwner");
        photoName = getArguments().getStringArrayList("photoName");

        gallaryGridView.setAdapter(ga);
        gallaryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DetectColorFragment dcf = new DetectColorFragment();
                Bundle args = new Bundle();
                args.putString("photo", userPhotos.get(position));
                args.putInt("frame", 1);
                dcf.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.contentFrame, dcf).commit();
            }
        });
        return view;
    }

    private class GallaryAdapter extends BaseAdapter {

        ImageView gallaryImageView;
        TextView ownerNameTextView, postTimeTextView, photoNameTextView;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.grid_gallary_custom, parent, false);
            gallaryImageView = (ImageView) convertView.findViewById(R.id.gallaryImageView);
            ownerNameTextView = (TextView) convertView.findViewById(R.id.ownerNameTextView);
            postTimeTextView = (TextView) convertView.findViewById(R.id.postTimeTextView);
            photoNameTextView = (TextView) convertView.findViewById(R.id.photoNameTextView);

            ownerNameTextView.setText(photoOwner.get(position));
            postTimeTextView.setText(photoCreatedTime.get(position));
            photoNameTextView.setText(photoName.get(position));
            imageLoader.displayImage(userPhotos.get(position), gallaryImageView, options);

            return convertView;
        }

        @Override
        public int getCount() {
            return userPhotos.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }
    }
}
