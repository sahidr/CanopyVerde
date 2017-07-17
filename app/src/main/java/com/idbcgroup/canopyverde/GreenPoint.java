package com.idbcgroup.canopyverde;

public class GreenPoint {

    private int id;
    private String location;
    private String date;
    private int canopySize;
    private int stemSize;
    private String height;
    private String treeType;
    private String image;
    private int status;
    private String user;

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

    public String getDate (){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getUser (){
        return user;
    }

    public void setUser(String user) { this.user = user; }

    public String getImage (){
        return image;
    }

    public void setImage(String image){
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


}
