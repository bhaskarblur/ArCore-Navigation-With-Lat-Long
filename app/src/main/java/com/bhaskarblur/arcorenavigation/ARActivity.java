package com.bhaskarblur.arcorenavigation;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import es.situm.sdk.model.location.CartesianCoordinate;

public class ARActivity extends AppCompatActivity implements SensorEventListener {

    private String arrow_uri = "https://raw.githubusercontent.com/2wizstudio/indoorNav/main/arrow.gltf";
    private String poi_uri = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/1.0/SmilingFace/glTF/SmilingFace.gltf";
    private boolean routePlaced = false;
    private boolean poiPlaced = false;

    private LatLng currentLatLng =
            new LatLng(30.929377, 75.807884);


    private List<CartesianCoordinate> coordinateList;
    private float Rot[] = null; //for gravity rotational data
    private float I[] = null; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float[] values = new float[3];
    private float yaw;
    private float pitch;
    private float roll;
    private SensorManager sensorManager;
    private Sensor sensor;
    ActivityAractivityBinding binding;
    private AnchorNode lastAnchorNode = new AnchorNode();
    private com.google.ar.sceneform.ux.ArFragment arFragment;
    private  LocationManager locationManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAractivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.getSupportActionBar().hide();

        //sensor manager & sensor required to calculate yaw
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        // check if the AR is supported & latest installed etc
        maybeEnableArButton();

    }

    private void ManageLogic() {

        getCurrentLocation();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

    }

    private void getCurrentLocation() {

        //this function helps us to get the current lat long of the user so that we can
        // tell coordinate converter about our current location,
        // you can use different location providers to fetch current LatLng
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                // this location LatLng can be highly inaccurate if being used in indoor location
                // or places where it's hard to navigate, it is suggested to use special
                // navigation systems like indoor navigation system for accurate results.

                // this inaccurate latlng can affect the coordinates of poi anchors, for testing
                // try with a fixed current latlng on which you are currently placed.

                currentLatLng = new LatLng(location.getLatitude(), location.getLatitude());
                binding.instrText.setText("Place your camera slightly next to your foot and tap on the floor shadow to begin!");

                // this function activates a single anchor poi when tapped on AR
                PoiAnchor();

                // this function activates a routes of poi anchors and draws line when tapped on AR
//                routeAnchor();

            }
        });

    }

    private void routeAnchor() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if(!routePlaced) {
                    // calling it here as we want to calculate coordinates only when tapped
                    addDatainPOIList();
                    Anchor anchor= hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);

                    DrawLinesBetweenPOIs(anchorNode, false);

                    for(int i=0; i<coordinateList.size(); i++) {
                        AnchorNode repNode= new AnchorNode(null);
                        repNode.setLocalPosition(
                                new Vector3((float) (anchorNode.getLocalPosition()
                                        .x + coordinateList.get(i).getX()), anchorNode.getLocalPosition()
                                        .y, (float) (anchorNode.getLocalPosition()
                                        .z - coordinateList.get(i).getY())));

                        DrawLinesBetweenPOIs(repNode, true);
                    }
                    routePlaced=true;
                    binding.instrText.setVisibility(View.GONE);
                }
            }
        });
    }

    private void PoiAnchor() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if(!poiPlaced) {
                    // calling it here as we want to calculate coordinates only when tapped
                    addDatainPOIList();
                    Anchor anchor= hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);

                    // passing the coordinates of target from the list of coordinates,
                    // in your case you can set your own lat long and convert it into local coordinates,
                    // passing the anchorNode as the reference node to place other anchors in world.
                    // make sure you point the ar camera on your foot and then tap on ar shadow to place anchor
                    PlacePOIinRealWorld(coordinateList.get(0), anchorNode);

                }
            }
        });
    }

    private void addDatainPOIList() {
        coordinateList = new ArrayList<>();

        // instantiating our cordinates helper class with our current coordinate
        CoordinatesHelper cartesianHelper=new CoordinatesHelper(currentLatLng);
        cartesianHelper.setCenter_(currentLatLng);

        // converting the LatLng coordinates of poi to local coordinates to get place in AR
        Coordinate Convcoordinate_ =
                        cartesianHelper.GetLocalCoordinates(new LatLng(
                                30.929455, 75.807879), yaw);

                CartesianCoordinate Singlecoordinate=
                        new CartesianCoordinate(Convcoordinate_.x, Convcoordinate_.y);

        Coordinate Convcoordinate_2 =
                        cartesianHelper.GetLocalCoordinates(new LatLng(
                                30.929436, 75.807821), yaw);

                CartesianCoordinate Singlecoordinate2=
                        new CartesianCoordinate(Convcoordinate_2.x, Convcoordinate_2.y);


                Coordinate Convcoordinate_3 =
                        cartesianHelper.GetLocalCoordinates(new LatLng(
                                30.929398, 75.807825), yaw);

                CartesianCoordinate Singlecoordinate3=
                        new CartesianCoordinate(Convcoordinate_3.x, Convcoordinate_3.y);


        coordinateList.add(Singlecoordinate);
        coordinateList.add(Singlecoordinate2);
        coordinateList.add(Singlecoordinate3);

    }

    private void DrawLinesBetweenPOIs(AnchorNode anchorNode, boolean place_) {
        if (lastAnchorNode != null) {
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            Vector3 point1, point2;
            point1 = lastAnchorNode.getWorldPosition();
            point2 = anchorNode.getWorldPosition();

            if(place_) {
                final Vector3 difference = Vector3.subtract(point1, point2);
                final Vector3 directionFromTopToBottom = difference.normalized();
                final Quaternion rotationFromAToB =
                        Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                Texture.Sampler sampler = Texture.Sampler.builder()
                        .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                        .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                        .build();

                Texture.builder()
                        .setSource(() -> getApplicationContext().getAssets().open("arrow_texture.png"))
                        .setSampler(sampler)
                        .build().thenAccept(texture -> {
                            MaterialFactory.makeTransparentWithTexture(getApplicationContext(), texture) //new Color(0, 255, 244))
                                    .thenAccept(
                                            material -> {

                                                ModelRenderable model = ShapeFactory.makeCube(
                                                        new Vector3(.3f, .006f, difference.length()),
                                                        Vector3.zero(), material);


                                                AnchorNode node = new AnchorNode();
                                                node.setParent(anchorNode);
                                                node.setRenderable(model);
                                                node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                                node.setWorldRotation(rotationFromAToB);

                                            }
                                    ).exceptionally(new Function<Throwable, Void>() {
                                        @Override
                                        public Void apply(Throwable throwable) {
                                            Toast.makeText(ARActivity.this, "Error: "+
                                                    throwable.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                            return null;
                                        }
                                    });
                        });

            }

            Log.d("position:",anchorNode.getLocalPosition().toString());
            lastAnchorNode = anchorNode;

        }
    }


    private void PlacePOIinRealWorld(CartesianCoordinate coordinate, AnchorNode reference) {

        // prepare renderable model for POI
        ModelRenderable.builder().
                setSource(
                        ARActivity.this,
                        RenderableSource
                                .builder()
                                .setSource(ARActivity.this, Uri.parse(
                                                poi_uri)
                                        , RenderableSource.SourceType.GLTF2)
                                .setScale(.3f)
                                .build())
                .setRegistryId(poi_uri)
                .build()
                .thenAccept(modelRenderable -> addPOIinScene( modelRenderable,reference, coordinate ))
                .exceptionally(throwable -> {
                    Toast.makeText(ARActivity.this, "error:"+throwable.getCause(), Toast.LENGTH_SHORT).show();
                    return null;
                });

        poiPlaced=true;
        binding.instrText.setVisibility(View.GONE);
    }

    private void addPOIinScene(ModelRenderable modelRenderable, AnchorNode reference, CartesianCoordinate targetCoordinate_) {

        AnchorNode anchorNode1=new AnchorNode(null);
        anchorNode1.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode1);

        CartesianCoordinate Singlecoordinate= targetCoordinate_;

        //set the position of your POI model based on the coordinates calculated by
        // the coordinate convertor
        anchorNode1.setLocalPosition(new Vector3(
                (float) (reference.getLocalPosition().x+ Singlecoordinate.getX()),
                reference.getLocalPosition().y+.15f,
                (float) (reference.getLocalPosition().z-Singlecoordinate.getY())
        ));

        //set rotation if needed ( optional)
        anchorNode1.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, -1f, 0f), 30f));

        Toast.makeText(ARActivity.this, "POI added in scene.", Toast.LENGTH_SHORT).show();

        poiPlaced= true;

        //        Toast.makeText(this, String.valueOf(Singlecoordinate.getX())+","+
