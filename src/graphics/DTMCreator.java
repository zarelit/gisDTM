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

import com.marcrh.graph.Point;
import com.marcrh.graph.Range;
import com.marcrh.graph.delaunay.Triad;
import com.marcrh.graph.delaunay.Voronoi;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.PointArray;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**
 *  Si occupa di creare il DTM effettuando la triangolazione di Delaunay
 * @author giuliano
 */
public class DTMCreator {
    //la lista dei punti da triangolare
    private LinkedList<Point3d> pointsList;
    //lista dei triangoli da visualizzare
    private LinkedList<DTMTriangle> triangles;
    //le coordinate X, Y massime/minime della lista di punti
    private double[] maxCoord = {Double.MIN_VALUE, Double.MIN_VALUE};
    private double[] minCoord = {Double.MAX_VALUE, Double.MAX_VALUE};

    public DTMCreator(LinkedList<Point3d> pointsList) {
        this.pointsList = pointsList;
    }

    public void createTriangles(){
        System.out.println("Inizio : " + System.currentTimeMillis());
        //controllo se le coordinate sono state settate
        if (maxCoord[0] == Double.MIN_VALUE || maxCoord[1] == Double.MIN_VALUE
         || minCoord[0] == Double.MAX_VALUE || minCoord[1] == Double.MAX_VALUE){
            System.err.println("Coordinate non inizializzate. Si prega di impostarle");
            return;
        }
    }

    public void createTestPoints(){
        triangles = createTriangles(null);
    }

    /**
     * Crea i vari triangoli da visualizzare
     * @param iterator iteratore per scorrere attraverso la lista di punti 3d
     * @return una {@link LinkedList} contenente tutti i triangoli di Delaunay
     */
    private LinkedList<DTMTriangle> createTriangles(Iterator<Point3d> iterator) {
        //è il vero DTM formato dai vari prismi
        /*TODO pensare se è necessario fare una lista o no*/
        LinkedList<DTMTriangle> dtm = new LinkedList<DTMTriangle>();
        //cambio in coordinate relative
        relCoordinates();
        PointArray points = new PointArray(this.pointsList.size(),
                                           PointArray.COORDINATES);
        //TESTING
        Iterator<Point3d> it = this.pointsList.iterator();
        Point3d p;
        int index = 0;
        while (it.hasNext()) {
            p = it.next();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Punto {0} aggiunto", index);
            points.setCoordinate(index ++ , p);
        }
        dtm.add(new DTMTriangle());
        //////////////////////////////////
        return dtm;
    }

    /**
     * calcola le coordinate relative al punto di origine O(minX, minY)
     */
    private void relCoordinates(){
        Iterator<Point3d> it = this.pointsList.iterator();
        Point3d point3d;
        while (it.hasNext()) {
            point3d = it.next();
            point3d.x = point3d.x - this.minCoord[0];
            point3d.y = point3d.y - this.minCoord[1];
            point3d.z = 0;
        }
    }

    public LinkedList<DTMTriangle> getTriangles() {
        relCoordinates();
        return triangles;
    }

    /**
     * @return the maxCoord
     */
    public double[] getMaxCoord() {
        return maxCoord;
    }

    /**
     * @return the minCoord
     */
    public double[] getMinCoord() {
        return minCoord;
    }

    /**
     * @param maxCoord the maxCoord to set
     */
    public void setMaxCoord(double[] maxCoord) {
        this.maxCoord = maxCoord;
    }

    /**
     * @param minCoord the minCoord to set
     */
    public void setMinCoord(double[] minCoord) {
        this.minCoord = minCoord;
    }

    /**
     * @param x la coordinata x minima dei punti del DTM
     * @param y la coordinata y minima dei punti del DTM
     * @param z la coordinata z minima dei punti del DTM
     */
    public void setMinCoord(double x, double y) {
        minCoord[0] = x;
        minCoord[1] = y;
    }

    /**
     * @param x la coordinata x massima dei punti del DTM
     * @param y la coordinata y massima dei punti del DTM
     * @param z la coordinata z massima dei punti del DTM
     */
    public void setMaxCoord(double x, double y) {
        maxCoord[0] = x;
        maxCoord[1] = y;
    }

    public static LinkedList<DTMTriangle> PerturbDelaunay(LinkedList<Point3d> data, Range r,
                double maxPerturbN, double maxPerturbE){
        System.out.println("Perturbazione dei dati.");
        //perturbazione dei punti per evitare spiacevoli allineamenti
        //che non fanno convergere l'algoritmo
        double pertE;
        double pertN;
        maxPerturbE=0.2*maxPerturbE;
        maxPerturbN=0.2*maxPerturbN;
        for(Point3d p:data){
            pertE=Math.random()*maxPerturbE;
            pertN=Math.random()*maxPerturbN;
            p.setX(p.getX()+pertE);
            p.setY(p.getY()+pertN);
        }

        //il range è leggermente più grande del previsto
        System.out.println("Fine della perturbazione dei dati.");

        //chiama l'algoritmo originale
        return Delaunay(data,r);
    }
    /**
     *
     * @param data la lista di punti (solitamente fornita da GisDb)
     * @return Una lista di triangoli (cioè tre vertici 3d) usati poi per il rendering.
     */
    public static LinkedList<DTMTriangle> Delaunay(LinkedList<Point3d> data, Range r){
        LinkedList<DTMTriangle> finalResult=new LinkedList<DTMTriangle>();

        //bisogna convertire da LinkedList<Point3d> ad ArrayList<Point>
        //Point3d appartiene a vecmath, Point a js-hull
        //quindi la conversione non è immediata
        ArrayList<Point> points=new ArrayList<Point>(data.size());

        for(Point3d p:data){
            //La quota dei punti è NULLA! in quanto la triangolazione deve
            //avvenire solo sulla planimetria.
            Point item=new Point(p.x,p.y,0);
            points.add(item);
        }

        //ora points contiene i punti per Voronoi
        Voronoi v=new Voronoi();

        //ordiniamo alla libreria di triangolare con l'algoritmo di sweep-hull
        //altrimenti noto come s-hull
        v.generate(points, r);

        //Lettura del risultato (espresso come una lista di Triad)
        //e costruzione di una LinkedList di DTMTriangle
        ArrayList<Triad> voronoiTriangs=v.getTriads();

        ArrayList<Point3d> dati=new ArrayList(data);

        //per ogni triangolo fornitoci da Voronoi
        //dobbiamo creare un DTMTriangle e aggiungerlo alla lista
        for(Triad t:voronoiTriangs){
            DTMTriangle singleShape;

            //t.a, t.b e t.c sono INDICI dei punti sia in points che in data
            singleShape=new DTMTriangle(dati.get(t.a),dati.get(t.b),dati.get(t.c));

            finalResult.add(singleShape);

            //test per capire come funzionano i Triad
            //System.out.print("Triangolo:");
            //System.out.print(data.get(t.b) + " ");
            //System.out.print(data.get(t.a) + " ");
            //System.out.println(data.get(t.c));
        }

        return finalResult;
    }
}