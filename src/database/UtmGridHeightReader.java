/*
 * Copyright (C) 2011 David Costa <david@zarel.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;

/**
 * Parses DTMs available from the Friuli Venezia Giulia's website.
 * This class parses the EPSG32633 format.
 *
 * The files provided by the IRDAT (Infrastruttura Regionale Dati Ambientali e
 * Territoriali) are in the following format:
 * A point on each row. Rows contain Northern, Eastern and Height coordinates in
 * this order, separated by a comma. They are the UTM coordinates based on the
 * 33N zone, on the WGS-84 ellipsoid.
 * @author David Costa <david@zarel.net>
 */
public class UtmGridHeightReader implements PointsReader{
    /**
     * indicates that the reader is ready and getAllPoints() can be called.
     * @see #getAllPoints()
     */
    private boolean ready;
    /**
     * The filename that points to the data source.
     * In this case a text file with the UTM/WGS-84 height data
     * @see #UtmGridHeightReader
     */
    private String filename;
    private BufferedReader dataReader;
    private double specialNullValue=-9999;

    /**
     * constructor. Does nothing. REMEMBER to use setSource before doing
     * anything else or exceptions will be thrown.
     */
    public UtmGridHeightReader(){
        ready=false;
    }

    /**
     * Indicates what value is used in the text file to indicate null data.
     * @return the specialNullValue
     */
    public double getNullValue(){
        return specialNullValue;
    }

    /**
     * Parses the text file and returns a list of points.
     * @return a LinkedList of Point3f with the parsed data found in the file.
     * @throws Exception if the source hasn't been set or the file doesn't exists
     * anymore.
     */
    @Override
    public LinkedList<Point3d> getAllPoints() throws Exception {
        //Contains a line of the file at a time
        String line="";
        int counter=0;
        /**
         * Pattern object containing the data structure of each row:
         * "northern,eastern,height"
         */
        Pattern structure = Pattern.compile(
                "(\\d*\\.?\\d+),(\\d*\\.?\\d+),(-?\\d*\\.?\\d+)"
                );
        Matcher value;
        String northing="", easting="", height="";
        LinkedList<Point3d> result=new LinkedList<Point3d>();

        //did we set the source?
        if(!ready) throw new Exception("The reader is not ready. Set source before.");

        dataReader=new BufferedReader(new FileReader(filename));

        //process one line at a time
        value=structure.matcher(line);
        while((line=dataReader.readLine()) != null){
            try{
                //recycling the same matcher to improve performance
                value.reset(line);
                if(value.matches()){
                    //the row matches against the pattern
                    //extract the values from the pattern groups
                    northing=value.group(1);
                    easting=value.group(2);
                    height=value.group(3);
                    //assign them to a new Point3f
                    Point3d temp=new Point3d();
                    temp.x=Double.valueOf(northing);
                    temp.y=Double.valueOf(easting);
                    temp.z=Double.valueOf(height);
                    //queue it in the linked list
                    result.add(temp);
                    //counter++;
                    //if(counter%100==0) System.out.println(counter);

                }else{
                    //the row didn't match against the pattern
                    throw new Exception("Invalid data row.");
                }
            }catch(Exception e){
                //an invalid row is not fatal. throw an exception and keep going
                Logger.getLogger(UtmGridHeightReader.class.getName())
                    .log(Level.WARNING, "Found line with invalid data structure");
            }
        }

        return result;
    }

    /**
     * Before UtmGridHeightReader can be used successfully a source must be set.
     * This class read the data files provided by the FVG website, so a String
     * containing a path to such a kind of file is expected,
     * @param source a path to a UTM grid data file.
     * @throws FileNotFoundException 
     */
@Override
    public void setSource(String source) throws FileNotFoundException {
        filename=source;
        if(new File(filename).exists()) ready=true;
        else ready=false;
    }

}
