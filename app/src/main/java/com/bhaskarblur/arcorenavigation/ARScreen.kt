package com.bhaskarblur.arcorenavigation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bhaskarblur.arcorenavigation.databinding.ActivityAractivityBinding
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import dagger.hilt.android.AndroidEntryPoint
import es.situm.sdk.model.location.CartesianCoordinate
import javax.inject.Inject

@AndroidEntryPoint
class ARScreen : AppCompatActivity() ,SensorEventListener {

    val arrow_uri = "https://raw.githubusercontent.com/2wizstudio/indoorNav/main/arrow.gltf"
    val poi_uri =
        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/1.0/SmilingFace/glTF/SmilingFace.gltf"
    var routePlaced = false
    var poiPlaced = false

    var currentLatLng = LatLng(30.9294242, 75.8078367)

    var coordinateList: ArrayList<CartesianCoordinate>? = null
    var Rot: FloatArray? = null //for gravity rotational data

    var I: FloatArray? = null //for magnetic rotational data

    var accels: FloatArray? = FloatArray(3)
    var mags: FloatArray? = FloatArray(3)
    val values = FloatArray(3)
    var yaw = 0f
    var pitch = 0f
    var roll = 0f

    @Inject
    lateinit var sensorManager: SensorManager;
    val sensor: Sensor? = null
    lateinit var binding: ActivityAractivityBinding;
    var lastAnchorNode: AnchorNode? = AnchorNode()
    var arFragment: ArFragment? = null

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAractivityBinding.inflate(getLayoutInflater())
        setContentView(binding.getRoot())
        this.getSupportActionBar()!!.hide()

