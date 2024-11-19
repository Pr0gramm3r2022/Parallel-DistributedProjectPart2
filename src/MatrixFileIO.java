import java.io.*;
import java.util.*;

public class MatrixFileIO {

    // Add two matrices
    public static int[][] addMatrices(int[][] matrix1, int[][] matrix2) {
        // Check matrix dimensions
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for addition");
        }

        // Add matrices
        int rows = matrix1.length;
        int cols = matrix1[0].length;
        int[][] sum = new int[rows][cols];
        for (int j = 0; j < rows; j++) {
            for (int k = 0; k < cols; k++) {
                sum[j][k] = matrix1[j][k] + matrix2[j][k];
            }
        }

        return sum;
    }

    // Subtract two matrices
    public static int[][] subtractMatrices(int[][] matrix1, int[][] matrix2) {
        // Check matrix dimensions
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for subtraction");
        }

        // Subtract matrices
        int rows = matrix1.length;
        int cols = matrix1[0].length;
        int[][] difference = new int[rows][cols];
        for (int j = 0; j < rows; j++) {
            for (int k = 0; k < cols; k++) {
                difference[j][k] = matrix1[j][k] - matrix2[j][k];
            }
        }

        return difference;
    }

    // Strassen's matrix multiplication
    public static int[][] StrassenMultiplication(int[][] matrix1, int[][] matrix2) {
        //calculates matrix if all variables needed are just numbers
        if (matrix1.length == 2) {
            int[][] result = new int[2][2];
            int m1;
            int m2;
            int m3;
            int m4;
            int m5;
            int m6;
            int m7;

            m1 = (matrix1[0][0] + matrix1[1][0]) * (matrix2[0][0] + matrix2[0][1]);
            m2 = (matrix1[0][1] + matrix1[1][1]) * (matrix2[1][0] + matrix2[1][1]);
            m3 = (matrix1[0][0] - matrix1[1][1]) * (matrix2[0][0] + matrix2[1][1]);
            m4 = matrix1[0][0] * (matrix2[0][1] - matrix2[1][1]);
            m5 = (matrix1[1][0] + matrix1[1][1]) * matrix2[0][0];
            m6 = (matrix1[0][0] + matrix1[0][1]) * matrix2[1][1];
            m7 = matrix1[1][1] * (matrix2[1][0] - matrix2[0][0]);
            result[0][0] = m2 + m3 - m6 - m7;
            result[0][1] = m4 + m6;
            result[1][0] = m5 + m7;
            result[1][1] = m1 - m3 - m4 - m5;
            return result;
        }


        //calculates matrix if variables are submatrices(if matrix is larger than 2x2)
        else {
            int matrixSize = matrix1.length / 2;
            int[][] matrix1A = new int[matrixSize][matrixSize];
            int[][] matrix1B = new int[matrixSize][matrixSize];
            int[][] matrix1C = new int[matrixSize][matrixSize];
            int[][] matrix1D = new int[matrixSize][matrixSize];
            int[][] matrix2E = new int[matrixSize][matrixSize];
            int[][] matrix2F = new int[matrixSize][matrixSize];
            int[][] matrix2G = new int[matrixSize][matrixSize];
            int[][] matrix2H = new int[matrixSize][matrixSize];

            int[][] m1;
            int[][] m2;
            int[][] m3;
            int[][] m4;
            int[][] m5;
            int[][] m6;
            int[][] m7;

            //write contents of matrices into submatrices
            for (int i = 0; i < matrix1.length / 2; i++) {
                for (int j = 0; j < matrix1[0].length / 2; j++) {
                    matrix1A[i][j] = matrix1[i][j];
                    matrix1B[i][j] = matrix1[i][j + matrix1[0].length / 2];
                    matrix1C[i][j] = matrix1[i + matrix1.length / 2][j];
                    matrix1D[i][j] = matrix1[i + matrix1.length / 2][j + matrix1[0].length / 2];
                    matrix2E[i][j] = matrix2[i][j];
                    matrix2F[i][j] = matrix2[i][j + matrix2[0].length / 2];
                    matrix2G[i][j] = matrix2[i + matrix2.length / 2][j];
                    matrix2H[i][j] = matrix2[i + matrix2.length / 2][j + matrix2[0].length / 2];
                }
            }

            m1 = StrassenMultiplication(addMatrices(matrix1A, matrix1C), addMatrices(matrix2E, matrix2F));
            m2 = StrassenMultiplication(addMatrices(matrix1B, matrix1D), addMatrices(matrix2G, matrix2H));
            m3 = StrassenMultiplication(subtractMatrices(matrix1A, matrix1D), addMatrices(matrix2E, matrix2H));
            m4 = StrassenMultiplication(matrix1A, subtractMatrices(matrix2F, matrix2H));
            m5 = StrassenMultiplication(addMatrices(matrix1C, matrix1D), matrix2E);
            m6 = StrassenMultiplication(addMatrices(matrix1A, matrix1B), matrix2H);
            m7 = StrassenMultiplication(matrix1D, subtractMatrices(matrix2G, matrix2E));

            int[][] result = new int[matrix1.length][matrix1[0].length];
            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    result[i][j] = m2[i][j] + m3[i][j] - m6[i][j] - m7[i][j];
                    result[i][j + matrixSize] = m4[i][j] + m6[i][j];
                    result[i + matrixSize][j] = m5[i][j] + m7[i][j];
                    result[i + matrixSize][j + matrixSize] = m1[i][j] - m3[i][j] - m4[i][j] - m5[i][j];
                }
            }
            return result;
        }
    }

    // Perform matrix multiplication on sets of matrices
    public static int[][] resultMatrix(matrix[] matrices) {
        // Base case: if only one matrix remains, return its data
        if (matrices.length == 1) {
            return matrices[0].getMatrixData();
        }

        // Ensure we have an even number of matrices to pair
        if (matrices.length % 2 != 0) {
            throw new IllegalArgumentException("Number of matrices must be even for pairing");
        }

        // Create new array to store paired results
        matrix[] pairedResults = new matrix[matrices.length / 2];

        // Process pairs and store results
        for (int i = 0; i < matrices.length; i += 2) {
            int[][] result = StrassenMultiplication(matrices[i].getMatrixData(), matrices[i + 1].getMatrixData());
            pairedResults[i/2] = new matrix(result);
        }

        // Recursive call with the new, smaller array
        return resultMatrix(pairedResults);
    }
}