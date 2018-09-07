package com.tuesday_apps.catchmycar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tuesday_apps.catchmycar.ui.PreviewFragment;
import com.tuesday_apps.catchmycar.utils.BitmapResultCallback;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;
import com.tuesday_apps.catchmycar.utils.TextUtils;
import com.tuesday_apps.catchmycar.utils.Utils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity implements
        PreviewFragment.OnFragmentInteractionListener,
        CropImageView.OnCropImageCompleteListener, BitmapResultCallback, MenuItem.OnMenuItemClickListener {

    public static final String PHOTO_ACTIVITY_RESULT_URI = "photo-activity-result-uri";
    private static final String PHOTO_ACTIVITY_RESULT_IMAGE_WIDTH = "photo-activity-result-width";
    private static final String PHOTO_ACTIVIRT_RESULT_IMAGE_HEIGHT = "photo-activity-result-height";

    @BindView(R.id.process_photo_iv)
    CropImageView processPhotoImageView;
    @BindView(R.id.photo_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.photo_activity_pb)
    ProgressBar mProgressBar;


    public static final String IMAGE_EXTRA = "image_extra";
    public static final String IMAGE_URI_EXTRA = "image_uri_extra";


    private Bitmap originalImage;
    private Bitmap processedImage;

    private int PLACE_PICKER_REQUEST = 1;
    private String mPhotoPath;

    private String mTimeDate;
    private String mLocation;

    private String mResultFilename;
    private int mResultImageHeight;
    private int mResultImageWidth;

    private static final String INSTANCE_ORIGINAL_IMAGE = "instance-original-image";
    private static final String INSTANCE_PROCESSED_IMAGE = "instance-processed-image";
    private static final String INSTANCE_PHOTO_PATH = "instance-photo-path";
    private static final String INSTANCE_TIMEDATE = "instance-timedate";
    private static final String INSTANCE_LOCATION = "instance-location";
    private static final String INSTANCE_RESULT_FILENAME = "instance-result-filename";
    private static final String INSTANCE_RESULT_IMAGE_HEIGHT = "instance-result-image-height";
    private static final String INSTANCE_RESULT_IMAGE_WIDTH = "instance-result-image-width";
    private static final String INSTANCE_PROCESS_IMAGE_VIEW = "instance-process-image-view";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_activity);
        ButterKnife.bind(this);

        initToolbar();

        if(savedInstanceState == null) {
            processPhotoImageView.setAspectRatio(1, 1);
            processPhotoImageView.setImageBitmap(originalImage);

            mPhotoPath = getIntent().getStringExtra(IMAGE_EXTRA);
            setPic();
        } else {
            restoreInstanceStateCommon(savedInstanceState);
        }

        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceStateCommon(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        onSaveInstanceStateCommon(outState);
    }

    private void onSaveInstanceStateCommon(Bundle outState) {
        outState.putString(INSTANCE_LOCATION, mLocation);
        Log.v("QZADA", "saving instance state");
        if(originalImage == null) {
            Log.v("QZADA", "saving originalImage is null");
        }
        outState.putParcelable(INSTANCE_ORIGINAL_IMAGE, originalImage);
        outState.putParcelable(INSTANCE_PROCESSED_IMAGE, processedImage);
        outState.putString(INSTANCE_PHOTO_PATH, mPhotoPath);
        outState.putString(INSTANCE_RESULT_FILENAME, mResultFilename);
        outState.putInt(INSTANCE_RESULT_IMAGE_HEIGHT, mResultImageHeight);
        outState.putInt(INSTANCE_RESULT_IMAGE_WIDTH, mResultImageWidth);
        outState.putString(INSTANCE_TIMEDATE, mTimeDate);
        outState
                .putParcelable(INSTANCE_PROCESS_IMAGE_VIEW,
                        processPhotoImageView.onSaveInstanceState());

        //This is necessary for CropImageView. CropShape of the CropeImageView might not have been
        //initialized when instance state saved. And there is no check when state restored
        //in order to keep library as is. I've added this piece of code as a workaround
        CropImageView.CropShape cropShape = CropImageView.CropShape.RECTANGLE;
        outState.putString("CROP_SHAPE", cropShape.name());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        restoreInstanceStateCommon(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreInstanceStateCommon(savedInstanceState);
    }

    private void restoreInstanceStateCommon(Bundle savedInstanceState) {
        processPhotoImageView.onRestoreInstanceState(savedInstanceState);
        mLocation = savedInstanceState.getString(INSTANCE_LOCATION);
        originalImage = savedInstanceState.getParcelable(INSTANCE_ORIGINAL_IMAGE);
        processPhotoImageView.setImageBitmap(originalImage);
        processedImage = savedInstanceState.getParcelable(INSTANCE_PROCESSED_IMAGE);
        mPhotoPath = savedInstanceState.getString(INSTANCE_PHOTO_PATH);
        mResultFilename = savedInstanceState.getString(INSTANCE_RESULT_FILENAME);
        mResultImageHeight = savedInstanceState.getInt(INSTANCE_RESULT_IMAGE_HEIGHT);
        mResultImageWidth = savedInstanceState.getInt(INSTANCE_RESULT_IMAGE_WIDTH);
        mTimeDate = savedInstanceState.getString(INSTANCE_TIMEDATE);
    }

    private void initToolbar() {
       setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        processPhotoImageView.setOnCropImageCompleteListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.photo_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.post_mi:
                resolvePost();
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    private void resolvePost() {
       processPhotoImageView.getCroppedImageAsync();
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        processedImage = result.getBitmap();
        processPhoto();
    }

    private void processPhoto() {
        String bottomText = (android.text.TextUtils.isEmpty(mTimeDate) ? "" : (mTimeDate + "\t\t"))
                + (android.text.TextUtils.isEmpty(mLocation) ? "" : mLocation);
        if (!android.text.TextUtils.isEmpty(bottomText)) {
           mProgressBar.setVisibility(View.VISIBLE);
            processedImage = BitmapUtils.drawTextToImage(processedImage, bottomText, 80);
            new BitmapUtils
                    .drawTextToImageTask(bottomText,
                    this,
                    80)
                    .execute(processedImage);
        } else {
            processPhotoAfterTextDraw();
        }
    }

    @Override
    public void onBitmapReady(Bitmap bitmap) {
        processPhotoAfterTextDraw();
    }
    private void processPhotoAfterTextDraw() {
        mProgressBar.setVisibility(View.GONE);
        mResultFilename = BitmapUtils.getImageUri(this, processedImage).toString();
        mResultImageHeight = processedImage.getHeight();
        mResultImageWidth = processedImage.getWidth();
        if (mResultFilename != null) {
            DialogFragment previewDialog = PreviewFragment.newInstance(mResultFilename);
            previewDialog.show(getSupportFragmentManager(), "TAG");
        } else {
            Toast.makeText(this, R.string.not_saved, Toast.LENGTH_LONG).show();
        }
    }

    public void resolveBw(boolean checked) {
        Bitmap image;
        if (checked) {
            processedImage = BitmapUtils.toGrayscale(originalImage);
        } else {
            processedImage = originalImage;
        }
        processPhotoImageView.setImageBitmap(processedImage);
    }

    private void resolveGeodata(boolean checked) {
        if (checked) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Crashlytics
                        .log(getString(R.string.google_services_error) + e.getMessage());
            } catch (GooglePlayServicesNotAvailableException e) {
                Crashlytics
                        .log(getString(R.string.google_services_error) + e.getMessage());
            }
        } else {
            mLocation = null;
        }
    }

    private void resolveTimeAndDate(boolean checked) {
        mTimeDate = checked ? Utils.getTimeAndDate() : null;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place.getAddress() != null) {
                    mLocation = place.getAddress().toString();
                } else {
                    mLocation = "";
                }
                Toast.makeText(this, mLocation, Toast.LENGTH_LONG).show();
            }
        }
    }

    //https://developer.android.com/training/camera/photobasics
    //https://stackoverflow.com/questions/25962151/decoded-images-some-times-are-rotated-by-90degree

    private void setPic() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int targetW = (int) (displayMetrics.widthPixels/displayMetrics.density);
        int targetH = (int) (displayMetrics.heightPixels/displayMetrics.density * 0.60F);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor/4;

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath, bmOptions);
        bitmap = BitmapUtils.rotateImageIfRequired(bitmap, mPhotoPath);
        originalImage = bitmap;
        processPhotoImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.putExtra(PHOTO_ACTIVITY_RESULT_URI, mResultFilename);
        intent.putExtra(PHOTO_ACTIVITY_RESULT_IMAGE_WIDTH, mResultImageWidth);
        intent.putExtra(PHOTO_ACTIVIRT_RESULT_IMAGE_HEIGHT, mResultImageHeight);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    public void toogleClick(View toggle) {
        int toggleId = toggle.getId();
        boolean checked = ((ToggleButton) toggle).isChecked();
        switch (toggleId) {
            case R.id.geodata_sw:
                resolveGeodata(checked);
                return;
            case R.id.timedate_sw:
                resolveTimeAndDate(checked);
                return;
            case R.id.blackwhite_sw:
                resolveBw(checked);
                return;
            default:
        }

    }

    public static String getPhotoActivityResultUri(Intent intent) {
        return intent.getStringExtra(PHOTO_ACTIVITY_RESULT_URI);
    }

    public static int getPhotoActivityResultHeight(Intent intent) {
        return intent.getIntExtra(PHOTO_ACTIVIRT_RESULT_IMAGE_HEIGHT, 400);
    }


    public static int getPhotoActivityResultWidth(Intent intent) {
        return intent.getIntExtra(PHOTO_ACTIVITY_RESULT_IMAGE_WIDTH, 400);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        toogleClick(item.getActionView());
        return true;
    }
}
