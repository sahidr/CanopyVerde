package com.idbcgroup.canopyverde;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * APIResponse class extended
 */
public class APIResponse {

    int status;
    private JSONObject body;
    private JSONArray bodyArray; // If the response is an JSONArray

    public APIResponse(int status) {
        this.status = status;
    }

    public APIResponse(JSONObject body, int status) {
        this.body = body;
        this.status = status;
    }

    /**
     * Constructor of the class with a JSONArray body
     * @param body JSONArray that contains the body of the response
     * @param status response status
     */
    public APIResponse(JSONArray body, int status) {
        this.bodyArray = body;
        this.status = status;
    }

    public JSONObject getBody() {
        return body;
    }

    /**
     * Get Body if it's and JSONArray
     * @return Body of the response in JSONArray format
     */
    JSONArray getBodyArray() {
        return bodyArray;
    }

    void setBody(JSONObject body) {
        this.body = body;
    }

    /**
     * Set Response's body in JSONArray format
     * @param body JSONArray for the Response's body
     */
    void setBody(JSONArray body) {
        this.bodyArray = body;
    }

    public int getStatus() {
        return status;
    }

}
