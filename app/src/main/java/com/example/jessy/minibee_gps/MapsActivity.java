package com.example.jessy.minibee_gps;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    /**
     * Lors de la création de l'activité (la classe)
     * Initialisation des parametres : view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Partie obligatoire, fournie par Google pour l'affichage de la map */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /* ------ */

        // Barre de navigation en bas de l'ecran
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.toolbar);

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
            // Ajout d'un marqueur sur notre position
            //mMap.addMarker(new MarkerOptions().position(myPos).title("Me"));
            // Affichage & positionnement de la camera + zoom (entre 2.0 et 21.0)
            // + tilt (=inclinaison, entre 0 et 90)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(Paris, 18.0f, 90.0f, 0.0f)));
            //System.out.println("Mon altitude : " + mLastLocation.getAltitude());
            //mLastLocation = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
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
}
