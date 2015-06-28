package com.wolfd.citygen.util;

import java.sql.*;
import java.util.ArrayList;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.city.block.CityBlock;
import com.wolfd.citygen.city.block.Lot;
import com.wolfd.citygen.city.road.Intersection;
import com.wolfd.citygen.city.road.Road;

public class Database {
	Connection con;

	public Database() throws ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
	}

	public void open(String databaseLocation) throws SQLException{
		con = DriverManager.getConnection("jdbc:sqlite:"+databaseLocation);
		con.setAutoCommit(false);
	}

	public boolean commit(){
		try {
			con.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}	
	}

	public boolean close(){
		try {
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void saveCityData(City c) throws SQLException{
		open("city.db");
		Statement s = con.createStatement();
		//city info
		s.executeUpdate("DROP TABLE IF EXISTS CITY;");
		s.executeUpdate("CREATE TABLE CITY (name, xSize, ySize);");
		PreparedStatement ps = con.prepareStatement("INSERT INTO CITY VALUES (?,?,?)");
		ps.setString(1, "City");
		ps.setInt(2, c.sizeX);
		ps.setInt(3, c.sizeY);
		ps.addBatch();
		ps.executeBatch();
		//roads
		s.executeUpdate("DROP TABLE IF EXISTS ROADS;");
		s.executeUpdate("CREATE TABLE ROADS (id, a, b, type, width);");
		{
			PreparedStatement p = con.prepareStatement("INSERT INTO ROADS VALUES (?, ?, ?, ?, ?);");
			ArrayList<Road> roadList = (ArrayList<Road>) c.rm.roads.queryAll();
			for(int i=0; i<roadList.size(); i++){
				Road r = roadList.get(i);
				p.setInt(1, i);
				p.setInt(2, r.a.id);
				p.setInt(3, r.b.id);
				p.setString(4, r.getType().toString());
				p.setDouble(5, r.width);
				p.addBatch();
			}

			p.executeBatch();
		}
		//intersections
		{
			s.executeUpdate("DROP TABLE IF EXISTS INTERSECTIONS;");
			s.executeUpdate("CREATE TABLE INTERSECTIONS (id, x, y, z);");

			PreparedStatement p = con.prepareStatement("INSERT INTO INTERSECTIONS VALUES (?, ?, ?, ?);");
			for(int i=0; i< Intersection.intersections.size(); i++){
				Intersection is = Intersection.intersections.get(i);
				p.setInt(1, is.id);
				p.setDouble(2, is.pos.x);
				p.setDouble(3, is.pos.y);
				p.setDouble(4, is.pos.z);
				p.addBatch();
			}

			p.executeBatch();
		}
		//blocks
		{
			s.executeUpdate("DROP TABLE IF EXISTS BLOCKS;");
			s.executeUpdate("CREATE TABLE BLOCKS (id, polygon, lots);");

			PreparedStatement p = con.prepareStatement("INSERT INTO BLOCKS VALUES (?, ?, ?);");
			for(int i=0; i<c.bm.blocks.size(); i++){
				CityBlock b = c.bm.blocks.get(i);
				p.setInt(1, i);
				p.setString(2, b.shape.toString());
				p.setString(3, b.lots.toString());
				p.addBatch();
			}

			p.executeBatch();
		}

		//lots
		{
			s.executeUpdate("DROP TABLE IF EXISTS LOTS;");
			s.executeUpdate("CREATE TABLE LOTS (blockid, polygon, buildingpolygon, buildingheight);");

			PreparedStatement p = con.prepareStatement("INSERT INTO LOTS VALUES (?, ?, ?, ?);");
			for(int i=0; i<c.bm.blocks.size(); i++){
				CityBlock b = c.bm.blocks.get(i);
				for(int j=0; j<b.lots.size(); j++){
					Lot l = b.lots.get(j);
					
					p.setInt(1, i);
					p.setString(2, l.shape.toString());
					if(l.building != null){
					p.setString(3, l.building.g.toString());
					p.setInt(4, l.building.height);
					}
					p.addBatch();
				}
				
			}

			p.executeBatch();
		}

		commit();
	}
}
