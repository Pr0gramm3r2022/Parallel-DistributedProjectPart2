import java.util.concurrent.*;
import java.util.Arrays;

public class MatrixFileIO {
    private static ExecutorService executor;
    private static int numberOfThreads = 1;
    private static final int SEQUENTIAL_THRESHOLD = 64;

    // Node class for binary tree structure
    private static class MatrixNode {
        matrix value;
        MatrixNode left;
        MatrixNode right;
        volatile Future<int[][]> result;
        final int level;  // Track level in tree for thread allocation

        MatrixNode(matrix value, int level) {
            this.value = value;
            this.level = level;
        }
    }

    public static void initializeThreadPool(int matrixCount) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        // Set number of threads based on matrix count as per requirements
        numberOfThreads = switch (matrixCount) {
            case 2 -> 1;   // p = 1
            case 4 -> 3;   // p = 3
            case 8 -> 7;   // p = 7
            case 16 -> 15; // p = 15
            case 32 -> 31; // p = 31
            default -> 1;  // default to single thread
        };

        System.out.println("Initializing thread pool with " + numberOfThreads + " threads for " +
                matrixCount + " matrices");
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    // Build binary tree from matrix array
    private static MatrixNode buildTree(matrix[] matrices, int start, int end, int level) {
        if (start > end) return null;
        if (start == end) return new MatrixNode(matrices[start], level);

        int mid = (start + end) / 2;
        MatrixNode root = new MatrixNode(null, level);
        root.left = buildTree(matrices, start, mid, level + 1);
        root.right = buildTree(matrices, mid + 1, end, level + 1);
        return root;
    }

    // Process tree nodes in parallel
    private static Future<int[][]> processNode(MatrixNode node) throws ExecutionException, InterruptedException {
        if (node.result != null) {
            return node.result;
        }

        if (node.left == null && node.right == null) {
            node.result = CompletableFuture.completedFuture(node.value.getMatrixData());
            return node.result;
        }

        // Process children first
        Future<int[][]> leftFuture = processNode(node.left);
        Future<int[][]> rightFuture = processNode(node.right);

        // Submit multiplication task
        node.result = executor.submit(() -> {
            int[][] leftMatrix = leftFuture.get();
            int[][] rightMatrix = rightFuture.get();
            return multiplyMatrices(leftMatrix, rightMatrix);
        });

        return node.result;
    }

    // Main method for parallel matrix multiplication
    public static int[][] resultMatrix(matrix[] matrices) throws ExecutionException, InterruptedException {
        if (matrices.length == 1) {
            return matrices[0].getMatrixData();
        }

        try {
            // Build binary tree
            MatrixNode root = buildTree(matrices, 0, matrices.length - 1, 0);

            // Process tree and get final result
            Future<int[][]> finalResult = processNode(root);
            return finalResult.get();
        } finally {
            if (matrices.length <= 2) {
                shutdown();
            }
        }
    }

    // Single-threaded version for baseline comparison
    public static int[][] resultMatrixSingleThread(matrix[] matrices) {
        if (matrices.length == 1) {
            return matrices[0].getMatrixData();
        }

        // Build binary tree without parallelization
        MatrixNode root = buildTree(matrices, 0, matrices.length - 1, 0);
        return processNodeSequential(root);
    }

    // Sequential processing for baseline comparison
    private static int[][] processNodeSequential(MatrixNode node) {
        if (node.left == null && node.right == null) {
            return node.value.getMatrixData();
        }

        int[][] leftResult = processNodeSequential(node.left);
        int[][] rightResult = processNodeSequential(node.right);

        return standardMultiply(leftResult, rightResult);
    }

    private static int[][] multiplyMatrices(int[][] a, int[][] b) throws ExecutionException, InterruptedException {
        if (a.length <= SEQUENTIAL_THRESHOLD) {
            return standardMultiply(a, b);
        }
        return strassenMultiply(a, b);
    }

    private static int[][] strassenMultiply(int[][] matrix1, int[][] matrix2) throws ExecutionException, InterruptedException {
        int n = matrix1.length;
        int size = n / 2;

        // Partition matrices
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

        // Compute the seven products
        int[][] p1 = multiplyMatrices(
                addMatrices(a11, a22), addMatrices(b11, b22));
        int[][] p2 = multiplyMatrices(
                addMatrices(a21, a22), b11);
        int[][] p3 = multiplyMatrices(
                a11, subtractMatrices(b12, b22));
        int[][] p4 = multiplyMatrices(
                a22, subtractMatrices(b21, b11));
        int[][] p5 = multiplyMatrices(
                addMatrices(a11, a12), b22);
        int[][] p6 = multiplyMatrices(
                subtractMatrices(a21, a11), addMatrices(b11, b12));
        int[][] p7 = multiplyMatrices(
                subtractMatrices(a12, a22), addMatrices(b21, b22));

        // Calculate quadrants of the result
        int[][] c11 = addMatrices(subtractMatrices(addMatrices(p1, p4), p5), p7);
        int[][] c12 = addMatrices(p3, p5);
        int[][] c21 = addMatrices(p2, p4);
        int[][] c22 = addMatrices(subtractMatrices(addMatrices(p1, p3), p2), p6);

        // Combine quadrants into result
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

    private static int[][] addMatrices(int[][] matrix1, int[][] matrix2) {
        int n = matrix1.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    private static int[][] subtractMatrices(int[][] matrix1, int[][] matrix2) {
        int n = matrix1.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    public static int getCurrentThreadCount() {
        return numberOfThreads;
    }
}