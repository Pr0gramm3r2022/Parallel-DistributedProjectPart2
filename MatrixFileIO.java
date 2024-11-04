import java.io.*;
import java.util.*;

public class MatrixFileIO {
    // Matrix separator in file
    private static final String MATRIX_SEPARATOR = "---";

    // Read matrices from file
    public static List<double[][]> readMatrices(String filename) throws IOException {
        List<double[][]> matrices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null || !line.startsWith("MATRICES: ")) {
                throw new IOException("Invalid file format: Missing header");
            }

            int expectedMatrixCount = Integer.parseInt(line.substring(9).trim());

            while ((line = reader.readLine()) != null) {
                // Skip separator lines
                if (line.trim().equals(MATRIX_SEPARATOR)) {
                    continue;
                }

                // Read matrix dimensions
                String[] dimensions = line.trim().split("\\s+");
                if (dimensions.length != 2) {
                    throw new IOException("Invalid matrix dimensions");
                }

                int rows = Integer.parseInt(dimensions[0]);
                int cols = Integer.parseInt(dimensions[1]);

                // Read matrix data
                double[][] matrix = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    line = reader.readLine();
                    if (line == null) {
                        throw new IOException("Unexpected end of file");
                    }

                    String[] elements = line.trim().split("\\s+");
                    if (elements.length != cols) {
                        throw new IOException("Invalid number of elements in row");
                    }

                    for (int j = 0; j < cols; j++) {
                        matrix[i][j] = Double.parseDouble(elements[j]);
                    }
                }

                matrices.add(matrix);
            }

            if (matrices.size() != expectedMatrixCount) {
                throw new IOException("Number of matrices doesn't match header count");
            }
        }

        return matrices;
    }

    // Print matrix from parameter
    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }


    public static List<double[][]> multiplyMatrices(List<double[][]> matrices) {
        /*
        Current implementation will multiply every set of two matrices ( in a row )
        i.e. Matrix A, Matrix B, Matrix C, Matrix D] will multiply A*B, B*C, C*D
         */
        List<double[][]> result = new ArrayList<>();

        // Check matrix dimensions
        for (int i = 0; i < matrices.size() - 1; i++) {
            double[][] matrix1 = matrices.get(i);
            double[][] matrix2 = matrices.get(i + 1);
            if (matrix1[0].length != matrix2.length) {
                throw new IllegalArgumentException("Matrix dimensions don't match for multiplication");
            }
        }
        // Multiply matrices
        for (int i = 0; i < matrices.size() - 1; i++) {
            double[][] matrix1 = matrices.get(i);
            double[][] matrix2 = matrices.get(i + 1);
            int rows = matrix1.length;
            int cols = matrix2[0].length;
            double[][] product = new double[rows][cols];
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    for (int l = 0; l < matrix1[0].length; l++) {
                        product[j][k] += matrix1[j][l] * matrix2[l][k];
                    }
                }
            }
            result.add(product);
        }

        return result;
    }

    public static List<double[][]> addMatrices(List<double[][]> matrices) {
        /*
        Current implementation will add every set of two matrices ( in a row )
        i.e. Matrix A, Matrix B, Matrix C, Matrix D] will add A+B, B+C, C+D
         */
        List<double[][]> result = new ArrayList<>();

        // Check matrix dimensions
        for (int i = 0; i < matrices.size() - 1; i++) {
            double[][] matrix1 = matrices.get(i);
            double[][] matrix2 = matrices.get(i + 1);
            if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
                throw new IllegalArgumentException("Matrix dimensions don't match for addition");
            }
        }
        // Add matrices
        for (int i = 0; i < matrices.size() - 1; i++) {
            double[][] matrix1 = matrices.get(i);
            double[][] matrix2 = matrices.get(i + 1);
            int rows = matrix1.length;
            int cols = matrix1[0].length;
            double[][] sum = new double[rows][cols];
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    sum[j][k] = matrix1[j][k] + matrix2[j][k];
                }
            }
            result.add(sum);
        }

        return result;
    }

    double m1;
    double m2;
    double m3;
    double m4;
    double m5;
    double m6;
    double m7;
    double i;
    double j;
    double k;
    double l;
    double[][] result;

    public  double[][] StrassenMultiplication(double[][] matrix1, double[][] matrix2) {
        
        //calculates matrix if all variables needed are just numbers

        if(matrix1.length == 2){
            result = new double[2][2];
            m1 = (matrix1[0][0] + matrix1[1][0]) * (matrix2[0][0] + matrix2[1][0]);
            m2 = (matrix1[0][1] + matrix1[1][1]) * (matrix2[1][0] + matrix2[1][1]);
            m3 = (matrix1[0][0] - matrix1[1][1]) * (matrix2[0][0] + matrix2[1][1]);
            m4 = matrix1[0][0] * (matrix2[1][0] - matrix2[1][1]);
            m5 = (matrix1[1][0] + matrix1[1][1]) * matrix2[0][0];
            m6 = (matrix1[0][0] + matrix1[0][1]) * matrix2[1][1];
            m7 = matrix1[1][1] * (matrix2[1][0]-matrix2[0][0]);
            result[0][0] = m2 + m3 - m6 + m7;
            result[0][1] = m4 + m6;
            result[1][0] = m5 + m7;
            result[1][1] = m1 - m3 - m4 - m5;
            return result;
        }
        //calculates matrix if variables are submatrices
        else{
            
        }
        return null;
    }
    

    
    // Strassen's matrix multiplication
    public static List<double[][]> StrassenMultiplicationAndSquareCheck(List<double[][]> matrices) {
        List<double[][]> result = new ArrayList<>();

        for ( int i = 0; i <matrices.size()-1; i++) {
            double[][] matrix1 = matrices.get(i);
            double[][] matrix2 = matrices.get(i + 1);
            if (matrix1.length == matrix2.length && matrix1[0].length == matrix2[0].length && 
                isPowerOfTwo(matrix1.length) && isPowerOfTwo(matrix1[0].length)) {
                    
                    
            
            } else {
                throw new IllegalArgumentException("Matrix dimensions don't match or are not powers of 2 for Strassen's multiplication");
            }
           

        }
        return null;
    }
    // Helper method to check if a number is a power of 2
    private static boolean isPowerOfTwo(int n) {
        return (n & (n - 1)) == 0 && n > 0;
    }


        



    // Example usage
        
        
       


    // Example usage
    public static void main(String[] args) {
        try {
            // Read matrices back
            List<double[][]> readMatrices = readMatrices("multiple_matrices.txt");

            // Multiply matrices and print result
            List<double[][]> result = multiplyMatrices(readMatrices);
            System.out.println("Result:");
            for (int i = 0; i < result.size(); i++) {
                System.out.println("\nMatrix " + (i + 1) + ":");
                printMatrix(result.get(i));
            }

            // Print read matrices
            System.out.println("\n\nRead " + readMatrices.size() + " matrices from file:");
            for (int i = 0; i < readMatrices.size(); i++) {
                System.out.println("\nMatrix " + (i + 1) + ":");
                printMatrix(readMatrices.get(i));
            }

        } catch (IOException e) {
            System.err.println("Error handling matrix file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}