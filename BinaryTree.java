import java.util.*;

public class BinaryTree {
    public Node root;
    public BinaryTree() {

       root = null;
    }
    public int listIndex = 0;
    public BinaryTree(List<double[][]> nodeMatrixList) {
        root = new Node();

        //find what power of 2 the number of nodes is
        int depth = 0;
        int numNodes = nodeMatrixList.size();
        while (Math.pow(2, depth) < numNodes) {
            depth++;
        }
        //create a perfect binary tree with bottom nodes storing matrices from list
        


        
        
    }

}



