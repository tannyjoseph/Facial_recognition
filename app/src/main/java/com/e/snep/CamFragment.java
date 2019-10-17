package com.e.snep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

public class CamFragment extends Fragment implements SurfaceHolder.Callback{

    Camera camera ;
    SurfaceView mysurfaceview;
    SurfaceHolder mysurfaceHolder;


    final int CAMERA_REQUEST_CODE = 1;

    Camera.PictureCallback jpegcallback;

    public static CamFragment newInstance() {

        CamFragment fragment = new CamFragment();
        return fragment;

    }

    int i =0;

    @Nullable
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cam, container, false);


        Button capture = view.findViewById(R.id.capture);

        mysurfaceview = view.findViewById(R.id.surfaceView);

        mysurfaceHolder = mysurfaceview.getHolder();



        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST_CODE);

        }else {

            mysurfaceHolder.addCallback(this);
            mysurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        jpegcallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
                intent.putExtra("capture", data);
                startActivity(intent);
                return;
            }
        };

        return view;
    }

    private void captureImage() {

        camera.takePicture(null, null, jpegcallback);

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        camera = Camera.open(0);

        Camera.Parameters parameters;
        parameters = camera.getParameters();

        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }

        parameters.setPictureSize(bestSize.width, bestSize.height);

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    mysurfaceHolder.addCallback(this);
                    mysurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                }else {
                    Toast.makeText(getActivity(), "Please allow", Toast.LENGTH_SHORT).show();

                }

                break;
            }
        }
    }
}
