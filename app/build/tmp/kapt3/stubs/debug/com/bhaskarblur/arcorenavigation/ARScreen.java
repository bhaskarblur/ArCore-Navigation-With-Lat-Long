package com.bhaskarblur.arcorenavigation;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArCoreApk.Availability;
import com.google.ar.core.ArCoreApk.InstallStatus;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.ArFragment;
import dagger.hilt.android.AndroidEntryPoint;
import es.situm.sdk.model.location.CartesianCoordinate;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0014\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0016\u0010`\u001a\u00020a2\u0006\u0010b\u001a\u00020/2\u0006\u0010c\u001a\u00020DJ\u0006\u0010d\u001a\u00020aJ\u0016\u0010e\u001a\u00020a2\u0006\u0010f\u001a\u00020\"2\u0006\u0010g\u001a\u00020/J\u0006\u0010h\u001a\u00020aJ\u0006\u0010i\u001a\u00020aJ\u001e\u0010j\u001a\u00020a2\u0006\u0010k\u001a\u00020l2\u0006\u0010g\u001a\u00020/2\u0006\u0010m\u001a\u00020\"J\b\u0010n\u001a\u00020aH\u0016J\b\u0010o\u001a\u00020aH\u0007J\u0006\u0010p\u001a\u00020DJ\u0006\u0010q\u001a\u00020aJ\u001a\u0010r\u001a\u00020a2\b\u0010Q\u001a\u0004\u0018\u00010R2\u0006\u0010s\u001a\u00020tH\u0016J\u0012\u0010u\u001a\u00020a2\b\u0010v\u001a\u0004\u0018\u00010wH\u0014J\b\u0010x\u001a\u00020aH\u0014J\b\u0010y\u001a\u00020aH\u0014J\u0010\u0010z\u001a\u00020a2\u0006\u0010{\u001a\u00020|H\u0016J\u0006\u0010}\u001a\u00020aR\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001c\u0010\n\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u001c\u0010\r\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u0007\"\u0004\b\u000f\u0010\tR\u001c\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u0014\u0010\u0016\u001a\u00020\u0017X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001a\u0010\u001a\u001a\u00020\u001bX\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR.\u0010 \u001a\u0016\u0012\u0004\u0012\u00020\"\u0018\u00010!j\n\u0012\u0004\u0012\u00020\"\u0018\u0001`#X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010%\"\u0004\b&\u0010\'R\u001a\u0010(\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b*\u0010+\"\u0004\b,\u0010-R\u001c\u0010.\u001a\u0004\u0018\u00010/X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b0\u00101\"\u0004\b2\u00103R\u001e\u00104\u001a\u0002058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b6\u00107\"\u0004\b8\u00109R\u001c\u0010:\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b;\u0010\u0007\"\u0004\b<\u0010\tR\u001a\u0010=\u001a\u00020>X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b?\u0010@\"\u0004\bA\u0010BR\u001a\u0010C\u001a\u00020DX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bE\u0010F\"\u0004\bG\u0010HR\u0014\u0010I\u001a\u00020\u0017X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010\u0019R\u001a\u0010K\u001a\u00020>X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bL\u0010@\"\u0004\bM\u0010BR\u001a\u0010N\u001a\u00020DX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bO\u0010F\"\u0004\bP\u0010HR\u0013\u0010Q\u001a\u0004\u0018\u00010R\u00a2\u0006\b\n\u0000\u001a\u0004\bS\u0010TR\u001e\u0010U\u001a\u00020V8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\bW\u0010X\"\u0004\bY\u0010ZR\u0011\u0010[\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\\\u0010\u0007R\u001a\u0010]\u001a\u00020>X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b^\u0010@\"\u0004\b_\u0010B\u00a8\u0006~"}, d2 = {"Lcom/bhaskarblur/arcorenavigation/ARScreen;", "Landroidx/appcompat/app/AppCompatActivity;", "Landroid/hardware/SensorEventListener;", "()V", "I", "", "getI", "()[F", "setI", "([F)V", "Rot", "getRot", "setRot", "accels", "getAccels", "setAccels", "arFragment", "Lcom/google/ar/sceneform/ux/ArFragment;", "getArFragment", "()Lcom/google/ar/sceneform/ux/ArFragment;", "setArFragment", "(Lcom/google/ar/sceneform/ux/ArFragment;)V", "arrow_uri", "", "getArrow_uri", "()Ljava/lang/String;", "binding", "Lcom/bhaskarblur/arcorenavigation/databinding/ActivityAractivityBinding;", "getBinding", "()Lcom/bhaskarblur/arcorenavigation/databinding/ActivityAractivityBinding;", "setBinding", "(Lcom/bhaskarblur/arcorenavigation/databinding/ActivityAractivityBinding;)V", "coordinateList", "Ljava/util/ArrayList;", "Les/situm/sdk/model/location/CartesianCoordinate;", "Lkotlin/collections/ArrayList;", "getCoordinateList", "()Ljava/util/ArrayList;", "setCoordinateList", "(Ljava/util/ArrayList;)V", "currentLatLng", "Lcom/google/android/gms/maps/model/LatLng;", "getCurrentLatLng", "()Lcom/google/android/gms/maps/model/LatLng;", "setCurrentLatLng", "(Lcom/google/android/gms/maps/model/LatLng;)V", "lastAnchorNode", "Lcom/google/ar/sceneform/AnchorNode;", "getLastAnchorNode", "()Lcom/google/ar/sceneform/AnchorNode;", "setLastAnchorNode", "(Lcom/google/ar/sceneform/AnchorNode;)V", "locationManager", "Landroid/location/LocationManager;", "getLocationManager", "()Landroid/location/LocationManager;", "setLocationManager", "(Landroid/location/LocationManager;)V", "mags", "getMags", "setMags", "pitch", "", "getPitch", "()F", "setPitch", "(F)V", "poiPlaced", "", "getPoiPlaced", "()Z", "setPoiPlaced", "(Z)V", "poi_uri", "getPoi_uri", "roll", "getRoll", "setRoll", "routePlaced", "getRoutePlaced", "setRoutePlaced", "sensor", "Landroid/hardware/Sensor;", "getSensor", "()Landroid/hardware/Sensor;", "sensorManager", "Landroid/hardware/SensorManager;", "getSensorManager", "()Landroid/hardware/SensorManager;", "setSensorManager", "(Landroid/hardware/SensorManager;)V", "values", "getValues", "yaw", "getYaw", "setYaw", "DrawLinesBetweenPOIs", "", "anchorNode", "place_", "ManageLogic", "PlacePOIinRealWorld", "coordinate", "reference", "PoiAnchor", "addDatainPOIList", "addPOIinScene", "modelRenderable", "Lcom/google/ar/sceneform/rendering/ModelRenderable;", "targetCoordinate_", "finish", "getCurrentLocation", "isARCoreSupportedAndUpToDate", "maybeEnableArButton", "onAccuracyChanged", "i", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onPause", "onResume", "onSensorChanged", "sensorEvent", "Landroid/hardware/SensorEvent;", "routeAnchor", "app_debug"})
public final class ARScreen extends androidx.appcompat.app.AppCompatActivity implements android.hardware.SensorEventListener {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String arrow_uri = "https://raw.githubusercontent.com/2wizstudio/indoorNav/main/arrow.gltf";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String poi_uri = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/1.0/SmilingFace/glTF/SmilingFace.gltf";
    private boolean routePlaced = false;
    private boolean poiPlaced = false;
    @org.jetbrains.annotations.NotNull
    private com.google.android.gms.maps.model.LatLng currentLatLng;
    @org.jetbrains.annotations.Nullable
    private java.util.ArrayList<es.situm.sdk.model.location.CartesianCoordinate> coordinateList;
    @org.jetbrains.annotations.Nullable
    private float[] Rot;
    @org.jetbrains.annotations.Nullable
    private float[] I;
    @org.jetbrains.annotations.Nullable
    private float[] accels;
    @org.jetbrains.annotations.Nullable
    private float[] mags;
    @org.jetbrains.annotations.NotNull
    private final float[] values = null;
    private float yaw = 0.0F;
    private float pitch = 0.0F;
    private float roll = 0.0F;
    @javax.inject.Inject
    public android.hardware.SensorManager sensorManager;
    @org.jetbrains.annotations.Nullable
    private final android.hardware.Sensor sensor = null;
    public com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding binding;
    @org.jetbrains.annotations.Nullable
    private com.google.ar.sceneform.AnchorNode lastAnchorNode;
    @org.jetbrains.annotations.Nullable
    private com.google.ar.sceneform.ux.ArFragment arFragment;
    @javax.inject.Inject
    public android.location.LocationManager locationManager;
    
