package com.e.snep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.e.snep.Camera.CameraSourcePreview;
import com.e.snep.Camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.view.View.GONE;

public class CamFragment extends Fragment {

    SurfaceView mysurfaceview;
    SurfaceHolder mysurfaceHolder;


    final int CAMERA_REQUEST_CODE = 1;

    Camera.PictureCallback jpegcallback;
    TextGraphic mTextGraphic;

    private final Thread observer = new Thread("observer") {

        {
            setDaemon(true);
        }

        public void run() {

            while( !isInterrupted() ) {
                /*
                TextGraphic mTextGraphic = new TextGraphic(mGraphicOverlay);
                mGraphicOverlay.add(mTextGraphic);*/
                //mTextGraphic.updateText(2);
            }

        }
    };

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private int typeFace = 0;
    private int typeFlash = 0;
    private boolean flashmode = false;
    private android.graphics.Camera camera;

    private static final int MASK[] = {
            R.id.no_filter,
            R.id.hair,
            R.id.op,
            R.id.snap,
            R.id.glasses2,
            R.id.glasses3,
            R.id.glasses4,
            R.id.glasses5,
            R.id.mask,
            R.id.mask2,
            R.id.mask3,
            R.id.dog,
            R.id.cat2
    };

    private CameraSourcePreview mPreview;
    private static GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static CamFragment newInstance() {

        CamFragment fragment = new CamFragment();
        return fragment;

    }

    @Nullable
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cam, container, false);

        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.faceOverlay);
        //mTextGraphic = new TextGraphic(mGraphicOverlay);
        //mGraphicOverlay.add(mTextGraphic);

        ImageButton face = (ImageButton) view.findViewById(R.id.face);
        face.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(view.findViewById(R.id.scrollView).getVisibility() == GONE){
                    view.findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                    ((ImageButton) view.findViewById(R.id.face)).setImageResource(R.drawable.face_select);
                }else{
                    view.findViewById(R.id.scrollView).setVisibility(GONE);
                    ((ImageButton) view.findViewById(R.id.face)).setImageResource(R.drawable.face);
                }
            }
        });

        ImageButton no_filter = (ImageButton) view.findViewById(R.id.no_filter);
        no_filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton hair = (ImageButton) view.findViewById(R.id.hair);
        hair.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 1;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton op = (ImageButton) view.findViewById(R.id.op);
        op.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 2;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton snap = (ImageButton) view.findViewById(R.id.snap);
        snap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 3;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses2 = (ImageButton) view.findViewById(R.id.glasses2);
        glasses2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 4;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses3 = (ImageButton) view.findViewById(R.id.glasses3);
        glasses3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 5;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses4 = (ImageButton) view.findViewById(R.id.glasses4);
        glasses4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 6;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton glasses5 = (ImageButton) view.findViewById(R.id.glasses5);
        glasses5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 7;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask = (ImageButton) view.findViewById(R.id.mask);
        mask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 8;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask2 = (ImageButton) view.findViewById(R.id.mask2);
        mask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 9;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton mask3 = (ImageButton) view.findViewById(R.id.mask3);
        mask3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 10;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton dog = (ImageButton) view.findViewById(R.id.dog);
        dog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("haha");
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 11;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton cat2 = (ImageButton) view.findViewById(R.id.cat2);
        cat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 12;
                view.findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton button = (ImageButton) view.findViewById(R.id.change);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        final ImageButton flash = (ImageButton) view.findViewById(R.id.flash);
        flash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int blue;
                if(flashmode==false){
                    flashmode = true;
                    blue = 0;
                }else{
                    flashmode = false;
                    blue = 255;
                }
                flash.setColorFilter(Color.argb(255, 255, 255, blue));
            }
        });

        ImageButton camera = (ImageButton) view.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //takeImage();
                onPause();
            }
        });



        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }



