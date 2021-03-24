/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scaffold{
    public String countryName;
    public String countryCode;
    public final int totalConfirmed;
    public final int totalRecovered;
    public final int totalDeaths;
    
    public Scaffold(@JsonProperty("Country") String countryName,
                    @JsonProperty("CountryCode") String countryCode,
                    @JsonProperty("TotalConfirmed") int totalConfirmed,
                    @JsonProperty("TotalRecovered") int totalRecovered,
                    @JsonProperty("TotalDeaths") int totalDeaths
                    
            ){
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.totalConfirmed = totalConfirmed;
        this.totalRecovered = totalRecovered;
        this.totalDeaths = totalDeaths;
    }
}
