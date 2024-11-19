import java.util.*;

public class BinaryTree {
    class Node implements Runnable{
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




    public BinaryTree() {

       root = null;
    }
    public int listIndex = 0;

    private Node root;
    public void setRoot(Node root){
        this.root = root;
    }

    public Node getRoot(){
        return root;
    }

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

    

    




    public long treeMultiplication(){
        currentNode = root;
        long startTime = System.nanoTime();
        while(root.nodeMatrix==null){
           
            if(currentNode.Left.nodeMatrix==null){
                
                currentNode = currentNode.Left;
            }
            else if(currentNode.Right.nodeMatrix==null){
                
                currentNode = currentNode.Right;
            }
            else{
               
                currentNode.nodeMatrix = MatrixFileIO.StrassenMultiplication(currentNode.Left.nodeMatrix, currentNode.Right.nodeMatrix);
                
                currentNode = currentNode.Parent;
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time taken for Strassen Multiplication without Multithreading: " + duration + " nanoseconds");
        return duration;
    }

    

    public long treeMultiplicationMultithreaded(){
        currentNode = root;
        long startTime = System.nanoTime();
        currentNode.nodeThread.start();
        try{
            currentNode.nodeThread.join();
        }
        catch(InterruptedException e){
            System.out.println("Thread interrupted");
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time taken for Strassen Multiplication with Multithreading: " + duration + " nanoseconds");
        return duration;
    }

}




