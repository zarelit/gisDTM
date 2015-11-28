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

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.CapabilityNotSetException;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.HiResCoord;
import javax.media.j3d.Locale;
import javax.media.j3d.MultipleParentException;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.RestrictedAccessException;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Costruisce l'universo nel quale visualizzare il DTM
 * @author giuliano
 */
public class DTMUniverse
{
    //La piattaforma che mi permette di gestire molte trasformazioni e avere
    //una visuale "home"

    private ViewingPlatform dtmViewingPlatform;
    //l'universo virtuale dove risiede il DTM
    private VirtualUniverse dtmUniverse;
    //il sistema di coordinate
    private Locale dtmLocale;
    //la parte della vista della scena
    private BranchGroup viewBranch;
    //la parte della scena dove si vede il DTM
    private BranchGroup sceneBranch;
    //la vista della scena
    private View dtmView;
    //la foglia a cui attaccare la vista
    private ViewPlatform dtmViewPlatform;
    //le dimensioni della scatola
    private double[] dtmBox;

    /**
     * Costruisce l'universo per visualizzare il DTM
     * @param dtmWidth  la lunghezza lungo x del DTM
     * @param dtmHeight la lunghezza lungo y del DTM
     * @param dtmLength la lunghezza lungo z del DTM
     */
    public DTMUniverse(double dtmWidth, double dtmHeight, double dtmLength)
    {
        dtmUniverse = new VirtualUniverse();
        double[] measure =
        {
            dtmWidth, dtmHeight, dtmLength
        };
        setDtmBox(measure);
        //oggetti che mi danno una mano
        dtmLocale = new Locale(dtmUniverse);
        //la parte dedicata alla vista del DTM
        viewBranch = createViewGraph();
        //la parte che contiene il DTM
        sceneBranch = createSceneGraph();
        //setto le luci
        Color3f white = new Color3f(Color.WHITE);
        Vector3f dir = new Vector3f(0.0f, 0.0f, -1.0f);
        //To set Background Geometry:
        Background back = new Background(white);
        Appearance ap = new Appearance();
        Sphere sphere = new Sphere(Float.POSITIVE_INFINITY, Primitive.GENERATE_TEXTURE_COORDS
                | Primitive.GENERATE_NORMALS_INWARD, ap);
        sceneBranch.addChild(sphere);
        back.setGeometry(sceneBranch);
        DirectionalLight light = new DirectionalLight(white, dir);
        //la luce arriva fino all'infinito
        light.setInfluencingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
        light.setEnable(true);
        sceneBranch.addChild(light);
        sceneBranch.addChild(new ColorCube(1000));
        //aggiungo i rami della vista e della scena all'universo
        dtmLocale.addBranchGraph(viewBranch);
        dtmLocale.addBranchGraph(sceneBranch);
    }