//                String.valueOf(Singlecoordinate.getY()), Toast.LENGTH_SHORT).show();
    }

    private void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            },200);
        }
        if (availability.isSupported()) {
            // Toast.makeText(this, "AR supported", Toast.LENGTH_SHORT).show();
            isARCoreSupportedAndUpToDate();

        } else { // The device is unsupported or unknown.
            //   Toast.makeText(this, "AR Not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isARCoreSupportedAndUpToDate() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        switch (availability) {
            case SUPPORTED_INSTALLED:
                ManageLogic();
                return true;

            case SUPPORTED_APK_TOO_OLD:
            case SUPPORTED_NOT_INSTALLED:
                try {
                    // Request ARCore installation or update if needed.
                    ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(ARActivity.this, true);
                    switch (installStatus) {
                        case INSTALL_REQUESTED:
                            ArCoreApk.InstallStatus installStatus_ = ArCoreApk.getInstance().requestInstall(ARActivity.this, true);
                            Toast.makeText(this, "AR Core required installation", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "ARCore installation requested.");
                            return false;
                        case INSTALLED:
                            return true;
                    }
                } catch (UnavailableException e) {
                    Log.e(TAG, "ARCore not installed", e);
                    Toast.makeText(this, "Ar error:" +e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return false;

            case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                // This device is not supported for AR.
                return false;

            case UNKNOWN_CHECKING:
                // ARCore is checking the availability with a remote query.
                // This function should be called again after waiting 200 ms to determine the query result.
            case UNKNOWN_ERROR:
            case UNKNOWN_TIMED_OUT:
                // There was an error checking for AR availability. This may be due to the device being offline.
                // Handle the error appropriately.
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType())
        {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = sensorEvent.values.clone();
                break;
        }

        if (mags != null && accels != null) {
            Rot = new float[9];
            I= new float[9];
            SensorManager.getRotationMatrix(Rot, I, accels, mags);

            // Correct if screen is in Landscape
            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);

            // here we calculated the final yaw(azimuth), roll & pitch of the device.
            // multiplied by a global standard value to get accurate results

            // this is the yaw or the azimuth we need
            yaw = values[0] * 57.2957795f;
            pitch =values[1] * 57.2957795f;
            roll = values[2] * 57.2957795f;

            //retrigger the loop when things are repopulated
            mags = null;
            accels = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}

