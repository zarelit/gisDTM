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
package examples;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author giuliano
 */
public class J3DTestClass extends JFrame{
    private SimpleUniverse universe;
    private BranchGroup sceneBranch;
    private BranchGroup viewBranch;
    private static final int COLS = 20;
    private static final int ROWS = 10;
    private static int NUM_VERTEX = COLS*ROWS;

    private static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;


    public J3DTestClass(Canvas3D canvas) throws HeadlessException {
        universe = new SimpleUniverse(canvas);
        sceneBranch = createSceneGraph();
        sceneBranch.compile();
        universe.addBranchGraph(sceneBranch);
    }

    private BranchGroup createSceneGraph(){
        BranchGroup bg = new BranchGroup();
        Transform3D t3d = new Transform3D();
//        rotazione di 30°
        t3d.rotX(Math.PI/6);
        t3d.setScale(0.4);
        t3d.setTranslation(new Vector3d(0, 0, -20));
        //setto la trasformazione per la matrice di punti
        TransformGroup tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setTransform(t3d);
        //esegui ogni 3 secondi una rotazione di 2*Math.PI
        Transform3D yAxis = new Transform3D();
        Alpha a = new Alpha(-1, Alpha.INCREASING_ENABLE,
                                0, 0,
                                1000, 0, 0,
                                0, 0, 0);

        MouseRotate mRot = new MouseRotate(tg);
        mRot.setSchedulingBounds(new BoundingSphere(new Point3d(), 100));
        tg.addChild(mRot);
        //creo la matrice di punti
        Shape3D shape3d = createPointMatrix();
        tg.addChild(shape3d);
        tg.addChild(new ColorCube(0.3));
        bg.addChild(tg);
        return bg;
    }

    private Shape3D createPointMatrix(){
        PointArray matrix = new PointArray(NUM_VERTEX, GeometryArray.COORDINATES);
        //come appariranno i punti
        Appearance app = new Appearance();
        app.setPointAttributes(new PointAttributes(3.0f, Boolean.TRUE));
        app.setMaterial(new Material());
        int vertexNum= 0;
        int halfRows = ROWS/2;
        int halfCols = COLS/2;
        double zQuote = Math.random();
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                zQuote = Math.random();
                matrix.setCoordinate(vertexNum ++, new Point3d(i - halfCols, j - halfRows, zQuote));
            }
        }
        return new Shape3D(matrix, app);
    }

    public static void main(String[] args){
        GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D( );
        gc3D.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );
        GraphicsDevice gd[] = GraphicsEnvironment.getLocalGraphicsEnvironment( ).getScreenDevices( );
        Canvas3D c3d = new Canvas3D( gd[0].getBestConfiguration( gc3D ) );
        c3d.setStereoEnable(Boolean.FALSE);
        c3d.setMonoscopicViewPolicy(View.CYCLOPEAN_EYE_VIEW);
        c3d.setSize( 300, 300);
        J3DTestClass test = new J3DTestClass(c3d);
        test.add(c3d);
        test.setTitle("mo vediamo");
        test.setMinimumSize(new Dimension(c3d.getWidth(), c3d.getHeight()));
        test.setLayout(new BorderLayout());
        test.setVisible(Boolean.TRUE);
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
