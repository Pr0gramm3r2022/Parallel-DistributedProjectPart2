import java.util.*;

public class BinaryTree {
    public Node root;

    public BinaryTree() {

        root = null;
    }

    public int listIndex = 0;


    public BinaryTree(List<double[][]> nodeMatrixList) {
        root = new Node();


        listIndex = 0;
        //find what power of 2 the number of nodes is
        int depth = 0;
        int numNodes = nodeMatrixList.size();

        while (Math.pow(2, depth) < numNodes) {
            depth++;
        }
        //create a perfect binary tree with bottom nodes storing matrices from list

        root = new Node(depth, nodeMatrixList, this);


    }

    Node currentNode = root;


    public void treeMultiplication() {
        currentNode = root;
        while (root.nodeMatrix == null) {

            if (currentNode.Left.nodeMatrix == null) {

                currentNode = currentNode.Left;
            } else if (currentNode.Right.nodeMatrix == null) {

                currentNode = currentNode.Right;
            } else {

                currentNode.nodeMatrix = MatrixFileIO.StrassenMultiplication(currentNode.Left.nodeMatrix, currentNode.Right.nodeMatrix);

                currentNode = currentNode.Parent;
            }
        }
    }


}



