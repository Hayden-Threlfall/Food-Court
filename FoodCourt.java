// Name: Hayden Threlfall
// UA ID: 010987873

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class FoodCourt {
    // Constants
    private static final int COUNTER_CAPACITY = 40;
    private static final int WAITING_AREA_CAPACITY = 100;
    private static final int NUM_COUNTERS = 3;
    private static final int NUM_CASHIERS = 10;
    private static final int TIME_UNIT = 100;
    // Shared resources
    private static Semaphore[] counters;
    private static Semaphore waitingArea;
    private static Semaphore cashierQueue;
    private static AtomicInteger waitingAreaCount;
    private static AtomicInteger[] counterCounts;
    private static AtomicInteger cashierCount;
    // Configuration variables
    private static int timeUnits;
    private static int customerLimit;

    public static void main(String[] args) {
        // Parse command-line arguments
        parseArguments(args);
        // Initialize counters and resources
        initializeResources();
        // Start the system thread (manages food court closing time)
        startSystemThread();
        // Start customer threads
        startCustomerThreads();
    }

    // Helper method to parse arguments
    private static void parseArguments(String[] args) {
        if (args.length != 2) {
            System.out.println(
                    "Usage: java FoodCourt <simulation time units> <number of customers max>");
            System.exit(1);
        }
        timeUnits = Integer.parseInt(args[0]);
        customerLimit = Integer.parseInt(args[1]);
    }

    // Initialize semaphores and atomic counters
    private static void initializeResources() {
        // Initialize semaphores
        waitingArea = new Semaphore(WAITING_AREA_CAPACITY);
        waitingAreaCount = new AtomicInteger(0);
        for (int i = 0; i < NUM_COUNTERS; i++) {
            counters[i] = new Semaphore(COUNTER_CAPACITY);
            counterCounts[i] = new AtomicInteger(0);
        }
        cashierQueue = new Semaphore(NUM_CASHIERS);
        cashierCount = new AtomicInteger(0);

    }

    // Start the main system thread to manage simulation duration
    private static void startSystemThread() {
        // Code for system thread that closes food court after timeUnits
        Thread sysThread = new Thread("System Time Thread");
        sysThread.start();
    }

    // Start threads for each customer
    private static void startCustomerThreads() {
        for (int i = 0; i < customerLimit; i++) {
            Thread th = new Customer(i);
            th.start();
        }
    }

    // Inner class to represent each Customer
    static class Customer extends Thread {
        int id;
        private final Random random = new Random();

        Customer(int id) {
            this.id = id;
        }

        public void run() {
            // Logic for customer's journey through the food court
            handleWaitingArea();
            chooseCounter();
            handleCashier();
        }

        private void handleWaitingArea() {
            // Logic for entering and leaving the waiting area
            try {
                waitingArea.acquire();
                for (int i = 0; i < NUM_COUNTERS; i++) {
                    counters[i].acquire();
                }
                waitingArea.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void chooseCounter() {
            // Logic for selecting and using a counter
            int counterI = random.nextInt(NUM_COUNTERS);
            try {
                Thread.sleep(random.nextInt(10));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleCashier() {
            // Logic for payment at the cashier
            try {
                waitingArea.acquire();
                chooseCounter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
