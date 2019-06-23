import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FDistances {

	public Fingerprint[][] fingeprints;
	public double[][] Dprim;
	public double[][] DprimRooms;
	public int fingerprintsNum;
	public int roomsFingerprintsNum;
	public int corridorFingerprintsNum;
	public int roomsNum;
	public int ids;
	public ArrayList<Fingerprint> fs;
	public int[] fingerprintRoom;   // store the room of every fingerprint
	public int[] doorRoom;			// store the index of the door for every room
	
	// Find the number of fingerprints from xml file
	public int countFingerprints(Document doc) {
		int count = 0;
		
		NodeList locs = doc.getElementsByTagName("loc");
		count += locs.getLength();
		
		return count;
	}
	
	// Parse the xml file, find the fingerprints and calculate distances between them
	public void findFingerprints(Document doc, String name) {
		
		int start = ids;
		
		NodeList locs = doc.getElementsByTagName("loc");
		NodeList wrs = doc.getElementsByTagName("wr");
		for (int i = 0; i < locs.getLength(); i++) {
			Node l = locs.item(i);
			Node w = wrs.item(i);
			if (l.getNodeType() == Node.ELEMENT_NODE && w.getNodeType() == Node.ELEMENT_NODE) {
				Element loc = (Element) l;
				Element wr = (Element) w;
				
				NodeList rs = wr.getChildNodes();
				HashMap<String, Integer> fingerprint = new HashMap<String, Integer>();
				for (int j = 0; j < rs.getLength(); j++) {
					Node r = rs.item(j);
					if (r.getNodeType() == Node.ELEMENT_NODE) {
						Element read = (Element) r;
						String bssid = read.getAttribute("b");
						Integer signal = Integer.parseInt(read.getAttribute("s"));
						//System.out.println("ID: " + bssid + ", SIGNAL: " + signal);
						fingerprint.put(bssid, signal);
					}
				}
				
				String lat = loc.getAttribute("lat");
				String lng = loc.getAttribute("lng");
				Fingerprint f = new Fingerprint(ids, name, Double.parseDouble(lat), Double.parseDouble(lng), fingerprint);
				//System.out.println("Fingerprint: " + f.getId() + " " + f.location.getX() + " " + f.location.getY());
				fs.add(f);
				ids++;
			}
		}
		
		if (name.toLowerCase().contains("corridor")) {
			this.corridorFingerprintsNum += locs.getLength();
		}
		
		System.out.println("Start: " + start + " Finish: " + ids + " " + name);
		
		for (int i = start; i < ids; i++) {
			Point2D.Double x = fs.get(i).location;
			for (int j = start; j < ids; j++) {
				Point2D.Double y = fs.get(j).location;
				Dprim[i][j] = getDistance(x, y);
			}
		}
		
	}
	
	// Set the connections between rooms
	public void setDoors() {
		//Corridor1 (ids: 0->46)
		//Corridor2 (ids: 46->76)
		//Corridor3 (ids: 76->108)
		//Corridor4 (ids: 108->159)
		//Room 702 (ids: 159->228)
		//Room 706 (ids: 228->291)
		//Room 707a (ids: 291->321)
		//Room 707b (ids: 321->411)
		
		//Door between 702 and Corridor 1
		int i = 159;
		int j = 28;
		Point2D.Double x = fs.get(i).location;
		Point2D.Double y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		
		//Door between 707 and Corridor 4
		i = 307;
		j = 157;
		x = fs.get(i).location;
		y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		
		//Door between 706 and Corridor 3
		i = 229;
		j = 78;
		x = fs.get(i).location;
		y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		//System.out.println(getDistance(x, y));
		
		//Door between 706 and Corridor 3
		i = 280;
		j = 98;
		x = fs.get(i).location;
		y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		
		//Door between Corridor 1 and Corridor 2
		i = 0;
		j = 46;
		x = fs.get(i).location;
		y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		
		//Door between Corridor 1 and Corridor 4
		i = 26;
		j = 108;
		for (int k = i; k < i+6; k++) {
			for (int l = j; l < j+6; l++) {
				x = fs.get(k).location;
				y = fs.get(l).location;
				Dprim[k][l] = getDistance(x, y);
				Dprim[l][k] = getDistance(x, y);
			}
		}
		
		//Door between Corridor 2 and Corridor 3
		i = 74;
		j = 79;
		x = fs.get(i).location;
		y = fs.get(j).location;
		Dprim[i][j] = getDistance(x, y);
		Dprim[j][i] = getDistance(x, y);
		
		//Door between Corridor 3 and Corridor 4
		i = 103;
		j = 147;
		for (int k = i; k < i+6; k++) {
			for (int l = j; l < j+6; l++) {
				x = fs.get(k).location;
				y = fs.get(l).location;
				Dprim[k][l] = getDistance(x, y);
				Dprim[l][k] = getDistance(x, y);
			}
		}
		
		//Door between 707a and 707b
		i = 315;
		j = 321;
		for (int k = i; k < i+10; k++) {
			for (int l = j; l < j+10; l++) {
				x = fs.get(k).location;
				y = fs.get(l).location;
				Dprim[k][l] = getDistance(x, y);
				Dprim[l][k] = getDistance(x, y);
			}
		}
	}
	
	// Find the distance in meters between 2 coordinates
	public static double getDistance(Point2D.Double p1, Point2D.Double p2) {
        final double R = 6381 * 1000; // metres

        double lat1 = p1.getX();
        double lon1 = p1.getY();
        double lat2 = p2.getX();
        double lon2 = p2.getY();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

	// Initialize the matrix of distances
	public void setFingerprintsNum(int fingerprintsNum) {
		this.fingerprintsNum = fingerprintsNum;
		this.corridorFingerprintsNum = 0;
		
		this.Dprim = new double[fingerprintsNum][fingerprintsNum];
		for (double[] row : Dprim) {
			Arrays.fill(row, 99999);
		}
		
		this.ids = 0;
		this.fs = new ArrayList<Fingerprint>();
		
		this.fingerprintRoom = new int[fingerprintsNum];
	}
	
	// Floyd Warshall implementation
	public void floydWarshall() {
		int i, j ,k;
		
		for (k = 0; k < Dprim.length; k++) {
			for (i = 0; i < Dprim.length; i++) {
				for (j = 0; j < Dprim.length; j++) {
					if (Dprim[i][k] + Dprim[k][j] < Dprim[i][j]) {
						Dprim[i][j] = Dprim[i][k] + Dprim[k][j];
					}
				}
			}
		}
	}
	
	// Write matrix in the file
	void writeMatrix(String filename, double[][] matrix) {
	    try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

	        for (int i = 0; i < matrix.length; i++) {
	        	for (int j = 0; j < matrix[i].length; j++) {
	        	    if(j == matrix[i].length-1) {
	        	        bw.write(String.valueOf(matrix[i][j]));
	        	    } else {
	        	        bw.write(String.valueOf(matrix[i][j]) + ",");
	        	    }
	        	}
	            bw.newLine();
	        }
	        bw.flush();
	        bw.close();
	    } catch (IOException e) {}
	}
	
	// Get the matrix of distances just for rooms, without corridor
	public void roomDistances() {
		this.roomsFingerprintsNum = this.fingerprintsNum - this.corridorFingerprintsNum;
		//System.out.println(roomsFingerprintsNum);
		//System.out.println(corridorFingerprintsNum);
		
		this.DprimRooms = new double[this.roomsFingerprintsNum][this.roomsFingerprintsNum];
		
		int i, j;
		int k, l;
		i = 0; j = 0;
		for (k = corridorFingerprintsNum; k < fingerprintsNum; k++) {
			j = 0;
			for (l = corridorFingerprintsNum; l < fingerprintsNum; l++) {
				DprimRooms[i][j] = Dprim[k][l];
				j++;
			}
			i++;
		}
	}
	
	public void findRooms() {
		this.roomsNum = 0;
		File file = new File("D:/LICENTA/MDS/output/clusters.txt");
		
		BufferedReader br;
		String st;
		try {
			br = new BufferedReader(new FileReader(file));
			int i = this.corridorFingerprintsNum;
			ArrayList<Integer> differentNums = new ArrayList<Integer>();
			while ((st = br.readLine()) != null) {
				if (!differentNums.contains(Integer.parseInt(st))) {
					differentNums.add(Integer.parseInt(st));
					this.roomsNum++;
				}
				this.fingerprintRoom[i] = Integer.parseInt(st);
				i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void findDoors() {
		// Room with index 1 will be at index 0 of the array doorRoom
		this.doorRoom = new int[this.roomsNum];
		
		for (int room = 0; room < this.roomsNum; room++) {
			double minDist = 999;
			int minIndex = 999;
			for (int i = 0; i < this.fingerprintsNum; i++) {
				if (this.fingerprintRoom[i] == room+1) {
					for (int j = 0; j < this.fingerprintsNum; j++) {
						if (this.fingerprintRoom[j] == 0) {
							if (this.Dprim[i][j] < minDist) {
								minDist = this.Dprim[i][j];
								minIndex = i;
							}
						}
					}
				}
			}
			doorRoom[room] = minIndex;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FDistances parser = new FDistances();
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
				
				// Calculate the MST
				// Prims prims = new Prims(parser.fingerprintsNum);
				// prims.primsAlgorithm(parser.Dprim);
				// prims.printMST();
				//prims.constructNewMatrix();
				//parser.writeMatrix("fMST.txt", prims.newMatrix);
				
				// Compute a matrix just with rooms
				// parser.roomDistances();
				// parser.writeMatrix("roomDistances.txt", parser.DprimRooms);
				
				parser.findRooms();
				for (int i = 0; i < parser.fingerprintsNum; i++) {
					System.out.print(parser.fingerprintRoom[i] + " ");
				}
				System.out.println();
				
				parser.findDoors();
				//System.out.println(parser.roomsNum);
				for (int i = 0; i < parser.roomsNum; i++) {
					System.out.println(parser.doorRoom[i]);
				}
			}
			
			//Corridor1 (ids: 0->46)
			//Corridor2 (ids: 46->76)
			//Corridor3 (ids: 76->108)
			//Corridor4 (ids: 108->159)
			//Room 702 (ids: 159->228)
			//Room 706 (ids: 228->291)
			//Room 707a (ids: 291->321)
			//Room 707b (ids: 321->411)
			
			/*for (int i = 321; i < 411; i++) {
				for (int j = 321; j < 411; j++) {
					System.out.print(parser.Dprim[i][j] + " ");
				}
				System.out.println();
			}*/
			
			//System.out.println(parser.Dprim[162][29]);
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

}
