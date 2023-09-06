package com.example.projectmanagement_calcapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.ExecutionException;

public class TaskInfoActivity extends AppCompatActivity {
    PreviewView taskLocationImage;
    Button saveTaskBtn;
    ImageView taskImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_task_info_layout);
        taskLocationImage = findViewById(R.id.taskLocationImage);
        saveTaskBtn = findViewById(R.id.saveTaskBtn);
        try {
            setListeners(populateCameraThumbnail(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public PreviewView populateCameraThumbnail(TaskInfoActivity view) throws ExecutionException, InterruptedException {
        PreviewView previewView = findViewById(R.id.taskLocationImage);

        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

        return previewView;
    }

    public void setListeners(PreviewView previewView){
        saveTaskBtn.setOnClickListener(v -> {
            taskImage = AnchorHandler.renderable.getView().findViewById(R.id.TaskImage);
            Bitmap bitmap = previewView.getBitmap();
            taskImage.setImageBitmap(bitmap);
            finish();
        });
    }
}