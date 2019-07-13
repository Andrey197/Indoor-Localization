import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

// An instance of this class represent an entry in the database
class Entry {
	HashMap<String, Integer> fingerprint;
	Point2D.Double location;
	
	public Entry(HashMap<String, Integer> fingerprint, Point2D.Double location) {
		this.fingerprint = fingerprint;
		this.location = location;
	}
}

public class DataManager {
	public Distances dis;							// Distances between sampled locations
	public FDistances parser;						// Distances between fingerprints
	public ArrayList<Entry> entries;				// Entries in database
	public HashMap<Integer, Integer> doorMap;		// Mapping of the doors
	public JSONArray ja;
	
	public DataManager() {
		entries = new ArrayList<Entry>();
		ja = new JSONArray();
		
		// Stress-free floor plan 
		dis = new Distances();
		dis.addNeighbours();
		dis.floydWarshall();
		int i, j;
		
		for (i = 0; i < dis.markers.length; i++) {
			for (j = 0; j < dis.markers.length; j++) {
				if (dis.markers[i][j] != null)
					System.out.print(dis.markers[i][j].getId() + " ");
				else 
					System.out.print(dis.markers[i][j] + " ");
			}
			System.out.println();
		}
		
		//System.out.println(dis.ms.get(2).getLocation());
		
		
		dis.writeMatrix("distances.txt", dis.D);
		
		//===========================================================================
		
		// Fingerprint space
		parser = new FDistances();
		int n = 0;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			File dir = new File("DataSet");
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					// Do something with child
					Document doc = builder.parse(child);
					n += parser.countFingerprints(doc);
				}
				parser.setFingerprintsNum(n);
				
				for (File child : directoryListing) {
					// Do something with child
					Document doc = builder.parse(child);
					parser.findFingerprints(doc, child.getName());
				}
				
				//System.out.println(parser.fs.get(159).location);
				
				parser.setDoors();
				parser.floydWarshall();
				parser.writeMatrix("fdistances.txt", parser.Dprim);
				
				parser.findRooms();
				parser.findDoors();
				
