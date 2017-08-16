package com.idbcgroup.canopyverde;

import android.widget.ImageView;

import java.sql.Date;

public class GreenPoint {

    private int id;
    private String location;
    private Date date;
    private int canopySize;
    private int stemSize;
    private String height;
    private String treeType;
    private ImageView image;
    private int status;
    private String username;
    private ImageView profileImage;
    private Float latitude, longitude;

    public GreenPoint(){}

    public int getId (){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getStatus (){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public String getTreeType (){
        return treeType;
    }

    public void setTreeType(String treeType){
        this.treeType = treeType;
    }

    public String getLocation (){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public Date getDate (){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public String getUsername (){
        return username;
    }

    public void setUsername(String username) { this.username = username; }

    public ImageView getImage (){ return image; }

    public void setImage(ImageView image){
        this.image = image;
    }

    public String getHeight (){
        return height;
    }

    public void setHeight(String height){
        this.height = height;
    }

    public int getCanopySize (){
        return canopySize;
    }

    public void setCanopySize(int canopy){
        this.canopySize = canopy;
    }

    public int getStemSize (){
        return stemSize;
    }

    public void setStemSize(int stem){
        this.stemSize = stem;
    }

    public ImageView getProfileImage (){
        return profileImage;
    }

    public void setProfileImage(ImageView image){
        this.profileImage = image;
    }

    public Float getLatitude (){
        return latitude;
    }

    public void setLatitude(Float latitude){
        this.latitude = latitude;
    }

    public Float getLongitude (){
        return longitude;
    }

    public void setLongitude(Float longitude){
        this.longitude = longitude;
    }
}
