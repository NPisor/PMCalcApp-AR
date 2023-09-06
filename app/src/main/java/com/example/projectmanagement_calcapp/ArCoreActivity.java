package com.example.projectmanagement_calcapp;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.example.projectmanagement_calcapp.TaskDetailsHandler.AssigneeHandler;
import com.google.ar.core.Anchor;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Future;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.gorisse.thomas.sceneform.light.LightEstimationConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class ArCoreActivity extends AppCompatActivity implements FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private ArFragment arFragment;
    public Renderable model;
    ViewRenderable viewRenderable;

    AnchorHandler anchorHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        anchorHandler = new AnchorHandler();
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        initListeners();
    }

    private void initListeners() {
        Button clearBtn = findViewById(R.id.clearBtn);
        Button resolveBtn = findViewById(R.id.resolveBtn);

        //------------------Init models------------------
        WeakReference<ArCoreActivity> weakActivity = new WeakReference<>(this);

        ModelRenderable.builder()
                .setSource(this, R.raw.anchormarker)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    ArCoreActivity arCoreActivity = weakActivity.get();
                    if (arCoreActivity != null) {
                        arCoreActivity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    ArCoreActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

        resolveBtn.setOnClickListener(v -> {
            new ResolveDialog(this, dialogValue -> resolveBtn.setText(dialogValue)).show();
        });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        arSceneView._lightEstimationConfig = LightEstimationConfig.DISABLED;

    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        try {
            anchorHandler.placeAnchor(this, arFragment, hitResult, model, viewRenderable);
            anchorHandler.displayTaskEntryScreen(this);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        Anchor anchor = anchorHandler.placeAnchor(this, arFragment, hitResult, model, viewRenderable);
//
//        Future future = arFragment.getArSceneView().getSession().hostCloudAnchorAsync(anchor, 1, (s, cloudAnchorState) -> {
//                if (cloudAnchorState.isError()) {
//                    Toast.makeText(ArCoreActivity.this, "Error hosting anchor: " + cloudAnchorState, Toast.LENGTH_LONG).show();
//                    return;
//                }
//                try {
//                    Toast.makeText(ArCoreActivity.this, "Now hosting anchor...", Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
    }

}
