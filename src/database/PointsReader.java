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
import java.util.LinkedList;
import javax.vecmath.*;

/**
 * a generic data reader which abstracts from the data source
 * (is it a file/socket/other thing?).
 * @author David Costa <david@zarel.net>
 */
public interface PointsReader {
    /**
     * make the reader read all the points at the specified source.
     * @see #setSource
     * @return a LinkedList with all the read points.
     */
    public LinkedList<Point3d> getAllPoints() throws Exception;
    /**
     * Tell the reader where to obtain the points data.
     * 
     * @param source specifies what data source to use.
     * This parameter is implementation-dependent, it could simply a filename
     * or a URL or other things.
     */
    public void setSource(String source) throws Exception;
    /**
     * Returns the double value used to specify "absent data"
     * @return the double value used to specify "absent data"
     */
    public double getNullValue();
}
