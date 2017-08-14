package com.idbcgroup.canopyverde;

import java.sql.Date;

public class Report {

    private int status;
        //RED 0
        //YELLOW 1
        //GREEN 2
    private String treeType;
    private String location;
    private java.sql.Date date;

    public Report(){}

    public Report(int status, String treeType, String location, Date date) {
        this.status = status;
        this.treeType = treeType;
        this.location = location;
        this.date = date;
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
}
