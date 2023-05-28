# ARCore Navigation Anchors using LatLng in AR World - Android
This repository developed in Native Android - Java demonstrates how to place objects &amp; draw routes between them in ARCore scene using Latitude &amp; Longitude. The code doesn't require any use of Google Geospatial API or any such library.

Developed by: Bhaskar Kaura (https://www.linkedin.com/in/bhaskar-kaura-300b09272/)

## How does this work?
This app uses ARCore with Sceneform to support AR & render objects in AR World. The current location of user is continuously calculated
and also the yaw(azimuth) aka the rotation of the device around it's -z axis which is respective to north. Now, we have our current
Latitude, Longitude & our rotation so we assume that we are an origin point on graph (0,0) and we added a Latitude & Longitude of a coordinate into the graph say it's (3,3). So, we convert this (3,3) via a CoordinateHelper class which converts LatLng into local coordinates using our current location, yaw and the target Location. Now we got the local coordinates, we set this as a LocalPosition
to our AnchorNode in AR World.

## Accuracy?
This solution is pretty accurate as it doesnt rely on any external system and just requires user location & yaw. But, keep in mind that if the location LatLng & yaw of user is incorrect or inaccurate, then this may affect the resulted coordinates.

## Examples

![Anchor placed in AR World using LatLng](https://github.com/bhaskarblur/ARCore-Navigation-Anchors-using-LatLng-in-AR-World---Android/blob/316265830063214b58c44a39c44988d7aa1859e5/poi.jpg)

Anchor placed in AR World using LatLng

![Route lines draw between different LatLng in AR World](https://github.com/bhaskarblur/ARCore-Navigation-Anchors-using-LatLng-in-AR-World---Android/blob/316265830063214b58c44a39c44988d7aa1859e5/route.jpg)

Route lines draw between different LatLng in AR World

## With this app you will be able to:
- Get current **user location & yaw**
- Convert entered **Latitude & Longitude** into local coordinates
- Place **anchor & object on LatLng** using coordinate converter in AR Scene
- **Draw routes between different LatLng** using coordinate converter in AR Scene

## Getting Started

### Setup all the required dependencies
```
implementation ('es.situm:situm-sdk:2.85.3@aar') {
        transitive = true
    }
implementation 'com.google.android.gms:play-services-maps:18.1.0'
implementation 'com.google.ar.sceneform.ux:sceneform-ux:1.17.1'
implementation 'com.github.appoly:ARCore-Location:1.2'
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'de.javagl:obj:0.2.1'
implementation 'com.google.ar:core:1.31.0'
implementation 'com.google.ar.sceneform:core:1.17.1'
``` 

### Setup Google Cloud Console Account & enable ArCore in it & make an API Key
### Add the required permissions and metadata of ArCore Api in Manifest file
### Enable ViewBinding in buildfeatures in Build Gradle file

## Get user location
Using the code below, retrieve & update the current user location

```
private LatLng currentLatLng =
            new LatLng(0, 0);
            
private LocationManager locationManager;

locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                // this location LatLng can be highly inaccurate if being used in indoor location
                // or places where it's hard to navigate, it is suggested to use special
                // navigation systems like indoor navigation system for accurate results.

                // this inaccurate latlng can affect the coordinates of poi anchors, for testing
                // try with a fixed current latlng on which you are currently placed.

                currentLatLng = new LatLng(location.getLatitude(), location.getLatitude());
                binding.instrText.setText("Place your camera slightly next to your foot and tap on the 
                floor shadow to begin!");


            }
        });
```

## Get user yaw or the rotation
Using the code below, retrieve the device yaw or the azimuth

```
private float I[] = null; //for magnetic rotational data
private float accels[] = new float[3];
private float mags[] = new float[3];
private float[] values = new float[3];
private float yaw;
private float pitch;
private float roll;
private SensorManager sensorManager;
private Sensor sensor;
    
//sensor manager & sensor required to calculate yaw
sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
```



Inside OnResume() :

```
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
        
```        



Now, implement the SensorEventListener in activity class and add these functions

```
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

```


## Convert the Latitude & Longitude in Local Coordinates
This code will help you to convert your desired LatLng into local coordinates to place model on it. It uses
CoordinatesHelper class to do conversion. **Make sure you call this code inside OnTapPlane Listener.**

```
     // instantiating our cordinates helper class with our current coordinate
        CoordinatesHelper cartesianHelper=new CoordinatesHelper(currentLatLng);
        cartesianHelper.setCenter_(currentLatLng);

        // converting the LatLng coordinates of poi to local coordinates to get place in AR
        Coordinate coordinate1 =
                        cartesianHelper.GetLocalCoordinates(new LatLng(
                               "Latitude", "Longitude"), yaw);
                               
```


## Place POI on the converted Local Coordinate
Below, we used onTapPlane to get information about our environment and set a node inside it. You can instantly place object
without tapping the plane by using OnUpdateListener of ArFragment ArFrame. **It is suggested to place first anchor on foot of user
or beneath the camera for best results**

```
  arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if(!poiPlaced) {
                    // calling it here as we want to calculate coordinates only when tapped                  
                    convertCoordinates("Latitude", "Longitude");
                    
                    Anchor anchor= hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);

                    // passing the coordinates of target from the list of coordinates,
                    // in your case you can set your own lat long and convert it into local coordinates,
                    // passing the anchorNode as the reference node to place other anchors in world.
                    // make sure you point the ar camera on your foot and then tap on ar shadow to place anchor
                    
                    PlacePOIinRealWorld(coordinate1, anchorNode);

                }
            }
        });
        
        
 ```  
 
 
 
 ```  
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

        //set rotation if needed (optional)
        anchorNode1.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, -1f, 0f), 30f));

        Toast.makeText(ARActivity.this, "POI added in scene.", Toast.LENGTH_SHORT).show();

        poiPlaced= true;
        
    }

```  
 
 
## Draw routes between different LatLng with the converted Local Coordinate
Below, we used onTapPlane to get information about our environment and set a node inside it. You can instantly place object
without tapping the plane by using OnUpdateListener of ArFragment ArFrame. **It is suggested to place first anchor on foot of user
or beneath the camera for best results**

``` 
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
        
``` 

``` 
private AnchorNode lastAnchorNode = new AnchorNode();

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
    
``` 

## All done, you're ready to go!
Thank you for viewing my repository, i hope it helped you achieve your desired results

## Let's Connect
You can find me here:
Linkedin: [Link](https://www.linkedin.com/in/bhaskar-kaura-300b09272/)
Instagram: [Link](https://www.instagram.com/bhaskar_blur/)
Twitter: [Link](https://twitter.com/Bhaskar_blur)

## Support information
For any question or bug report, please send an email to [bhaskar7005blur@gmail.com](mailto:bhaskar7005blur@gmail.com)
