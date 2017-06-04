package com.idbcgroup.canopyverde;

import org.json.JSONObject;

public class APIResponse {
    int status;
    JSONObject body;

    public APIResponse(int status) {
        this.status = status;
    }

    public APIResponse(JSONObject body, int status) {
        this.body = body;
        this.status = status;
    }

    public JSONObject getBody() {
        return body;
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }
}
