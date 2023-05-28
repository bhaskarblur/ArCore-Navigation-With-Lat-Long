# ARCore Navigation Anchors using LatLng in AR World - Android
This repository developed in Native Android - Java demonstrates how to place objects &amp; draw routes between them in ARCore scene using Latitude &amp; Longitude. The code doesn't require any use of Google Geospatial API or any such library.

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

### Setup ARCore & SceneForm dependencies
### Setup Google Cloud Console Account & enable ArCore in it & make an API Key
### Add the required permissions and metadata of ArCore Api in Manifest file
### Enable ViewBinding in buildfeatures in Build Gradle file

### Get user location
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
                binding.instrText.setText("Place your camera slightly next to your foot and tap on the floor shadow to begin!");


            }
        });
```



