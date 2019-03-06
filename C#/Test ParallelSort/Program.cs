using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Test_ParallelSort
{
    class Program
    {
        static long Msize = 500;
        static long Asize = 100000;
        const int RUNS = 10;
        public static int numberOfProcesses = 1;
        static void Main(string[] args)
        {
            int i, j;
            DateTime start, end;
            TimeSpan duration;
            double avgDuration;
            // init array
            Console.WriteLine("Mergesort \nLenght\t\t Threads\t\t Time");
            while (Asize < 1000000000)
            {
                long[] ar = new long[Asize];
                for (i = 0; i < ar.Length; i++)
                    ar[i] = ar.Length - i;

                /*  for (i = 0; i < 10; i++)
                      Console.WriteLine(ar[i]);*/
                for (int tcount = 0; tcount < 5; tcount++)
                {
                    numberOfProcesses = 1 << tcount;
                    avgDuration = 0;
                    for (i = 1; i <= RUNS; i++)
                    {
                        start = DateTime.Now;
                        Paralle.Sort(ar, null);
                        end = DateTime.Now;
                        duration = end - start;
                        avgDuration += duration.TotalMilliseconds;
                    }
                    Console.WriteLine(ar.Length + "\t\t " + numberOfProcesses + "\t\t " + avgDuration / RUNS);
                }
                Asize *= 10;
            }
            //Console.ReadLine();
            Console.WriteLine("Matrix Multiplication \nLenght\t\tThreads\t\t Time");

            while (Msize <= 2000)
            {
                int[,] a = new int[Msize, Msize];
                int[,] b = new int[Msize, Msize];
                int[,] c = new int[Msize, Msize];
                Random rand = new Random();

                // fill source matrixes with random numbers
                for (i = 0; i < Msize; i++)
                {
                    for (j = 0; j < Msize; j++)
                    {
                        a[i, j] = rand.Next(0, 30);
                        b[i, j] = rand.Next(0, 30);
                    }
                }
                for (int tcount = 0; tcount < 5; tcount++)
                {
                    numberOfProcesses = 1 << tcount;
                    avgDuration = 0;
                    for (i = 1; i <= RUNS; i++)
                    {
                        start = DateTime.Now;
                        ParalleledMatrixMultiplication(a, b, c);
                        end = DateTime.Now;
                        duration = end - start;
                        avgDuration += duration.TotalMilliseconds;
                    }
                    Console.WriteLine(Msize + "\t\t " + numberOfProcesses + "\t\t " + avgDuration / RUNS);
                }
                Msize += 250;
            }
        }
        //Console.ReadLine();
        static void printMatrix(int[,] matrix)
        {
            for (int i = 0; i < Msize; i++)
            {
                for (int j = 0; j < Msize; j++)
                {
                    Console.Write(string.Format("{0} ", matrix[i, j]));
                }
                Console.Write(Environment.NewLine + Environment.NewLine);
            }
        }

        static void ParalleledMatrixMultiplication(int[,] a, int[,] b, int[,] c)
        {
            int s = a.GetLength(0);
            Parallel.For(0, s, new ParallelOptions { MaxDegreeOfParallelism = numberOfProcesses }, delegate (int i)
            {
                for (int j = 0; j < s; j++)
                {
                    int v = 0;

                    for (int k = 0; k < s; k++)
                    {
                        v += a[i, k] * b[k, j];
                    }

                    c[i, j] = v;
                }
            });
        }
    }
    static class Paralle
    {

        /// <summary>
        /// Parallel sort an array
        /// </summary>
        /// <typeparam name="T">Array type</typeparam>
        /// <param name="array">The array object</param>
        /// <param name="comparer">Comparer object, if null the default comparer will be used</param>
        public static void Sort<T>(T[] array, IComparer<T> comparer = null)
        {
            if (comparer == null)
                comparer = Comparer<T>.Default;

            if (array == null)
                throw new ArgumentNullException("array");
            if (array.Length == 0)
                throw new ArgumentException("array");

            // fallback to sequntial if the number of elements is too small
            if (array.Length <= Environment.ProcessorCount * 2)
            {
                Array.Sort(array, comparer);
            }

            T[] auxArray = new T[array.Length];
            Array.Copy(array, auxArray, array.Length);

            int totalWorkers = Program.numberOfProcesses;// Numero de threads executand
            Task[] workers = new Task[totalWorkers - 1];
            // definição de quantas iteraçãos são necessárias até chegar uso de todas as threads
            int iterations = (int)Math.Log(totalWorkers, 2);

            //divide partições para cada uma das threads
            int partitionSize = array.Length / totalWorkers;
            int remainder = array.Length % totalWorkers;
            //Barreira para sincronizar as threads e preparar o merge.
            Barrier barrier = new Barrier(totalWorkers, (b) =>
            {
                partitionSize <<= 1;
                var temp = auxArray;
                auxArray = array;
                array = temp;
            });

            Action<object> workAction = (obj) =>
            {
                int index = (int)obj;

                //calcula a partição que será executada para essa thread
                int low = index * partitionSize;
                if (index > 0)
                    low += remainder;
                int high = (index + 1) * partitionSize - 1 + remainder;

                MergeSort(array, auxArray, low, high, comparer);
                barrier.SignalAndWait();
                for (int j = 0; j < iterations; j++)
                {
                    if (index % 2 == 1)
                    {
                        barrier.RemoveParticipant();
                        break;
                    }

                    int newHigh = high + partitionSize / 2;
                    index >>= 1; //Realiza junção entre as threads
                    Merge(array, auxArray, low, high, high + 1, newHigh, comparer);
                    high = newHigh;
                    barrier.SignalAndWait();
                }

            };
            //execução das threads
            for (int i = 0; i < workers.Length; i++)
            {
                workers[i] = Task.Factory.StartNew(obj => workAction(obj), i + 1);
            }
            workAction(0);

            if (iterations % 2 != 0)
                Array.Copy(auxArray, array, array.Length);

        }


        /// <summary>
        /// Merge sort an array recursively
        /// </summary>
        private static void MergeSort<T>(T[] array, T[] auxArray, int low, int high, IComparer<T> comparer)
        {
            if (low >= high) return;

            int mid = (high + low) / 2;
            MergeSort<T>(auxArray, array, low, mid, comparer);
            MergeSort<T>(auxArray, array, mid + 1, high, comparer);

            Merge<T>(array, auxArray, low, mid, mid + 1, high, comparer);

        }

        /// <summary>
        /// Mrge two sorted sub arrays
        /// </summary>
        private static void Merge<T>(T[] array, T[] auxArray, int low1, int low2, int high1, int high2, IComparer<T> comparer)
        {
            int ptr1 = low1;
            int ptr2 = high1;
            int ptr = low1;
            for (; ptr <= high2; ptr++)
            {
                if (ptr1 > low2)
                    array[ptr] = auxArray[ptr2++];
                else if (ptr2 > high2)
                    array[ptr] = auxArray[ptr1++];

                else
                {
                    if (comparer.Compare(auxArray[ptr1], auxArray[ptr2]) <= 0)
                    {
                        array[ptr] = auxArray[ptr1++];
                    }
                    else
                        array[ptr] = auxArray[ptr2++];
                }
            }
        }
    }
}

