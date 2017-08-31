package com.idbcgroup.canopyverde;

import java.sql.Date;

/**
 * Class for de management of the user's Game Report
 * Status
 * report
 */
class GameReport {

    private int status;
    private String reportType;
    private int point;
    private Date date;

    public GameReport(){}

    GameReport(int status, String reportType, int point, Date date) {
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

    String getReportType(){
        return reportType;
    }

    public void setReportType(String reportType){
        this.reportType = reportType;
    }

    int getPoints(){
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
