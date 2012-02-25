/*
 * Copyright (C) 2011
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

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.View;
import javax.vecmath.Vector3d;

/**
 * Gestore del DTM
 * @author giuliano
 */
public class DTMEngine
{
    private static final Logger dtmEngineLogger =
            Logger.getLogger(DTMEngine.class.getCanonicalName());
    /**
     * Nome di default per il DTM se per caso non fosse stato impostato
     */
    private static final String DEFAULT_DTM_NAME = "DTM";

    private double defaultDTMRotation = -Math.PI/4;

    private final double SCROLL_PERCENT = 0.2;
    /**
     * Il DTM
     */
    private DTM dtm;

    /**
     * La scena dove sarà visualizzato il DTM
     */
    private DTMScene dtmScene;

    /**
     * L'oggetto che gestisce la grafica
     */
    private Canvas3D canvas;

    /**
     * Costruttore di default del gestore della scena.
     * @param canvas    dove la scena è renderizzata
     * @param triangles la {@link List} di {@link DTMTriangle} che contiene
     * @param
     * i triangoli da renderizzare
     */
    public DTMEngine(Canvas3D canvas, List<DTMTriangle> triangles, String dtmName)
    {

    }

    public DTMEngine(Canvas3D canvas, List<DTMTriangle> triangles, String dtmName, double[] maxCoord)
    {
        this(canvas, triangles, dtmName, maxCoord, null);
    }

    public DTMEngine(Canvas3D canvas, List<DTMTriangle> triangles)
    {
        this(canvas, triangles, DEFAULT_DTM_NAME);
    }

    public DTMEngine(Canvas3D canvas, List<DTMTriangle> triangles, String dtmName, double[] maxCoord, double[] minCoord)
    {
        setLogger();
        dtmEngineLogger.fine("Creating Engine");
        this.canvas = canvas;
        dtmEngineLogger.fine("- INIT DTM");
        dtm = new DTM(triangles, minCoord, maxCoord);
        dtm.setName(dtmName);
        //imposto la vista basandomi sulla dimensione del DTM sull'asse x
    }

    public void setGrillView()
    {
        if(dtm == null || dtm.getDtmShape() == null) return;
        dtm.changeAppearance(PolygonAttributes.POLYGON_LINE);
    }

    public void setFillView()
    {
        if(dtm == null || dtm.getDtmShape() == null) return;
        dtm.changeAppearance(PolygonAttributes.POLYGON_FILL);
    }

    public void createDTM()
    {
        if(dtm == null)
        {
            throw new NullPointerException("DTM object is not initialized");
        }
        dtm.createDTM();
    }

    public void createDTMScene()
    {
        double[] maxCoord = dtm.getMaxCoord();
        double[] minCoord = dtm.getMinCoord();
        //se non esiste la scena la creo
        if(dtmScene == null) dtmScene = new DTMScene(canvas, calcApplicationBound());
        //altrimenti imposto solo i nuovi limiti
        else
        {
            dtmScene.setUniverseBound(calcApplicationBound());
            dtmScene.setApplicationBackground(dtmScene.getBackgroundColor());
            dtmScene.setMouseBehaviourBounds();
        }
        //calcolo la scala da applicare al DTM
        double scale = calcDTMScale();
        dtmScene.setDefaultScale(scale);
        //imposto la vista in modo che si possa vedere l'intero DTM nel canvas
        dtmScene.setDTMViewTransform((maxCoord[0] - minCoord[0]) * scale);
        dtmScene.setDTMXRotation(defaultDTMRotation);
    }

    public void renderScene()
    {
        if(dtm == null) throw new NullPointerException("DTM is null");
        if(dtm.getName() == null || dtm.getName().isEmpty())
        {
            renderScene(DEFAULT_DTM_NAME);
        }
        else
        {
            renderScene(dtm.getName());
        }
    }

    public void renderScene(String dtmName)
    {
        dtmScene.addShape(dtm.getDtmShape(), dtmName);
        dtmScene.setMouseTranslateFactor(1.0);
    }

    /**
     * Elimina il DTM presente
     */
    public void removeDTM()
    {
        dtmScene.removeAllChildren();
        //distruggo il DTM
        dtm = null;
        System.gc();
    }

    /**
     * Aggiunge un nuovo DTM alla scena con un nome di default
     * @param triangles la {@link List} di {@link DTMTriangle} che contengono
     * le informazioni sul DTM
     */
    public void addDTM(List<DTMTriangle> triangles)
    {
        addDTM(triangles, DEFAULT_DTM_NAME);
    }

