package com.android.elliotmiller.week3appem1974;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.InfoWindowAdapter {

    private static final int PERMISSIONS_REQUEST_LOCATION = 9;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private MarkerOptions markerOptions;
    private Marker mapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        this.setupLocationConfig();
    }

    private void setupLocationConfig() {
        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                ) {
            requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSIONS_REQUEST_LOCATION);
            Toast.makeText(MainActivity.this, "You need to have Location Permission enabled.", Toast.LENGTH_LONG).show();
            finish();
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        // Got last known location. In some rare situations this can be null.
                        if (lastLocation != null && markerOptions == null) {
                            // ...
                            Toast.makeText(MainActivity.this, "Location " + lastLocation, Toast.LENGTH_SHORT).show();
                            LatLng ll = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            markerOptions = new MarkerOptions().position(ll).title("I'm here");
                            mapMarker = mMap.addMarker(markerOptions);
                        }
                    }
                });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        for (Location location : locationResult.getLocations()) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (markerOptions == null) {
                                markerOptions = new MarkerOptions().position(currentLocation).title("I'm here");
                                mapMarker = mMap.addMarker(markerOptions);
                            } else {
                                mapMarker.setPosition(currentLocation);
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        }
                    }

                    ;
                },
                null /* Looper */
        );
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(this);
    }

    // Menu configuration
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_normal: {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.menu_hybrid: {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            }
            case R.id.menu_satellite: {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }
            case R.id.menu_terrain: {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            }
            default: {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.map_info_window, null);
        LatLng ll = marker.getPosition();
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(ll.latitude, ll.longitude, 1);
                Log.e("Tag", "Addresses:" + addresses);
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getAddressLine(1);
                    String country = addresses.get(0).getCountryName();
                    String zipCode = addresses.get(0).getPostalCode();
                    ((TextView) view.findViewById(R.id.tv_address)).setText("Address: " + address);
                    ((TextView) view.findViewById(R.id.tv_city)).setText("City: " + city);
                    ((TextView) view.findViewById(R.id.tv_country)).setText("Country: " + country);
                    ((TextView) view.findViewById(R.id.tv_zipcode)).setText("Zip Code: " + zipCode);
                }
            } catch (IOException e) {
                Log.e("Tag", "Exception:" + e.toString());
            }
        } else {
            Toast.makeText(this, "Geocoding is not present on this device", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {

                // If the request is cancelled, the result array will be empty (0)//
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "You need to have Location Permission enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }
}
