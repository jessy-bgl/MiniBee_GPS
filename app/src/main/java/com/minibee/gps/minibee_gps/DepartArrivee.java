package com.minibee.gps.minibee_gps;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

//import com.javacodegeeks.androidgoogleplacesautocomplete.R;

public class DepartArrivee extends Activity {

    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyCClg3TKdjO06Flg3lVM0KNnxIbvgJaGgw";

    double lat_depart = 0,lon_depart =0;
    double lat_arrivee = 0,lon_arrivee =0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address );

        // Autocompletion de Google sur les barres de recherche
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setHint("Ma position");
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));

        AutoCompleteTextView autoCompView2 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);
        autoCompView2.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));

        // Creation des listeners specifiques aux barres de recherche pour recuperer les coordonnees GPS
        OnItemClickListener ocl_depart = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String str = (String) adapterView.getItemAtPosition(i);
                try
                {
                    Geocoder geoCoder = new Geocoder( getApplicationContext() , Locale.getDefault() );

                    List<Address> addresses = geoCoder.getFromLocationName(str , 1);
                    if (addresses.size() > 0)
                    {
                        lat_depart = addresses.get ( 0 ).getLatitude ();
                        lon_depart = addresses.get ( 0 ).getLongitude ();
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        OnItemClickListener ocl_arrivee = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String str = (String) adapterView.getItemAtPosition(i);
                try
                {
                    Geocoder geoCoder = new Geocoder( getApplicationContext() , Locale.getDefault() );

                    List<Address> addresses = geoCoder.getFromLocationName(str , 1);
                    if (addresses.size() > 0)
                    {
                        lat_arrivee = addresses.get ( 0 ).getLatitude ();
                        lon_arrivee = addresses.get ( 0 ).getLongitude ();
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        // Attribution des listeners
        autoCompView.setOnItemClickListener(ocl_depart);
        autoCompView2.setOnItemClickListener(ocl_arrivee);

        // Gestion du bouton de validation => redirection avec donnees
        Button valid_search = (Button) findViewById(R.id.valid_search);
        valid_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("lat_depart", lat_depart);
                returnIntent.putExtra("lon_depart", lon_depart);
                returnIntent.putExtra("lat_arrivee", lat_arrivee);
                returnIntent.putExtra("lon_arrivee", lon_arrivee);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }

    @SuppressLint("LongLogTag")
    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:fr");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                //System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                //System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return String.valueOf ( resultList.get(index) );
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}