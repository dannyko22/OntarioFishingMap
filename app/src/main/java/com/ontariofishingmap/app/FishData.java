package com.ontariofishingmap.app;

/**
 * Created by Danny on 02/07/2014.
 */
public class FishData {

    // private variables
    int _id;
    String Name = "name";
    String Latitude = "Latitude";
    String Longitude = "Longitude";
    String Location = "Location";
    String URL = "URL";
    String Species = "Species";

    // constructor.  empty data.
    public FishData(){

    }


    public void setID(int id)
    {
        this._id = id;
    }

    public void setName(String _name)
    {
        this.Name = _name;
    }

    public void setLatitude(String _Latitude)
    {
        this.Latitude = _Latitude;
    }

    public void setLongitude(String _Longitude)
    {
        this.Longitude = _Longitude;
    }

    public void setLocation(String _Location)
    {
        this.Location = _Location;
    }

    public void setURL(String _URL)
    {
        this.URL = _URL;
    }

    public void setSpecies(String _Species)
    {
        this.Species = _Species;
    }

    public int getID()
    {
        return _id;
    }

    public String getName()
    {
        return Name;
    }

    public String getLatitude()
    {
        return Latitude;
    }

    public String getLongitude()
    {
        return Longitude;
    }

    public String getLocation()
    {
        return Location;
    }

    public String getURL()
    {
        return URL;
    }

    public String getSpecies()
    {
        return Species;
    }

}
