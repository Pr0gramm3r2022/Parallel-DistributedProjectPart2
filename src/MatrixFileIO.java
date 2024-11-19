import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class MatrixFileIO {
    // Threshold for switching to standard multiplication -- Helps with thread overhead
    private static final int THRESHOLD = 64;

    // Thread pool for Strassen multiplication
    private static final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Add two matrices
    public static int[][] addMatrices(int[][] matrix1, int[][] matrix2) {
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for addition");
        }

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
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Matrix dimensions don't match for subtraction");
        }

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

    // Improved Strassen multiplication with proper multithreading
    public static int[][] StrassenMultiplication(int[][] matrix1, int[][] matrix2)
            throws ExecutionException, InterruptedException {
        return strassenMultiplyRecursive(matrix1, matrix2, true);
    }

    private static int[][] strassenMultiplyRecursive(int[][] matrix1, int[][] matrix2, boolean useThreads)
            throws ExecutionException, InterruptedException {
        int n = matrix1.length;

        // Use standard multiplication for small matrices or when below threshold
        if (n <= THRESHOLD || !useThreads) {
            return standardMultiply(matrix1, matrix2);
        }

        int size = n / 2;
        int[][] a11 = new int[size][size];
        int[][] a12 = new int[size][size];
        int[][] a21 = new int[size][size];
        int[][] a22 = new int[size][size];
        int[][] b11 = new int[size][size];
        int[][] b12 = new int[size][size];
        int[][] b21 = new int[size][size];
        int[][] b22 = new int[size][size];

        // Split matrices
        split(matrix1, a11, 0, 0);
        split(matrix1, a12, 0, size);
        split(matrix1, a21, size, 0);
        split(matrix1, a22, size, size);
        split(matrix2, b11, 0, 0);
        split(matrix2, b12, 0, size);
        split(matrix2, b21, size, 0);
        split(matrix2, b22, size, size);

        // Only create threads at the top level
        Future<int[][]> m1Future = null;
        Future<int[][]> m2Future = null;
        Future<int[][]> m3Future = null;
        Future<int[][]> m4Future = null;
        Future<int[][]> m5Future = null;
        Future<int[][]> m6Future = null;
        Future<int[][]> m7Future = null;

        if (useThreads) {
            m1Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            addMatrices(a11, a22), addMatrices(b11, b22), false));
            m2Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            addMatrices(a21, a22), b11, false));
            m3Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            a11, subtractMatrices(b12, b22), false));
            m4Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            a22, subtractMatrices(b21, b11), false));
            m5Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            addMatrices(a11, a12), b22, false));
            m6Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            subtractMatrices(a21, a11), addMatrices(b11, b12), false));
            m7Future = executor.submit(() ->
                    strassenMultiplyRecursive(
                            subtractMatrices(a12, a22), addMatrices(b21, b22), false));
        }

        int[][] m1, m2, m3, m4, m5, m6, m7;

        if (useThreads) {
            m1 = m1Future.get();
            m2 = m2Future.get();
            m3 = m3Future.get();
            m4 = m4Future.get();
            m5 = m5Future.get();
            m6 = m6Future.get();
            m7 = m7Future.get();
        } else {
            // Sequential computation for recursive calls
            m1 = strassenMultiplyRecursive(
                    addMatrices(a11, a22), addMatrices(b11, b22), false);
            m2 = strassenMultiplyRecursive(
                    addMatrices(a21, a22), b11, false);
            m3 = strassenMultiplyRecursive(
                    a11, subtractMatrices(b12, b22), false);
            m4 = strassenMultiplyRecursive(
                    a22, subtractMatrices(b21, b11), false);
            m5 = strassenMultiplyRecursive(
                    addMatrices(a11, a12), b22, false);
            m6 = strassenMultiplyRecursive(
                    subtractMatrices(a21, a11), addMatrices(b11, b12), false);
            m7 = strassenMultiplyRecursive(
                    subtractMatrices(a12, a22), addMatrices(b21, b22), false);
        }

        int[][] c11 = addMatrices(subtractMatrices(addMatrices(m1, m4), m5), m7);
        int[][] c12 = addMatrices(m3, m5);
        int[][] c21 = addMatrices(m2, m4);
        int[][] c22 = addMatrices(subtractMatrices(addMatrices(m1, m3), m2), m6);

        // Combine results
        return combine(c11, c12, c21, c22);
    }

    private static int[][] standardMultiply(int[][] matrix1, int[][] matrix2) {
        int n = matrix1.length;
        int[][] result = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }

        return result;
    }

    private static void split(int[][] source, int[][] dest, int startRow, int startCol) {
        for (int i = 0; i < dest.length; i++) {
            System.arraycopy(source[startRow + i], startCol, dest[i], 0, dest.length);
        }
    }

    private static int[][] combine(int[][] c11, int[][] c12, int[][] c21, int[][] c22) {
        int n = c11.length * 2;
        int[][] result = new int[n][n];

        for (int i = 0; i < c11.length; i++) {
            System.arraycopy(c11[i], 0, result[i], 0, c11.length);
            System.arraycopy(c12[i], 0, result[i], c11.length, c11.length);
            System.arraycopy(c21[i], 0, result[i + c11.length], 0, c11.length);
            System.arraycopy(c22[i], 0, result[i + c11.length], c11.length, c11.length);
        }

        return result;
    }

    // Recursive method to multiply a list of matrices with multithreading
    public static int[][] resultMatrix(matrix[] matrices) throws ExecutionException, InterruptedException {
        if (matrices.length == 1) {
            return matrices[0].getMatrixData();
        }

        if (matrices.length % 2 != 0) {
            throw new IllegalArgumentException("Number of matrices must be even for pairing");
        }

        matrix[] pairedResults = new matrix[matrices.length / 2];

        // Process pairs and store results
        for (int i = 0; i < matrices.length; i += 2) {
            int[][] result = StrassenMultiplication(
                    matrices[i].getMatrixData(),
                    matrices[i + 1].getMatrixData()
            );
            pairedResults[i / 2] = new matrix(result);
        }

        return resultMatrix(pairedResults);
    }

    // Single-threaded version for baseline comparison
    public static int[][] StrassenSingleThread(int[][] matrix1, int[][] matrix2)
            throws ExecutionException, InterruptedException {
        return strassenMultiplyRecursive(matrix1, matrix2, false);
    }

    public static int[][] resultMatrixSingleThread(matrix[] matrices)
            throws ExecutionException, InterruptedException {
        if (matrices.length == 1) {
            return matrices[0].getMatrixData();
        }

        if (matrices.length % 2 != 0) {
            throw new IllegalArgumentException("Number of matrices must be even for pairing");
        }

        matrix[] pairedResults = new matrix[matrices.length / 2];

        for (int i = 0; i < matrices.length; i += 2) {
            int[][] result = StrassenSingleThread(
                    matrices[i].getMatrixData(),
                    matrices[i + 1].getMatrixData()
            );
            pairedResults[i / 2] = new matrix(result);
        }

        return resultMatrixSingleThread(pairedResults);
    }

    // Add shutdown method to clean up executor
    public static void shutdown() {
        executor.shutdown();
    }
}