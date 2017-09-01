package com.idbcgroup.canopyverde;

import java.sql.Date;

/**
 * Class for de management of the user's Game Report
 *  Status the status of the event
 *  Point the points of the event
 *  ReportType the event that causes the increment of points
 *  Date the date of the event in SQL format
 */
class GameReport {

    private int status;
    private String reportType;
    private int point;
    private Date date;

    /**
     * Void Constructor of the class
     */
    public GameReport(){}

    /**
     *
     * @param status the status of the event
     * @param reportType the event that causes the increment of points
     * @param point the points of the event
     * @param date date of the event
     */
    GameReport(int status, String reportType, int point, Date date) {
        this.status = status;
        this.reportType = reportType;
        this.point = point;
        this.date = date;
    }

    /**
     * Get Status of the event
     * @return status as integer
     */
    public int getStatus (){
        return status;
    }

    /**
     * Set Status for a Report
     * @param status Reported tree status
     *               0 for Red
     *               1 for Yellow
     *               2 for Green
     */
    public void setStatus(int status){
        this.status = status;
    }

    /**
     * Get Type of the event
     * @return type as integer
     */
    String getReportType(){
        return reportType;
    }

    /**
     * Set Type of the event
     * @param reportType type of the event
     */
    public void setReportType(String reportType){
        this.reportType = reportType;
    }

    /**
     * Get Points of the event
     * @return points as integer
     */
    int getPoints(){
        return point;
    }

    /**
     * Set Points of the event
     * @param points of the event
     */
    public void setPoints (int points){
        this.point = points;
    }

    /**
     * Set Date of the event
     * @return date of the event
     */
    public Date getDate (){
        return date;
    }

    /**
     * Set Date of the event
     * @param date date of the event
     */
    public void setDate(Date date){
        this.date = date;
    }
}
