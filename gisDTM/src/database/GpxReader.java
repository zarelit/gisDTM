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

import java.io.File;
import java.util.LinkedList;
import javax.vecmath.Point3d;
import us.zuercher.gpx2map.data.GpxParser;
import us.zuercher.gpx2map.data.Track;
import us.zuercher.gpx2map.data.TrackPoint;

/**
 * Parses a common .gpx format containing a GPS track log.
 * @author David Costa <david@zarel.net>
 */
public class GpxReader implements PointsReader{
    /**
     * indicates that the reader is ready and getAllPoints() can be called.
     * @see #getAllPoints()
     */
    private boolean ready;
    
    /**
     * The filename that points to the data source.
     * In this case a GPS track log in GPX (XML-based) format
     */
    private String filename;
    
    /**
     * constructor. Does nothing. REMEMBER to use setSource before doing
     * anything else or exceptions will be thrown.
     */
    public GpxReader(){
        ready=false;
    }
    
    @Override
    public LinkedList<Point3d> getAllPoints() throws Exception {
        LinkedList<Point3d> result=new LinkedList<Point3d>();
        Point3d element;
        //did we set the source?
        if(!ready) throw new Exception("The reader is not ready. Set source before.");
        
        
        //create a new GpxParser (3rd party library)
        GpxParser gpxparser=new GpxParser(new File(filename));
        //do the actual parsing
        gpxparser.parse();
        for(Track t:gpxparser.getTracks()){
            //per ogni track ci sono diversi punti
            for(TrackPoint p:t){
                //ogni punto della track diventa un punto nella lista
                element=new Point3d();
                element.x=p.getLocation().getLongitude();
                element.y=p.getLocation().getLatitude();
                //altezza ellissoidica!
                //se il modello del terreno copre pochi kilometri
                //geoide ed ellissoide sono quasi un piano, solamente sfalsati
                //di una quota (costante) h0
                element.z=p.getDecimalProperty("ele");
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public void setSource(String source) throws Exception {
        filename=source;
        if(new File(filename).exists()) ready=true;
        else ready=false;
    }

    @Override
    public double getNullValue() {
        //Qualunque valore va bene poichè non esistono punti nel GPX con
        //elevazione null.
        return -9999;
    }
    
}
