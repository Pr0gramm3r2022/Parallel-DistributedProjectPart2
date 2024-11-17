import java.util.List;

public class Node {

    public Node() {

    }

    public Node(double[][] nodeMatrix) {
        this.nodeMatrix = nodeMatrix;
    }

    public Node(int depth) {
        if (depth > 1) {
            Left = new Node(depth - 1);
            Right = new Node(depth - 1);
            Left.Parent = this;
            Right.Parent = this;
        }

    }


    // creates a perfect binary tree with bottom nodes storing matrices from list
    public Node(int depth, List<double[][]> nodeMatrixList, BinaryTree tree) {

        if (depth > 1) {
            Left = new Node(depth - 1, nodeMatrixList, tree);
            tree.listIndex++;
            Right = new Node(depth - 1, nodeMatrixList, tree);
            Left.Parent = this;
            Right.Parent = this;
        }

        if (depth == 1) {
            Left = new Node();
            Right = new Node();
            Left.Parent = this;
            Right.Parent = this;
            Left.nodeMatrix = nodeMatrixList.get(tree.listIndex);

            tree.listIndex++;
            Right.nodeMatrix = nodeMatrixList.get(tree.listIndex);

        }
    }

    public double[][] nodeMatrix;
    public Node Left;
    public Node Right;
    public Node Parent;
}
