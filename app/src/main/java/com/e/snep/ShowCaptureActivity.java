package com.e.snep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ShowCaptureActivity extends AppCompatActivity {

    Bitmap rotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        byte[] b = extras.getByteArray("capture");

        if (b!=null){
            ImageView imageView = findViewById(R.id.imagecaptured);

            Bitmap decodeBit = BitmapFactory.decodeByteArray(b, 0, b.length);

             rotate = rotate(decodeBit);

            imageView.setImageBitmap(rotate);
        }


        Button story = findViewById(R.id.save);

        story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToStories();
            }
        });

    }

    private void saveToStories() {

        final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        final String key = db.push().getKey();

        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("captures").child(key);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotate.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] dataToUpload = baos.toByteArray();
        UploadTask uploadTask = filePath.putBytes(dataToUpload);


        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        String url = uri.toString();
                        Long currentTS = System.currentTimeMillis();
                        Long end = currentTS + (24*60*60*1000);

                        Map<String, Object> mapToUpload = new HashMap<>();
                        mapToUpload.put("imageurl", url);
                        mapToUpload.put("tsbeg", currentTS);
                        mapToUpload.put("tsend", end);

                        db.child(key).setValue(mapToUpload);


                        startActivity(new Intent(ShowCaptureActivity.this, FaceFilterActivity.class));
                        Toast.makeText(ShowCaptureActivity.this, "ho gya", Toast.LENGTH_SHORT).show();

                        return;

                    }
                });

            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                Toast.makeText(ShowCaptureActivity.this, "nhi hua", Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }

    private Bitmap rotate(Bitmap decodeBit) {

        int w = decodeBit.getWidth();
        int h = decodeBit.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodeBit, 0, 0, w, h, matrix, true);
    }
}
