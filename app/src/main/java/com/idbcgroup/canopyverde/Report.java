package com.idbcgroup.canopyverde;

import java.util.Date;

public class Report {

    //private int status;
    private String treeType;
    private String location;
    private String date;

    public Report(){}

    public Report( /*int status,*/
            String treeType, String location, String date) {
        //this.status = status;
        this.treeType = treeType;
        this.location = location;
        this.date = date;
    }
/*
    public int getStatus (){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }
*/
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
}