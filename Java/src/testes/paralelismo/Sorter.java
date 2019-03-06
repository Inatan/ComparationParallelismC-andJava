/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes.paralelismo;

import static testes.paralelismo.TestesParalelismo.parallelMergeSort;

/**
 *
 * @author Inatan
 */
public class Sorter implements Runnable {
	private int[] a;
	private int threadCount;
    	
	public Sorter(int[] a, int threadCount) {
		this.a = a;
		this.threadCount = threadCount;
	}
	
	public void run() {
            parallelMergeSort(a, threadCount);
	}
}
