import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class MatrixTestHarness {
    private static final int[] MATRIX_SIZES = {16,32,64,128,512};
    private static final int[] MATRIX_COUNTS = {2, 4, 8, 16, 32};
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TEST_ITERATIONS = 5;

    public static class TestResult {
        public int matrixSize;
        public int matrixCount;
        public long parallelTime;
        public long baselineTime;
        public double speedup;
        public double efficiency;
        public int threadCount;
        public int treeDepth;
        public List<Long> iterationTimes;

        public TestResult(int size, int count) {
            this.matrixSize = size;
            this.matrixCount = count;
            this.treeDepth = (int)(Math.log(count) / Math.log(2));
            this.iterationTimes = new ArrayList<>();
        }

        public void calculateMetrics() {
            // Remove warmup iterations and calculate average
            if (iterationTimes.size() > WARMUP_ITERATIONS) {
                List<Long> testTimes = iterationTimes.subList(WARMUP_ITERATIONS, iterationTimes.size());
                this.parallelTime = (long) testTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            }
            this.speedup = (double) baselineTime / parallelTime;
            this.efficiency = speedup / threadCount;
        }
    }

    public static void main(String[] args) {
        List<TestResult> results = new ArrayList<>();

        for (int size : MATRIX_SIZES) {
            for (int count : MATRIX_COUNTS) {
                System.out.printf("\nTesting matrices: size=%dx%d, count=%d%n", size, size, count);

                TestResult result = runTest(size, count);
                if (result != null) {
                    results.add(result);
                    printIterationResults(result);
                }

                MatrixFileIO.shutdown();
                System.gc();
                sleep(5000);
            }
        }

        saveDetailedResults(results);
        generateVisualizationData(results);
        printSummary(results);
    }

    private static TestResult runTest(int size, int count) {
        TestResult result = new TestResult(size, count);
        matrix[] matrices = generateMatrices(size, count);

        if (matrices == null) return null;

        try {
            // Run baseline test
            System.out.println("Running baseline test...");
            result.baselineTime = runBaselineTest(matrices);

            // Run parallel tests with warmup
            System.out.println("Running parallel tests with warmup...");
            result.threadCount = getThreadCount(count);

            for (int i = 0; i < WARMUP_ITERATIONS + TEST_ITERATIONS; i++) {
                MatrixFileIO.initializeThreadPool(count);
                long time = runParallelTest(matrices.clone());
                result.iterationTimes.add(time);

                System.out.printf("Iteration %d/%d: %.3f seconds%n",
                        i + 1, WARMUP_ITERATIONS + TEST_ITERATIONS, time / 1e9);

                MatrixFileIO.shutdown();
                System.gc();
                sleep(1000);
            }

            result.calculateMetrics();
            return result;

        } catch (Exception e) {
            System.err.printf("Error in test (size=%d, count=%d): %s%n", size, count, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static matrix[] generateMatrices(int size, int count) {
        try {
            matrix[] matrices = new matrix[count];
            for (int i = 0; i < count; i++) {
                matrices[i] = new matrix(MatrixGenerator.generateMatrix(size, 1, 10));
            }
            return matrices;
        } catch (OutOfMemoryError e) {
            System.err.printf("Out of memory generating matrices (size=%d, count=%d)%n", size, count);
            return null;
        }
    }

    private static long runBaselineTest(matrix[] matrices) throws ExecutionException, InterruptedException {
        MatrixFileIO.initializeThreadPool(2);
        long startTime = System.nanoTime();
        MatrixFileIO.resultMatrix(matrices.clone());
        return System.nanoTime() - startTime;
    }

    private static long runParallelTest(matrix[] matrices) throws ExecutionException, InterruptedException {
        long startTime = System.nanoTime();
        MatrixFileIO.resultMatrix(matrices);
        return System.nanoTime() - startTime;
    }

    private static int getThreadCount(int matrixCount) {
        return switch (matrixCount) {
            case 2 -> 1;
            case 4 -> 3;
            case 8 -> 7;
            case 16 -> 15;
            case 32 -> 31;
            default -> 1;
        };
    }

    private static void printIterationResults(TestResult result) {
        System.out.printf("\nResults for %dx%d matrices (count: %d):%n",
                result.matrixSize, result.matrixSize, result.matrixCount);
        System.out.printf("Tree Depth: %d%n", result.treeDepth);
        System.out.printf("Thread Count: %d%n", result.threadCount);
        System.out.printf("Baseline Time: %.3f s%n", result.baselineTime / 1e9);
        System.out.printf("Average Parallel Time: %.3f s%n", result.parallelTime / 1e9);
        System.out.printf("Speedup: %.2fx%n", result.speedup);
        System.out.printf("Efficiency: %.2f%%%n", result.efficiency * 100);
    }

    private static void saveDetailedResults(List<TestResult> results) {
        try (PrintWriter writer = new PrintWriter("matrix_detailed_results.csv")) {
            writer.println("Size,Count,TreeDepth,ThreadCount,BaselineTime,ParallelTime,Speedup,Efficiency,AllIterationTimes");
            for (TestResult r : results) {
                writer.printf("%d,%d,%d,%d,%d,%d,%.3f,%.3f,\"%s\"%n",
                        r.matrixSize, r.matrixCount, r.treeDepth, r.threadCount,
                        r.baselineTime, r.parallelTime, r.speedup, r.efficiency,
                        String.join(",", r.iterationTimes.stream().map(String::valueOf).toList()));
            }
        } catch (IOException e) {
            System.err.println("Failed to save detailed results: " + e.getMessage());
        }
    }

    private static void generateVisualizationData(List<TestResult> results) {
        try (PrintWriter writer = new PrintWriter("visualization_data.csv")) {
            writer.println("Category,MatrixSize,MatrixCount,Value");

            for (TestResult r : results) {
                writer.printf("Speedup,%d,%d,%.3f%n", r.matrixSize, r.matrixCount, r.speedup);
                writer.printf("Efficiency,%d,%d,%.3f%n", r.matrixSize, r.matrixCount, r.efficiency);
                writer.printf("ExecutionTime,%d,%d,%.3f%n", r.matrixSize, r.matrixCount, r.parallelTime / 1e9);
            }
        } catch (IOException e) {
            System.err.println("Failed to save visualization data: " + e.getMessage());
        }
    }

    private static void printSummary(List<TestResult> results) {
        System.out.println("\nOverall Performance Summary:");

        // Group by matrix size
        Map<Integer, List<TestResult>> bySize = new TreeMap<>();
        results.forEach(r -> bySize.computeIfAbsent(r.matrixSize, k -> new ArrayList<>()).add(r));

        bySize.forEach((size, sizeResults) -> {
            System.out.printf("\nMatrix Size: %dx%d%n", size, size);
            System.out.printf("Average Speedup: %.2fx%n",
                    sizeResults.stream().mapToDouble(r -> r.speedup).average().orElse(0));
            System.out.printf("Average Efficiency: %.2f%%%n",
                    sizeResults.stream().mapToDouble(r -> r.efficiency * 100).average().orElse(0));

            // Find best case
            TestResult best = sizeResults.stream()
                    .max(Comparator.comparingDouble(r -> r.speedup))
                    .orElse(null);

            if (best != null) {
                System.out.printf("Best Case: Count=%d, Speedup=%.2fx, Efficiency=%.2f%%%n",
                        best.matrixCount, best.speedup, best.efficiency * 100);
            }
        });
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}