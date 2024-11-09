import java.util.List;

public class Node {

    public Node() {
        
    }
    public Node(double[][] nodeMatrix) {
        this.nodeMatrix = nodeMatrix;
    }

    public Node(int depth) {
        if (depth > 0) {
            Left = new Node(depth - 1);
            Right = new Node(depth - 1);
            Left.Parent = this;
            Right.Parent = this;
        }
    }

    public Node(int depth, List<double[][]> nodeMatrixList) {
        if (depth > 0) {
            Left = new Node(depth - 1);
            Right = new Node(depth - 1);
            Left.Parent = this;
            Right.Parent = this;
        }
    }

    public double[][] nodeMatrix;
    public Node Left;
    public Node Right;
    public Node Parent;
}
