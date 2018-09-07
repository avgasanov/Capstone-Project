package com.tuesday_apps.catchmycar.utils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    private static final String IMAGE_ROTATION_ERROR = "image rotation error";
    private static final String TIMEDATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final String JPEG_ = "JPEG_";
    private static final String ERROR_OBTAIN_EXIF = "exif obtaining error";

    //useful method from Advanced Andoid course: emojify application
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat(TIMEDATE_FORMAT,
                Locale.getDefault()).format(new Date());
        String imageFileName = JPEG_ + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    //https://stackoverflow.com/questions/25962151/decoded-images-some-times-are-rotated-by-90degree
    public static Bitmap rotateImageIfRequired(Bitmap originalImage, String photoPath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoPath);
        } catch (IOException e) {
            Crashlytics.log(IMAGE_ROTATION_ERROR);
        }
        if (exif == null) {
            Crashlytics.log(ERROR_OBTAIN_EXIF);
            return originalImage;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        if(orientation != ExifInterface.ORIENTATION_NORMAL) {

            int rotatesize;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatesize = -90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatesize = -180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatesize = -270;
                    break;

                default:
                    break;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap resultImage = Bitmap.createBitmap(
                    originalImage,
                    0,
                    0,
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    matrix,
                    true);
            return resultImage;
        }
        return null;
    }

    //https://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android/3391061#3391061
    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    //https://stackoverflow.com/questions/25693636/centering-text-on-android-canvas-including-accurate-bounds
    public static Bitmap drawTextToBitmapAuto(Bitmap bitmap, String textOnCanvas, int size, int rectSize) {
        try {

            Canvas canvas = new Canvas(bitmap);

            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(255, 255, 255));

            paint.setTextSize(size);
            int y1 = bitmap.getHeight() - rectSize;
            int x2 = bitmap.getWidth() - 20;
            int y2 = bitmap.getHeight() - 20;
            Rect bounds = new Rect(0, y1, x2, y2);

            StaticLayout sl = new StaticLayout(textOnCanvas, paint,
                    bounds.width(), Layout.Alignment.ALIGN_OPPOSITE, 1, 1, true);
            canvas.save();

            Paint.FontMetrics fm = paint.getFontMetrics();
            float textHeight = fm.descent - fm.ascent;

            int numberOfTextLines = sl.getLineCount();
            float textYCoordinate = bounds.exactCenterY() -
                    ((numberOfTextLines * textHeight) / 2);


            float textXCoordinate = bounds.left;
            canvas.translate(textXCoordinate, textYCoordinate);

            sl.draw(canvas);
            canvas.restore();
            return bitmap;
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());
            return null;
        }
    }



    public static Bitmap drawTextToImage(Bitmap bitmap, String textOnCanvas, int size) {
        int newW = bitmap.getWidth();
        int rectH = (bitmap.getHeight()/100)*15;
        int newH = bitmap.getHeight() + rectH;
        Bitmap newBitmap = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(0,0,0));
        canvas.drawBitmap(bitmap, 0,0, null);
        canvas.drawRect(0, newH - rectH, newW, newH, paint);

        newBitmap = drawTextToBitmapAuto(newBitmap, textOnCanvas, size, rectH);

        return newBitmap;
    }

    //https://stackoverflow.com/questions/4352172/how-do-you-pass-images-bitmaps-between-android-activities-using-bundles/7890405#7890405
    public static String saveBitmap(Bitmap bitmap, Context context, String fileName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());

            fileName = null;
        }
        return fileName;
    }

    public static Bitmap getBitmapFromFile(Context context, String filename) {
        try {
            return BitmapFactory.decodeStream(context
                    .openFileInput(filename));
        } catch (FileNotFoundException e) {
            Crashlytics.log(e.getMessage());
            return null;
        }
    }

    //source https://stackoverflow.com/questions/12555420/how-to-get-a-uri-object-from-bitmap
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static Uri compressBitmap(Uri uri, Context context) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
            return Uri.parse(path);
        } catch (IOException e) {
           Crashlytics.log(e.getMessage());
        }
        return null;
    }

    public static class drawTextToImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private String mText;
        private BitmapResultCallback mListener;
        private int mTextSize;

        public drawTextToImageTask(String text,
                            BitmapResultCallback listener,
                            int textSize) {
            mText = text;
            mListener = listener;
            mTextSize = textSize;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            return drawTextToImage(bitmaps[0], mText, mTextSize);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mListener.onBitmapReady(bitmap);
        }
    }
}
