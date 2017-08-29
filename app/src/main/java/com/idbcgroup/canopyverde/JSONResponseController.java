package com.idbcgroup.canopyverde;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;


public class JSONResponseController {

    /**
     *
     * @param connection
     * @param is_JSONobject represents the data type of the response
     *                      true if it's a JSONObject
     *                      false if it's a JSONArray
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static APIResponse getJsonResponse(HttpURLConnection connection, boolean is_JSONobject) throws IOException, JSONException {

        /**
         * Initializes an APIResponse instance with the connection's response code in order to check
         * whether the request is valid or not.
         */
        APIResponse response;
        try {
            response = new APIResponse(connection.getResponseCode());
            StringBuilder responseBody = new StringBuilder();

            if ((response.getStatus() == HttpURLConnection.HTTP_OK) ||
                    (response.getStatus() == HttpURLConnection.HTTP_CREATED)) {
                /**
                 * If valid, sets the APIResponse's body (JSON) after processing response body.
                 * Finally, a valid APIResponse is returned.
                 */
                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader((in)));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                if (is_JSONobject) {
                    response.setBody(new JSONObject(responseBody.toString()));
                } else {
                    response.setBody(new JSONArray(responseBody.toString()));
                }
                return response;

            } else if ((response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) ||
                    (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) ||
                    (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND)
                    ) {
                /**
                 * If invalid, sets the APIResponse's error body (JSON) after processing response body.
                 * Finally, an invalid APIResponse is returned.
                 */
                InputStream in = new BufferedInputStream(connection.getErrorStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader((in)));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                response.setBody(new JSONObject(responseBody.toString()));
                return response;

            } else {

                /**
                 * If there was an error during communication (e.g. +500 errors), null is returned.
                 */
                response = new APIResponse(-2);
                return response;
            }
        } catch (java.net.SocketTimeoutException e){
            response = new APIResponse(-2);
            return response;
        }
    }
}
