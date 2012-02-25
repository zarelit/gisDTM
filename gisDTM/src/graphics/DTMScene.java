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
import com.sun.j3d.utils.universe.MultiTransformGroup;
import java.awt.Color;
import java.util.Enumeration;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Locale;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 *  Modella la scena dove si costruisce il DTM, contiene tutti i riferimenti
 *  all'universo J3D e si occupa di renderizzare il DTM con i metodi di J3D.
 *  Il ramo della vista ha un numero di trasformazioni pari a 3, configurabile
 *  tramite il campo VIEW_TRAFSFORM (default = 3).
 * @author giuliano
 */
public class DTMScene
{
    public static int VIEW_TRANSFORMS = 3;
    public static int SCENE_TRANSFORMS = 2;
    /**
     * La rotazione di default per lo scale TransformGroup
     */
    private final double DEFAULT_ROTATION = Math.PI/4;

    /**
     * Il nome della trasformazione di scala del DTM. Per facilitare l'accesso al
     * {@link TransformGroup}
     */
    public static String SCALE_TRANSFORM_NAME = "Scale";

    /**
     * Il nome della trasformazione di rotazione del DTM. Per facilitare
     * l'accesso al {@link TransformGroup}
     */
    public static String ROTATEX_TRANSFORM_NAME ="RotateX";

    /**
     * La scala di default. Modificabile a seconda delle esigenze.
     * Il valore base è 0.08
     */
    private double defaultScale = 0.08;
    //colore di default dello sfondo dell'universo
    private Color3f backgroundColor = new Color3f(Color.LIGHT_GRAY);
    //la sfera che contiene l'intera scena
    private BoundingSphere universeBound;
    //l'universo virtuale dove risiede il DTM
    private VirtualUniverse dtmUniverse;
    //la parte della scena che contiene il DTM
    //sceneBranch -- RotateGroup -- ScaleGroup -- Shape3d
    private BranchGroup sceneBranch;
    //la parte che si occupa della vista e delle sue trasformazioni
    private BranchGroup viewBranch;
    //oggetti di comodo per recuperare i TransformGroup
    //Zoom -- Rotate -- Translate
    private MultiTransformGroup viewMTG;
    //rotate -- scale
    private MultiTransformGroup sceneMTG;
    //la vista associata al DTM
    private View dtmView;

    private MouseRotate mr;
    private MouseTranslate mt;
    private MouseWheelZoom mz;

    private Vector3d viewPosition;

    /**
     * Costruttore della scena
     * @param dtmCanvas dove sarà visualizzata la scena
     * @param applicationBounds i confini dell'applicazione oltre i quali non
     * si renderizza
     */
    public DTMScene(Canvas3D dtmCanvas, float applicationBounds)
    {
        universeBound = new BoundingSphere(new Point3d(), 2 * applicationBounds);
//////        dtmUniverse = createDTMUniverse(dtmCanvas, VIEW_TRANSFORMS);
        dtmUniverse = new VirtualUniverse();
        Locale locale = new Locale(dtmUniverse);
        //creo la parte visuale della scena
        viewBranch = createViewGraph(dtmCanvas);
        viewBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        viewBranch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        viewBranch.compile();
        //creo il contenuto della scena
        sceneBranch = createSceneGraph();
        sceneBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        sceneBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        sceneBranch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        sceneBranch.setCapability(BranchGroup.ALLOW_BOUNDS_WRITE);
        sceneBranch.compile();

        locale.addBranchGraph(viewBranch);
        locale.addBranchGraph(sceneBranch);
    }

    /**
     * Aggiunge un oggetto grafico alla scena
     * @param newShape  l'oggetto {@link Shape3D} da aggiungere alla scena
     * @param name il nome dell'oggetto nella scena
     */
    public void addShape(Shape3D newShape, String name)
    {

        newShape.setName(name);

        TransformGroup tg = getScaleDTMGroup();
        BranchGroup bg = new BranchGroup();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        bg.addChild(newShape);
        tg.addChild(bg);
    }

