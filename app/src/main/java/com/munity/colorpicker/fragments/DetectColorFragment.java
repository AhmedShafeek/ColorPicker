package com.munity.colorpicker.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.munity.colorpicker.FavoriteClass;
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
public class DetectColorFragment extends Fragment {

    View view;
    ImageView pickerImageView;
    String photo;
    int frame;
    DisplayImageOptions options;
    ImageLoaderConfiguration config;
    ImageLoader imageLoader;
    PopupWindow colorPopUp;
    FavoriteClass FC;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.detect_color_fragment, container, false);
        pickerImageView = (ImageView) view.findViewById(R.id.pickerImageView);
        frame = getArguments().getInt("frame");
        FC = new FavoriteClass(getActivity());

        photo = getArguments().getString("photo");
        if(frame == 1){ // From Instagram AND Flicker
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
            imageLoader.displayImage(photo, pickerImageView, options);
        } else if(frame == 2){//Camera And Gallary
            pickerImageView.setImageBitmap(base64ToBitmap(photo));
        }

        pickerImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        pickerImageView.setDrawingCacheEnabled(true);
                        Bitmap bitmap = Bitmap.createBitmap(pickerImageView.getDrawingCache());
                        int pixel = bitmap.getPixel(x, y);
                        int redValue = Color.red(pixel);
                        int blueValue = Color.blue(pixel);
                        int greenValue = Color.green(pixel);
                        int[] location = new int[2];
                        pickerImageView.getLocationOnScreen(location);
                        Point point = new Point();
                        point.x = location[0];
                        point.y = location[1];
                        dialogColor(getActivity(), point, redValue, greenValue, blueValue, x, y);
                        break;
                }
                return true;
            }
        });

        return view;
    }

    Bitmap base64ToBitmap(String photo)
    {
        byte[] imageAsBytes = Base64.decode(photo.getBytes(),Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private void dialogColor(Activity context, Point p, final int r, final int g, final int b, int x, int y) {
        int popupWidth = 450;
        int popupHeight = 200;
        int OFFSET_X = x - 220;
        int OFFSET_Y = y - 100;
        RelativeLayout viewGroup = (RelativeLayout) context.findViewById(R.id.linearLayout1);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_color, viewGroup);
        colorPopUp = new PopupWindow(context);
        colorPopUp.setContentView(layout);
        colorPopUp.setWidth(popupWidth);
        colorPopUp.setHeight(popupHeight);
        colorPopUp.setOutsideTouchable(true);
        colorPopUp.setBackgroundDrawable(new BitmapDrawable());
        colorPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
        TextView rgbTextView = (TextView) layout.findViewById(R.id.rgbTextView);
        TextView hexTextView = (TextView) layout.findViewById(R.id.hexTextView);
        ImageView colorDetectImageView = (ImageView) layout.findViewById(R.id.colorDetectImageView);
        ImageView addToFavorite = (ImageView) layout.findViewById(R.id.addToFavorite);
        colorDetectImageView.setColorFilter(Color.parseColor(String.format("#%02x%02x%02x", r, g,b)));
        rgbTextView.setText("RGB :   " + r + "|" + g + "|" + b);
        hexTextView.setText("HEX :   " + String.format("#%02x%02x%02x", r, g,b));
        addToFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> rgb = new ArrayList<String>();
                ArrayList<String> hex = new ArrayList<String>();
                rgb.add(r + "|" + g + "|" + b);
                hex.add(String.format("#%02x%02x%02x", r, g,b));
                rgb.addAll(FC.getListString("rgb"));
                hex.addAll(FC.getListString("hex"));
                FC.putListString("rgb", rgb);
                FC.putListString("hex", hex);
                Snackbar.make(view, "Color Added", Snackbar.LENGTH_LONG).setActionTextColor(Color.WHITE)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            colorPopUp.dismiss();
        }catch (Exception e){

        }
    }
}
