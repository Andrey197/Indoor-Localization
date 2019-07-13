import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Parse a xml file and find the total number of APs
public class MyDomParser {
	public static ArrayList<String> APs = new ArrayList<String>();
	public Connection con;
	
	public void findAPs(Document doc) {
		
		NodeList records = doc.getElementsByTagName("r");
		for (int i = 0; i < records.getLength(); i++) {
			Node r = records.item(i);
			if (r.getNodeType() == Node.ELEMENT_NODE) {
				Element record = (Element) r;
				String bssid = record.getAttribute("b");
				if (!APs.contains(bssid)) {
					APs.add(bssid);
				}
			}
		}
	}
	
	void createConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fingerprintdb", "root", "root");
			
			System.out.println("Database connection success!!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MyDomParser parser = new MyDomParser();
		parser.createConnection();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			File dir = new File("DataSet");
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					// Do something with child
					Document doc = builder.parse(child);
					parser.findAPs(doc);
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
		
		try {
			//Write in file
			PrintStream fileOut = new PrintStream("APs.txt");
			System.setOut(fileOut);
			
			//Prepare for inserting in table
			Statement stmt = parser.con.createStatement();
			
			for (int i = 0; i < APs.size(); i++) {
				System.out.println(APs.get(i));
				
				String address = APs.get(i);
				String operation = "INSERT INTO aps VALUES('" + address + "');";
				
				stmt.execute(operation);
			}
			
			stmt.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(APs.size());
	}
}