    /**
     * Dà nuovi confini all'applicazione, oltre i quali non sarà renderizzato
     * alcunché
     * @param newBound i nuovi confini dell'applicazione
     */
    public void setUniverseBound(float newBound)
    {
        universeBound = new BoundingSphere(new Point3d(), newBound);
        sceneBranch.setBounds(universeBound);
        System.gc();
    }

    /**
     * Pulisce la scena da qualsiasi oggetto grafico che ci sia. Questo include
     * l'eliminazione di tutte le trasformazioni.
     */
    public void removeAllChildren()
    {
        TransformGroup scaleGroup = getScaleDTMGroup();
        //poiché il DTM è contenuto solo nello scaleGroup lo elimino direttamente
        Enumeration scaleChildren = scaleGroup.getAllChildren();
        Node child;
        while(scaleChildren.hasMoreElements())
        {
            child = (Node) scaleChildren.nextElement();
            if(child instanceof BranchGroup)
            {
                scaleGroup.removeChild(child);
                break;
            }
        }
        System.gc();
    }

    /**
     * Crea un TranformGroup a cui è applicata una trasformazione di rotazione
     * sull'asse X di xRotation radianti
     *
     * @param xRotation rotazione sull'asse X in radianti
     * @return {@link TransformGroup } con una trasformazione di rotazione di
     * xRotation radianti
     */
    private TransformGroup initRotateGroup(double xRotation)
    {
        TransformGroup xRot = sceneMTG.getTransformGroup(0);
        xRot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        xRot.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        Transform3D rotation = new Transform3D();
        rotation.rotX(xRotation);
        xRot.setTransform(rotation);
        return xRot;
    }

    /**
     * Elimina dalla scena l'oggetto grafico con il nome passato per parametro
     * @param name il nome dell'oggetto da eliminare
     */
    public void removeChild(String name)
    {
        Enumeration enumeration = sceneBranch.getAllChildren( );
        int index = 0;
        SceneGraphObject sgObject;
        while ( enumeration.hasMoreElements() )
        {
            sgObject = (SceneGraphObject) enumeration.nextElement( );
            if ( sgObject.getName().equals(name))
            {

                sceneBranch.removeChild( index );
            }
            index++;
        }
    }

    /**
     * Setta il colore dello sfondo dell'applicazione
     */
    public void setApplicationBackground(Color3f bkgColor)
    {
        //se è già impostato un background lo rimuovo e ne inserisco uno nuovo
        Enumeration children = sceneBranch.getAllChildren();
        boolean foundBkg = false;
        Node node = null;
        Background back = null;
        while(children.hasMoreElements() & !foundBkg){
            node = (Node) children.nextElement();
            if(node.getClass().isAssignableFrom(Background.class))
            {
                back = (Background) node;
                break;
            }
        }
        backgroundColor = bkgColor;
        back.setColor(bkgColor);
        back.setApplicationBounds(universeBound);
    }

