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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;

/**
 * Abstraction/access layer to the points database.
 *
 * It provides method for database creation/check/manipulation/query
 *
 * How to use this class:
 * - Create an instance specifying the DB filename:
 *  GisDb data=new GisDb("databasefile.db");
 *
 * - If you want to fill the database with data you must use "addPoints": read
 * the UtmGridHeightReader documentation to find how to read the FVG DTM data files.
 * - If you want to get the minimum/maximum/average along an axis use
 *  getMin() getMax() and getAvg() methods.
 * - If you want a LinkedList with all the points stored in the database
 * call getPointsList();
 * @author David Costa <david@zarel.net>
 */
public class GisDb {
    /**
     * Contains the full/relative path to the points database.
     */
    private String dbPath;
    /**
     * the JDBC connection to the points database.
     */
    private Connection conn;

    /**
     * The constructor. It opens a JDBC connection and creates the specified
     * file if it doesn't exist.
     *
     * It is NONSENSE to open or create a database without a filename, so
     * a filename is _mandatory_
     *
     * @param filename the file that contains/will contain the data.
     * @throws IllegalArgumentException
     */
    public GisDb(String filename) throws IllegalArgumentException,Exception{
        //empty filename = error!
        if(!filename.isEmpty()){
            dbPath=filename;
        }else{
            dbPath="";
            throw new
                IllegalArgumentException("The DB filename cannot be empty.");
        }

        //check wheter filename exists or not
        if(new File(filename).exists()){
            //database exists, open it with JDBC and check schema
            if(!initializeConnection()) throw new Exception("Cannot open DB");
            if(!checkSchema()) throw new IllegalArgumentException("It's not a valid DB");
        }else{
            //database doesn't exist, create it and fill in the schema
            if(!initializeConnection()) throw new Exception("Cannot create DB");
            initializeSchema();
        }
    }

    /**
     * Open the JDBC connection for reading. It creates the database if it
     * doesn't exist. The filename will be read from the object attribute.
     * @see #dbPath
     * @return whether the connection to the DB succeeded.
     */
    private boolean initializeConnection(){
        //do we have a filename to connect to?
        if(dbPath.isEmpty()) return false;

        //do we have the correct JDBC driver?
        try {
            Class.forName("org.sqlite.JDBC");
            conn=DriverManager.getConnection("jdbc:sqlite:"+dbPath);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GisDb.class.getName())
                    .log(Level.SEVERE, "SQLite JDBC driver not installed.", ex);
            return false;
        } catch(SQLException sqle){
            Logger.getLogger(GisDb.class.getName())
                    .log(Level.SEVERE, "Cannot open/create database:"+dbPath);
            return false;
        }

