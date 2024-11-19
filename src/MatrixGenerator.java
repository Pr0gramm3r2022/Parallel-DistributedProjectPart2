import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MatrixGenerator {

    public static void main(String[] args) {
        // Define the size of the matrices
        int numMatrices = 32;
        int matrixSize = 128;

        // Initialize a random number generator
        Random random = new Random();

        // Create a FileWriter to write to the file
        try (FileWriter writer = new FileWriter("multiple_matrices.txt")) {
            // Write the number of matrices at the top
            writer.write("MATRICES: " + numMatrices + "\n");

            // Loop through the matrices
            for (int matrixIndex = 0; matrixIndex < numMatrices; matrixIndex++) {
                // Write the dimensions of the matrix
                writer.write(matrixSize + " " + matrixSize + "\n");

                // Generate and write the matrix
                for (int row = 0; row < matrixSize; row++) {
                    // Generate a row of random floating-point numbers
                    for (int col = 0; col < matrixSize; col++) {
                        // Generate a random float between -10 and 10
                        double value = -10 + (10 - (-10)) * random.nextDouble();
                        // Write the value to the file with 6 decimal places
                        writer.write(String.format("%.6f", value));
                        if (col < matrixSize - 1) {
                            writer.write(" ");  // Space between values
                        }
                    }
                    writer.write("\n");
                }

                // Write the separator
                writer.write("---\n");
            }

            System.out.println("Text file 'matrices.txt' has been generated.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }
}