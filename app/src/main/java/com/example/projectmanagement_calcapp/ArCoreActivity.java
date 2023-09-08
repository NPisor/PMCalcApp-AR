package com.example.projectmanagement_calcapp;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.MutableLiveData;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.gorisse.thomas.sceneform.light.LightEstimationConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class ArCoreActivity extends AppCompatActivity implements FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private ArFragment arFragment;
    public Renderable model;
    MutableLiveData<Bitmap> bitmap;
    AnchorHandler anchorHandler;
    String imageUuid;

    WeakReference<ArCoreActivity> weakActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        weakActivity = new WeakReference<>(this);
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
        Button resolveBtn = findViewById(R.id.resolveBtn);       //------------------Init models------------------


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
        config.setInstantPlacementMode(Config.InstantPlacementMode.DISABLED);
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.DISABLED);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUuid = data.getStringExtra("uuid");
        bitmap.postValue(BitmapFactory.decodeFile(data.getStringExtra("imagePath")));
    }

    public void displayTaskEntryScreen(ArCoreActivity activity){

        Intent intent = new Intent(activity, TaskInfoActivity.class);
        activity.startActivityForResult(intent, 1);
    }


    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        anchorHandler = new AnchorHandler();
        bitmap = new MutableLiveData<>();
        Anchor anchor = hitResult.createAnchor();
        displayTaskEntryScreen(this);

        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    ArCoreActivity activity = weakActivity.get();
                    if (activity != null) {
                        bitmap.observe(this, bitmap -> {
                            try {
                                anchorHandler.placeAnchor(anchor, arFragment, imageUuid, model, viewRenderable, bitmap);
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to build ViewRenderable", Toast.LENGTH_LONG).show();
                    return null;
                });

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
