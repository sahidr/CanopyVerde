package com.idbcgroup.canopyverde;

import org.xml.sax.DTDHandler;

import java.sql.Date;

/**
 * Created by Rogelio on 1/8/2017.
 */

/**
 * Class for User Badges
 */
public class Badge {

    private int status;
    private String reportType;
    private int point;
    private Date date;

    public Badge(){}

    public Badge(int status, String reportType, int point, Date date) {
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

    public Date getDate (){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }
}
