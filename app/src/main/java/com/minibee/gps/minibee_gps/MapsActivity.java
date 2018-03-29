package com.minibee.gps.minibee_gps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.content.pm.PackageManager; // Pour la gestion de la permission de localisation
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat; // Pour la gestion de la permission de localisation
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import static java.lang.Math.abs;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

// ============================================================================================== //

/**
 * Classe principale : ecran principal
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener,
        BarreMenuInfo.OnFragmentInteractionListener, BarreItineraire.OnFragmentInteractionListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    // Google Map (objet)
    private GoogleMap mMap;
    // Google API
    private GoogleApiClient mGoogleApiClient;
    // Hauteur de la vue
    private int view_height;
    // Position de la camera
    private CameraPosition mCameraPosition;
    // Altitude
    private float altitude;
    private float required_altitude;
    private float initial_altitude_bar_position_y;
    // Vitesse
    private float vitesse;

    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private static final int CONNECTION_RESOLUTION_REQUEST = 2;

    // Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    // The entry point to the Fused Location Provider
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastLocation;

    // Keys for storing activity state
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Provides access to the Location Settings API.
    private SettingsClient mSettingsClient;

    // Tracks the status of the location updates request.
    private Boolean mRequestingLocationUpdates;

    // Boolean for automatic/manual camera movement
    private Boolean autoCameraMove;

    // Boolean for search detection : used to disable automatic camera movement after search
    private Boolean search;

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    // The fastest rate for active location updates. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Stores parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;

    // Callback for Location events.
    private LocationCallback mLocationCallback;

    // Stores the types of location services the client is interested in using. Used for checking
    // settings to determine if the device has optimal location settings.
    private LocationSettingsRequest mLocationSettingsRequest;

    // Barre d'altitude
    private ImageView barre_altitude;

    // Echelle d'altitude
    private ImageView echelle_altitude;
    private float hauteur_echelle_altitude;

    // Champ text vitesse
    private TextView text_vitesse;
    // Champ text altitude
    private TextView text_altitude;

    // Zoom camera
    private float zoom;

    // Identifiant de la requete de destination
    public final static int DESTINATION_REQUEST = 0;

    // Barre inferieur avec bouton "itineraire"
    private View barre_itineraire;

    // Bouton myLocationButton
    private ImageView my_location_btn;

    // Gestion du sensor pour la boussole
    private static SensorManager mySensorManager;
    private boolean sersorrunning;
    // Boussole
    private MyCompassView myCompassView;

    // Marqueurs sur la map
    List<Marker> mMarkers;
    Marker marker;


    /**
     * Lors de la création de l'activité (la classe principale)
     * Initialisation des parametres, etc.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state
        if (savedInstanceState != null)
        {
            mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map
        setContentView(R.layout.activity_maps);

        // Set zoom camera
        zoom = 21.0f;

        // Set altitude
        altitude = 0;
        required_altitude = 25;
        text_altitude = (TextView) findViewById(R.id.text_altitude);
        text_altitude.setText((int) altitude + " m");

        // Set speed
        vitesse = 100;
        text_vitesse = (TextView) findViewById(R.id.text_vitesse);
        text_vitesse.setText((int) vitesse + " km/h");

        // MyLocationButton
        my_location_btn = (ImageView) findViewById(R.id.my_location_btn);

        // Barre & echelle d'altitude
        barre_altitude = (ImageView) findViewById(R.id.barre_altitude);
        echelle_altitude = (ImageView) findViewById(R.id.echelle_altitude);

        // Marqueurs
        mMarkers = new ArrayList<Marker>();

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mRequestingLocationUpdates = true;
        autoCameraMove = false;
        search = false;
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Bouton "menu" de la barre de navigation inferieure
        ImageButton btn_menu = findViewById(R.id.toolbar_menu);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.container);
                // Bouton menu : afficher/Cacher le menu
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        // Menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Bouton "interface boutons"
        ImageButton buttons = (ImageButton)findViewById(R.id.buttons);
        buttons.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    Intent i = new Intent(MapsActivity.this, InterfaceBoutons.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Barre inferieure (fragment) avec bouton itineraire (cachée par defaut)
        barre_itineraire = findViewById(R.id.itinerary_bar);
        barre_itineraire.setVisibility(View.GONE);

        // Bouton "itineraire"
        Button btn_itineraire = (Button) findViewById(R.id.btn_itineraire);
        btn_itineraire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cacher la barre inferieure
                barre_itineraire.setVisibility(View.GONE);
                // Suppression des marqueurs
                removeMarkers();
                // Centrer sur notre position
                mMap.setPadding(0,view_height/2,0,0);
                LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(myPos, zoom, 90.0f, 0.0f)));
                // Activation du suivi temps reel
                search = false;
                mRequestingLocationUpdates = true;
                // Desactivation du bouton de localisation
                my_location_btn.setVisibility(View.GONE);
            }
        });

        // Boussole
        myCompassView = (MyCompassView)findViewById(R.id.my_compass_view);
        mySensorManager = (SensorManager)getSystemService(getApplicationContext().SENSOR_SERVICE);
        List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if(mySensors.size() > 0){
            mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            sersorrunning = true;
        }
        else{
            sersorrunning = false;
            finish();
        }

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        // Creation de l'API client pour acceder aux services Google Play
        buildGoogleAPIClient();

        // POUR LES TESTS D'ALTITUDE => A ADAPTER PAR LA SUITE
        modifyAltitude();

    }

    /**
     * Gestion du senseur pour la boussole
     */
    private SensorEventListener mySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            myCompassView.updateDirection((float)event.values[0]);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sersorrunning){
            mySensorManager.unregisterListener(mySensorEventListener);
        }
    }

    /**
     * Saves the state of the map when the activity is paused
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (mMap != null)
        {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Creation de l'API client, necessaire pour utiliser certains services
     * Google Play, notamment la geolocalisation
     */
    private void buildGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulate the map when it's available
     * This callback is triggered when the map is ready to be used
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Mise a jour de la barre d'altitude
        hauteur_echelle_altitude = echelle_altitude.getHeight();
        initial_altitude_bar_position_y = barre_altitude.getY();
        updateAltitudeUI();

        // Customise the styling of the base map using a JSON object defined in a raw resource file
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        // Get the view height of the map & set the padding for a "navigation mode"
        view_height = getSupportFragmentManager().findFragmentById(R.id.map).getView().getHeight();
        mMap.setPadding(0,view_height / 2,0,0);

        // Prompt the user for permission
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map
        updateLocationUI();

        // Get the current location of the device and set the position to the map
        //getDeviceLocation(); // locate 1 time
        startLocationUpdates(); // real-time location

    }

    /**
     * Sets up the location request.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Recuperation de notre position
                if (mLastLocation == null)
                {
                    Location first_pos = locationResult.getLastLocation();
                    //marker = mMap.addMarker(new MarkerOptions().position(new LatLng(first_pos.getLatitude(), first_pos.getLongitude())));
                    marker = null;
                }
                mLastLocation = locationResult.getLastLocation();
                LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                // Affichage & positionnement de la camera + zoom (entre 2.0 et 21.0)
                // + tilt (=inclinaison, entre 0 et 90)
                if (mRequestingLocationUpdates == true)
                {
                    autoCameraMove = true;
                    // Deplacement de la camera + zoom
                    LatLngInterpolator lli = new LatLngInterpolator.Linear();
                    if (marker != null) {
                        MarkerAnimation.animateMarkerToICS(marker, myPos, lli);
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                marker.getPosition(), zoom, 90.0f, 0.0f)), 1000, null);
                        /*CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(marker.getPosition())             // Sets the center of the map to current location
                                .zoom(zoom)                   // Sets the zoom
                                .bearing(myCompassView.getDirection()) // Sets the orientation of the camera
                                .tilt(90.0f)                   // Sets the tilt of the camera to 90 degrees
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
                    }
                    else {
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                myPos, zoom, 90.0f, 0.0f)), 1000, null);
                        // Le code suivant ajoute la fonction de rotation de camera en fonction de la rotation du telephone
                        /*CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(myPos)             // Sets the center of the map to current location
                                .zoom(zoom)                   // Sets the zoom
                                .bearing(myCompassView.getDirection()) // Sets the orientation of the camera
                                .tilt(90.0f)                   // Sets the tilt of the camera to 90 degrees
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
                    }
                    autoCameraMove = false;
                }

                // Ecriture de notre position dans un fichier XML
                try {
                    positionInXML();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }

                // Mise a jour de la vitesse
                text_vitesse.setText((int) vitesse + " km/h");

                // Mise a jour de l'altitude
                updateAltitudeUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Recuperation et traitement de resultats suite a une requete
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationUpdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
            // Recuperation des coordonnees de depart & arrivee
            case DESTINATION_REQUEST:
                search = true; // will disable the automatic camera movement
                if (resultCode == RESULT_OK) {
                    // Recuperation des coordonnees GPS de depart et d'arrivee
                    double lat_depart = data.getDoubleExtra("lat_depart", 0);
                    double lon_depart = data.getDoubleExtra("lon_depart", 0);
                    double lat_arrivee = data.getDoubleExtra("lat_arrivee", 0);
                    double lon_arrivee = data.getDoubleExtra("lon_arrivee", 0);
                    // Cas où position de depart automatique (notre position)
                    if (lat_depart ==0 && lon_depart ==0) {
                        lat_depart = mLastLocation.getLatitude();
                        lon_depart = mLastLocation.getLongitude();
                    }
                    // Ajout des coordonnees dans un fichier XML (pour envoyer au serveur)
                    try {
                        positionsInXML(lat_depart, lon_depart, lat_arrivee, lon_arrivee);
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }
                    // Creation des marqueurs & ajout sur la map
                    LatLng pt_depart, pt_arrivee;
                    pt_depart = new LatLng(lat_depart, lon_depart);
                    pt_arrivee = new LatLng(lat_arrivee, lon_arrivee);
                    Marker marqueur_depart = mMap.addMarker(new MarkerOptions().position(pt_depart));
                    Marker marqueur_arrivee = mMap.addMarker(new MarkerOptions().position(pt_arrivee));
                    mMarkers.add(marqueur_depart);
                    mMarkers.add(marqueur_arrivee);
                    // Creation d'une ligne reliant les marqueurs
                    PolylineOptions line = new PolylineOptions()
                            .add(pt_depart)
                            .add(pt_arrivee);
                    mMap.addPolyline(line);
                    // Centrage de la camera sur les marqueurs
                    mMap.setPadding(0,0,0,0);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : mMarkers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int padding = (int) (width * 0.10); // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);
                    // Barre (fragment) avec bouton itineraire
                    barre_itineraire.setVisibility(View.VISIBLE);
                }
        }
    }


    /**
     * Gets the current location of the device, and positions the map's camera
     */
    private void getDeviceLocation()
    {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastLocation = task.getResult();

                            // Recuperation de notre position
                            LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            LatLng Paris = new LatLng(48.864716, 2.349014);

                            // Affichage & positionnement de la camera + zoom (entre 2.0 et 21.0)
                            // + tilt (=inclinaison, entre 0 et 90)
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition(Paris, zoom, 90.0f, 0.0f)));

                            // Recuperation de la hauteur de la vue & decentrage de la camera
                            view_height = getSupportFragmentManager().findFragmentById(R.id.map).getView().getHeight();
                            mMap.moveCamera(CameraUpdateFactory.scrollBy(0,-(float)(view_height/(4))));

                            // Ecriture de notre position dans un fichier XML
                            try {
                                positionInXML();
                            } catch (TransformerException e) {
                                e.printStackTrace();
                            }

                            //System.out.println("Mon altitude : " + mLastLocation.getAltitude());
                            //mLastLocation = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            my_location_btn.setVisibility(View.GONE);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MapsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Lorsque l'application redemarre apres une mise en pause
     */
    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        // Si on ne fait pas de recherche d'itineraire => activation de la localisation temps reel
        if (search == false)
            mRequestingLocationUpdates = true;
        // Activation de la localisation temps reel
        if (mRequestingLocationUpdates && mLocationPermissionGranted) {
            startLocationUpdates();
        } else if (!mLocationPermissionGranted) {
            getLocationPermission();
        }
    }

    /**
     * Lorsque l'application est mise en pause
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Remove location updates to save battery.
        stopLocationUpdates();
    }


    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    startLocationUpdates();
                }
            }
        }
        //updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            // Desactivation de "Indoor Levels"
            mMap.setIndoorEnabled(false);
            // Desactivation de "Map Toolbar" quand on clique sur un marqueur
            mMap.getUiSettings().setMapToolbarEnabled(false);
            // Desactivation du bouton de localisation par defaut & de la boussole de google
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            my_location_btn.setVisibility(View.GONE);
            // Modification du comportement du bouton de localisation : decentrage + reactivation suivi si necessaire
            my_location_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Notre position
                    LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    // Deplacement de la camera
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition(myPos, zoom, 90.0f, 0.0f)));
                    mRequestingLocationUpdates = true;
                    startLocationUpdates();
                    // Desactivation du bouton de localisation
                    my_location_btn.setVisibility(View.GONE);
                }
            });
            // Stop location updates if we move the camera manually
            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int i) {
                if (autoCameraMove == false)
                {
                    mRequestingLocationUpdates = false;
                    // Si le bouton de localisation est desactive => on le reactive
                    if (my_location_btn.getVisibility() == View.GONE)
                        my_location_btn.setVisibility(View.VISIBLE);
                }
                }
            });
            // Gestion des permissions
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mLastLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Lorsque la connection est suspendue : affichage d'un message
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    /**
     * En cas d'echec de connexion : nouvelle tentative de connexion
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        // S'il est possible de se reconnecter => on reessaie
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        }
        // Sinon => affichage de l'erreur
        else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 1);
            dialog.show();
        }
    }

    /**
     * Action lorsqu'on clic sur un item du menu de l'application
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.container);
        // Selection d'un element du menu
        switch (item.getItemId()) {
            case R.id.nav_search:
                try {
                    Intent i = new Intent(MapsActivity.this, RechercheItineraire.class);
                    startActivityForResult(i, DESTINATION_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Fonction d'ajustement de la position et de la couleur de la barre d'altitude
     */
    public void updateAltitudeUI() {
        text_altitude.setText(altitude + " m");
        // Position
        float unite = hauteur_echelle_altitude / (required_altitude*2);
        if ( (altitude < required_altitude * 2 - 1) && (altitude > 0))
            barre_altitude.setY(initial_altitude_bar_position_y - (altitude * unite));
        // Couleur
        float diff = abs(required_altitude - altitude);
        if (diff > 5 && diff <= 10) barre_altitude.setBackgroundColor(Color.rgb(231,188,116));
        else if (diff > 10) barre_altitude.setBackgroundColor(Color.RED);
        else barre_altitude.setBackgroundColor(Color.GREEN);
    }

    /**
     * Fonction de modification manuelle d'altitude
     */
    public void modifyAltitude() {
        Button add_altitude = (Button) findViewById(R.id.add_altitude);
        // Lorsque l'altitude augmente
        add_altitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modification de l'altitude
                altitude += 1;
                // Ajustement du zoom
                if (zoom <= 21.0f && zoom > 17.0f)
                {
                    zoom -= 0.1;
                    zoom = Math.round(zoom*10);
                    zoom = zoom / 10;
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                }
                updateAltitudeUI();
            }
        });
        Button suppr_altitude = (Button) findViewById(R.id.suppr_altitude);
        // Lorsque l'altitude diminue
        suppr_altitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modification de l'altitude
                if (altitude > 0)
                    altitude -= 1;
                // Ajustement du zoom
                if (zoom >= 17.0f && zoom < 21.0f && altitude < 40)
                {
                    zoom += 0.1f;
                    zoom = Math.round(zoom*10);
                    zoom = zoom / 10;
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                }
                updateAltitudeUI();
            }
        });
    }

    /**
     * Fonction d'ecriture de notre position dans un XML
     * @throws TransformerException
     */
    public void positionInXML() throws TransformerException {
        Itineraire itineraire = new Itineraire(getApplicationContext(), getFilesDir().getAbsolutePath()+"/");
        // Ecriture de notre position dans un XML
        try {
            itineraire.addPosition((float) mLastLocation.getLatitude(), (float) mLastLocation.getLongitude(),0.f);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(itineraire.getItineraire());
    }

    /**
     * Fonction d'ecriture des positions de depart/arrivee dans un XML
     * @param lat_depart
     * @param lon_depart
     * @param lat_arrivee
     * @param lon_arrivee
     * @throws TransformerException
     */
    public void positionsInXML(double lat_depart, double lon_depart, double lat_arrivee, double lon_arrivee) throws TransformerException {
        Itineraire itineraire = new Itineraire(getApplicationContext(), getFilesDir().getAbsolutePath()+"/");
        try {
            itineraire.addPosition((float) lat_depart, (float) lon_depart,0.f);
            itineraire.addPosition((float) lat_arrivee, (float) lon_arrivee,0.f);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapsActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(itineraire.getItineraire());
    }

    /**
     * Clear map markers
     */
    private void removeMarkers() {
        for (Marker marker: mMarkers) {
            marker.remove();
        }
        mMarkers.clear();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
