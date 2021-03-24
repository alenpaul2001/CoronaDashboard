/*
 * Copyright (C) 2021 AlenPaulVarghese <alenpaul2001@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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