    /**
     * Aggiunge un oggetto da renderizzare con una geometria e l'aspetto.
     * @param geo la {@link Geometry} dell'oggetto.
     * @param app la {@link Appearance} dell'oggetto.
     * @return
     */
    public boolean addShape(Geometry geo, Appearance app)
    {
        Shape3D newShape = new Shape3D(geo, app);
        sceneBranch.detach();
        try
        {
            Enumeration children = sceneBranch.getAllChildren();
            Object child;
            TransformGroup tg = null;
            while (children.hasMoreElements())
            {
                child = children.nextElement();
                if (child.getClass().isAssignableFrom(TransformGroup.class))
                {
                    tg = (TransformGroup) child;
                }
            }
            if (tg != null)
            {
                tg.addChild(newShape);
                dtmLocale.addBranchGraph(sceneBranch);
                return true;
            }
        } catch (CapabilityNotSetException e)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Non è stato possibile aggiungere una nuova forma alla scena.");
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Motivo :" + e.getClass().getName() + " = " + e.getMessage());
        } catch (RestrictedAccessException e)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Non è stato possibile aggiungere una nuova forma alla scena.");
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Motivo :" + e.getClass().getName() + " = " + e.getMessage());
        } catch (MultipleParentException e)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Non è stato possibile aggiungere una nuova forma alla scena.");
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                    "Motivo :" + e.getClass().getName() + " = " + e.getMessage());
        }
        return false;
    }

    /**
     * Calcola e imposta il centro di riferimento nel centro della "scatola"
     * immaginaria contente il modello digitale del terreno
     * @param measure un array che contiene le misure della scatola
     * @return un {@link HiResCoord} per fissare il centro del sistema di
     * riferimento
     */
    private HiResCoord getUniverseOrigin(double[] measure)
    {
        int[][] coord = new int[3][8];
        //l'indice della cifra nell'array di cifre in base 2^32
        int intPartIndex = 3;
        int index = 0;
        for (double e : measure)
        {
            if (e > Integer.MAX_VALUE)
            {
                System.err.println("Coordinate che non possono essere associate a un DTM");
                System.exit(1);
            }
            //prendo la metà di ogni misura per individuare il centro del DTM
            coord[index++][intPartIndex] = new Double(e).intValue() / 2;
        }
        return new HiResCoord(coord[0], coord[1], coord[2]);
    }

    /**
     * Estrae il massimo valore dall'array di double
     * @param array
     * @return il massimo valore contenuto nell'array
     */
    private double getMaxFromArray(double... array)
    {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < array.length; i++)
        {
            max = (array[i] > max) ? array[i] : max;
        }
        return max;
    }

    /**
     * Crea il ramo "Vista" della scena.
     * @param coord le coordinate del centro dell'universo
     * @return una {@link ViewPlatform} da connettere a un TransformGroup
     */
    private ViewPlatform createViewPlatform(double[] coord)
    {
        ViewPlatform vp = new ViewPlatform();
        double max = getMaxFromArray(coord);
        //per precauzione
        max = (max > Float.MAX_VALUE) ? Float.MAX_VALUE : max;
        vp.setViewAttachPolicy(View.RELATIVE_TO_FIELD_OF_VIEW);
        //è il confine dell'applicazione, oltre il quale non sarà renderizzato nulla
        vp.setActivationRadius((float) max);
        return vp;
    }

    private View createView(ViewPlatform viewPlatform)
    {
        View view = new View();
        PhysicalBody pb = new PhysicalBody();
        PhysicalEnvironment pe = new PhysicalEnvironment();
        view.setPhysicalEnvironment(pe);
        view.setPhysicalBody(pb);
        view.setScreenScalePolicy(View.SCALE_EXPLICIT);

        if (viewPlatform != null)
        {
            view.attachViewPlatform(viewPlatform);
        }
        //il piano di vista è distante quanto la massima coordinata
        view.setBackClipDistance(viewPlatform.getActivationRadius());
        view.setFrontClipDistance(0.1);
        return view;
    }

    /**
     * Crea la parte della vista del DTM.
     * @return
     */
    private BranchGroup createViewGraph()
    {
        BranchGroup vb = new BranchGroup();
        //per avere il movimento di visuale
        TransformGroup tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(new Vector3d(0.0, 0.0, 20.0));
        t3d.setScale(0.001);
        tg.setTransform(t3d);
        dtmViewPlatform = createViewPlatform(dtmBox);
        dtmView = createView(dtmViewPlatform);
        //per la rotazione
        MouseRotate mr = new MouseRotate(tg);
        mr.setSchedulingBounds(dtmViewPlatform.getBounds());
        tg.addChild(mr);
        //per la traslazione
        MouseTranslate mt = new MouseTranslate(tg);
        mt.setSchedulingBounds(dtmViewPlatform.getBounds());
        tg.addChild(mt);
        //per lo zoom
        MouseWheelZoom mz = new MouseWheelZoom(tg);
        mz.setSchedulingBounds(dtmViewPlatform.getBounds());
        tg.addChild(mz);
        //imposto la vista in modo che abbia il centro nel centro del DTM
        tg.addChild(dtmViewPlatform);
        vb.addChild(tg);
        return vb;
    }

    /**
     * Crea la parte della scena che contiene il DTM
     * @return il {@link BranchGroup} che contiene il DTM
     */
    private BranchGroup createSceneGraph()
    {
        BranchGroup sg = new BranchGroup();
        sg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        sg.setCapability(BranchGroup.ALLOW_DETACH);
        TransformGroup tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        sg.addChild(tg);
        return sg;
    }

    public void setViewScale(double scale)
    {
        this.getDtmView().setScreenScale(scale);
    }

    public double getViewScale()
    {
        return getDtmView().getScreenScale();
    }

    private void setDtmBox(double[] measure)
    {
        this.dtmBox = measure;
    }

    /**
     * @return the dtmUniverse
     */
    public VirtualUniverse getDtmUniverse()
    {
        return dtmUniverse;
    }

    /**
     * @param dtmUniverse the dtmUniverse to set
     */
    public void setDtmUniverse(VirtualUniverse dtmUniverse)
    {
        this.dtmUniverse = dtmUniverse;
    }

    /**
     * @return the dtmLocale
     */
    public Locale getDtmLocale()
    {
        return dtmLocale;
    }

    /**
     * @param dtmLocale the dtmLocale to set
     */
    public void setDtmLocale(Locale dtmLocale)
    {
        this.dtmLocale = dtmLocale;
    }

    /**
     * @return the dtmView
     */
    public View getDtmView()
    {
        return dtmView;
    }

    /**
     * @param dtmView the dtmView to set
     */
    public void setDtmView(View dtmView)
    {
        this.dtmView = dtmView;
    }

    /**
     * Imposta il canvas della vista. Senza il Canvas non si può vedere la scena
     * @param canvas il {@link Canvas3D} nel quale si vede il DTM
     */
    public void setViewCanvas(Canvas3D canvas)
    {
        dtmView.addCanvas3D(canvas);
    }
    //TESTING
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("SCALE = 0.4");
        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas.setVisible(true);
        frame.add(canvas);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(400, 400));
        SimpleUniverse universe = new SimpleUniverse(canvas);
        BranchGroup bg = new BranchGroup();
        TransformGroup tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        Transform3D t3d = new Transform3D();
        Transform3D t3d2 = new Transform3D();
        MouseRotate mt = new MouseRotate(tg);
        mt.setSchedulingBounds(new BoundingSphere(new Point3d(), 1.0));
        mt.setFactor(0.1, 0.0);
        tg.addChild(mt);
        t3d2.setTranslation(new Vector3d(0.0, 0.0, 10.0));
        t3d.rotZ(Math.PI/4);
        tg.setTransform(t3d);
        tg.addChild(new ColorCube(0.8));
        bg.addChild(tg);
        bg.compile();
        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(bg);
//        universe.getViewer().getView().setFieldOfView(Math.PI);
//        universe.getViewer().getView().setFieldOfView(Math.PI/3);
//        universe.getViewer().getView().setFieldOfView(Math.PI/8);
    }
}
