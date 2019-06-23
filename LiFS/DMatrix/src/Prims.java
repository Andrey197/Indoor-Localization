import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
 
public class Prims
{
    private boolean unsettled[];
    private boolean settled[];
    private int numberofvertices;
    private double adjacencyMatrix[][];
    private double key[];
    public static final int INFINITE = 999;
    private int parent[];
    public double newMatrix[][];
 
    public Prims(int numberofvertices) {
        this.numberofvertices = numberofvertices;
        unsettled = new boolean[numberofvertices];
        settled = new boolean[numberofvertices];
        key = new double[numberofvertices];
        parent = new int[numberofvertices];
        
        adjacencyMatrix = new double[numberofvertices][numberofvertices];
        newMatrix = new double[numberofvertices][numberofvertices];
    }
 
    public int getUnsettledCount(boolean unsettled[])
    {
        int count = 0;
        for (int index = 0; index < unsettled.length; index++)
        {
            if (unsettled[index])
            {
                count++;
            }
        }
        return count;
    }
 
    public void primsAlgorithm(double adjacencyMatrix[][])
    {
        int evaluationVertex;
        for (int source = 0; source < numberofvertices; source++)
        {
            for (int destination = 0; destination < numberofvertices; destination++)
            {
                this.adjacencyMatrix[source][destination] = adjacencyMatrix[source][destination];
            }
        }
 
        for (int index = 0; index < numberofvertices; index++)
        {
            key[index] = INFINITE;
        }
        key[0] = 0;
        unsettled[0] = true;
        parent[0] = 0;
 
        while (getUnsettledCount(unsettled) != 0)
        {
            evaluationVertex = getMimumKeyVertexFromUnsettled(unsettled);
            unsettled[evaluationVertex] = false;
            settled[evaluationVertex] = true;
            evaluateNeighbours(evaluationVertex);
        }
    } 
 
    private int getMimumKeyVertexFromUnsettled(boolean[] unsettled2)
    {
        double min = Integer.MAX_VALUE;
        int node = 0;
        for (int vertex = 0; vertex < numberofvertices; vertex++)
        {
            if (unsettled[vertex] == true && key[vertex] < min)
            {
                node = vertex;
                min = key[vertex];
            }
        }
        return node;
    }
 
    public void evaluateNeighbours(int evaluationVertex)
    {
 
        for (int destinationvertex = 0; destinationvertex < numberofvertices; destinationvertex++)
        {
            if (settled[destinationvertex] == false)
            {
                if (adjacencyMatrix[evaluationVertex][destinationvertex] != INFINITE)
                {
                    if (adjacencyMatrix[evaluationVertex][destinationvertex] < key[destinationvertex])
                    {
                        key[destinationvertex] = adjacencyMatrix[evaluationVertex][destinationvertex];
                        parent[destinationvertex] = evaluationVertex;
                    }
                    unsettled[destinationvertex] = true;
                }
            }
        }
    }
    
    // Floyd Warshall implementation
 	public void floydWarshall() {
 		int i, j ,k;
 		
 		for (k = 0; k < newMatrix.length; k++) {
 			for (i = 0; i < newMatrix.length; i++) {
 				for (j = 0; j < newMatrix.length; j++) {
 					if (newMatrix[i][k] + newMatrix[k][j] < newMatrix[i][j]) {
 						newMatrix[i][j] = newMatrix[i][k] + newMatrix[k][j];
 					}
 				}
 			}
 		}
 	}
 
    public void printMST() {
    	
        System.out.println("SOURCE  : DESTINATION = WEIGHT");
        
        for (int vertex = 0; vertex < numberofvertices; vertex++) {
            System.out.println(parent[vertex] + "\t:\t" + vertex +"\t=\t"+ adjacencyMatrix[parent[vertex]][vertex]);
        }
    }
    
    public void constructNewMatrix() {
    	
    	for (double[] row : newMatrix) {
			Arrays.fill(row, 9999);
		}
    	
    	for (int vertex = 0; vertex < numberofvertices; vertex++) {
            //System.out.println(parent[vertex] + "\t:\t" + vertex +"\t=\t"+ adjacencyMatrix[parent[vertex]][vertex]);
    		newMatrix[parent[vertex]][vertex] = adjacencyMatrix[parent[vertex]][vertex];
    		newMatrix[vertex][parent[vertex]] = adjacencyMatrix[parent[vertex]][vertex];
    		newMatrix[vertex][vertex] = 0;
        }
    	floydWarshall();
    }
 
    /*public static void main(String... arg)
    {
        double adjacency_matrix[][];
        int number_of_vertices;
        Scanner scan = new Scanner(System.in);
 
        try
        {
            System.out.println("Enter the number of vertices");
            number_of_vertices = scan.nextInt();
            adjacency_matrix = new double[number_of_vertices][number_of_vertices];
 
            System.out.println("Enter the Weighted Matrix for the graph");
            for (int i = 0; i < number_of_vertices; i++)
            {
                for (int j = 0; j < number_of_vertices; j++)
                {
                    adjacency_matrix[i][j] = scan.nextInt();
                    if (i == j)
                    {
                        adjacency_matrix[i][j] = 0;
                        continue;
                    }
                    if (adjacency_matrix[i][j] == 0)
                    {
                        adjacency_matrix[i][j] = INFINITE;
                    }
                }
            }
 
            Prims prims = new Prims(number_of_vertices);
            prims.primsAlgorithm(adjacency_matrix);
            prims.printMST();
 
        } catch (InputMismatchException inputMismatch)
        {
            System.out.println("Wrong Input Format");
        }
        scan.close();
    }*/
}