        //sensor manager & sensor required to calculate yaw
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        // check if the AR is supported & latest installed etc
        maybeEnableArButton()
    }

    fun ManageLogic() {
        getCurrentLocation()
        arFragment = getSupportFragmentManager().findFragmentById(R.id.arFragment) as ArFragment?
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getCurrentLocation() {

        //this function helps us to get the current lat long of the user so that we can
        // tell coordinate converter about our current location,
        // you can use different location providers to fetch current LatLng
        locationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA
                ),
                101)
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER, 1L, 0f
        ) { location ->

            // this location LatLng can be highly inaccurate if being used in indoor location
            // or places where it's hard to navigate, it is suggested to use special
            // navigation systems like indoor navigation system for accurate results.

            // this inaccurate latlng can affect the coordinates of poi anchors, for testing
            // try with a fixed current latlng on which you are currently placed.
            currentLatLng = LatLng(location.latitude, location.latitude)
            binding.instrText.setText("Place your camera slightly next to your foot and tap on the floor shadow to begin!")

            // this function activates a single anchor poi when tapped on AR
//            PoiAnchor()
            routeAnchor()

            // this function activates a routes of poi anchors and draws line when tapped on AR
            //                routeAnchor();
        }
    }

    fun routeAnchor() {
        arFragment!!.setOnTapArPlaneListener { hitResult, _, _ ->
            if (!routePlaced) {
                // calling it here as we want to calculate coordinates only when tapped
                addDatainPOIList()
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                DrawLinesBetweenPOIs(anchorNode, false)
                for (i in coordinateList!!.indices) {
                    val repNode = AnchorNode(null)
                    repNode.localPosition = Vector3(
                        (anchorNode.localPosition.x + coordinateList!![i].x).toFloat(),
                        anchorNode.localPosition.y, (anchorNode.localPosition.z - coordinateList!![i].y).toFloat()
                    )
                    DrawLinesBetweenPOIs(repNode, true)
                }
                routePlaced = true
                binding.instrText.visibility = View.GONE
            }
        }
    }

     fun PoiAnchor() {
        arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!poiPlaced) {
                // calling it here as we want to calculate coordinates only when tapped
                addDatainPOIList()
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)

                // passing the coordinates of target from the list of coordinates,
                // in your case you can set your own lat long and convert it into local coordinates,
                // passing the anchorNode as the reference node to place other anchors in world.
                // make sure you point the ar camera on your foot and then tap on ar shadow to place anchor
                PlacePOIinRealWorld(coordinateList!![0], anchorNode)
            }
        }
    }

    fun addDatainPOIList() {
        coordinateList = ArrayList()

        // instantiating our cordinates helper class with our current coordinate
        val cartesianHelper = CoordinatesHelper(currentLatLng)
        cartesianHelper.setCenter_(currentLatLng)

        // converting the LatLng coordinates of poi to local coordinates to get place in AR
        val Convcoordinate_ = cartesianHelper.GetLocalCoordinates(
            LatLng(
                30.919326, 75.831798
            ), yaw.toDouble()
        )
        val Singlecoordinate = CartesianCoordinate(Convcoordinate_.x, Convcoordinate_.y)
        val Convcoordinate_2 = cartesianHelper.GetLocalCoordinates(
            LatLng(
                30.919330, 75.831967

            ), yaw.toDouble()
        )
        val Singlecoordinate2 = CartesianCoordinate(Convcoordinate_2.x, Convcoordinate_2.y)
        val Convcoordinate_3 = cartesianHelper.GetLocalCoordinates(
            LatLng(
                30.919255, 75.831969

            ), yaw.toDouble()
        )
        val Singlecoordinate3 = CartesianCoordinate(Convcoordinate_3.x, Convcoordinate_3.y)
        coordinateList!!.add(Singlecoordinate)
        coordinateList!!.add(Singlecoordinate2)
        coordinateList!!.add(Singlecoordinate3)
    }

    fun DrawLinesBetweenPOIs(anchorNode: AnchorNode, place_: Boolean) {
        if (lastAnchorNode != null) {
            anchorNode.setParent(arFragment!!.arSceneView.scene)
            val point1: Vector3
            val point2: Vector3
            point1 = lastAnchorNode!!.worldPosition
            point2 = anchorNode.worldPosition
            if (place_) {
                val difference = Vector3.subtract(point1, point2)
                val directionFromTopToBottom = difference.normalized()
                val rotationFromAToB =
                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())
                val sampler = Texture.Sampler.builder()
                    .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                    .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                    .build()
                Texture.builder()
                    .setSource {
                        getApplicationContext().getAssets().open("arrow_texture.png")
                    }
                    .setSampler(sampler)
                    .build().thenAccept { texture: Texture? ->
                        MaterialFactory.makeTransparentWithTexture(
                            getApplicationContext(),
                            texture
                        ) //new Color(0, 255, 244))
                            .thenAccept { material: Material? ->
                                val model = ShapeFactory.makeCube(
                                    Vector3(.3f, .006f, difference.length()),
                                    Vector3.zero(), material
                                )
                                val node = AnchorNode()
                                node.setParent(anchorNode)
                                node.renderable = model
                                node.worldPosition = Vector3.add(point1, point2).scaled(.5f)
                                node.worldRotation = rotationFromAToB
                            }.exceptionally { p0 ->
                                Toast.makeText(
                                    this@ARScreen, "Error: " +
                                            p0!!.message.toString(), Toast.LENGTH_SHORT
                                ).show()
                                null
                            }
                    }
            }
            Log.d("position:", anchorNode.localPosition.toString())
            lastAnchorNode = anchorNode
        }
    }

    fun PlacePOIinRealWorld(coordinate: CartesianCoordinate, reference: AnchorNode) {

        // prepare renderable model for POI
        ModelRenderable.builder().setSource(
            this@ARScreen,
            RenderableSource
                .builder()
                .setSource(
                    this@ARScreen, Uri.parse(
                        poi_uri
                    ), RenderableSource.SourceType.GLTF2
                )
                .setScale(.3f)
                .build()
        )
            .setRegistryId(poi_uri)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                if (modelRenderable != null) {
                    addPOIinScene(
                        modelRenderable,
                        reference,
                        coordinate
                    )
                }
            }
            .exceptionally { throwable: Throwable ->
                Toast.makeText(this@ARScreen, "error:" + throwable.cause, Toast.LENGTH_SHORT)
                    .show()
                null
            }
        poiPlaced = true
        binding.instrText.setVisibility(View.GONE)
        Toast.makeText(this@ARScreen, "Object placed", Toast.LENGTH_SHORT).show()
    }

    fun addPOIinScene(
        modelRenderable: ModelRenderable,
        reference: AnchorNode,
        targetCoordinate_: CartesianCoordinate
    ) {
        val anchorNode1 = AnchorNode(null)
        anchorNode1.renderable = modelRenderable
        arFragment!!.arSceneView.scene.addChild(anchorNode1)

        //set the position of your POI model based on the coordinates calculated by
        // the coordinate convertor
        anchorNode1.localPosition = Vector3(
            (reference.localPosition.x + targetCoordinate_.x).toFloat(),
            reference.localPosition.y + .15f,
            (reference.localPosition.z - targetCoordinate_.y).toFloat()
        )

        //set rotation if needed ( optional)
        anchorNode1.localRotation =
            Quaternion.axisAngle(Vector3(0f, -1f, 0f), 30f)
        Toast.makeText(this@ARScreen, "POI added in scene.", Toast.LENGTH_SHORT).show()
        poiPlaced = true

        //        Toast.makeText(this, String.valueOf(Singlecoordinate.getX())+","+
//                String.valueOf(Singlecoordinate.getY()), Toast.LENGTH_SHORT).show();
    }

    fun maybeEnableArButton() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({ maybeEnableArButton() }, 200)
        }
        if (availability.isSupported) {
            // Toast.makeText(this, "AR supported", Toast.LENGTH_SHORT).show();
            isARCoreSupportedAndUpToDate()
        } else { // The device is unsupported or unknown.
            //   Toast.makeText(this, "AR Not supported", Toast.LENGTH_SHORT).show();
        }
    }

    fun isARCoreSupportedAndUpToDate(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        when (availability) {
            Availability.SUPPORTED_INSTALLED -> {
                ManageLogic()
                return true
            }

            Availability.SUPPORTED_APK_TOO_OLD, Availability.SUPPORTED_NOT_INSTALLED -> {
                try {
                    // Request ARCore installation or update if needed.
                    val installStatus =
                        ArCoreApk.getInstance().requestInstall(this@ARScreen, true)
                    return when (installStatus) {
                        InstallStatus.INSTALL_REQUESTED -> {
                            val installStatus_ =
                                ArCoreApk.getInstance().requestInstall(this@ARScreen, true)
                            Toast.makeText(
                                this,
                                "AR Core required installation",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.i(ContentValues.TAG, "ARCore installation requested.")
                            false
                        }

                        InstallStatus.INSTALLED -> true
                    }
                } catch (e: UnavailableException) {
                    Log.e(ContentValues.TAG, "ARCore not installed", e)
                    Toast.makeText(this, "Ar error:" + e.message, Toast.LENGTH_SHORT).show()
                }
                return false
            }

            Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->                 // This device is not supported for AR.
                return false

            Availability.UNKNOWN_CHECKING, Availability.UNKNOWN_ERROR, Availability.UNKNOWN_TIMED_OUT -> {}
        }
        return false
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        when (sensorEvent.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> mags = sensorEvent.values.clone()
            Sensor.TYPE_ACCELEROMETER -> accels = sensorEvent.values.clone()
        }
        if (mags != null && accels != null) {
            Rot = FloatArray(9)
            I = FloatArray(9)
            SensorManager.getRotationMatrix(Rot, I, accels, mags)

            // Correct if screen is in Landscape
            val outR = FloatArray(9)
            SensorManager.remapCoordinateSystem(
                Rot,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                outR
            )
            SensorManager.getOrientation(outR, values)

            // here we calculated the final yaw(azimuth), roll & pitch of the device.
            // multiplied by a global standard value to get accurate results

            // this is the yaw or the azimuth we need
            yaw = values[0] * 57.2957795f
            pitch = values[1] * 57.2957795f
            roll = values[2] * 57.2957795f

            //retrigger the loop when things are repopulated
            mags = null
            accels = null
        }
    }

   override fun onResume() {
        super.onResume()

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(
                this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
            )
        }
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticField != null) {
            sensorManager.registerListener(
                this, magneticField,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun finish() {
        super.finish()
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {}

}

