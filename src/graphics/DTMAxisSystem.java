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

import java.awt.Color;
import java.awt.Font;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 *
 * @author giuliano
 */
public class DTMAxisSystem
{
    /**
     *  Il colore del testo 3D degli assi
     */
    public static Color3f   TEXT_COLOR = new Color3f(Color.BLACK);
    /**
     *  Il colore degli assi
     */
    public static Color3f   LINE_COLOR = new Color3f(Color.DARK_GRAY);
    private static String   AXIS_X = "x";
    private static String   AXIS_Y = "y";
    private static String   AXIS_Z = "z";

    /**
     * Per il numero di divisioni dell'asse x, y , z in questo ordine
     */
    private int[] divisions = {0, 0, 0};
    /**
     * Il gruppo che conterrà gli assi, le tacche sugli assi e i testi 2D
     */
    private BranchGroup dtmAxes;
    /**
     * Per le etichette degli assi
     */
    private Text3D[] axesLabels;

    public DTMAxisSystem(int xDiv, int yDiv, int zDiv)
    {
      //imposto le divisioni sugli assi
        divisions[0] = xDiv;
        divisions[1] = yDiv;
        divisions[2] = zDiv;
      //imposto il testo per le etichette degli assi
        axesLabels = new Text3D[3];
        Font3D axesFont = new Font3D(Font.getFont(Font.SERIF), null);
      //asse x
        axesLabels[0] = new Text3D(axesFont, AXIS_X);
        axesLabels[0].setCapability(Text3D.ALLOW_POSITION_WRITE);
      //asse y
        axesLabels[1] = new Text3D(axesFont, AXIS_Y);
        axesLabels[1].setCapability(Text3D.ALLOW_POSITION_WRITE);
      //asse z
        axesLabels[2] = new Text3D(axesFont, AXIS_Z);
        axesLabels[2].setCapability(Text3D.ALLOW_POSITION_WRITE);
    }

    /**
     * Crea gli assi con i testi e le divisioni impostate dal costruttore
     * @param sizes array di {@code double} che contiene le misure degli assi
     */
    public void generateAxes (double... sizes)
    {
        if(sizes.length != 3) throw new IllegalArgumentException("Sizes' length must be 3");
        dtmAxes = new BranchGroup();
        TransformGroup rotateTG = new TransformGroup();
      //per ruotare gli assi in modo che siano coerenti con il DTM
        Transform3D t3d = new Transform3D();
        t3d.rotX(-Math.PI);     //perché è una CCW rotation
        rotateTG.setTransform(t3d);
      //creo i 3 assi
        BranchGroup xAxis = createAxis(sizes[0], divisions[0], axesLabels[0]);
        BranchGroup yAxis = createAxis(sizes[1], divisions[1], axesLabels[1]);
        BranchGroup zAxis = createAxis(sizes[2], divisions[2], axesLabels[2]);
      //i 3 assi sono adagiati su X, ruoto Y e Z con Z verso l'alto
      //TODO: RUOTARE GLI ASSI ABSOLUTELYAMENTE
      //aggiungo i 3 assi alla Transform3D
        rotateTG.addChild(xAxis);
        rotateTG.addChild(yAxis);
        rotateTG.addChild(zAxis);

        dtmAxes.addChild(rotateTG);
        dtmAxes.compile();
    }

    /**
     * Crea il {@link BranchGroup} che contiene l'asse
     * @param size      la grandezza dell'asse
     * @param numDiv    il numero di suddivisioni dell'asse
     * @param axisName  il nome che sarà visualizzato nell'asse
     * @return un {@link BranchGroup} che contiene l'asse
     */
    private BranchGroup createAxis(double size, int numDiv, Text3D axisName)
    {
     //il ramo che contiene tutti gli elementi della mia asse
        BranchGroup axisGroup = new BranchGroup();
        TransformGroup axisTg = new TransformGroup();
        axisTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        axisGroup.addChild(axisTg);
        Appearance axisApp = createAxisApp();
        Shape3D axisShape = new Shape3D();
        axisShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        axisShape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

     //l'array delle divisioni
        LineArray notches = createNotches(numDiv, size);
     //le etichette delle suddivisioni
        Text3D[] labels;

        if(axisName.getString().equals(AXIS_X))
        {
            labels = createLabels(numDiv, size, true);
        }
        else
        {
            labels = createLabels(numDiv, size, false);
        }

     //l'asse, formata da una linea e 2 triangoli
        LineArray axisLine = new LineArray(2, LineArray.COORDINATES |
                                                    LineArray.COLOR_3);
        IndexedTriangleArray axisArrow = createArrow(size);
     //costruisco l'asse
        axisLine.setCoordinate(0, new Point3d());               //(0,0,0)                   (size,0,0)
        axisLine.setCoordinate(1, new Point3d(size, 0, 0));    //   |---------------------------|
     //unisco i 3 array in un unica Shape3D
        axisShape.addGeometry(notches);     //suddivisioni
        axisShape.addGeometry(axisArrow);   //freccia
        axisShape.addGeometry(axisLine);    //asse
        //etichette
        for (int i = 0; i < labels.length; i++)
        {
            axisShape.addGeometry(labels[i]);
        }
     //imposto come apparirà l'asse
        axisShape.setAppearance(axisApp);
     //aggiungo l'asse al TransformGroup
        axisTg.addChild(axisShape);
     //asse creato, ritorno al chiamante
        return axisGroup;
    }

