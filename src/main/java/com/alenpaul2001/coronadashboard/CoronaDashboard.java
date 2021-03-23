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
package com.alenpaul2001.coronadashboard;

import org.apache.commons.lang3.EnumUtils;


/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */

enum Config{
    IN,
    AF,
    AL,
    US
}

public class CoronaDashboard {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Response res = Request.request();
        update(res);
        System.out.println(res.date);
    }
    
    public static void update(Response r){
        r.countries.forEach((country)-> {
            if(EnumUtils.isValidEnum(Config.class, country.countryCode)){
                System.out.println(country.countryName);
            }
        });
    }
}
