package com.idbcgroup.canopyverde;

import java.sql.Date;

/**
 * Class for the management of the user's Report
 *
 * Status: Status of the tree report
 *          0 for Red types of Tree Report
 *          1 for Yellow type of Tree Report
 *          2 for Green type of Tree Report
 *
 * TreeType: Type of the Tree reported
 * Location: Location of the Tree reported
 * Date: Date of the Tree reported using SQL date instead of Java Util date
 */
class Report {

    private int status;
    private String treeType;
    private String location;
    private java.sql.Date date;

    public Report(){}

    Report(int status, String treeType, String location, Date date) {
        this.status = status;
        this.treeType = treeType;
        this.location = location;
        this.date = date;
    }

    /**
     * Get Status of the tree reported
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
     * Get the Type of the tree reported
     * @return type of the reported tree
     */
    public String getTreeType (){
        return treeType;
    }

    /**
     * Set Type of the tree reported
     * @param treeType type of the reported tree
     */
    public void setTreeType(String treeType){
        this.treeType = treeType;
    }

    /**
     * Get the Location of the tree reported
     * @return location of the reported tree
     */
    public String getLocation (){
        return location;
    }

    /**
     * Set Location of the tree reported
     * @param location location of the reported tree
     */
    public void setLocation(String location){
        this.location = location;
    }

    /**
     * Set Date of the tree reported
     * @return date of the reported tree
     */
    public Date getDate (){
        return date;
    }

    /**
     * Set Date of the tree reported
     * @param date date of the reported tree
     */
    public void setDate(Date date){
        this.date = date;
    }
}
