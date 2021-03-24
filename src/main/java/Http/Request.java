/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
public class Request {
    public static Response request(){
        try{
            URL url = new URL(" https://api.covid19api.com/summary");
            // URL url = new URL(" http://127.0.0.1:8000");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            Response res = mapper.readValue(responseStream, Response.class);
            return res;
            
        } catch(IOException err){
            System.out.println(err);
            return null;
        }
        // APOD apod = mapper.readValue(responseStream, APOD.class);
        
    }
}