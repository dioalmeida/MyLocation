### Contributed by Diogo Almeida

* Use Google Services Libraries for Maps and display your current location
* This “receipt” uses Google Maps Library API to show a view of a map on Android.
* Uses permissions to be able to obtain current location.
* Uses Google Clouds API to generate an API key so we can use Google Maps on the app.
* Tackles location with FusedLocationClientProvider, permissions, maps and markers.
* Note #1: Permission treatment is not implemented for  older Android APIs for simplicity.
* Repository: https://github.com/dioalmeida/MyLocation

#### Permissions & Libraries
* Must ask for the ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION and INTERNET permissions to work.
* We must add the necessary libraries to build.gradle(app)
* Google API Key must be set on the manifest inside the “application” tag by a “meta-data” tag, the key is not included in the repository code, you must get your own through Google Clouds API.
#### GoogleMaps, Markers & Location
There are a lot of rules and prerequisites that keep changing regularly so try to implement this part by always looking at the most recent documentation and examples as this code may not work in the future.
* Essentially we need to implement OnMapReadyCallback, this is the callback that will be triggered once the map is ready, and all the code involving the initialization of the map itself(not the view) must be dealt with inside this.
* We also need to override several functions of the activities or fragments lifecycle in order to pause, resume,stop, etc the map as the activity/fragment does the same or else the map won’t load.
* When we load the map we set the locationCallback, this code will update our markers InfoWindow text and position as the callbacks are being called, the frequency to which these callbacks are triggered is adjustable when creating the LocationRequest.
