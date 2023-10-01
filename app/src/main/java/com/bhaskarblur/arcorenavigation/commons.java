package com.example.indoortracking;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.ArrayList;
import java.util.List;

import es.situm.sdk.model.cartography.Point;

public class commons {

    private String API_KEY="ef34e0a493c5eb6345a3513a7cb03cf6fb418ee6d10d865f3f7e3b7c050e3b57";
    private String API_EMAIL="nick@zeniamobile.com";

    public String getGoogleMapApi() {
        return googleMapApi;
    }

    //this is my own, need to replace.
    private String googleMapApi="AIzaSyBS4QH7KbXakdfJP8v1qErqT0iF9GRJbII";

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getAPI_EMAIL() {
        return API_EMAIL;
    }

//    private void drawLine (AnchorNode node1, AnchorNode node2, Point p1, Point p2){
//        Draw a line between two AnchorNodes (adapted from https://stackoverflow.com/a/52816504/334402)
//        Log.d(TAG, "drawLine");
//        Vector3 point1, point2;
//        point1 = node1.getWorldPosition();
//        point2 = node2.getWorldPosition();
//
//
//        First, find the vector extending between the two points and define a look rotation
//        in terms of this Vector.
//        final Vector3 difference = Vector3.subtract(point1, point2);
//        final Vector3 directionFromTopToBottom = difference.normalized();
//        final Quaternion rotationFromAToB =
//                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
//        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
//                .thenAccept(
//                        material -> {
//                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
//                                   to extend to the necessary length.  */
//                            Log.d(TAG, "drawLine inside .thenAccept");
//                            ModelRenderable model = ShapeFactory.makeCube(
//                                    new Vector3(.5f, .1f, difference.length()),
//                                    Vector3.zero(), material);
//                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
//                                   the midpoint between the given points . */
//                            Anchor lineAnchor = node2.getAnchor();
//                            nodeForLine = new Node();
//                            nodeForLine.setParent(node1);
//                            nodeForLine.setRenderable(model);
//                            nodeForLine.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
//                            nodeForLine.setWorldRotation(rotationFromAToB);
//                            arFragment.getArSceneView().getScene().addChild(nodeForLine);
//                        }
//                );


//    private void addModeltoScene(Anchor anchor, ModelRenderable modelRenderable, String rotate) {
//        //AnchorNode node= new AnchorNode(anchor);
//        List<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
//        for (Node node_ : children) {
//            if (node_ instanceof AnchorNode) {
//                if (((AnchorNode) node_).getAnchor() != null) {
//                    ((AnchorNode) node_).getAnchor().detach();
//
//                }
//            }
//            if (!(node_ instanceof Camera) && !(node_ instanceof Sun)) {
//                node_.setParent(null);
//            }
//        }
//        node = new Node();
//        node.setParent(arFragment.getArSceneView().getScene());
//        node.setRenderable(modelRenderable);
//        //  AnchorNode anchorNode1=new AnchorNode(anchor);
//        //anchorNode1.setRenderable(modelRenderable);
//        arFragment.getArSceneView().getScene().addChild(node);
//        arFragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
//            @Override
//            public void onUpdate(FrameTime frameTime) {
//                Camera camera=arFragment.getArSceneView().getScene().getCamera();
//                Ray ray= camera.screenPointToRay(1080/2f, 2800/2f);
//                Vector3 newpos= ray.getPoint(2f);
//                node.setLocalPosition(newpos);
//                //node.setLocalPosition(new Vector3(0f,
//                //        camera.getLocalPosition().y-2f,
//                //        camera.getLocalPosition().z -2.5f));
//                if(rotate.equals("straight")) {
//                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(-.8f, 1.8f, 1.5f), 90f));
//                }
//                if(rotate.equals("left")) {
//                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 180f));
//                }
//                if(rotate.equals("back")) {
//                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 270f));
//                }
//                if(rotate.equals("right")) {
//                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
//                }
//
//            }
//        });
//
//
//
//    }

//    private void changeDirection(String rotate) {
//
//        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
//            @Override
//            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//                Toast.makeText(ARActivity.this, plane.getType().ordinal(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        ModelRenderable.builder().
//                setSource(
//                        ARActivity.this,
//                        RenderableSource
//                                .builder()
//                                .setSource(ARActivity.this, Uri.parse(
//                                                arrow_uri)
//                                        , RenderableSource.SourceType.GLTF2)
//                                .setScale(2.2f)
//                                .build())
//                .setRegistryId(arrow_uri)
//                .build()
//                .thenAccept(modelRenderable -> addModeltoScene(null, modelRenderable, rotate))
//                .exceptionally(throwable -> {
//                    //   Toast.makeText(ARActivity.this, "error:"+throwable.getCause(), Toast.LENGTH_SHORT).show();
//                    return null;
//                });
//    }


}