    /**
     * Aggiunge un nuovo DTM alla scena con nome {@code name}
     * @param trianglesla {@link List} di {@link DTMTriangle} che contengono
     * le informazioni sul DTM
     * @param name nome da dare al DTM. Utile per facilitare le operazioni di
     * eliminazione
     */
    public void addDTM(List<DTMTriangle> triangles, String name)
    {
        if(dtm != null) removeDTM();
        dtm = new DTM(triangles);
        dtm.setName(name);
    }

    /**
     * Calcola quali sono i confini della vista in base alle dimensioni del DTM
     * @return un {@code float} che indica quali sono i confini dell'applicaizione
     */
    private float calcApplicationBound()
    {
        if(dtm == null)
        {
            dtmEngineLogger.warning("DTM is null. Cannot calculate bounds");
            return 0;
        }
        //creo una sfera pari al doppio della maggior dimensione del DTM
        double[] max = dtm.getMaxCoord();
        double[] min = dtm.getMinCoord();
        double width = max[0] - min[0];
        double height = max[1] - min[1];
        double depth = max[2] - min[2];
        double sqrt = Math.sqrt(width * width + height * height + depth * depth);
        if (sqrt > Float.MAX_VALUE) sqrt = Float.MAX_VALUE;
        return (float) sqrt;
    }

    /**
     * Calcola quanto deve essere la scala del DTM per essere visualizzato all'interno
     * della vista
     * @return
     */
    private double calcDTMScale()
    {
        if (dtm == null) throw
                new NullPointerException("Cannot calculate DTM scale. DTM is Null");
        if (dtmScene == null)throw
                new NullPointerException("Cannot calculate DTM scale. DTMScene is Null");
        double[] maxCoord = dtm.getMaxCoord();
        double[] minCoords = dtm.getMinCoord();
        double width = maxCoord[0] - minCoords[0];
        double height = maxCoord[1] - minCoords[1];
        double depth = maxCoord[2] - minCoords[2];
        double maxSize = getMaxFromArray(width, height, depth);
        View sceneView = dtmScene.getDtmView();
        double viewDistance = 0.5 * width * Math.tan(sceneView.getFieldOfView()/2);
        //la scala di default è del 100%
        double scale = 1, denom = 1;
        double totalDistance = maxSize + viewDistance;
         while(totalDistance > sceneView.getBackClipDistance())
        {
            denom = maxSize + viewDistance;
            scale = sceneView.getBackClipDistance() / denom;
            maxSize *= scale;
            viewDistance *= scale;
            totalDistance = maxSize + viewDistance;
        }
        return scale;
    }

    /**
     * Imposta la scala del DTM a quella di default
     */
    public void setSceneDefaultScale()
    {
        dtmScene.resetSceneScale();
    }

    /**
     * Calcola il massimo valore da un array di {@code double}
     * @param array un array di {@code double} inizializzato
     * @return il valore massimo presente nell'array
     */
    private double getMaxFromArray(double ... array)
    {
        if(array == null) throw new NullPointerException("Array cannot be null");
        double max = Double.MIN_VALUE;
        for (int i = 0; i < array.length; i++)
        {
            max = (array[i] > max)? array[i] : max;
        }
        return max;
    }

    /**
     * @return the DTM_NAME
     */
    public String getDTMName()
    {
        return dtm.getName();
    }

    /**
     * @param n the DTM_NAME to set
     */
    public void setDTMName(String name)
    {
        dtm.setName(name);
    }

    private void setLogger()
    {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new SimpleFormatter());
        dtmEngineLogger.addHandler(ch);
        dtmEngineLogger.setLevel(Level.ALL);
        dtmEngineLogger.setUseParentHandlers(false);
        dtmEngineLogger.setParent(Logger.getLogger(Logger.GLOBAL_LOGGER_NAME));
    }

    public void setMouseMovementFactor(int unit2scroll)
    {
        double scaleFactor = 0.0;
        double lowerZ = 2.0;
        double oldDistance = dtmScene.getViewPosition().z;
        double newDistance = oldDistance * ( 1 - SCROLL_PERCENT);
        double appBound = calcApplicationBound();
        //se sono troppo vicino e sto cercando di avvicinarmi
        if (oldDistance <= lowerZ && unit2scroll < 0)
        {
            //non avanzo con il mouse
            dtmScene.setViewPosition(new Vector3d(0.0, 0.0, lowerZ));
        }
        //se sono troppo lontano e mi sto allontanando
        else if (oldDistance >=  appBound && unit2scroll > 0)
        {
            dtmScene.setViewPosition(new Vector3d(0.0, 0.0, appBound - 1.0));
        }
        else
        {
            scaleFactor = (oldDistance - newDistance) / Math.abs(unit2scroll);
        }
        dtmScene.setMouseZoomFactor(scaleFactor);
    }
}