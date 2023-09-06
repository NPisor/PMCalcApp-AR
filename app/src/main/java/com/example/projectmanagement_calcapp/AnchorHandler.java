package com.example.projectmanagement_calcapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;

import com.example.projectmanagement_calcapp.TaskDetailsHandler.AssigneeHandler;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public class AnchorHandler {
    AssigneeHandler assigneeHandler;

    static ViewRenderable renderable;


    public Anchor placeAnchor(ArCoreActivity activity, ArFragment fragment, HitResult hitResult, Renderable renderable, ViewRenderable viewRenderable) throws ExecutionException, InterruptedException {

        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = animateModel(fragment, anchorNode, renderable);

        Node titleNode = new Node();
        titleNode.setParent(model);
        titleNode.setEnabled(false);
        titleNode.setLocalPosition(new Vector3(0.0f, .75f, 0.0f));
        titleNode.setRenderable(viewRenderable);
        titleNode.setEnabled(true);
        this.renderable = viewRenderable;

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Vector3 cameraPosition = fragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 cardPosition = titleNode.getWorldPosition();
            Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            titleNode.setWorldRotation(lookRotation);
            viewRenderable.getView().setAlpha(1 - calculateDistance(fragment.getArSceneView().getScene().getCamera(), model));
        });
//        assigneeHandler = new AssigneeHandler();
//        ListView assigneeListView = (ListView) viewRenderable.getView().findViewById(R.id.assigneeList);
//        assigneeHandler.populateAssigneeList(activity, assigneeListView);

        return anchor;
    }

    public TransformableNode animateModel(ArFragment fragment, AnchorNode anchorNode, Renderable renderable){
        TransformableNode model = new TransformableNode(fragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
        model.setRenderable(renderable).animate(true).start();
        model.select();

        return model;
    }

    public float calculateDistance(Camera camera, TransformableNode model){
        final int MAX_DISTANCE = 20;
        final int MIN_DISTANCE = 1;
        float convertedDistance = 0;

        convertedDistance = (float) Math.sqrt(Math.pow(camera.getWorldPosition().x - model.getWorldPosition().x, 2) +
                Math.pow(camera.getWorldPosition().y - model.getWorldPosition().y, 2) +
                Math.pow(camera.getWorldPosition().z - model.getWorldPosition().z, 2) * 100);

        convertedDistance = (convertedDistance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);

        return convertedDistance;
    }

    public void displayTaskEntryScreen(ArCoreActivity activity){
        Intent intent = new Intent(activity, TaskInfoActivity.class);
        activity.startActivity(intent);
    }
}
