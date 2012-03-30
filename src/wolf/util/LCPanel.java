package wolf.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JPanel;

import wolf.city.City;
import wolf.city.Roadmap;
import wolf.city.road.Intersection;
import wolf.city.road.Road;
import wolf.city.road.RoadType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class LCPanel extends JPanel implements MouseListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2648698404305683130L;
	private Road testRoad;
	private Intersection movingIntersection;
	private LinkedList<Road> roads;
	public int windowHeight = 500;
	public int windowWidth = 500;
	private boolean mouseDown = false;
	private Coordinate mouseLoc;
	private Roadmap rm;
	private City c;
	private Road testedRoad;
	
	public LCPanel(){
		roads = new LinkedList<Road>();
		testRoad = new Road(new Intersection(new Coordinate(10,100)),new Intersection(new Coordinate(400,400)), RoadType.STREET);
		roads.add(new Road(new Intersection(new Coordinate(100,10)),new Intersection(new Coordinate(400,300)), RoadType.STREET));
		c = new City(windowHeight*4, windowHeight*4, 0);
		rm = new Roadmap(c);
		for(Road r: roads){
			rm.grid.add(r);
			rm.roads.add(r);
		}
		
		mouseLoc = new Coordinate(0,0);
		this.addMouseListener(this);
	}
	
	
	
	public void paintComponent(Graphics gr){
		gr.clearRect(0, 0, windowWidth, windowHeight);
		mouseLoc.x = MouseInfo.getPointerInfo().getLocation().x-getLocationOnScreen().x;
		mouseLoc.y = MouseInfo.getPointerInfo().getLocation().y-getLocationOnScreen().y;

		if(mouseDown){
			if(movingIntersection == null){
				movingIntersection = getNearestIntersection();
			}

			moveIntersection();
		}else{
			movingIntersection = null;
		}
		
		testedRoad = rm.localConstraints(new Road(testRoad));
		
		for(int i=0;i<roads.size(); i++){//render roads
			Geometry g = roads.get(i).getGeometry();
			
			gr.setColor(Color.DARK_GRAY);
			int[] x = new int[4];
			int[] y = new int[4];
			for(int j=0;j<4;j++){
				Coordinate p = g.getCoordinates()[j];
				x[j] = (int) p.x;
				y[j] = (int) p.y;
				
			}
			gr.drawOval((int)roads.get(i).a.pos.x-5, (int)roads.get(i).a.pos.y-5, 10, 10);
			gr.drawPolygon(x, y, 4);

		}
		{//render testRoad
			Geometry g = testRoad.getGeometry();
			
			gr.setColor(Color.RED);
			int[] x = new int[4];
			int[] y = new int[4];
			for(int j=0;j<4;j++){
				Coordinate p = g.getCoordinates()[j];
				x[j] = (int) p.x;
				y[j] = (int) p.y;
				
			}
			gr.drawOval((int)testRoad.a.pos.x-5, (int)testRoad.a.pos.y-5, 10, 10);
			gr.drawPolygon(x, y, 4);
		}
		if(testedRoad != null){//render testedRoad
			Geometry g = testedRoad.getGeometry();
			
			gr.setColor(Color.ORANGE);
			int[] x = new int[4];
			int[] y = new int[4];
			for(int j=0;j<4;j++){
				Coordinate p = g.getCoordinates()[j];
				x[j] = (int) p.x;
				y[j] = (int) p.y;
				
			}
			gr.fillPolygon(x, y, 4);
			//gr.drawPolygon(x, y, 4);
		}
		repaint();
	}
	
	public void moveIntersection(){
		getNearestIntersection();
		if(movingIntersection != null){
			movingIntersection.pos.x = mouseLoc.x;
			movingIntersection.pos.y = mouseLoc.y;
			for(Road i: c.rm.roads){
				if(i.a == movingIntersection || i.b == movingIntersection){
					System.out.println("moving");
					c.rm.grid.move(i);
				}
			}
			
		}
	}


	public Intersection getNearestIntersection(){
		
		double closestDistance = 25; //25 is largest distance to snap to
		Intersection closest = null;
		
		for(int i=0;i<roads.size();i++){
			if(roads.get(i).a.pos.distance(mouseLoc)<closestDistance) closest = roads.get(i).a;
			if(roads.get(i).b.pos.distance(mouseLoc)<closestDistance) closest = roads.get(i).b;
		}
		if(testRoad.a.pos.distance(mouseLoc)<closestDistance) closest = testRoad.a;
		if(testRoad.b.pos.distance(mouseLoc)<closestDistance) closest = testRoad.b;
		return closest;
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {	
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}
}