    /**
     * Costruisce i 2 triangoli che formano il verso positivo dell'asse
     * @param size  la grandezza dell'asse
     * @return un {@link IndexedTriangleArray} che contiene i vertici dei 2
     * triangoli che formano il verso positivo
     */
    private IndexedTriangleArray createArrow (double size)
    {
     //la lunghezza di ogni divisione, pari a un ventesimo della lunghezza della
     //intera asse
        double arrowHigh = size * 0.05d;
        IndexedTriangleArray axisArrow = new IndexedTriangleArray(5,
                            IndexedTriangleArray.COORDINATES |
                            IndexedTriangleArray.COLOR_3,
                            6);
             //costruisco la freccia
        //primo triangolo
        axisArrow.setCoordinate(0, new Point3d(size, 0, 0));        //vertice superiore
        axisArrow.setCoordinate(1, new Point3d(size - arrowHigh, arrowHigh/2, 0));
        axisArrow.setCoordinate(2, new Point3d(size - arrowHigh, -arrowHigh/2, 0));
        //secondo triangolo della freccia
        axisArrow.setCoordinate(3, new Point3d(size - arrowHigh, 0, arrowHigh/2));
        axisArrow.setCoordinate(4, new Point3d(size - arrowHigh, 0, -arrowHigh/2));
     //creo gli indici dei 2 triangoli
        //primo triangolo
        axisArrow.setCoordinateIndex(0, 0);
        axisArrow.setCoordinateIndex(1, 1);
        axisArrow.setCoordinateIndex(2, 2);
        //secondo triangolo
        axisArrow.setCoordinateIndex(3, 0);
        axisArrow.setCoordinateIndex(4, 3);
        axisArrow.setCoordinateIndex(5, 4);
        return axisArrow;
    }

    /**
     * Crea l'{@link Appearance} dell'asse.
     * @return un oggetto di tipo {@link Appearance} che contiene le informazioni
     * su come sarà visualizzata l'asse
     */
    private Appearance createAxisApp()
    {
        Appearance app = new Appearance();
     //triangoli riempiti
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);
     //triangoli e linee del colore LINE_COLOR
        ColoringAttributes ca = new ColoringAttributes(LINE_COLOR,
                                            ColoringAttributes.FASTEST);
        ca.setColor(LINE_COLOR);
     //imposto la APPEARANCE
        app.setPolygonAttributes(pa);
        app.setColoringAttributes(ca);

        return app;
    }

    /**
     * Crea le suddivisioni dell'asse. Sono lunghe il 5% della {@code size} dell'asse.
     * @param numDiv    il numero di suddivisioni da mettere sull'asse
     * @param size      la lunghezza dell'asse
     * @return un {@link LineArray} contenente i vertici delle suddivisioni
     *
     */
    private LineArray createNotches(int numDiv, double size)
    {
      //-2 perché c'è il triangolo di fine asse che copre le ultime divisioni
        int numOfNotches = numDiv - 2;
      //imposto il numero di etichette delle suddivisioni degli assi
        Text3D[] labels = new Text3D[numOfNotches];
      //le effettive suddivisioni
        LineArray notches = new LineArray(numOfNotches, LineArray.COORDINATES |
                                                        LineArray.COLOR_3);
      //la distanza di ogni divisione rispetto alla precedente
        double notchDistance = size / numDiv;
      //faccio le suddivisioni grandi un ventesimo rispetto alla size dell'asse
        double notchLength = size * 0.05d;
      //prossimo punto dove mettere la suddivisione
        Point3d nextPoint = new Point3d(0.0, notchLength/2, 0);
        int numOfVertices = numOfNotches * 2;
      //creo le suddivisioni
        for (int i = 0; i < numOfVertices; i += 2)
        { //prossima x dove posizionare la suddivisione
            nextPoint.x += notchDistance;
          //primo punto in alto
            notches.setCoordinate(i, nextPoint);    //(0,0,0)  (nl,nl/2,0)
                                                         //   |----------|----------|
          //prossimo vertice collocato in basso               (nl,-nl/2,0)
            i++;
            nextPoint.y -= notchLength;
          //veritce in basso
            notches.setCoordinate(i, nextPoint);
          //ripristino la y del punto in alto
            nextPoint.y += notchLength;
        }

        return notches;
    }

    /**
     * Costruisce le etichette da scrivere sopra o sotto gli assi a seconda
     * del valore di {@code belowAxis}
     * @param numDiv        il numero di suddivisioni dell'asse
     * @param size          la lunghezza dell'asse
     * @param belowAxis     sopra (= {@code false}) o sotto (={@code true}) l'asse
     * @return le etichette inizializzate e posizionate nel mondo 3D
     */
    private Text3D[] createLabels(int numDiv, double size, boolean belowAxis)
    {
        Font3D font3d = new Font3D(Font.getFont(Font.SERIF), null);
        Text3D[] labels = new Text3D[numDiv - 2];
      //la distanza di ogni divisione rispetto alla precedente
        float notchDistance = (float) (size / numDiv);
        float labelDistance = (float) size * 0.03f;
      //la distanza delle etichette dagli assi
        Point3f nextPoint = new Point3f(0.0f, labelDistance, 0.0f);
      //se ho chiesto di mettere le etichette al di sotto dell'asse
        if (belowAxis)
        { //posiziono al di sotto le etichette
            nextPoint.y -= (2 * labelDistance);
        }
      //setto le etichette delle suddivisioni
        for (int i = 0; i < labels.length; i++)
        {
            nextPoint.x += notchDistance;
            labels[i] = new Text3D(font3d, Float.toString(nextPoint.x));
            labels[i].setAlignment(Text3D.ALIGN_CENTER);
            labels[i].setPath(Text3D.PATH_LEFT);
            labels[i].setPosition(nextPoint);
        }
        return labels;
    }

}
