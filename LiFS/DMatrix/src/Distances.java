import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Distances {
	public Marker[][] markers;
	public Fingerprint[][] fingeprints;
	public double[][] D;
	public double[][] Dprim;
	public int markersNum;
	public int fingerprintsNum;
	public int roomsNum;
	public int[] doorRoom;
	public int[] gridRoom;
	public ArrayList<Marker> ms;
	
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
				
				if (j == 7 && (i <= 1 || (i >= 3 && i <= 7) || (i >= 9 && i <= 10) || (i >= 13 && i <= 20))) {
					//Marker m = new Marker(ids, i, j, '|');
					//markers[i][j] = m;
					markers[i][j] = null;
				} else if (i >= 10 && i <= 20 && j >= 10 && j <= 20) {
					//Marker m = new Marker(ids, i, j, '|');
					//markers[i][j] = m;
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
		
		// set doors
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
	
	public boolean sameRoom(Marker m1, Marker m2) {
		if (m1 == null || m2 == null) {
			return false;
		}
		
		if (m1.getRoom() == m2.getRoom() || m1.isDoor() || m2.isDoor()) {
			return true;
		}
		
		return false;
	}
	
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
		
		System.out.println(dis.ms.get(2).getLocation());
		
		/*for (i = 0; i < dis.D.length; i++) {
			for (j = 0; j < dis.D.length; j++) {
				if (dis.D[i][j] != 99999)
					System.out.print(dis.D[i][j] + "-(i:" + i + " j:" + j + ") ");
			}
			System.out.println();
		}*/
		
		/*for (i = 0; i < dis.D.length; i++) {
			System.out.print(dis.D[0][i] + "-(i:0" + " j:" + i + ") ");
		}*/
		
		/*for (i = 0; i < dis.D.length; i++) {
			System.out.println(dis.D[i].length);
		}*/
		
		dis.writeMatrix("distances.txt", dis.D);
		
	}

}