        return true;
    }

    public double getMaxPerturb(Axis a) throws SQLException{
        ResultSet res;
        Double perturb;
        String sqlGetPerturb="SELECT (max("+a+")-min("+a+"))/count(distinct "+a+") "+
                "FROM points WHERE "+a+" IS NOT NULL";
        Statement q=conn.createStatement();
        res=q.executeQuery(sqlGetPerturb);
        perturb=res.getDouble(1);
        res.close();
        return perturb;  
    }
    /**
     * verifies that the file has the expected schema.
     * @return whether the schema is okay or not.
     */
    private boolean checkSchema(){
        //in order to verify the schema
        //we will select a maximum of one row from each table, specifying
        //every single attribute.
        //WARNING: the schema is hardcoded
        try{
            Statement q=conn.createStatement();
            q.executeQuery("SELECT northing, easting, height FROM points LIMIT 1")
                    .close();
        } catch (SQLException e){
            return false;
        }
        return true;
    }

    /**
     * writes the schema in the database file.
     */
    private void initializeSchema() throws SQLException{
        Statement q=conn.createStatement();
        String sql;
        sql="CREATE TABLE points(northing REAL, easting REAL, ";
        sql=sql+"height REAL, PRIMARY KEY(northing,easting))";
        q.execute(sql);
    }

    /**
     * store points into the database. The list is tipically provided by a PointsReader
     * like UtmGridHeightReader.
     * @param data the points to be saved into the database.
     * @param nullSpecialValue a value that actually means NULL. For example
     * -9999 is a popular value to specify "no data".
     * @throws SQLException
     */
    public void addPoints(LinkedList<Point3d> data, double nullSpecialValue)throws SQLException{
        //start transaction
        conn.setAutoCommit(false);

        PreparedStatement prep = conn.prepareStatement(
            "INSERT OR IGNORE INTO points VALUES (?, ?, ?)");

        //foreach cycle
        //see http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html
        for( Point3d p:data ){
            prep.setDouble(1, p.y); //northing
            prep.setDouble(2, p.x); //easting
            //check for nulls (-9999)
            if(p.z != nullSpecialValue)
                prep.setDouble(3, p.z); //height
            else
                prep.setNull(3, java.sql.Types.NULL);

            //queue operation in the transaction
            prep.addBatch();
        }
        try{
            //execute the transaction
            prep.executeBatch();
            conn.commit();
        }catch(SQLException sqle){
            Logger.getLogger(GisDb.class.getName())
                    .log(Level.WARNING, "Cannot insert a row");
            System.err.println(sqle);
            System.err.println("Rollbacking...");
            conn.rollback();
        }
    }


    /**
     * reads all the points stored in the database and returns them as a
     * LinkedList of vecmath 3d double-based points. Points with NULL height will
     * be discarded.
     * @return a LinkedList of points.
     */
    public LinkedList<Point3d> getPointsList() throws SQLException{
        double north,east,z;
        String selectAll="SELECT * FROM points WHERE height IS NOT NULL";
        Statement q = conn.createStatement();
        ResultSet rs = q.executeQuery(selectAll);
        LinkedList <Point3d> resultList = new LinkedList<Point3d>();
        while (rs.next()) {
            north = rs.getDouble("northern");
            east = rs.getDouble("eastern");
            z = rs.getDouble("height");
            Point3d p = new Point3d(east, north, z);
            resultList.add(p);
        }
        rs.close();
        return resultList;
    }

    /**
     * Find the minimum value of a certain axis in the data.
     * @param a Direction of which we're about to calculate the minimum.
     * @see Axis
     * @return the minimum value found
     * @throws SQLException
     */
    public double getMin(Axis a) throws SQLException{
        ResultSet res;
        Double min;
        String sqlGetMin="SELECT min("+a+") FROM points WHERE "+a+" IS NOT NULL";
        Statement q=conn.createStatement();
        res=q.executeQuery(sqlGetMin);
        min=res.getDouble(1);
        res.close();
        return min;
    }

    /**
     * Find the maximum value of a certain axis in the data.
     * @param a Direction of which we're about to calculate the maximum.
     * @see Axis
     * @return the maximum value found
     * @throws SQLException
     */
    public double getMax(Axis a) throws SQLException{
        ResultSet res;
        Double max;
        String sqlGetMax="SELECT max("+a+") FROM points WHERE "+a+" IS NOT NULL";
        Statement q=conn.createStatement();
        res=q.executeQuery(sqlGetMax);
        max=res.getDouble(1);
        res.close();
        return max;  
    }

    /**
     * Calculate the average of the data in a particular direction.
     * @see #getMax
     * @param a the Axis we are averaging to.
     * @return the data average on that axis.
     * @throws SQLException
     */
    public double getAvg(Axis a) throws SQLException{
        ResultSet res;
        Double avg;
        String sqlGetAvg="SELECT avg("+a+") FROM points WHERE "+a+" IS NOT NULL";
        Statement q=conn.createStatement();
        res=q.executeQuery(sqlGetAvg);
        avg=res.getDouble(1);
        res.close();
        return avg;  
    }
    
    /**
     * @return the path of the opened DB
     */
    public String getName(){
        return dbPath;
    }
    
    /**
     * Retrieve all points in a certain range
     */
    public LinkedList<Point3d> getPoints(
            double minN, double minE, double maxN, double maxE)
            throws SQLException
    {
        double north,east,z;
        ResultSet rs;
        String selectPart="SELECT * FROM points WHERE height IS NOT NULL"+
                          " AND northing BETWEEN ? and ?"+
                          " AND easting BETWEEN ? and ?";
        PreparedStatement q=conn.prepareStatement(selectPart);
        q.setDouble(1,minN);
        q.setDouble(2,maxN);
        q.setDouble(3,minE);
        q.setDouble(4,maxE);
        q.executeQuery();
        rs=q.executeQuery();
        
        LinkedList <Point3d> resultList = new LinkedList<Point3d>();
        while (rs.next()) {
            north = rs.getDouble("northing");
            east = rs.getDouble("easting");
            z = rs.getDouble("height");
            Point3d p = new Point3d(east, north, z);
            resultList.add(p);
        }
        rs.close();
        return resultList;
    }
    
    /**
     * count how many points are in the defined region
     */
    public int countPoints(double minN, double minE, double maxN, double maxE)
            throws SQLException
    {
        ResultSet rs;
        int count;
        String selectPart="SELECT count(*) FROM points WHERE height IS NOT NULL"+
                          " AND northing BETWEEN ? and ?"+
                          " AND easting BETWEEN ? and ?";
        PreparedStatement q=conn.prepareStatement(selectPart);
        q.setDouble(1,minN);
        q.setDouble(2,maxN);
        q.setDouble(3,minE);
        q.setDouble(4,maxE);
        q.executeQuery();
        rs=q.executeQuery();
        count=rs.getInt(1);
        rs.close();
        return count;
    }
}
