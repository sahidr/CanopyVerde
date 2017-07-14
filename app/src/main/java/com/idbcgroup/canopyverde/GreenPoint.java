package com.idbcgroup.canopyverde;

/**
 * Created by Rogelio on 14/7/2017.
 */

public class GreenPoint {

    private int id;
    private String location;
    private String date;
    private int canopySize;
    private int stemSize;
    private int height;
    private String treeType;
    private String image;
    private int status;
    private String user;

    public GreenPoint(){}

    public GreenPoint(int id, String location, String date, int canopySize, int stemSize, int height,
            String treeType, String image, int status){  //, String user){

        this.id = id;
        this.location = location;
        this.date = date;
        this.canopySize = canopySize;
        this.stemSize = stemSize;
        this.height = height;
        this.treeType = treeType;
        this.image = image;
        this.status = status;
        //this.user = user;
    }

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

    public String getDate (){
        return date;
    }
    public void setDate(String date){
        this.date = date;
    }

    public String getUser (){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getImage (){
        return image;
    }

    public void setImage(String image){
        this.image = image;
    }

}
