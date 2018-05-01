package com.example.spencer.mapdistance;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

//comment code
//fix clear

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    TextView areaText;              //display area
    TextView markerCountText;       //display marker count
    TextView lengthsText;           //displays lengths between markers
    TextView coordinatesText;       //displays coordinates of markers
    Polyline mPolyline;
    Polygon area;
    Button clear;                   //clears map
    Button drop;                    //drops GPS marker
    Button setMarker;               //sets marker position
    Button setMarkerGPS;            //sets GPS marker
    Button btnOrder;                //puts markers in order
    Button btnEditSpots;
    Button btnCreateTaken;
    Button btnCreateRoad;
    Button btnOffLimits;
    Button btnMarkers;
    Button btnConvert;
    Button btnMakePath;
    RadioGroup modeToggle;
    RadioButton manToggle;
    RadioButton gpsToggle;
    boolean isModeMan = true;
    boolean placeMarkers = true;

    //columns and rows of lot
    int col = 0;
    int row = 0;

    private static final int TAG_CODE_PERMISSION_LOCATION = 0;

    private GoogleMap mMap;

    LocationManager locationManager;
    Location location;

    List<Marker> markerList = new ArrayList<>();//keeps track of marksers

    final List<Marker> markers = new ArrayList<>();                     //list of markers on the map
    final List<MarkerOptions> markerOptionsList = new ArrayList<>();    //list of options for each marker
    final List<Double> distances = new ArrayList<>();                   //list of distances between markers in meters
    final List<LatLng> coorList = new ArrayList<>();                    //list of marker coordinates

    double totalArea = 1.0;
    Marker marker = null;

    List<Spot> spotArray = new ArrayList<>();
    ArrayList<Polygon> polyList = new ArrayList<>();

    //edit parking spots
    //boolean editPark;//edit parking spots
    boolean editOffLimits = false;//edit off limits spots
    boolean editTaken = false;//edit taken spots
    boolean editRoad = false;
    boolean editMarkers = false;

    List<Marker> dragMarkers = new ArrayList<>();

    public void handleMarkers(MarkerOptions markerOptions, Marker marker) {             //handles distance between markers, line drawing, and adds marker to the map

        markers.add(marker);
        markerList.add(marker);
        mPolyline = mMap.addPolyline(new PolylineOptions().geodesic(true));
        markerCountText.setText("Marker Count: " + coorList.size());


        if (markers.size() == 1) {                                  //no information with just one marker
            mMap.clear();
        }

        if (markers.size() == 4) {
            for(int i = 0; i < markers.size()-1; i++ ){
                double distance = SphericalUtil.computeDistanceBetween(markers.get(i).getPosition(), markers.get(i+1).getPosition());  //for markers 1-4 find distance & area
                distances.add(distance);
            }
            double distance = SphericalUtil.computeDistanceBetween(markers.get(3).getPosition(), markers.get(0).getPosition());     //distance between 1 and 4
            distances.add(distance);
            area = mMap.addPolygon(new PolygonOptions().add(markers.get(0).getPosition(), markers.get(1).getPosition(), markers.get(2).getPosition(),
                    markers.get(3).getPosition()).strokeColor(Color.BLACK));


            //lengthsText.setText("lengths: " + distances.get(0) + ", " + distances.get(1) + ", " + distances.get(2) + ", " + distances.get(3));a
            markers.clear();                                                                //clear markers from list
            totalArea = computeArea(distances);                                             //computer area based on distances list
            String areaSTR = String.format("%.2f", totalArea);
            coordinatesText.setText(coorList.toString());
            distances.clear();                                                              //clear distances list
            coorList.clear();                                                               //clear coordinates list
            areaText.setText("Area (sq m): " + areaSTR);   //using a string to trunc to 2 decs
        } else {
            //lengthsText.setText("");
        }

        mMap.addMarker(markerOptions);                                                      //add marker through markerOptions to map


    }

    public double computeArea(List<Double> distances) {
        BigDecimal tmp = new BigDecimal(0);
        BigDecimal TWO = new BigDecimal(2);

        double a = distances.get(0);
        double b = distances.get(1);
        double c = distances.get(2);
        double d = distances.get(3);

        for (Double val : distances) {
            tmp = tmp.add(new BigDecimal(val.intValue()));
        }

        double s = tmp.divide(TWO).doubleValue();
        return Math.sqrt((s - a) * (s - b) * (s - c) * (s - d));
    }

    void setupMap(){
        areaText = findViewById(R.id.areaText);
        markerCountText = findViewById(R.id.markerCountText);
        coordinatesText = findViewById(R.id.coordinatesText);
        lengthsText = findViewById(R.id.lengthsText);
        clear = findViewById(R.id.clearButton);
        btnOrder = findViewById(R.id.btnOrder);
        btnEditSpots = findViewById(R.id.btnEditSpots);
        btnCreateRoad = findViewById(R.id.btnCreateRoad);
        btnCreateTaken = findViewById(R.id.btnCreateTaken);
        btnOffLimits = findViewById(R.id.btnOffLimits);
        btnMarkers = findViewById(R.id.btnMarkers);
        btnConvert = findViewById(R.id.btnConvert);
        btnMakePath = findViewById(R.id.btnMakePath);
        drop = findViewById(R.id.drop);
        setMarker = findViewById(R.id.setMarkerButton);
        setMarkerGPS = findViewById(R.id.setMarkerGPS);
        modeToggle = findViewById(R.id.toggle);
        manToggle = findViewById(R.id.manToggle);
        gpsToggle = findViewById(R.id.gpsToggle);
        //markerCountText.setText("Marker Count: " + markers.size());
        //areaText.setText("Area (sq m): ");
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) ==
                            PackageManager.PERMISSION_GRANTED){
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET},
                    TAG_CODE_PERMISSION_LOCATION);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));  //move camera to current GPS location

        areaText.setTextColor(Color.RED);
        markerCountText.setTextColor(Color.RED);
        lengthsText.setTextColor(Color.RED);
        coordinatesText.setTextColor(Color.RED);

        setMarker.setEnabled(false);
        clear.setEnabled(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final MarkerOptions markerOptions = new MarkerOptions();

        setupMap();

        modeToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == R.id.manToggle) {
                    isModeMan = true;
                    Log.d("manual", "toggle");
                    drop.setVisibility(View.GONE);
                    setMarker.setVisibility(View.VISIBLE);
                    setMarkerGPS.setVisibility(View.GONE);
                }
                if(i == R.id.gpsToggle){
                    isModeMan = false;
                    if (!isModeMan) {
                        drop.setVisibility(View.VISIBLE);
                        setMarker.setVisibility(View.GONE);
                        setMarkerGPS.setVisibility(View.GONE);
                        clear.setEnabled(true);

                        //////////////////////// GPS Mode ///////////////////////////////////

                        drop.setOnClickListener(new View.OnClickListener() {                    //drop GPS marker

                            public void onClick(View view) {
                                if (!isModeMan) {
                                    try {
                                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    } catch (SecurityException e) {
                                        markerCountText.setText("no GPS permission");
                                    }
                                    Log.d("GPS", "drop");
                                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());         //get GPS lat/long
                                    markerOptions.position(newLatLng);
                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(true));  //create marker

                                    drop.setVisibility(View.GONE);          //switches drop marker to lock marker
                                    setMarkerGPS.setVisibility(View.VISIBLE);

                                    gpsToggle.setEnabled(false);                 //disable mode switch after gps marker drop, but before locking it
                                    manToggle.setEnabled(false);                 //(causes some issues if user changes mode with un-set marker)
                                }

                            }
                        });

                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {     //drag


                            @Override
                            public void onMarkerDragStart(Marker marker) {

                            }

                            @SuppressWarnings("unchecked")
                            @Override
                            public void onMarkerDragEnd(Marker arg0) {      //set new position when done dragging
                                Log.d("GPS", "drag");
                                if(editMarkers == true) {
                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                                    LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                                    markerOptions.position(newLatLng);

                                }else{
                                    Log.d("GPS", "zz");
                                    marker.setPosition(arg0.getPosition());
                                }
                            }

                            @Override
                            public void onMarkerDrag(Marker arg0) {
                            }

                        });

                        setMarkerGPS.setOnClickListener(new View.OnClickListener() {  //lock GPS marker
                            public void onClick(View view) {
                                if (!isModeMan) {
                                    Log.d("GPS", "setMarker");
                                    modeToggle.setEnabled(true);
                                    LatLng tmp = markerOptions.getPosition();
                                    coorList.add(tmp);
                                    markerOptionsList.add(markerOptions);
                                    handleMarkers(markerOptions, marker);
                                    drop.setVisibility(View.VISIBLE);
                                    setMarkerGPS.setVisibility(View.GONE);

                                    gpsToggle.setEnabled(true);                        //disables changing mode before setting marker
                                    manToggle.setEnabled(true);
                                }

                            }
                        });
                    } else {
                        Log.d("MODE", "manual");
                        drop.setVisibility(View.GONE);
                        setMarker.setVisibility(View.VISIBLE);
                        setMarkerGPS.setVisibility(View.GONE);
                    }
                }
            }
        });


        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //clears the map & initializes vals

                mMap.clear();
                markers.clear();
                clear.setEnabled(false);
                //might need to tweek
                spotArray.clear();
                polyList.clear();
                dragMarkers.clear();
                markerList = new ArrayList<Marker>();
                row = 0;
                col = 0;
                nMarkers.clear();
                //markerCountText.setText("Marker Count: " + markers.size());
                //areaText.setText("Area (sq m): ");
                areaText.setText("");
                lengthsText.setText(" ");
                coordinatesText.setText(" ");
                distances.clear();
                coorList.clear();
                totalArea = 1;
                markerList.clear();
                if (!isModeMan) {
                    Log.d("GPS","clear");
                    drop.setVisibility(View.VISIBLE);          //clear in GPS mode reverts lock to drop
                    setMarkerGPS.setVisibility(View.GONE);
                    setMarker.setVisibility(View.GONE);
                    clear.setEnabled(true);
                } else {
                    Log.d("manual","clear");
                    drop.setVisibility(View.GONE);
                    setMarker.setVisibility(View.VISIBLE);
                    setMarker.setEnabled(false);            //disables setMarker when no marker is on screen
                    setMarkerGPS.setVisibility(View.GONE);
                }
                modeToggle.setEnabled(true);
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //clears the map & initializes vals
            order();;
            createSquare();
            createNewMarkers();
            drawLot();
                btnEditSpots.setVisibility(View.VISIBLE);
            }
        });

        btnEditSpots.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //clears the map & initializes vals
                if(placeMarkers == true){
                    Log.d("Edit on", "on");
                    placeMarkers = false;
                    btnMarkers.setVisibility(View.VISIBLE);
                    btnCreateTaken.setVisibility(View.VISIBLE);
                    btnCreateRoad.setVisibility(View.VISIBLE);
                    btnOffLimits.setVisibility(View.VISIBLE);
                    btnMakePath.setVisibility(View.VISIBLE);
                }else{
                    Log.d("Edit off", "off");
                    placeMarkers = true;
                    editOffLimits = false;//edit off limits spots
                    editTaken = false;//edit taken spots
                    editRoad = false;
                    btnMarkers.setVisibility(View.INVISIBLE);
                    btnCreateTaken.setVisibility(View.INVISIBLE);
                    btnCreateRoad.setVisibility(View.INVISIBLE);
                    btnOffLimits.setVisibility(View.INVISIBLE);
                    btnConvert.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnOffLimits.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //sets boolean for creating parking spots
                if(editOffLimits == true){
                    editOffLimits = false;
                }else{
                    editOffLimits = true;
                    editTaken = false;//edit taken spots
                    editRoad = false;

                }
            }
        });

        btnCreateTaken.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //sets boolean for taken spots
                if(editTaken == true){
                    Log.d("Edit on", "on");
                    editTaken = false;
                }else{
                    Log.d("Edit off", "off");
                    editTaken = true;
                    editOffLimits = false;//edit off limits spots
                    editRoad = false;
                }
            }
        });

        btnCreateRoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //allows user to create a road
                if(editRoad == true){
                    Log.d("Edit on", "on");
                    editRoad = false;
                }else{
                    Log.d("Edit off", "off");
                    editRoad = true;
                    editOffLimits = false;//edit off limits spots
                    editTaken = false;//edit taken spots
                }
            }
        });

        btnMarkers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //allows user to edit multiple spots
                if(editMarkers == true){
                    //Log.d("Edit on", "off");
                    editMarkers = false;
                }else{
                    //Log.d("Edit off", "on");
                    Marker m1 = mMap.addMarker(new MarkerOptions().position(new LatLng(markerList.get(0).getPosition().latitude, markerList.get(0).getPosition().longitude)).draggable(true));
                    Marker m2 = mMap.addMarker(new MarkerOptions().position(new LatLng(markerList.get(1).getPosition().latitude, markerList.get(1).getPosition().longitude)).draggable(true));

                    m1.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    m2.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                    btnConvert.setVisibility(View.VISIBLE);

                    dragMarkers.add(m1);
                    dragMarkers.add(m2);
                    editMarkers = true;
                    editRoad = false;
                    editOffLimits = false;//edit off limits spots
                    editTaken = false;//edit taken spots
                }
            }
        });

        btnConvert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //converts spots inbetween markers
                double longLeft = 0;
                double longRight = 0;
                if(dragMarkers.get(0).getPosition().longitude > dragMarkers.get(1).getPosition().longitude){
                    longLeft = dragMarkers.get(1).getPosition().longitude;
                    longRight = dragMarkers.get(0).getPosition().longitude;
                }else{
                    longLeft = dragMarkers.get(0).getPosition().longitude;
                    longRight = dragMarkers.get(1).getPosition().longitude;
                }

                for (int x = 0; x < spotArray.size(); x++){


                    if(spotArray.get(x).ul.latitude > dragMarkers.get(0).getPosition().latitude && spotArray.get(x).ll.latitude < dragMarkers.get(1).getPosition().latitude){
                        polyList.get(x).setFillColor(Color.TRANSPARENT);
                        spotArray.get(x).type = 2;
                    }
                    if(spotArray.get(x).ul.longitude < longLeft && spotArray.get(x).ur.longitude > longRight){
                        Log.d("Convert", x + " " + spotArray.get(x).ul.longitude);
                        Log.d("Convert", x + " " + spotArray.get(x).ur.longitude);
                        Log.d("Convert", x + " " + dragMarkers.get(0).getPosition().longitude);
                        polyList.get(x).setFillColor(Color.TRANSPARENT);
                        spotArray.get(x).type = 2;
                    }
                }
            }
        });

        btnMakePath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //creates a generic path that may not be optimal
                for(int i = 0; i < spotArray.size(); i++) {
                    if(spotArray.get(i).row % 3 == 0){
                        polyList.get(i).setFillColor(Color.TRANSPARENT);
                        spotArray.get(i).type = 2;
                    }


                    if(spotArray.get(i).col % 6 == 0){
                        polyList.get(i).setFillColor(Color.TRANSPARENT);
                        spotArray.get(i).type = 2;
                        polyList.get(i + 1).setFillColor(Color.TRANSPARENT);
                        spotArray.get(i + 1).type = 2;
                    }

                }
            }
        });

        //////////////////////////////// Manual Mode ////////////////////////////////////////

        mMap.setOnMapClickListener(new OnMapClickListener() {
            Boolean button = false;         //disables onClick to set new latlng after a marker has been dropped, but before it's locked (causes crash otherwise see issue #1)
            Marker marker = null;

            @Override
            public void onMapClick(final LatLng latLng) {

                //add if for marker and spots
                if(placeMarkers == true) {
                    if (!button) {                                                //prevent issue #1
                        if (isModeMan) {                                  //if in manual mode
                            Log.d("manual", "mapClick");
                            final List<Marker> tmpMarkers = new ArrayList<>();
                            final MarkerOptions markerOptions = new MarkerOptions();
                            gpsToggle.setEnabled(false);                        //disables changing mode before setting marker
                            manToggle.setEnabled(false);

                            setMarker.setEnabled(true);                          //enables setMarker when marker is on screen
                            clear.setEnabled(false);                             //prevents deleting marker before setting causing bug
                            if (!button) {                                       //prevents onClick input while manipulating marker (issue #1)
                                markerOptions.position(latLng);
                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).draggable(true));  //create marker
                                button = true;                                      //since marker is created onClick is re-enabled
                            }

                            if (isModeMan) {
                                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {     //drag
                                    @Override
                                    public void onMarkerDragStart(Marker marker) {
                                        button = false;
                                    }

                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public void onMarkerDragEnd(Marker arg0) {                  //set new position when done dragging
                                        if (isModeMan) {
                                            if(placeMarkers == true) {
                                                Log.d("manual", "dragg");
                                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                                                LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                                                markerOptions.position(newLatLng);
                                                button = true;
                                            }else{
                                                Log.d("man", "zx");
                                                marker.setPosition(arg0.getPosition());

                                            }

                                        }

                                    }

                                    @Override
                                    public void onMarkerDrag(Marker arg0) {
                                    }

                                });
                            }

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {           //remove a marker
                                    if(placeMarkers == true) {
                                        Log.d("manual", "remove");  //GPS remove only removes marker not entry
                                        button = false;
                                        marker.remove();
                                        return true;
                                    }
                                    return  false;
                                }
                            });


                            setMarker.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {                //lock a marker in place
                                    if (button) {
                                        Log.d("manual", "setMarker");
                                        modeToggle.setEnabled(true);
                                        tmpMarkers.add(marker);
                                        markerOptionsList.add(markerOptions);
                                        LatLng tmp = markerOptions.getPosition();
                                        coorList.add(tmp);
                                        setMarker.setEnabled(false);                          //diables setMarker when no marker is on screen
                                        clear.setEnabled(true);
                                        handleMarkers(markerOptions, marker);
                                        button = false;


                                        gpsToggle.setEnabled(true);
                                        manToggle.setEnabled(true);
                                    }

                                }
                            });
                        }
                    }
                }else{
                    editSpots(latLng);
                }
            }
        });


    }

    //////////////////////////////// Place Parking Spots ////////////////////////////////////////

    Spot s;
    Spot lSpot;
    LatLng lot;
    double lotArea;

    double[] latArray = new double[4];
    double[] longArray = new double[4];
    int[] pos = new int[4];//order is top left, top right, bottom right, bottom left
    //check to make sure positions are accurate

    void editSpots(LatLng latLng){//allows editing of spots and allows user to change their types
        //for loop for all the spots
        Log.d("???: ", spotArray.size() + "");
        for(int x = 0; x < spotArray.size(); x++){
            if(spotArray.get(x).ul.latitude > latLng.latitude && spotArray.get(x).ll.latitude < latLng.latitude && spotArray.get(x).ul.longitude < latLng.longitude && spotArray.get(x).ur.longitude > latLng.longitude){
                //x+= width road;
                Log.d("Spot: ", x + " " + spotArray.get(x).col + " " + spotArray.get(x).row);

                if (editRoad) {//create roads
                    if (spotArray.get(x).type == 2) {
                        polyList.get(x).setFillColor(Color.BLUE);
                        spotArray.get(x).type = -1;
                    } else {
                        polyList.get(x).setFillColor(Color.TRANSPARENT);
                        spotArray.get(x).type = 2;
                    }
                } else if (editTaken == true) {//Mark space as taken
                    if (spotArray.get(x).type != 1) {
                        polyList.get(x).setFillColor(Color.GREEN);
                        spotArray.get(x).type = 1;
                    } else {
                        polyList.get(x).setFillColor(Color.BLUE);
                        spotArray.get(x).type = -1;
                    }
                }else if(editOffLimits){//make space off limits
                    if (spotArray.get(x).type != 0) {
                        polyList.get(x).setFillColor(Color.RED);
                        spotArray.get(x).type = 0;
                    } else {
                        polyList.get(x).setFillColor(Color.BLUE);
                        spotArray.get(x).type = -1;
                    }
                }else{
                    polyList.get(x).setFillColor(Color.BLUE);
                    spotArray.get(x).type = -1;
                }
            }
        }

    }

    void order(){//put markers in order
        Log.d("marklist", markerList.size() + " ");
        //Spot sspp = new Spot(markerList.get(0).getPosition());
        //drawSpot(sspp);
        int setInvis = markerList.size();

        markerList.get(setInvis- 4).setVisible(false);
        markerList.get(setInvis- 3).setVisible(false);
        markerList.get(setInvis- 2).setVisible(false);
        markerList.get(setInvis- 1).setVisible(false);

        for(int x = 0; x < 4; x++){
            latArray[x] = markerList.get(x).getPosition().latitude;
            longArray[x] = markerList.get(x).getPosition().longitude;
            pos[x] = x;
        }

        //puts in order by latitude
        for(int x = 0; x < 4; x++){
            for(int i = 0; i < 4; i++){
                if(latArray[x] < latArray[i]){
                    double tempLat;
                    int  tempPos;
                    double tempLong;

                    tempPos = pos[x];
                    tempLat = latArray[x];
                    tempLong = longArray[x];

                    latArray[x] = latArray[i];
                    pos[x] = pos[i];
                    longArray[x] = longArray[i];

                    latArray[i] = tempLat;
                    pos[i] = tempPos;
                    longArray[i] = tempLong;
                }
            }
        }

        //detemines top left and top right
        if(longArray[0] > longArray[1]){//if true then switch
            double tempLat;
            int  tempPos;
            double tempLong;

            tempLat = latArray[0];
            tempPos = pos[0];
            tempLong = longArray[0];

            latArray[0] = latArray[1];
            pos[0] = pos[1];
            longArray[0] = longArray[1];

            latArray[1] = tempLat;
            pos[1] = tempPos;
            longArray[1] = tempLong;
        }

        //detemines bottom left and bottom right
        if(longArray[2] > longArray[3]){//if true then switch
            double tempLat;
            int  tempPos;
            double tempLong;

            tempLat = latArray[2];
            tempPos = pos[2];
            tempLong = longArray[2];

            latArray[2] = latArray[3];
            pos[2] = pos[3];
            longArray[2] = longArray[3];

            latArray[3] = tempLat;
            pos[3] = tempPos;
            longArray[3] = tempLong;
        }

        //the previous switches are wrong so we will switch them again
        //switch latarray, longarray, and position

        double tempLat;
        double tempLong;
        int tempPos;

        //switch 0 and 2
        tempLat = latArray[0];
        tempLong = longArray[0];
        tempPos = pos[0];
        latArray[0] = latArray[2];
        longArray[0] = longArray[2];
        pos[0] = pos[2];

        latArray[2] = tempLat;
        longArray[2] = tempLong;
        pos[2] = tempPos;

        //switch 1 and 3
        tempLat = latArray[1];
        tempLong = longArray[1];
        tempPos = pos[1];
        latArray[1] = latArray[3];
        longArray[1] = longArray[3];
        pos[1] = pos[3];

        latArray[3] = tempLat;
        longArray[3] = tempLong;
        pos[3] = tempPos;

    }



    void createSquare(){//changes polygon into a square by lining up the edges helps if object is drawn like square
        Log.d("Highest bef Top Left:", latArray[0] + " Top Right: " + latArray[1]);
        //take the highest marker and align it with top left or top right
        if(latArray[0] > latArray[1]){
            latArray[0] = latArray[1];
        }else{
            latArray[1] = latArray[0];
        }


        Log.d("Highest After Top Left:", latArray[0] + " Top Right: " + latArray[1]);


        //Align Lower markers
        if(latArray[2] < latArray[3]){//if this doesn't work > might need to be <
            latArray[2] = latArray[3];
        }else{
            latArray[2] = latArray[3];
        }

        //take the west most marker and align it with top left or bottom left
        if(longArray[0] < longArray[2]){//if this doesn't work > might need to be <
            longArray[0] = longArray[2];
        }else{
            longArray[2] = longArray[0];
        }

        //take the east most marker and align it with bottom right or top right
        if(longArray[1] > longArray[3]){//if this doesn't work > might need to be <
            latArray[1] = latArray[3];
        }else{
            longArray[3] = longArray[1];
        }
    }

    List<Marker> nMarkers = new ArrayList<>();//order topleft/ top right/ bottom left/ bottom right

    //Markers in order
    void createNewMarkers(){
        //GREEN IS TOP LEFT 2
        //PURPLE IS TOP RIGHT 3
        //ORANGE IS BOTTOM LEFT 0
        //BLUE IS BOTTOM RIGHT 1
        // YOU NEED TO  SWITCH THEM
        //SWITCH 0 WITH 2
        //now orange is top left green is bottom left
        //this is correct
        //SWITCH 1 WITH 3
        //now blue is top right
        //purple is bottom righ
        //this is correct

        //int markerArraySize = nMarkers.size();

        nMarkers.add(mMap.addMarker(new MarkerOptions()//should be top left
                .position(new LatLng( latArray[0],longArray[0]))
                //.title("This is my title")
                //.snippet("and snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));

        nMarkers.add(mMap.addMarker(new MarkerOptions()//should be top right
                .position(new LatLng( latArray[1],longArray[1]))
                //.title("This is my title")
                //.snippet("and snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

        nMarkers.add(mMap.addMarker(new MarkerOptions()//should be bottom left
                .position(new LatLng( latArray[2],longArray[2]))
                //.title("This is my title")
                //.snippet("and snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

        nMarkers.add(mMap.addMarker(new MarkerOptions()//should be bottom right
                .position(new LatLng( latArray[3],longArray[3]))
                //.title("This is my title")
                //.snippet("and snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))));

        nMarkers.get(0).setVisible(false);
        nMarkers.get(1).setVisible(false);
        nMarkers.get(2).setVisible(false);
        nMarkers.get(3).setVisible(false);
    }

    void drawSpot(Spot s){
        PolygonOptions rectOptions = new PolygonOptions()

                .add(new LatLng(s.ul.latitude, s.ul.longitude))
                .add(new LatLng(s.ll.latitude, s.ll.longitude))
                .add(new LatLng(s.lr.latitude, s.lr.longitude))
                .add(new LatLng(s.ur.latitude, s.ur.longitude))
                .add(new LatLng(s.ul.latitude, s.ul.longitude));

        rectOptions.fillColor(Color.BLUE);

        polyList.add(mMap.addPolygon(rectOptions));

    }

    void drawLot(){//draws lot and populates lot with spots
        //double spotArea = 0;//Area withing markers
        LatLng currentLatLng = new LatLng(latArray[0],longArray[0]);
        Spot leftSpot = new Spot(currentLatLng);
        int numSpots = 0;
        //parking lot bounds
        //double longEnd = latArray[3];
        double longBound = longArray[1];//boud of longitude
        double latBound = 0;//boud of latitude
        if(latArray[2] < latArray[3]){
            latBound = latArray[2];
        }else{
            latBound = latArray[3];
        }
        double bottomLat = currentLatLng.latitude;

        Log.d("CurrentLat: ", currentLatLng.latitude + "");
        Log.d("bound: ", latBound + "");
        //calculate and draw number of spaces in the future longitude bounds should update with each row
        while(currentLatLng.latitude > latBound && bottomLat > latBound){
            Log.d("spot: ", numSpots + "");
            Spot n = new Spot(currentLatLng);
            n.col = col;
            col++;
            //stop it when it goes past longitude bounds
            Log.d("CurrentLong", n.ur.longitude +"");

            if(n.ur.longitude > longBound){//if the spot is outside of the longitude bounds
                row++;
                col = 0;
                currentLatLng = new LatLng(leftSpot.ll.latitude, leftSpot.ll.longitude);
                n = new Spot(leftSpot.ll);
                leftSpot = new Spot(n.ul);
                currentLatLng = n.ur;
                bottomLat = n.ll.latitude;
                n.row = row;
                n.col = col;
                col++;
            }else {

                currentLatLng = n.ur;
                bottomLat = n.ll.latitude;
                Log.d("ur", n.ur.longitude + "");

                n.row = row;

            }

            if(n.ll.latitude > latBound) {
                Log.d("Draw", n.ul.longitude + "");
                drawSpot(n);
                spotArray.add(n);
                numSpots++;
                //spotArea += 162;
            }
        }
        col = spotArray.size()/row;
        //Log.d("COl: ", col + "");
        //Log.d("ROW: ", row + "");
        //Log.d("ROW: ", spotArray.size() +"");
    }


}