    /**
     * Crea il ramo della scena con 2 trasformazioni applicate al modello di DTM
     * Le trasformazioni hanno nomi predefiniti accessibili da altri.
     * @return un {@link BranchGroup} contenente la scena
     * @see SCALE_TRANSFORM_NAME
     * @see
     */
    private BranchGroup createSceneGraph()
    {

        BranchGroup bg = new BranchGroup();
        sceneMTG = new MultiTransformGroup(SCENE_TRANSFORMS);
        Background bkg = new Background(backgroundColor);
        bkg.setCapability(Background.ALLOW_COLOR_WRITE);
        bkg.setCapability(Background.ALLOW_APPLICATION_BOUNDS_WRITE);
        bkg.setApplicationBounds(universeBound);
        bg.addChild(bkg);
        TransformGroup rotX = initRotateGroup(DEFAULT_ROTATION);
        rotX.setName(ROTATEX_TRANSFORM_NAME);

        mr = new MouseRotate();
        mr.setCapability(MouseRotate.ALLOW_BOUNDS_WRITE);
        mr.setTransformGroup(rotX);
        mr.setSchedulingBounds(universeBound);
        rotX.addChild(mr);


        TransformGroup scale = initScaleGroup(defaultScale);
        scale.setName(SCALE_TRANSFORM_NAME);

        mt = new MouseTranslate();
        mt.setCapability(MouseTranslate.ALLOW_BOUNDS_WRITE);
        mt.setSchedulingBounds(universeBound);
        mt.setTransformGroup(rotX);
        scale.addChild(mt);

        Color3f lColor1 = new Color3f( 0.8f,0.8f,0.8f );
        Vector3f lDir1  = new Vector3f( 0.0f,1.0f,1.0f );
        // create the directional light
        DirectionalLight lgt1 = new DirectionalLight( lColor1, lDir1 );
        lgt1.setInfluencingBounds( universeBound );
        bg.addChild( lgt1 );
        //DEBUG aggiunto giusto perché sce piasce
        bg.addChild(rotX);
        return bg;
    }

    private BranchGroup createViewGraph(Canvas3D canvas)
    {
        BranchGroup bg = new BranchGroup();
        ViewPlatform vp = createViewPlatform();
        dtmView = createView(vp);
        mz = new MouseWheelZoom();
        mz.setSchedulingBounds(universeBound);
        dtmView.addCanvas3D(canvas);
        viewMTG = new MultiTransformGroup(VIEW_TRANSFORMS);
        TransformGroup tg = null;
        for (int i = 0; i < viewMTG.getNumTransforms(); i++)
        {
            tg = viewMTG.getTransformGroup(i);
            tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        }
        mz.setTransformGroup(tg);
        //tg è l'ultimo TransformGroup di viewMTG dopo il ciclo
        tg.addChild(vp);
        tg.addChild(mz);
        //aggiungo la vista e le sue trasformazioni al ramo principale
        bg.addChild(viewMTG.getTransformGroup(0));
        return bg;
    }

    /**
     * Imposta la vista in modo che l'intero DTM sia visibile basandosi sul
     * parametro passato
     * @param size la misura del DTM, è un {@code double}
     */
    public void setDTMViewTransform(double size)
    {
        double theta = 0.5 * (Math.PI - dtmView.getFieldOfView());
        double high = size / 2 * Math.tan(theta);
        viewPosition = new Vector3d(0.0, 0.0, high);
        setViewPosition(viewPosition);
    }

    /**
     * Posiziona la visuale al punto passato come parametro
     * @param pos il {@link Vector3d} che specifica la posizione della visuale
     */
    public void setViewPosition(Vector3d pos)
    {
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(pos);
        viewMTG.getTransformGroup(VIEW_TRANSFORMS - 1).setTransform(t3d);
    }

    /**
     * Setta la scala del DTM.
     * @param scale
     */
    public void setDTMScale(double scale)
    {
        if(scale == 0 || scale == Double.MAX_VALUE || scale == Double.MIN_VALUE)
        {

            return;
        }
        TransformGroup tg = getScaleDTMGroup();
        if(tg == null)
        {

            return;
        }
        Transform3D t3d = new Transform3D();
        tg.getTransform(t3d);
        t3d.setScale(scale);
        tg.setTransform(t3d);
    }

    /**
     * Imposta la rotazione a {@code radiants} radianti. Elimina ogni altra
     * rotazione presente
     * @param radiants di quanto ruotare su X il DTM
     */
    public void setDTMXRotation(double radiants)
    {
        if(radiants == 0)
        {
            return;
        }

        TransformGroup rotateGroup = getRotateXGroup();
        if(rotateGroup == null)
        {
            return;
        }
        Transform3D oldT3d = new Transform3D();
        rotateGroup.getTransform(oldT3d);
        Transform3D t3d = new Transform3D(oldT3d);
        t3d.rotX(radiants);
        rotateGroup.setTransform(t3d);
    }

