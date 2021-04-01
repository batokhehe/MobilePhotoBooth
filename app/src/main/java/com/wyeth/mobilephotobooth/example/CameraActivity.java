package com.wyeth.mobilephotobooth.example;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.wyeth.mobilephotobooth.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CameraActivity extends Activity {
    private static final String LOG_TAG = "error";
    private ImageView imageView;
    static final int CAM_REQUEST = 1;
    public static int counter = 1;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        verifyStoragePermissions(this);

        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
                counter++;
                captureButton.setText(String.format("Capture (%s)", counter));
            }
        });

        imageView = findViewById(R.id.imageView2);
//        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        String root = Environment.getExternalStorageDirectory().toString();
//        String path = root + "/Blue_&_Gold_Perade" + "/selfie_cam" + curentTime + ".jpg";
//        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
//                        new File(path)));
//        startActivityForResult(camera_intent, CAM_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setAndSaveImageWithOverlay(getBitmapOfSnappedImage(0));
    }

    public Bitmap getBitmapOfSnappedImage(int rotation) {
        String root = Environment.getExternalStorageDirectory().toString();
        String path = root + "/Blue_&_Gold_Perade" + "/selfie_cam" + counter + ".jpg";

        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        bitmap = rotate(bitmap, rotation);
        return bitmap;
    }

    public void setAndSaveImageWithOverlay(Bitmap snappedImage) {
        Bitmap b = Bitmap.createBitmap(snappedImage.getWidth(), snappedImage.getHeight(), Bitmap.Config.ARGB_8888);

        //the overlay png file from drawable folder
        Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
        overlay = Bitmap.createScaledBitmap(overlay, snappedImage.getHeight(), snappedImage.getWidth(), false);

        //create canvas with a clean bitmap
        Canvas canvas = new Canvas(b);
        //draw the snappedImage on the canvas
        canvas.drawBitmap(snappedImage, 0, 0, new Paint());
        //draw the overlay on the canvas
        canvas.drawBitmap(overlay, 0, 0, new Paint());

        imageView.setImageBitmap(b);

        SaveImage(b);
    }


    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Blue_&_Gold_Perade");
        myDir.mkdirs();
        String fname = "selfie_cam" + counter + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Display display = getWindowManager().getDefaultDisplay();
            int rotation = 0;
            switch (display.getRotation()) {
                case Surface.ROTATION_0: // This is display orientation
                    rotation = 90;
                    break;
                case Surface.ROTATION_90:
                    rotation = 0;
                    break;
                case Surface.ROTATION_180:
                    rotation = 270;
                    break;
                case Surface.ROTATION_270:
                    rotation = 180;
                    break;
            }
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                setAndSaveImageWithOverlay(getBitmapOfSnappedImage(rotation));
                if (counter >= 3)
                    Toast.makeText(CameraActivity.this, "Selesai!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {

        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root + "/Blue_&_Gold_Perade");
        if (!folder.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }

        return new File(folder, "selfie_cam" + counter + ".jpg");
    }

    public static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }

}
