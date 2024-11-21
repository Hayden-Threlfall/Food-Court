/*
 * MutexAndSemaphore01.java This code extends Thread instead of Runnable. It extends the class
 * Thread and overrides the public run method with the code for the new thread, creates an instance
 * of the Thread subclass, and calls its start method. 10/30/17 by drt
 */

// NetBeans likes this
// package mutexandsemaphore01;

import java.util.concurrent.Semaphore;

public class MutexAndSemaphore01 {
    private static int numInCriticalSection = 3;

    // create the semaphore set to numInCriticalSection;
    private static Semaphore semaphore = new Semaphore(numInCriticalSection);

    static class MyThread extends Thread {

        private String name = "";

        public MyThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                // acquire semaphore
                System.out.println(name + " : available Semaphore slots before: "
                        + semaphore.availablePermits());
                System.out.println(name + " : acquiring lock.");
                semaphore.acquire();
                System.out.println(name + " : got the free slot!");

                // Sleep 1 second to simulate processing
                Thread.sleep(1000);

                // release semaphore
                System.out.println(name + " : releasing lock.");
                semaphore.release();
                System.out.println(name + " : available Semaphore slots after: "
                        + semaphore.availablePermits());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Semaphore Example");
        System.out.println();
        System.out.println("Total available slots: " + semaphore.availablePermits());

        // create and start thread
        MyThread thread1 = new MyThread("Thread 1");
        thread1.start();

        // create and start thread
        MyThread thread2 = new MyThread("Thread 2");
        thread2.start();

        // create and start thread
        MyThread thread3 = new MyThread("Thread 3");
        thread3.start();

        // create and start thread
        MyThread thread4 = new MyThread("Thread 4");
        thread4.start();

        // create and start thread
        MyThread thread5 = new MyThread("Thread 5");
        thread5.start();
    }
}