				for (i = 0; i < parser.roomsNum; i++) {
					System.out.println(parser.doorRoom[i]);
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 *  Door mapping algorithm
	 *  Is known that we have only 3 doors
	 */
	public void mapDoors() {
		this.doorMap = new HashMap<Integer, Integer>();
		
		double minDif = 999;
		int middleRoomP = 0;
		
		for (int i = 0; i < dis.doorRoom.length; i++) {
			ArrayList<Integer> others = new ArrayList<Integer>();
			for (int j = 0; j < dis.doorRoom.length; j++) {
				if (i != j) {
					others.add(dis.doorRoom[j]);
				}
			}
			if (Math.abs(dis.D[dis.doorRoom[i]][others.get(0)] - dis.D[dis.doorRoom[i]][others.get(1)]) < minDif) {
				minDif = Math.abs(dis.D[dis.doorRoom[i]][others.get(0)] - dis.D[dis.doorRoom[i]][others.get(1)]);
				//System.out.println(minDif);
				middleRoomP = i;
			}
		}
		
		minDif = 999;
		int middleRoomF = 0;
		
		for (int i = 0; i < parser.doorRoom.length; i++) {
			ArrayList<Integer> others = new ArrayList<Integer>();
			for (int j = 0; j < parser.doorRoom.length; j++) {
				if (i != j) {
					others.add(parser.doorRoom[j]);
				}
			}
			if (Math.abs(parser.Dprim[parser.doorRoom[i]][others.get(0)] - parser.Dprim[parser.doorRoom[i]][others.get(1)]) < minDif) {
				minDif = Math.abs(parser.Dprim[parser.doorRoom[i]][others.get(0)] - parser.Dprim[parser.doorRoom[i]][others.get(1)]);
				middleRoomF = i;
			}
		}
		
		//System.out.println(parser.doorRoom[middleRoomF]);
		
		double minDist = 999;
		double maxDist = 0;
		int indexMinF = 0;
		int indexMaxF = 0;
		for (int i = 0; i < parser.doorRoom.length; i++) {
			if (i != middleRoomF) {
				if (parser.Dprim[parser.doorRoom[i]][parser.doorRoom[middleRoomF]] < minDist) {
					minDist = parser.Dprim[parser.doorRoom[i]][parser.doorRoom[middleRoomF]];
					indexMinF = i;
				}
				
				if (parser.Dprim[parser.doorRoom[i]][parser.doorRoom[middleRoomF]] > maxDist) {
					maxDist = parser.Dprim[parser.doorRoom[i]][parser.doorRoom[middleRoomF]];
					indexMaxF = i;
				}
				//System.out.println(parser.doorRoom[i] + " " + parser.Dprim[parser.doorRoom[i]][parser.doorRoom[middleRoomF]]);
			}
		}
		
		minDist = 999;
		maxDist = 0;
		int indexMinP = 0;
		int indexMaxP = 0;
		for (int i = 0; i < dis.doorRoom.length; i++) {
			if (i != middleRoomP) {
				if (dis.D[dis.doorRoom[i]][dis.doorRoom[middleRoomP]] < minDist) {
					minDist = dis.D[dis.doorRoom[i]][dis.doorRoom[middleRoomP]];
					indexMinP = i;
				}
				
				if (dis.D[dis.doorRoom[i]][dis.doorRoom[middleRoomP]] > maxDist) {
					maxDist = dis.D[dis.doorRoom[i]][dis.doorRoom[middleRoomP]];
					indexMaxP = i;
				}
				//System.out.println(dis.doorRoom[i] + " " + dis.D[dis.doorRoom[i]][dis.doorRoom[middleRoomP]]);
			}
		}
		
		System.out.println(parser.doorRoom[indexMinF] + " --- " + dis.doorRoom[indexMinP]);
		System.out.println(parser.doorRoom[indexMaxF] + " --- " + dis.doorRoom[indexMaxP]);
		System.out.println(parser.doorRoom[middleRoomF] + " --- " + dis.doorRoom[middleRoomP]);
		
		this.doorMap.put(middleRoomP, middleRoomF);
		Entry e = new Entry(parser.fs.get(parser.doorRoom[middleRoomF]).fp, dis.ms.get(dis.doorRoom[middleRoomP]).getLocation());
		entries.add(e);
		
		this.doorMap.put(indexMinP, indexMinF);
		e = new Entry(parser.fs.get(parser.doorRoom[indexMinF]).fp, dis.ms.get(dis.doorRoom[indexMinP]).getLocation());
		entries.add(e);
		
		this.doorMap.put(indexMaxP, indexMaxF);
		e = new Entry(parser.fs.get(parser.doorRoom[indexMaxF]).fp, dis.ms.get(dis.doorRoom[indexMaxP]).getLocation());
		entries.add(e);
	}
	
	// Room mapping algorithm
	public void mapRooms() {
		// Sampled locations from rooms
		ArrayList<ArrayList<Marker>> roomsP = new ArrayList<ArrayList<Marker>>();
		// Fingerprints from rooms
		ArrayList<ArrayList<Fingerprint>> roomsF = new ArrayList<ArrayList<Fingerprint>>();
		
		for (int i = 0; i < this.doorMap.size(); i++) {
			System.out.println(i + "->" + this.doorMap.get(i));
		}
		
		for (int i = 0; i < dis.doorRoom.length; i++) {
			ArrayList<Marker> neigh = new ArrayList<Marker>();
			
			for (int j = 0; j < dis.ms.size(); j++) {
				if (dis.ms.get(j).getRoom() == dis.ms.get(dis.doorRoom[i]).getRoom()) {
					neigh.add(dis.ms.get(j));
				}
			}
			
			roomsP.add(neigh);
		}
		
		/*for (int i = 0; i < roomsP.get(0).size(); i++) {
			System.out.print(roomsP.get(0).get(i).getId() + " ");
		}
		System.out.println();*/
		
		for (int i = 0; i < parser.doorRoom.length; i++) {
			ArrayList<Fingerprint> neigh = new ArrayList<Fingerprint>();
			
			for (int j = 0; j < parser.fingerprintRoom.length; j++) {
				if (parser.fingerprintRoom[j] == parser.fingerprintRoom[parser.doorRoom[i]]) {
					neigh.add(parser.fs.get(j));
				}
			}
			
			roomsF.add(neigh);
		}
		
		/*for (int i = 0; i < roomsF.get(1).size(); i++) {
			System.out.print(roomsF.get(1).get(i).getId() + " ");
		}
		System.out.println();*/
		
		// Sort markers from every room from the closest to room door to the farthest
		for (int i = 0; i < roomsP.size(); i++) {
			Collections.sort(roomsP.get(i), new Comparator<Marker>() {
				
				@Override
				public int compare(Marker o1, Marker o2) {
					// TODO Auto-generated method stub
					if (dis.D[o1.getId()][dis.doorRoom[dis.gridRoom[o1.getId()]-1]] > dis.D[o2.getId()][dis.doorRoom[dis.gridRoom[o2.getId()]-1]]) {
						return 1;
					} else if (dis.D[o1.getId()][dis.doorRoom[dis.gridRoom[o1.getId()]-1]] < dis.D[o2.getId()][dis.doorRoom[dis.gridRoom[o2.getId()]-1]]){
						return -1;
					} else {
						return 0;
					}
					
				}
				
			});
		}

		
		// Sort fingerprints from every room from the closest to room door to the farthest
		for (int i = 0; i < roomsF.size(); i++) {
			Collections.sort(roomsF.get(i), new Comparator<Fingerprint>() {
				
				@Override
				public int compare(Fingerprint o1, Fingerprint o2) {
					// TODO Auto-generated method stub
					if (parser.Dprim[o1.getId()][parser.doorRoom[parser.fingerprintRoom[o1.getId()]-1]] > parser.Dprim[o2.getId()][parser.doorRoom[parser.fingerprintRoom[o2.getId()]-1]]) {
						return 1;
					} else if (parser.Dprim[o1.getId()][parser.doorRoom[parser.fingerprintRoom[o1.getId()]-1]] < parser.Dprim[o2.getId()][parser.doorRoom[parser.fingerprintRoom[o2.getId()]-1]]){
						return -1;
					} else {
						return 0;
					}
					
				}
				
			});
		}
		
		for (int i = 0; i < this.doorMap.size(); i++) {
			System.out.println(roomsP.get(i).size() + " ----> " + roomsF.get(this.doorMap.get(i)).size());
			for (int j = 0; j < roomsP.get(i).size(); j++) {
				System.out.print(roomsP.get(i).get(j).getId() + " ");
			}
			System.out.println();
			for (int j = 0; j < roomsF.get(this.doorMap.get(i)).size(); j++) {
				System.out.print(roomsF.get(this.doorMap.get(i)).get(j).getId() + " ");
			}
			System.out.println();
		}
		
		for (int i = 0; i < this.doorMap.size(); i++) {
			int step = roomsF.get(this.doorMap.get(i)).size() / roomsP.get(i).size();
			
			for (int j = 0; j < roomsP.get(i).size(); j++) {
				for (int k = step * j; k < step * j + step; k++) {
					System.out.print(roomsP.get(i).get(j).getId() + " - " + roomsF.get(this.doorMap.get(i)).get(k).getId() + ", ");
					Entry e = new Entry(roomsF.get(this.doorMap.get(i)).get(k).fp, roomsP.get(i).get(j).getLocation());
					entries.add(e);
				}
			}
			System.out.println();
		}
			
	}
	
	// Corridor mapping algorithm
	public void mapCorridor() {
		ArrayList<Marker> corridorP = new ArrayList<Marker>();
		ArrayList<Fingerprint> corridorF = new ArrayList<Fingerprint>();
		
		for (int i = 0; i < dis.gridRoom.length; i++) {
			if (dis.gridRoom[i] == 0) {
				corridorP.add(dis.ms.get(i));
			}
		}
		//System.out.println(corridorP.size());
		
		for (int i = 0; i < parser.fingerprintRoom.length; i++) {
			if (parser.fingerprintRoom[i] == 0) {
				corridorF.add(parser.fs.get(i));
			}
		}
		
		Collections.sort(corridorP, new Comparator<Marker>() {

			@Override
			public int compare(Marker arg0, Marker arg1) {
				// TODO Auto-generated method stub
				int i = 0;
				if (dis.D[arg0.getId()][dis.doorRoom[i]] > dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return 1;
				} else if (dis.D[arg0.getId()][dis.doorRoom[i]] < dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		Collections.sort(corridorF, new Comparator<Fingerprint>() {

			@Override
			public int compare(Fingerprint o1, Fingerprint o2) {
				// TODO Auto-generated method stub
				int i = doorMap.get(0);
				
				if (parser.Dprim[o1.getId()][parser.doorRoom[i]] > parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return 1;
				} else if (parser.Dprim[o1.getId()][parser.doorRoom[i]] < parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		//System.out.println(corridorF.size());
		
		for (int i = 0; i < corridorP.size(); i++) {
			System.out.print(corridorP.get(i).getId() + " ");
		}
		System.out.println();
		for (int i = 0; i < corridorF.size(); i++) {
			System.out.print(corridorF.get(i).getId() + " ");
		}
		System.out.println();
		
		for (int i = 0; i < corridorP.size()/2; i++) {
			Entry e = new Entry(corridorF.get(i).fp, corridorP.get(i).getLocation());
			entries.add(e);
		}
		
		Collections.sort(corridorP, new Comparator<Marker>() {

			@Override
			public int compare(Marker arg0, Marker arg1) {
				// TODO Auto-generated method stub
				int i = 1;
				if (dis.D[arg0.getId()][dis.doorRoom[i]] > dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return 1;
				} else if (dis.D[arg0.getId()][dis.doorRoom[i]] < dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		Collections.sort(corridorF, new Comparator<Fingerprint>() {

			@Override
			public int compare(Fingerprint o1, Fingerprint o2) {
				// TODO Auto-generated method stub
				int i = doorMap.get(1);
				
				if (parser.Dprim[o1.getId()][parser.doorRoom[i]] > parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return 1;
				} else if (parser.Dprim[o1.getId()][parser.doorRoom[i]] < parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		for (int i = 0; i < corridorP.size()/2; i++) {
			Entry e = new Entry(corridorF.get(i).fp, corridorP.get(i).getLocation());
			entries.add(e);
		}
		
		Collections.sort(corridorP, new Comparator<Marker>() {

			@Override
			public int compare(Marker arg0, Marker arg1) {
				// TODO Auto-generated method stub
				int i = 2;
				if (dis.D[arg0.getId()][dis.doorRoom[i]] > dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return 1;
				} else if (dis.D[arg0.getId()][dis.doorRoom[i]] < dis.D[arg1.getId()][dis.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		Collections.sort(corridorF, new Comparator<Fingerprint>() {

			@Override
			public int compare(Fingerprint o1, Fingerprint o2) {
				// TODO Auto-generated method stub
				int i = doorMap.get(2);
				
				if (parser.Dprim[o1.getId()][parser.doorRoom[i]] > parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return 1;
				} else if (parser.Dprim[o1.getId()][parser.doorRoom[i]] < parser.Dprim[o2.getId()][parser.doorRoom[i]]) {
					return -1;
				} else {
					return 0;
				}
			}
			
		});
		
		for (int i = 0; i < corridorP.size()/2; i++) {
			Entry e = new Entry(corridorF.get(i).fp, corridorP.get(i).getLocation());
			entries.add(e);
		}
	}
	
	// Transform database entries in JSON objects
	public void entriesToJS() {
		
		for (int i = 0; i < this.entries.size(); i++) {
			JSONObject jo = new JSONObject();
			Entry e = this.entries.get(i);
			
			Set<String> keys = e.fingerprint.keySet();
			for (String key: keys) {
				jo.put(key, e.fingerprint.get(key));
			}
			
			jo.put("lat", Double.toString(e.location.getX()));
			jo.put("lng", Double.toString(e.location.getY()));
			
			this.ja.add(jo);
		}
		
		System.out.println(ja.size());
	}
	
	// Write JSON to file
	public void writeJS() { 
        PrintWriter pw;
		try {
			pw = new PrintWriter("D:/LICENTA/DateDB/Date LIFS/LIFS.json");
			pw.write(ja.toJSONString()); 
	          
	        pw.flush(); 
	        pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long startTime = System.nanoTime();
		
		DataManager data = new DataManager();
		
		data.mapDoors();
		data.mapRooms();
		data.mapCorridor();
		data.entriesToJS();
		data.writeJS();

		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		long durationInMillis = TimeUnit.NANOSECONDS.toMillis(totalTime);
		System.out.println(durationInMillis);
	}

}
