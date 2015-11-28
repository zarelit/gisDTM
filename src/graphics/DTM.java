/*
 * Copyright (C) 2011 giuliano
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
package graphics;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * Gestore del DTM
 * @author giuliano
 */
public class DTM {
    private static final Logger dtmLogger = Logger.getLogger(DTM.class .getCanonicalName());
    private List<DTMTriangle> rawDtm;
    private Geometry dtmGeometry;
    private Appearance dtmAppearance;
    private Shape3D dtmShape;
    private String name;
    private double[] maxCoord = {
                                      Double.MIN_VALUE,
                                      Double.MIN_VALUE,
                                      Double.MIN_VALUE
    };
    private double[] minCoord = {
                                     Double.MAX_VALUE,
                                     Double.MAX_VALUE,
                                     Double.MAX_VALUE
    };

    public DTM(List<DTMTriangle> rawDtm, double[] minCoord, double[] maxCoord)
    {
        dtmLogger.setLevel(Level.ALL);
        dtmLogger.addHandler(new ConsoleHandler());
        if(rawDtm == null){
            dtmLogger.warning("Raw DTM is empty");
            return;
        }
        dtmLogger.fine("Setting Raw DTM");
        this.rawDtm = rawDtm;
        System.out.println("Number of Triangles = " + rawDtm.size());
        if(minCoord == null || minCoord.length != 3)
        {
            dtmLogger.fine("Finding min Coordinates");
            findMinCoord();
        }
        else
        {
            this.minCoord = minCoord;
        }

        if(maxCoord == null || minCoord.length != 3)
        {
            dtmLogger.fine("Finding max Coordinates");
            findMaxCoord();
        }
        else
        {
            this.maxCoord = maxCoord;
        }
        //ciclo di attesa, perde pochi microsecondi se non sono vuoti
        //in questo modo elimino le attese su findMin/MaxCoord e lancio
        //tutto in parallelo
        while(this.maxCoord == null && this.minCoord == null);
        for (int i = 0; i < this.maxCoord.length; i++)
        {
            while (this.maxCoord[i] == Double.MIN_VALUE);
            while (this.minCoord[i] == Double.MAX_VALUE);
        }
        dtmLogger.fine("Setting DTM appearance");
        initAppearance();
    }

    public DTM(List<DTMTriangle> triangles)
    {
        this(triangles, null, null);
    }

    /**
     * Costruisce il DTM da una lista di DTMTriangles
     */
    public void createDTM()
    {
        int triangleVertexCount = rawDtm.size() * 3;
        GeometryInfo upperGI = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        Point3d[] upperCoordinates = new Point3d[triangleVertexCount];
        NormalGenerator ng = new NormalGenerator();
        int vertexIndex = 0;
        dtmLogger.fine("Creating DTM");
        for (DTMTriangle triangle : rawDtm)
        {
            for (int i = 0; i < 3; i++)
            {
                //shallow copy
                upperCoordinates[vertexIndex] = triangle.getVertex(i);
                vertexIndex ++;
            }
        }
        upperGI.setCoordinates(upperCoordinates);
        System.gc();
        upperGI.indexify();
        dtmLogger.fine("Generate DTM normals");
        ng.generateNormals(upperGI);
        System.gc();
        dtmLogger.fine("Generate DTM shape");
        dtmShape = new Shape3D(upperGI.getGeometryArray());
        dtmAppearance.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        dtmShape.setAppearance(dtmAppearance);
    }

    private void initAppearance()
    {
        Color3f objColor = new Color3f( 0.4f, 0.7f, 0.8f );
        Color3f black = new Color3f( 0.0f, 0.0f, 0.0f );
        PolygonAttributes pa = new  PolygonAttributes();
        pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        dtmLogger.fine("Init Appearance");
        dtmAppearance = new Appearance();
        dtmAppearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        dtmAppearance.setPolygonAttributes(pa);
        dtmAppearance.setMaterial( new Material( objColor, black, objColor, black, 80.0f ) );
    }