    public ARScreen() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getArrow_uri() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPoi_uri() {
        return null;
    }
    
    public final boolean getRoutePlaced() {
        return false;
    }
    
    public final void setRoutePlaced(boolean p0) {
    }
    
    public final boolean getPoiPlaced() {
        return false;
    }
    
    public final void setPoiPlaced(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.android.gms.maps.model.LatLng getCurrentLatLng() {
        return null;
    }
    
    public final void setCurrentLatLng(@org.jetbrains.annotations.NotNull
    com.google.android.gms.maps.model.LatLng p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.ArrayList<es.situm.sdk.model.location.CartesianCoordinate> getCoordinateList() {
        return null;
    }
    
    public final void setCoordinateList(@org.jetbrains.annotations.Nullable
    java.util.ArrayList<es.situm.sdk.model.location.CartesianCoordinate> p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final float[] getRot() {
        return null;
    }
    
    public final void setRot(@org.jetbrains.annotations.Nullable
    float[] p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final float[] getI() {
        return null;
    }
    
    public final void setI(@org.jetbrains.annotations.Nullable
    float[] p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final float[] getAccels() {
        return null;
    }
    
    public final void setAccels(@org.jetbrains.annotations.Nullable
    float[] p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final float[] getMags() {
        return null;
    }
    
    public final void setMags(@org.jetbrains.annotations.Nullable
    float[] p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final float[] getValues() {
        return null;
    }
    
    public final float getYaw() {
        return 0.0F;
    }
    
    public final void setYaw(float p0) {
    }
    
    public final float getPitch() {
        return 0.0F;
    }
    
    public final void setPitch(float p0) {
    }
    
    public final float getRoll() {
        return 0.0F;
    }
    
    public final void setRoll(float p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.hardware.SensorManager getSensorManager() {
        return null;
    }
    
    public final void setSensorManager(@org.jetbrains.annotations.NotNull
    android.hardware.SensorManager p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final android.hardware.Sensor getSensor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding getBinding() {
        return null;
    }
    
    public final void setBinding(@org.jetbrains.annotations.NotNull
    com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.google.ar.sceneform.AnchorNode getLastAnchorNode() {
        return null;
    }
    
    public final void setLastAnchorNode(@org.jetbrains.annotations.Nullable
    com.google.ar.sceneform.AnchorNode p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.google.ar.sceneform.ux.ArFragment getArFragment() {
        return null;
    }
    
    public final void setArFragment(@org.jetbrains.annotations.Nullable
    com.google.ar.sceneform.ux.ArFragment p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.location.LocationManager getLocationManager() {
        return null;
    }
    
    public final void setLocationManager(@org.jetbrains.annotations.NotNull
    android.location.LocationManager p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    public final void ManageLogic() {
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.S)
    public final void getCurrentLocation() {
    }
    
    public final void routeAnchor() {
    }
    
    public final void PoiAnchor() {
    }
    
    public final void addDatainPOIList() {
    }
    
    public final void DrawLinesBetweenPOIs(@org.jetbrains.annotations.NotNull
    com.google.ar.sceneform.AnchorNode anchorNode, boolean place_) {
    }
    
    public final void PlacePOIinRealWorld(@org.jetbrains.annotations.NotNull
    es.situm.sdk.model.location.CartesianCoordinate coordinate, @org.jetbrains.annotations.NotNull
    com.google.ar.sceneform.AnchorNode reference) {
    }
    
    public final void addPOIinScene(@org.jetbrains.annotations.NotNull
    com.google.ar.sceneform.rendering.ModelRenderable modelRenderable, @org.jetbrains.annotations.NotNull
    com.google.ar.sceneform.AnchorNode reference, @org.jetbrains.annotations.NotNull
    es.situm.sdk.model.location.CartesianCoordinate targetCoordinate_) {
    }
    
    public final void maybeEnableArButton() {
    }
    
    public final boolean isARCoreSupportedAndUpToDate() {
        return false;
    }
    
    @java.lang.Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull
    android.hardware.SensorEvent sensorEvent) {
    }
    
    @java.lang.Override
    protected void onResume() {
    }
    
    @java.lang.Override
    protected void onPause() {
    }
    
    @java.lang.Override
    public void finish() {
    }
    
    @java.lang.Override
    public void onAccuracyChanged(@org.jetbrains.annotations.Nullable
    android.hardware.Sensor sensor, int i) {
    }
}