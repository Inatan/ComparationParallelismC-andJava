/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes.paralelismo;

/**
 *
 * @author Inatan
 */
public class WorkerTh implements Runnable {
     private int row;
     private int TN;
    private int A[][];
    private int B[][];
    private int C[][];
    private int size;
    
    public WorkerTh(int row,int TN ,int A[][], int B[][], int C[][], int size )
    {
        this.row = row;
        this.A = A;
        this.B = B;
        this.C = C;
        this.TN = TN;
        this.size=size;
    }
    
    @Override
    public void run()
    {
        int i;
      for( i=row; i<TN; i++){
        for (int j=0; j < size; j++){
            for(int k = 0; k < size; k++)
            {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
      }  
    }
}
