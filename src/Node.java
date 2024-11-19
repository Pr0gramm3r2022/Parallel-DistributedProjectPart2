import java.util.List;

public class Node implements Runnable {

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

    

    public Thread nodeThread;

// creates a perfect binary tree with bottom nodes storing matrices from list
    public Node(int depth, List<double[][]> nodeMatrixList, BinaryTree tree) {
        
        nodeThread = new Thread(this);
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
            Left.nodeThread = new Thread(Left);
            tree.listIndex++;
            Right.nodeMatrix = nodeMatrixList.get(tree.listIndex);
            Right.nodeThread = new Thread(Right);
        }
    }

    public boolean noOptions = false;

    public void run(){
       if(Left.Left != null){
        Left.nodeThread.start();
        Right.nodeThread.start();
        try{
            Left.nodeThread.join();
            Right.nodeThread.join();
        }
        catch(InterruptedException e){
            System.out.println("Thread interrupted");
        }

        nodeMatrix = MatrixFileIO.StrassenMultiplication(Left.nodeMatrix, Right.nodeMatrix);
       }
       
         else{
              nodeMatrix = MatrixFileIO.StrassenMultiplication(Left.nodeMatrix, Right.nodeMatrix);
         }
         

    }

    public double[][] nodeMatrix;
    public Node Left;
    public Node Right;
    public Node Parent;
}
