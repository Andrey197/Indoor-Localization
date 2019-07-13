import java.awt.geom.Point2D;
import java.util.HashMap;

// Structure for store a fingerprint
public class Fingerprint {
	private int id;
	private String room;
	private boolean door;
	Point2D.Double location;
	HashMap<String, Integer> fp;
	
	public Fingerprint(int id, String room, double lat, double lng, HashMap<String, Integer> fp) {
		this.id = id;
		this.room = room;
		this.door = false;
		this.location = new Point2D.Double(lat, lng);
		this.fp = fp;
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
