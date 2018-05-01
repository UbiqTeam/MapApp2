package com.example.spencer.mapdistance;


import com.google.android.gms.maps.model.LatLng;

public class Spot {

    public LatLng ur;//upper right corner
    public LatLng ul;//upper left corner
    public LatLng lr;//lower right corner
    public LatLng ll;//lower left corner
    public int type;//0 is OffLimits, 1 is Taken, -1 is normal, 2 is road
    public int col;
    public int row;

    //lat is north south

    Spot(LatLng start){
        ul = new LatLng(start.latitude, start.longitude);
        ur = new LatLng(start.latitude , start.longitude + 0.000017145012519);
        ll = new LatLng(start.latitude - 0.00003657599883, start.longitude);
        lr = new LatLng(start.latitude - 0.00003657599883, start.longitude + 0.000017145012519);
        type = -1;
    }
}
