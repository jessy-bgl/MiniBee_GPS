package com.minibee.gps.minibee_gps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
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
import android.widget.ImageButton;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    // Google Map (objet)
    private GoogleMap mMap;
    // Google API
    private GoogleApiClient mGoogleApiClient;
    // Hauteur de la vue
    private int view_height;
    // Position de la camera
    private CameraPosition mCameraPosition;

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

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

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


    private static String xmlFile;
    private DocumentBuilderFactory factory;




    /**
     * Lors de la création de l'activité (la classe)
     * Initialisation des parametres : view
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

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mRequestingLocationUpdates = true;
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Bouton menu de la barre de navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.toolbar);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Bouton "interface boutons"
        ImageButton buttons = (ImageButton)findViewById(R.id.buttons);
        buttons.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                System.out.println("OK !!!!!!!!!!!!!!!!!!!!");
                try {
                    Intent i = new Intent(MapsActivity.this, ButtonsInterface.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        // Creation de l'API client pour acceder aux services Google Play
        buildGoogleAPIClient();
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

        // Customise the styling of the base map using a JSON object defined in a raw resource file
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        //met un marqueur sur paris
        /*LatLng paris = new LatLng(48.864716, 2.349014);
        googleMap.addMarker(new MarkerOptions().position(paris)
                .title("marqueur de PARIS EST MAGIQUE"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(paris));*/

        // Prompt the user for permission
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map
        updateLocationUI();

        // Get the current location of the device and set the position to the map
        //getDeviceLocation(); // locate 1 time
        startLocationUpdates();
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

                mLastLocation = locationResult.getLastLocation();

                // Recuperation de notre position
                LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                LatLng Paris = new LatLng(48.864716, 2.349014);

                // Affichage & positionnement de la camera + zoom (entre 2.0 et 21.0)
                // + tilt (=inclinaison, entre 0 et 90)
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(myPos, 18.0f, 90.0f, 0.0f)));

                // Recuperation de la hauteur de la vue & decentrage de la camera
                view_height = getSupportFragmentManager().findFragmentById(R.id.map).getView().getHeight();
                mMap.moveCamera(CameraUpdateFactory.scrollBy(0,-(float)(view_height/(4))));

                // Ecriture de notre position dans un fichier XML
                try {
                    positionInXML();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
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
                                    new CameraPosition(Paris, 18.0f, 90.0f, 0.0f)));

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
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates && mLocationPermissionGranted) {
            startLocationUpdates();
        } else if (!mLocationPermissionGranted) {
            getLocationPermission();
            //requestPermissions();
        }
    }

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
        updateLocationUI();
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
            // Desactivation de "Map Toolbar" quand on clic sur un marqueur
            mMap.getUiSettings().setMapToolbarEnabled(false);
            // Modification du comportement du bouton de localisation : decentrage
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
                @Override
                public boolean onMyLocationButtonClick()
                {
                    // Notre position
                    LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    // Deplacement de la camera
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition(myPos, 18.0f, 90.0f, 0.0f)));
                    mMap.moveCamera(CameraUpdateFactory.scrollBy(0,-(float)(view_height/(4))));
                    startLocationUpdates();
                    return true;
                }
            });

            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
     * Action lorsqu'on clic sur un item selectionnable dans l'application
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.container);
        // Bouton menu : afficher/Cacher le menu
        if (item.getItemId() == R.id.toolbar_menu)
        {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
        }
        // Selection d'un element du menu
        else {
            switch (item.getItemId()) {
                case R.id.nav_camera:
                    break;
                case R.id.nav_gallery:
                    break;
                case R.id.nav_slideshow:
                    break;
                case R.id.nav_manage:
                    break;
            }
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

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

        //Itineraire.getItineraire();
    }


}
