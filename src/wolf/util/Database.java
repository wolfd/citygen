package wolf.util;

import java.sql.*;

import wolf.city.City;
import wolf.city.block.CityBlock;
import wolf.city.road.Intersection;
import wolf.city.road.Road;

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

		//roads
		s.executeUpdate("DROP TABLE IF EXISTS ROADS;");
		s.executeUpdate("CREATE TABLE ROADS (id, a, b, type, width);");
		{
			PreparedStatement p = con.prepareStatement("INSERT INTO ROADS VALUES (?, ?, ?, ?, ?);");
			for(int i=0; i<c.rm.roads.size(); i++){
				Road r = c.rm.roads.get(i);
				p.setInt(1, i);
				p.setInt(2, r.a.id);
				p.setInt(3, r.b.id);
				p.setString(4, r.getType().toString());
				p.setInt(5, r.width);
				p.addBatch();
			}

			p.executeBatch();
		}
		//intersections
		{
			s.executeUpdate("DROP TABLE IF EXISTS INTERSECTIONS;");
			s.executeUpdate("CREATE TABLE INTERSECTIONS (id, x, y, z);");

			PreparedStatement p = con.prepareStatement("INSERT INTO INTERSECTIONS VALUES (?, ?, ?, ?);");
			for(int i=0; i<Intersection.intersections.size(); i++){
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
		
		commit();
	}
	public void nothing() throws SQLException{
		Statement stat = con.createStatement();
		stat.executeUpdate("drop table if exists people;");
		stat.executeUpdate("create table people (name, occupation);");
		PreparedStatement prep = con.prepareStatement("insert into people values (?, ?);");

		prep.setString(1, "Gandhi");
		prep.setString(2, "politics");
		prep.addBatch();
		prep.setString(1, "Turing");
		prep.setString(2, "computers");
		prep.addBatch();
		prep.setString(1, "Wittgenstein");
		prep.setString(2, "smartypants");
		prep.addBatch();

		con.setAutoCommit(false);
		prep.executeBatch();
		con.setAutoCommit(true);

		ResultSet rs = stat.executeQuery("select * from people;");
		while (rs.next()) {
			System.out.println("name = " + rs.getString("name"));
			System.out.println("job = " + rs.getString("occupation"));
		}
		rs.close();
		con.close();
	}
}