    public void changeAppearance(int polygonFill)
    {
        PolygonAttributes pa = dtmShape.getAppearance().getPolygonAttributes();
        pa.setPolygonMode(polygonFill);
    }

    /**
     * Scorre i punti per trovare le coordinate massime per entrambi gli assi.
     * La funzione sfrutta la ricerca parallela su una lista.
     * Ciò implica che si deve impostare un ciclo di attesa se si vuole utilizzare
     * immediatamente l'array di ritorno
     * @return un array di double = {x_max, y_max, z_max}
     */
    private double[] findMaxCoord()
    {
        //calcolo X massima
        new Runnable()
        {

            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.x > maxCoord[0])
                        {
                            maxCoord[0] = vertex.x;
                        }
                    }
                }
            }
        }.run();
        //calcolo Y massima
        new Runnable()
        {
            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.y >maxCoord[1])
                        {
                            maxCoord[1] = vertex.y;
                        }
                    }
                }
            }
        }.run();
        //calcolo Z massima
        new Runnable()
        {
            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.z >maxCoord[2])
                        {
                            maxCoord[2] = vertex.z;
                        }
                    }
                }
            }
        }.run();
        //si deve fare attenzione al valore di ritorno
        return maxCoord;
    }

    /**
     * Scorre i punti per trovare le coordinate minime per entrambi gli assi
     * La funzione sfrutta la ricerca parallela su una lista.
     * Ciò implica che si deve impostare un ciclo di attesa se si vuole utilizzare
     * immediatamente l'array di ritorno
     * @return un array di double = {x_min, y_min, z_min}
     */
    private double[] findMinCoord()
    {
        //calcolo X minima
        new Runnable()
        {
            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.x < minCoord[0])
                        {
                            minCoord[0] = vertex.x;
                        }
                    }
                }
            }
        }.run();
        //calcolo Y minima
        new Runnable()
        {
            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.y < minCoord[1])
                        {
                            minCoord[1] = vertex.y;
                        }
                    }
                }
            }
        }.run();
        //calcolo Z minima
        new Runnable()
        {
            @Override
            public void run()
            {
                Iterator<DTMTriangle> it = rawDtm.iterator();
                Iterator<Point3d> pointIt;
                Point3d vertex;
                while (it.hasNext())
                {
                    pointIt = it.next().getVertices().iterator();
                    while (pointIt.hasNext())
                    {
                        vertex = pointIt.next();
                        if (vertex.z < minCoord[2])
                        {
                            minCoord[2] = vertex.z;
                        }
                    }
                }
            }
        }.run();
        return minCoord;
    }

    /**
     * @return the rawDtm
     */
    public List<DTMTriangle> getRawDtm()
    {
        return rawDtm;
    }

    /**
     * @param rawDtm the rawDtm to set
     */
    public void setRawDtm(List<DTMTriangle> rawDtm)
    {
        this.rawDtm = rawDtm;
    }

    /**
     * @return the dtmGeometry
     */
    public Geometry getDtmGeometry()
    {
        return dtmGeometry;
    }

    /**
     * @return the dtmAppearance
     */
    public Appearance getDtmAppearance()
    {
        return dtmAppearance;
    }

    /**
     * @return the dtmShape
     */
    public Shape3D getDtmShape()
    {
        if(rawDtm == null)
        {
            throw new NullPointerException("RawDTM is not set");
        }
        if(dtmShape == null)
        {
            createDTM();
        }
        return dtmShape;
    }

    /**
     * @return the maxCoord
     */
    public double[] getMaxCoord()
    {
        return maxCoord;
    }

    /**
     * @return the minCoord
     */
    public double[] getMinCoord()
    {
        return minCoord;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param maxCoord the maxCoord to set
     */
    public void setMaxCoord(double[] maxCoord)
    {
        this.maxCoord = maxCoord;
    }

    /**
     * @param minCoord the minCoord to set
     */
    public void setMinCoord(double[] minCoord)
    {
        this.minCoord = minCoord;
    }
}