    /**
     * Recupera il {@link TransformGroup} di rotazione su X del DTM.
     * Usa ROTATEX_TRANSFORM_NAME per trovare il {@link TransformGroup} di
     * interesse
     * @return il {@link TransformGroup} di rotazione su X del DTM
     */
    private TransformGroup getRotateXGroup()
    {

        return sceneMTG.getTransformGroup(0);
    }

    /**
     * Recupera il {@link TransformGroup} di traslazione del DTM.
     * Usa SCALE_TRANSFORM_NAME per trovare il {@link TransformGroup} di
     * interesse
     * @return il {@link TransformGroup} di traslazione su X del DTM
     */
    private TransformGroup getScaleDTMGroup()
    {
        return sceneMTG.getTransformGroup(1);
    }

    /**
     * Si occupa di inizializzare il {@link TransformGroup} adibito allo
     * scalamento del modello DTM.
     * @param scale la scala da applicare al modello
     * @return un {@link TransformGroup} con una trasformazione di scalamento
     */
    private TransformGroup initScaleGroup(double scale)
    {
        TransformGroup scaleGroup = sceneMTG.getTransformGroup(1);
        scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        Transform3D scaleT3D = new Transform3D();
        scaleT3D.setScale(scale);
        scaleGroup.setTransform(scaleT3D);
        return scaleGroup;
    }

    /**
     * Reimposta la scala di default del modello
     */
    public void resetSceneScale()
    {
        setDTMScale(defaultScale);
    }

    /**
     * @return the defaultScale
     */
    public double getDefaultScale()
    {
        return defaultScale;
    }

    /**
     * @param defaultScale the defaultScale to set
     */
    public void setDefaultScale(double defaultScale)
    {
        this.defaultScale = defaultScale;
    }

    private ViewPlatform createViewPlatform()
    {
        ViewPlatform vp = new ViewPlatform( );
        vp.setViewAttachPolicy( View.RELATIVE_TO_FIELD_OF_VIEW );
        if(universeBound == null) throw new NullPointerException("Universe Bound is null");
        //nel caso radius fosse maggiore di Float.MAX_VALUE dovrebbe scattare una
        //eccezione
        vp.setActivationRadius((float) universeBound.getRadius());
        return vp;
    }

    private View createView(ViewPlatform vp)
    {
        View view = new View( );

        PhysicalBody pb = new PhysicalBody();
        PhysicalEnvironment pe = new PhysicalEnvironment();

        view.setPhysicalEnvironment( pe );
        view.setPhysicalBody( pb );
        if( vp != null )
                view.attachViewPlatform( vp );
        //in questo modo ho il rapporto ottimale tra le due distanze
        view.setBackClipDistance(1000);
        view.setFrontClipDistance(1);
        return view;
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
     * @return the BKG_COLOR
     */
    public Color3f getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * @param BKG_COLOR the BKG_COLOR to set
     */
    public void setBackgroundColor(Color3f newBackgroundColor)
    {
        this.backgroundColor = newBackgroundColor;
    }

    public void setMouseBehaviorsFactor(double factor)
    {
        mt.setFactor(factor);
        mz.setFactor(factor);
    }

    public void setMouseTranslateFactor(double factor)
    {
        mt.setFactor(factor);
    }

    public void setMouseZoomFactor(double factor)
    {
        mz.setFactor(factor);
    }

    public void setMouseBehaviourBounds()
    {
        mt.setSchedulingBounds(universeBound);
        mr.setSchedulingBounds(universeBound);
        mz.setSchedulingBounds(universeBound);
    }

    /**
     * @return the viewPosition
     */
    public Vector3d getViewPosition()
    {
        Vector3d v = new Vector3d();
        TransformGroup viewTG = viewMTG.getTransformGroup(VIEW_TRANSFORMS - 1);
        Transform3D t3d = new Transform3D();
        viewTG.getTransform(t3d);
        t3d.get(v);
        return v;
    }
}