//        Button capture = view.findViewById(R.id.capture);
//
//        mysurfaceview = view.findViewById(R.id.surfaceView);
//
//        mysurfaceHolder = mysurfaceview.getHolder();
//
//
//
//        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST_CODE);
//
//        }else {
//
//            mysurfaceHolder.addCallback(this);
//            mysurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//
//        }
//
//        capture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                captureImage();
//            }
//        });
//
//        jpegcallback = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//
//                Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
//                intent.putExtra("capture", data);
//                startActivity(intent);
//                return;
//            }
//        };
//
//        return view;
//    }
//
//    private void captureImage() {
//
//        camera.takePicture(null, null, jpegcallback);
//
//    }
//
//
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//
//        camera = Camera.open(0);
//
//        Camera.Parameters parameters;
//        parameters = camera.getParameters();
//
//        camera.setDisplayOrientation(90);
//        parameters.setPreviewFrameRate(30);
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//
//        Camera.Size bestSize = null;
//        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
//        bestSize = sizeList.get(0);
//        for(int i = 1; i < sizeList.size(); i++){
//            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
//                bestSize = sizeList.get(i);
//            }
//        }
//
//        parameters.setPictureSize(bestSize.width, bestSize.height);
//
//        camera.setParameters(parameters);
//
//        try {
//            camera.setPreviewDisplay(holder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        camera.startPreview();
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode){
//            case CAMERA_REQUEST_CODE:{
//                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//
//                    mysurfaceHolder.addCallback(this);
//                    mysurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//                }else {
//                    Toast.makeText(getActivity(), "Please allow", Toast.LENGTH_SHORT).show();
//
//                }
//
//                break;
//            }
//        }
        return view;
   }

    private void takeImage() {
        try{
            //openCamera(CameraInfo.CAMERA_FACING_BACK);
            //releaseCameraSource();
            //releaseCamera();
            //openCamera(CameraInfo.CAMERA_FACING_BACK);
            //setUpCamera(camera);
            //Thread.sleep(1000);
            mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

                private File imageFile;
                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);

                        // rotate Image
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(getActivity().getWindowManager().getDefaultDisplay().getRotation());
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                loadedImage.getWidth(), loadedImage.getHeight(),
                                rotateMatrix, false);
                        String state = Environment.getExternalStorageState();
                        File folder = null;
                        if (state.contains(Environment.MEDIA_MOUNTED)) {
                            folder = new File(Environment
                                    .getExternalStorageDirectory()+"/faceFilter");
                        } else {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + "/faceFilter");
                        }

                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdirs();
                        }
                        if (success) {
                            java.util.Date date = new java.util.Date();
                            imageFile = new File(folder.getAbsolutePath()
                                    + File.separator
                                    //+ new Timestamp(date.getTime()).toString()
                                    + "Image.jpg");

                            imageFile.createNewFile();
                        } else {
                            Toast.makeText(getActivity().getBaseContext(), "Image Not saved",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                        // save image into gallery
                        rotatedBitmap = resize(rotatedBitmap, 800, 600);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        ContentValues values = new ContentValues();

                        values.put(MediaStore.Images.Media.DATE_TAKEN,
                                System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.MediaColumns.DATA,
                                imageFile.getAbsolutePath());

//                        setResult(Activity.RESULT_OK); //add this
//                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception ex){
        }

    }


    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final CamFragment thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(getActivity(), permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        FaceDetector detector = new FaceDetector.Builder(getActivity().getApplicationContext())
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new FaceFilterActivity.GraphicFaceTrackerFactory())
                        .build());

        //new MultiProcessor.Builder<>(new GraphicTextTrackerFactory()).build();

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), detector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
        //observer.start();
        /*
        TextGraphic mTextGraphic = new TextGraphic(mGraphicOverlay);
        mGraphicOverlay.add(mTextGraphic);
        mTextGraphic.updateText(2);*/
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity().getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class GraphicTextTrackerFactory implements MultiProcessor.Factory<String> {
        @Override
        public Tracker<String> create(String face) {
            return new FaceFilterActivity.GraphicTextTracker(mGraphicOverlay);
        }
    }

    private class GraphicTextTracker extends Tracker<String> {
        private GraphicOverlay mOverlay;
        private TextGraphic mTextGraphic ;

        GraphicTextTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mTextGraphic = new TextGraphic(overlay);
        }

        public void onUpdate() {
            mOverlay.add(mTextGraphic);
            mTextGraphic.updateText(3);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mTextGraphic);
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    static class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new FaceFilterActivity.GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay,typeFace);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face,typeFace);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    public void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }



}
