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

import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point3d;

/**
 * modella ogni triangolo che forma la superficie del DTM
 * @author giuliano
 */
public class DTMTriangle {
    private List<Point3d> vertices;

    /**
     * Costruttore che accetta 3 punti come vertici
     * @param vertices i vertici del triangolo
     * @throws IllegalArgumentException se non si sono passati 3 punti
     */
    public DTMTriangle(Point3d... vertices) throws IllegalArgumentException{
        if(vertices.length != 3 ){
            throw new IllegalArgumentException("Si devono passare 3 vertici per costruire un triangolo");
        }
        this.vertices = new LinkedList<Point3d>();
        this.vertices.add(vertices[0]);
        this.vertices.add(vertices[1]);
        this.vertices.add(vertices[2]);
    }

    /**
     * Ritorna il vertice indicato da Index % 3
     * @param index l'indice del vertice. Se index > 3 torna il vertice index % 3
     * @return il vertice richiesto da index
     */
    public Point3d getVertex(int index)
    {
        if(index < 0) throw new IllegalArgumentException("Index < 0 is not valid");
        return vertices.get(index % 3);
    }

    /**
     * Ritorna i 3 vertici incapsulati in una lista
     * @return una {@link List} contenente i vertici del triangolo
     */
    public List<Point3d> getVertices() {
        return vertices;
    }

    /**
     * Setta i vertici del triangolo
     * @param vertices {@link List} di {@link Point3d}
     */
    public void setVertices(List<Point3d> vertices) {
        this.vertices = vertices;
    }
}