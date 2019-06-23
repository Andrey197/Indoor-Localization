import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Marker {
	private int id;
	private String room;
	private boolean door;
	public ArrayList<Integer> neighbours;
	private int i;
	private int j;
	private Point2D.Double location;
	
	public Marker(int id, int i, int j, String room, Point2D.Double location) {
		this.id = id;
		this.i = i;
		this.j = j;
		this.room = room;
		this.door = false;
		this.neighbours = new ArrayList<Integer>();
		this.location = location;
	}
	
	public void addNeighbour(int x) {
		this.neighbours.add(x);
	}
	
	public String toString() {
		//return Integer.toString(id) + "(room " + this.room + ")";
		return room;
	}
	
	public int getId() {
		return id;
	}
	
	public String getRoom() {
		return room;
	}
	
	public Point2D.Double getLocation() {
		return location;
	}
	
	public void setDoor(boolean door) {
		this.door = door;
	}
	
	public boolean isDoor() {
		if (this.door == true) {
			return true;
		} else {
			return false;
		}
	}
}
