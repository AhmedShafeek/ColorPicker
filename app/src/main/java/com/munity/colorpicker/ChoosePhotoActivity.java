package com.munity.colorpicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.munity.colorpicker.fragments.DetectColorFragment;
import com.munity.colorpicker.fragments.FavoriteFragment;
import com.munity.colorpicker.fragments.GallaryFragment;
import com.munity.colorpicker.instagram_integration.ApplicationData;
import com.munity.colorpicker.instagram_integration.InstagramApp;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by shafeek on 15/06/16.
 */
public class ChoosePhotoActivity extends FragmentActivity{

    int PICK_IMAGE, PICK_CAMERA;
    ImageView favorite;
    LinearLayout splashLayout;
    Animation animAccelerate;
    InstagramApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        favorite = (ImageView) findViewById(R.id.favorite);
        splashLayout = (LinearLayout) findViewById(R.id.splashLayout);

        splashScreen();
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteFragment FF = new FavoriteFragment();
                getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.contentFrame, FF).commit();
            }
        });
    }

    protected void splashScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                animAccelerate = AnimationUtils.loadAnimation(ChoosePhotoActivity.this, R.anim.fade_out);
                splashLayout.startAnimation(animAccelerate);
                splashLayout.setClickable(false);
            }
        }, 4000);
    }

    public void choosePhoto(View v) {
        PICK_IMAGE = 1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void takePhoto(View v) {
        PICK_CAMERA = 2;
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), PICK_CAMERA);
    }

    public void instagram(View v){
        instagramConnection();
        connectOrDisconnectUser();
    }

    public void flickr(View v){
        new FlickrPhotos().execute();
    }

    void instagramConnection(){
        mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
                ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                new InstagramPhotos().execute();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(ChoosePhotoActivity.this, error, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void connectOrDisconnectUser() {
        if (mApp.hasAccessToken()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ChoosePhotoActivity.this);
            builder.setMessage(getResources().getString(R.string.disconnect_from_instagram))
                    .setCancelable(true)
                    .setPositiveButton(getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    mApp.resetAccessToken();
                                    mApp.authorize();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    new InstagramPhotos().execute();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            mApp.authorize();
        }
    }

    class InstagramPhotos extends AsyncTask<String, String, String>{
        String result, url;
        JSONObject jsonObject, dataJson, imagesJson, thumbnailJson, userJson;
        JSONArray jsonArray;
        ArrayList<String> userPhotos, photoCreatedTime, photoOwner, photoName;
        Dialog  loadingDialog;
        GallaryFragment GF;
        Bundle args;
        @Override
        protected String doInBackground(String... args) {
            try {
                url = "https://api.instagram.com/v1/users/self/media/recent/?access_token=";
                URL example = new URL(url + mApp.getTOken().toString());
                URLConnection tc = example.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));
                result = in.readLine();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new Dialog(ChoosePhotoActivity.this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.loading_progrees_dialog);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ProgressBar pb = (ProgressBar) loadingDialog.findViewById(R.id.progressBar);
            pb.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.move), PorterDuff.Mode.SRC_ATOP);
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                jsonObject = new JSONObject(result);
                jsonArray = jsonObject.getJSONArray("data");
                userPhotos = new ArrayList<String>();
                photoOwner = new ArrayList<String>();
                photoCreatedTime = new ArrayList<String>();
                photoName = new ArrayList<String>();
                GF = new GallaryFragment();
                args = new Bundle();

                for (int i = 0; i < jsonArray.length(); i++) {
                    dataJson = (JSONObject) jsonArray.get(i);
                    imagesJson = (JSONObject) dataJson.getJSONObject("images");
                    thumbnailJson = (JSONObject) imagesJson.getJSONObject("standard_resolution");
                    userJson = (JSONObject) dataJson.getJSONObject("user");

                    photoOwner.add(userJson.getString("full_name"));
                    userPhotos.add(thumbnailJson.getString("url"));
                    photoCreatedTime.add(timeStampToTime(dataJson.getString("created_time")));
                }

                for (int i = 0; i < userPhotos.size(); i++)
                    photoName.add(FilenameUtils.getBaseName(userPhotos.get(i)));

                args.putStringArrayList("userPhotos", userPhotos);
                args.putStringArrayList("photoOwner", photoOwner);
                args.putStringArrayList("photoCreatedTime", photoCreatedTime);
                args.putStringArrayList("photoName", photoName);
                GF.setArguments(args);
                getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.contentFrame, GF).commit();
            } catch (Exception e){
                Toast.makeText(ChoosePhotoActivity.this, getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            loadingDialog.dismiss();
        }
    }

    public class FlickrPhotos extends AsyncTask<String, String, String>{
        String url, result;
        Dialog loadingDialog;
        JSONObject jsonObject, tempJson;
        JSONArray jsonArray;
        ArrayList<String> photoOwner, photos, photoName, photoCreatedTime;
        GallaryFragment GF;
        Bundle args;
        @Override
        protected String doInBackground(String... args) {
            try {
                url = "https://api.flickr.com/services/rest/?&format=json&nojsoncallback=1&api_key=7933c896487ef2828eb00fd32e9e936d&method=flickr.interestingness.getList";
                URL example = new URL(url);
                URLConnection tc = example.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));
                result = in.readLine();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new Dialog(ChoosePhotoActivity.this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.loading_progrees_dialog);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ProgressBar pb = (ProgressBar) loadingDialog.findViewById(R.id.progressBar);
            pb.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.move), PorterDuff.Mode.SRC_ATOP);
            loadingDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                jsonObject = new JSONObject(result);
                jsonArray = jsonObject.getJSONObject("photos").getJSONArray("photo");
                photos = new ArrayList<>();
                photoOwner = new ArrayList<String>();
                photoName = new ArrayList<String>();
                photoCreatedTime = new ArrayList<>();
                GF = new GallaryFragment();
                args = new Bundle();

                for (int i = 0; i < jsonArray.length(); i++) {
                    tempJson = jsonArray.getJSONObject(i);
                    photos.add("https://farm" + tempJson.getString("farm") + ".staticflickr.com/" + tempJson.getString("server") + "/" + tempJson.getString("id") + "_" + tempJson.getString("secret") + ".jpg");
                    photoOwner.add(tempJson.getString("owner"));
                    photoName.add(tempJson.getString("id") + "_" + tempJson.getString("secret"));
                    photoCreatedTime.add("00-00-0000");
                }

                args.putStringArrayList("userPhotos", photos);
                args.putStringArrayList("photoOwner", photoOwner);
                args.putStringArrayList("photoCreatedTime", photoCreatedTime);
                args.putStringArrayList("photoName", photoName);
                GF.setArguments(args);
                getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.contentFrame, GF).commit();
            } catch (Exception e){
                Toast.makeText(ChoosePhotoActivity.this, getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            loadingDialog.dismiss();
        }
    }

    String timeStampToTime(String timeStamp){
        Date date = new Date(Long.parseLong(timeStamp)*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        return sdf.format(date);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (resultCode == RESULT_OK) {
                Bitmap photo = null;
                if (requestCode == PICK_CAMERA) {
                    photo = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == PICK_IMAGE) {
                    Uri selectedImageUri = data.getData();
                    photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, 0);

                DetectColorFragment dcf = new DetectColorFragment();
                Bundle args = new Bundle();
                args.putString("photo", encodedImage);
                args.putInt("frame", 2);
                dcf.setArguments(args);
                getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.contentFrame, dcf).commit();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
