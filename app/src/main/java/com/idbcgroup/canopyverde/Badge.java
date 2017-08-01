package com.idbcgroup.canopyverde;

/**
 * Created by Rogelio on 1/8/2017.
 */

public class Badge {

    private int status;
    //RED 0
    //YELLOW 1
    //GREEN 2
    private String reportType;
    private int point;
    private String date;

    public Badge(){}

    public Badge(int status, String reportType, int point, String date) {
        this.status = status;
        this.reportType = reportType;
        this.point = point;
        this.date = date;
    }

    public int getStatus (){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public String getReportType (){
        return reportType;
    }

    public void setReportType(String reportType){
        this.reportType = reportType;
    }

    public int getPoints (){
        return point;
    }

    public void setPoints (int point){
        this.point = point;
    }

    public String getDate (){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }
}
