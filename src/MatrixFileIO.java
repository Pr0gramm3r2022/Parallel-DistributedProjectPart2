import java.io.*;
import java.util.*;

public class MatrixFileIO {

    // Add two matrices
    public static double[][] addMatrices(double[][] matrix1, double[][] matrix2) {


        // Check matrix dimensions


        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for addition");
        }

        // Add matrices


        int rows = matrix1.length;
        int cols = matrix1[0].length;
        double[][] sum = new double[rows][cols];
        for (int j = 0; j < rows; j++) {
            for (int k = 0; k < cols; k++) {
                sum[j][k] = matrix1[j][k] + matrix2[j][k];
            }


        }

        return sum;
    }

    // Subtract two matrices

    public static double[][] subtractMatrices(double[][] matrix1, double[][] matrix2) {
        // Check matrix dimensions
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for subtraction");
        }

        // Subtract matrices
        int rows = matrix1.length;
        int cols = matrix1[0].length;
        double[][] difference = new double[rows][cols];
        for (int j = 0; j < rows; j++) {
            for (int k = 0; k < cols; k++) {
                difference[j][k] = matrix1[j][k] - matrix2[j][k];
            }
        }

        return difference;
    }

    // Strassen's matrix multiplication
    public static double[][] StrassenMultiplication(double[][] matrix1, double[][] matrix2) {

        //calculates matrix if all variables needed are just numbers

        if (matrix1.length == 2) {
            double[][] result = new double[2][2];
            double m1;
            double m2;
            double m3;
            double m4;
            double m5;
            double m6;
            double m7;

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
            double[][] matrix1A = new double[matrixSize][matrixSize];
            double[][] matrix1B = new double[matrixSize][matrixSize];
            double[][] matrix1C = new double[matrixSize][matrixSize];
            double[][] matrix1D = new double[matrixSize][matrixSize];
            double[][] matrix2E = new double[matrixSize][matrixSize];
            double[][] matrix2F = new double[matrixSize][matrixSize];
            double[][] matrix2G = new double[matrixSize][matrixSize];
            double[][] matrix2H = new double[matrixSize][matrixSize];

            double[][] m1;
            double[][] m2;
            double[][] m3;
            double[][] m4;
            double[][] m5;
            double[][] m6;
            double[][] m7;

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

            double[][] result = new double[matrix1.length][matrix1[0].length];
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

}