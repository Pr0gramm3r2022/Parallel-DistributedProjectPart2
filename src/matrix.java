public class matrix implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // Change to private final to ensure immutability
    private final int[][] matrixData;

    public matrix(int[][] data) {
        // Create a deep copy of the input array to ensure immutability
        this.matrixData = new int[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(data[i], 0, this.matrixData[i], 0, data[i].length);
        }
    }

    public int[][] getMatrixData() {
        // Return a deep copy to maintain encapsulation
        int[][] copy = new int[matrixData.length][matrixData[0].length];
        for (int i = 0; i < matrixData.length; i++) {
            System.arraycopy(matrixData[i], 0, copy[i], 0, matrixData[i].length);
        }
        return copy;
    }

    // Add helper methods for debugging
    public int getRows() {
        return matrixData.length;
    }

    public int getCols() {
        return matrixData[0].length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix [").append(getRows()).append("x").append(getCols()).append("]:\n");
        for (int i = 0; i < Math.min(5, matrixData.length); i++) {
            for (int j = 0; j < Math.min(5, matrixData[i].length); j++) {
                sb.append(String.format("%4d ", matrixData[i][j]));
            }
            if (matrixData[i].length > 5) {
                sb.append("...");
            }
            sb.append("\n");
        }
        if (matrixData.length > 5) {
            sb.append("...\n");
        }
        return sb.toString();
    }
}