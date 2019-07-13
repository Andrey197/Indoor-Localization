import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Distances {
	public Marker[][] markers;
	public double[][] D;
	public double[][] Dprim;
	public int markersNum;
	public int roomsNum;
	public int[] doorRoom;
	public int[] gridRoom;
	public ArrayList<Marker> ms;
	
	// Building coordinates
	private final static Point2D.Double topLeft = new Point2D.Double(44.435107, 26.047573);
    private final static Point2D.Double topRight = new Point2D.Double(44.435107, 26.047937);
    private final static Point2D.Double bottomLeft = new Point2D.Double(44.434851, 26.047573);
    private final static Point2D.Double bottomRight = new Point2D.Double(44.434851, 26.047937);
	
	public Distances() {
		markers = new Marker[30][30];
		ms = new ArrayList<Marker>();
		roomsNum = 3;
		doorRoom = new int[roomsNum];
		int ids = 0;
		int i, j;
		
		double stepLat = Math.abs(topLeft.getX() - bottomRight.getX()) / 30;
        double stepLng = Math.abs(topLeft.getY() - topRight.getY()) / 30;
        double angleLng = 0.0000001;
        double angleLat = 0.0000001;
        
		// Sample location from the floor plan from meter to meter
		for (i = 0; i < markers.length; i++) {
			for (j = 0; j < markers.length; j++) {
				Point2D.Double latLng = new Point2D.Double(bottomLeft.getX() + stepLat * i - angleLat * j,
                        bottomLeft.getY() + stepLng * j + angleLng * i);
				
				if (i >= 23 && j <= 14) {
					Marker m = new Marker(ids, i, j, "704", latLng);
					markers[i][j] = m;
				} else if (i >= 23 && j >= 15) {
					Marker m = new Marker(ids, i, j, "705", latLng);
					markers[i][j] = m;
				} else if (i >= 10 && i <= 22 && j <= 7) {
					Marker m = new Marker(ids, i, j, "703", latLng);
					markers[i][j] = m;
				} else if (i >= 10 && i <= 22 && j >= 23) {
					Marker m = new Marker(ids, i, j, "706", latLng);
					markers[i][j] = m;
				} else if (i >= 5 && i <= 9 && j <= 7) {
					Marker m = new Marker(ids, i, j, "702", latLng);
					markers[i][j] = m;
				} else if (i <= 4 && j <= 7) {
					Marker m = new Marker(ids, i, j, "701", latLng);
					markers[i][j] = m;
				} else if (i <= 6 && j >= 10 && j <= 24) {
					Marker m = new Marker(ids, i, j, "708", latLng);
					markers[i][j] = m;
				} else if (i <= 9 && j >= 25) {
					Marker m = new Marker(ids, i, j, "707", latLng);
					markers[i][j] = m;
				} else {
					Marker m = new Marker(ids, i, j, "c", latLng);
					markers[i][j] = m;
				}
				
				// Remove samples from inaccessible areas
				if (j == 7 && (i <= 1 || (i >= 3 && i <= 7) || (i >= 9 && i <= 10) || (i >= 13 && i <= 20))) {
					markers[i][j] = null;
				} else if (i >= 10 && i <= 20 && j >= 10 && j <= 20) {
					markers[i][j] = null;
				} else if (i == 9 && j <= 5) {
					markers[i][j] = null;
				} else if (i == 5 && j <= 5) {
					markers[i][j] = null;
				} else if (i == 7 && j == 0) {
					markers[i][j] = null;
				} else if ((i >= 3 && i <= 6) && j == 27) {
					markers[i][j] = null;
				} else if (i == 0 && j >= 25) {
					markers[i][j] = null;
				} else if (i == 4 && j == 29) {
					markers[i][j] = null;
				} else if (i == 22 && j >= 25) {
					markers[i][j] = null;
                } else if ((i <= 23 && i >= 10) && j == 29) {
                	markers[i][j] = null;
                } else if ((i == 22 || (i <= 20 && i >= 12) || i == 10) && j == 23) {
                	markers[i][j] = null;
				} else if (markers[i][j].getRoom() != "702" && markers[i][j].getRoom() != "706" && markers[i][j].getRoom() != "707" &&  markers[i][j].getRoom() != "c") {
					markers[i][j] = null;
				} else {
					ms.add(markers[i][j]);
					ids++;
				}
			}
		}
		
		// Set doors
		int x;
		markers[11][23].setDoor(true);
		//x = markers[12][23].getId();
		//ms.get(x).setDoor(true);
		
		markers[8][25].setDoor(true);
		x = markers[8][25].getId();
		ms.get(x).setDoor(true);
		doorRoom[0] = x;
		
		markers[21][23].setDoor(true);
		x = markers[21][23].getId();
		ms.get(x).setDoor(true);
		doorRoom[1] = x;
	
		markers[8][7].setDoor(true);
		x = markers[8][7].getId();
		ms.get(x).setDoor(true);
		doorRoom[2] = x;
		
		this.markersNum = ids;
		D = new double[ids][ids];
		for (double[] row : D) {
			Arrays.fill(row, 99999);
		}
		
		setRooms();
		for (int k = 0; k < this.gridRoom.length; k++) {
			System.out.print(this.gridRoom[k] + " ");
		}
		System.out.println();
	}
	
	// Set room index for every location index
	public void setRooms() {
		this.gridRoom = new int[this.markersNum];
		
		for (int i = 0; i < this.doorRoom.length; i++) {
			for (int j = 0; j < this.ms.size(); j++) {
				if (this.ms.get(j).getRoom() == this.ms.get(this.doorRoom[i]).getRoom()) {
					gridRoom[j] = i+1;
				}
			}
		}
	}
	
	// Verify if the markers are from the same room
	public boolean sameRoom(Marker m1, Marker m2) {
		if (m1 == null || m2 == null) {
			return false;
		}
		
		if (m1.getRoom() == m2.getRoom() || m1.isDoor() || m2.isDoor()) {
			return true;
		}
		
		return false;
	}
	
	/*
	 * Store distances to neighbours
	 * For up, down, left and right neighbours the distance is 1 meter
	 * For diagonally neighbours, the distance is sqrt(2) - from the square diagonal formula
	 */
	public void addNeighbours() {
		int i, j;
		
		for (i = 0; i < markers.length; i++) {
			for (j = 0; j < markers.length; j++) {
				if (i - 1 >= 0 && j - 1 >= 0 && sameRoom(markers[i][j], markers[i-1][j-1])) {
					D[markers[i][j].getId()][markers[i-1][j-1].getId()] = 1.41;
				} 
				if (i - 1 >= 0 && sameRoom(markers[i][j], markers[i-1][j])) {
					D[markers[i][j].getId()][markers[i-1][j].getId()] = 1;
				}
				if (i - 1 >= 0 && j + 1 < markers.length && sameRoom(markers[i][j], markers[i-1][j+1])) {
					D[markers[i][j].getId()][markers[i-1][j+1].getId()] = 1.41;
				} 
				if (j + 1 < markers.length && sameRoom(markers[i][j], markers[i][j+1])) {
					D[markers[i][j].getId()][markers[i][j+1].getId()] = 1;
				}
				if (i + 1 < markers.length && j + 1 < markers.length && sameRoom(markers[i][j], markers[i+1][j+1])) {
					D[markers[i][j].getId()][markers[i+1][j+1].getId()] = 1.41;
				}
				if (i + 1 < markers.length && sameRoom(markers[i][j], markers[i+1][j])) {
					D[markers[i][j].getId()][markers[i+1][j].getId()] = 1;
				}
				if (i + 1 < markers.length && j - 1 >= 0 && sameRoom(markers[i][j], markers[i+1][j-1])) {
					D[markers[i][j].getId()][markers[i+1][j-1].getId()] = 1.41;
				}
				if (j - 1 >= 0 && sameRoom(markers[i][j], markers[i][j-1])) {
					D[markers[i][j].getId()][markers[i][j-1].getId()] = 1;
				}
			}
		}
		
		for (i = 0; i < D.length; i++) {
			D[i][i] = 0;
		}
	}
	
	// Used for finding the distances between every sampled locations
	public void floydWarshall() {
		int i, j ,k;
		
		for (k = 0; k < D.length; k++) {
			for (i = 0; i < D.length; i++) {
				for (j = 0; j < D.length; j++) {
					if (D[i][k] + D[k][j] < D[i][j]) {
						D[i][j] = D[i][k] + D[k][j];
					}
				}
			}
		}
	}
	
	// Write a matrix in a file
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
	    } catch (IOException e) {}
	}
	
	// This main is for testing the functions
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Distances dis = new Distances();
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
		
		System.out.println(dis.ms.size());
		
		System.out.println(dis.ms.get(2).getLocation());
		
		dis.writeMatrix("distances.txt", dis.D);
		
	}

}