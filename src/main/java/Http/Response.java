/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    public final Scaffold global;
    public final List<Scaffold> countries;
    public final String date;
    
    public Response(@JsonProperty("Global") Scaffold global,
                    @JsonProperty("Countries") List<Scaffold> countries,
                    @JsonProperty("Date") String date
            ){ 
        this.global = global;
        this.countries = countries;
        this.date = date;
    }
    
}