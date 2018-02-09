package com.example.jessy.minibee_gps;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentContainer;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.pm.PackageManager; // Pour la gestion de la permission de localisation
import android.location.Location;
import android.support.v4.content.ContextCompat; // Pour la gestion de la permission de localisation
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private static final int CONNECTION_RESOLUTION_REQUEST = 2;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location mLastLocation;
    private int view_height;

    /**
     * Lors de la création de l'activité (la classe)
     * Initialisation des parametres : view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Partie fournie par Google pour l'affichage de la map */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /* ------ */

        // Barre de navigation en bas de l'ecran
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.toolbar);
        // Gestion des evenements (lorsqu'on clic sur le bouton menu)
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            // Afficher/Cacher le menu
                            case R.id.toolbar_menu:
                                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.container);
                                if (drawer.isDrawerOpen(GravityCompat.START)) {
                                    drawer.closeDrawer(GravityCompat.START);
                                } else {
                                    drawer.openDrawer(GravityCompat.START);
                                }
                                break;
                        }
                        return true;
                    }
                });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                        return true;
                    }
                });

        // Creation de l'API client pour acceder aux services Google Play
        buildGoogleAPIClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        buildGoogleAPIClient();
    }

    /**
     * Creation de l'API client, necessaire pour utiliser certains services
     * Google Play, notamment la geolocalisation
     */
    private void buildGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Lors de la connection au service
     * => lancement de la fonction de geolocalisation
     * et d'affichage de notre position
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        findLocation();
    }

    /**
     * Demarrage
     */
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * Deconnexion
     */
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Fonction fournie par Google
     * Initialisation de la map lors du chargement
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
            // Sinon => affichage de l'erreur
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 1);
            dialog.show();
        }
    }

    /**
     * Si la requete de connexion reussi => connexion aux services Google
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECTION_RESOLUTION_REQUEST && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Si la permission est accordée => lancement de la fonction de
     * localisation & d'affichage de la map
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findLocation();
                }
            }
        }
    }

    /**
     * Apres obtention de la permission => recuperation de notre geolocalisation
     * & affichage de notre position (centrage camera + zoom)
     */
    private void findLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
        } else {
            // Desactivation de "Indoor Levels"
            mMap.setIndoorEnabled(false);

            // Desactivation de "Map Toolbar" quand on clic sur un marqueur
            mMap.getUiSettings().setMapToolbarEnabled(false);

            // Ajout du bouton "My Location"
            mMap.setMyLocationEnabled(true);

            // Recuperation de notre position
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            LatLng Paris = new LatLng(48.864716, 2.349014);

            // Affichage & positionnement de la camera + zoom (entre 2.0 et 21.0)
            // + tilt (=inclinaison, entre 0 et 90)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(myPos, 18.0f, 90.0f, 0.0f)));

            // Recuperation de la hauteur de la vue & decentrage de la camera
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            view_height = mapFragment.getView().getHeight();
            mMap.moveCamera(CameraUpdateFactory.scrollBy(0,-(float)(view_height/(4))));

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
                    return true;
                }
            });

            //System.out.println("Mon altitude : " + mLastLocation.getAltitude());
            //mLastLocation = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
        }
    }


}
