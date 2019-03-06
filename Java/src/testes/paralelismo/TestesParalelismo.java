/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes.paralelismo;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Inatan
 */
public class TestesParalelismo {
        private static final Random RAND = new Random(42);
        public static int NUM_OF_THREADS = 8;
        private static int Msize=2000;
 
	public static void main(String[] args) {
		int LENGTH = 100000;
		int RUNS   =  10;   
                long startTime1;
                long endTime1;
                long avgTime;
                System.out.println("Mergesort \nLength \t Threads \t Time");
                while(LENGTH < 1000000000){
                    for (int tcount=0;tcount<5;tcount++){
                        NUM_OF_THREADS = 1 << tcount;
                        avgTime=0;
                        for (int i = 1; i <= RUNS; i++) {
                            int[] a = createRandomArray(LENGTH);
                            startTime1 = System.currentTimeMillis();
                            parallelMergeSort(a);
                            endTime1 = System.currentTimeMillis();
                            if (!isSorted(a)) {
                                    throw new RuntimeException("not sorted afterward: " + Arrays.toString(a));
                            }
                            System.out.println(endTime1 - startTime1);
                            avgTime+=(endTime1 - startTime1);
                        }
                        System.out.println(LENGTH + "\t" + NUM_OF_THREADS + "\t" + avgTime/RUNS);
                    }
                    LENGTH *= 10;
                }
            
            int row;
            int col;
            System.out.println("Matrix Multipication \nLength \t\t Threads \t\t Time");
            while(Msize <= 2000){
                int A[][] = new int[Msize][Msize];
                int B[][] = new int[Msize][Msize];
                int C[][] = new int[Msize][Msize];
                for(row = 0 ; row < A.length; row++)
                {
                    for (col = 0 ; col < B.length; col++ )
                    {
                        A[row][col]=RAND.nextInt(10);
                        B[row][col]=RAND.nextInt(10);
                    }
                }
                for (int tcount=0;tcount<2;tcount++){
                    NUM_OF_THREADS = 1 << tcount;
                    Thread[] thrd = new Thread[NUM_OF_THREADS];

                     int int_num = (int)Math.floor((Msize)/NUM_OF_THREADS);
                     avgTime=0;
                     for (int i = 1; i <= RUNS; i++) {
                        startTime1 = System.currentTimeMillis();
                        for(int ThreadIndex = 0 ; ThreadIndex < NUM_OF_THREADS; ThreadIndex++)
                        {
                           thrd[ThreadIndex] = new Thread(new WorkerTh(int_num*ThreadIndex,int_num*(ThreadIndex+1), A, B, C,Msize));
                           thrd[ThreadIndex].start();
                        }
                        try 
                        {
                            for (int ThreadIndex = 0 ; ThreadIndex < NUM_OF_THREADS; ThreadIndex++) 
                            {
                                    thrd[ThreadIndex].join();
                            }

                        } catch (InterruptedException e) {}
                        endTime1 = System.currentTimeMillis();
                        avgTime+=(endTime1 - startTime1);
                     }
                     System.out.println(Msize + "\t\t" +  NUM_OF_THREADS + "\t\t" + avgTime/RUNS);
                }
            Msize+=250;
            }
	}
        
        public static void parallelMergeSort(int[] a) {
		// int cores = Runtime.getRuntime().availableProcessors();
		parallelMergeSort(a, NUM_OF_THREADS);
	}
	
	public static void parallelMergeSort(int[] a, int threadCount) {
		if (threadCount <= 1) {
			mergeSort(a);
		} else if (a.length >= 2) {
			int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
			int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
			Thread rThread = new Thread(new Sorter(right, threadCount / 2));
			parallelMergeSort(left,threadCount / 2);
			rThread.start();
			try {
				rThread.join();
			} catch (InterruptedException ie) {}
			merge(left, right, a);
		}
	}
	
	// Arranges the elements of the given array into sorted order
	// using the "merge sort" algorithm, which splits the array in half,
	// recursively sorts the halves, then merges the sorted halves.
	// It is O(N log N) for all inputs.
	public static void mergeSort(int[] a) {
		if (a.length >= 2) {
			// split array in half
			int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
			int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
			
			// sort the halves
			mergeSort(left);
			mergeSort(right);
			
			// merge them back together
			merge(left, right, a);
		}
	}
	
	// Combines the contents of sorted left/right arrays into output array a.
	// Assumes that left.length + right.length == a.length.
	public static void merge(int[] left, int[] right, int[] a) {
		int i1 = 0;
		int i2 = 0;
		for (int i = 0; i < a.length; i++) {
			if (i2 >= right.length || (i1 < left.length && left[i1] < right[i2])) {
				a[i] = left[i1];
				i1++;
			} else {
				a[i] = right[i2];
				i2++;
			}
		}
	}
	
	
	
	
	
	
	
	// Swaps the values at the two given indexes in the given array.
	public static final void swap(int[] a, int i, int j) {
		if (i != j) {
			int temp = a[i];
			a[i] = a[j];
			a[j] = temp;
		}
	}
	
	// Randomly rearranges the elements of the given array.
	public static void shuffle(int[] a) {
		for (int i = 0; i < a.length; i++) {
			// move element i to a random index in [i .. length-1]
			int randomIndex = (int) (Math.random() * a.length - i);
			swap(a, i, i + randomIndex);
		}
	}
	
	// Returns true if the given array is in sorted ascending order.
	public static boolean isSorted(int[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i] > a[i + 1]) {
				return false;
			}
		}
		return true;
	}
	
	

	// Creates an array of the given length, fills it with random
	// non-negative integers, and returns it.
	public static int[] createRandomArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = a.length-i;
			// a[i] = RAND.nextInt(40);
		}
		return a;
	}
    
}

