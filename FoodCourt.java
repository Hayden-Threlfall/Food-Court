// Name: Hayden Threlfall
// UA ID: 010987873

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static boolean isOpen = true;

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
        counters = new Semaphore[NUM_COUNTERS];
        counterCounts = new AtomicInteger[NUM_COUNTERS];
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
        Thread systemThread = new Thread(() -> {
            try {
                Thread.sleep(timeUnits * TIME_UNIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isOpen = false;
            System.out.println("Food Court is closed!");
        });
        systemThread.start();
    }

    // Start threads for each customer
    private static void startCustomerThreads() {
        for (int i = 0; i < customerLimit; i++) {
            new Customer(i).start();
            try {
                Thread.sleep(new Random().nextInt(10 * TIME_UNIT)); // Random delay between customer
                                                               // arrivals
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Inner class to represent each Customer
    static class Customer extends Thread {
        public final int id;
        private final Random random = new Random();
        private final int counterIndex = random.nextInt(NUM_COUNTERS);

        Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            // Logic for customer's journey through the food court
            System.out.printf("Customer %d arives at the food court.\n", id);
            if(!isOpen) {
                System.out.printf("The customer %d leaves the food court in frustration. Food court is closed.\n", id);
                return; // Ends the thread
            }
            if (!handleWaitingArea()) {
                System.out.printf("The customer %d leaves the food court in frustration. Waiting area is full.\n", id);
                return; // Ends the thread
            }
            chooseCounter();
            handleCashier();
        }

        private boolean  handleWaitingArea() {
            // Logic for entering and leaving the waiting area
            try {
                if (waitingArea.tryAcquire()) {
                    waitingAreaCount.incrementAndGet();
                    System.out.printf("Customer %d enters the waiting area.\n", id);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        }

        private void chooseCounter() {
            // Logic for selecting and using a counter
            try {
                counters[counterIndex].acquire();
                counterCounts[counterIndex].incrementAndGet();

                waitingArea.release();
                waitingAreaCount.decrementAndGet();

                System.out.printf("Customer %d leaves waiting area and joins Counter %d (Counter count: %d)\n", id, counterIndex + 1, counterCounts[counterIndex].get());
                counters[counterIndex].release();
                counterCounts[counterIndex].decrementAndGet();
                Thread.sleep(random.nextInt(10) * TIME_UNIT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleCashier() {
            // Logic for payment at the cashier
            try {
                cashierQueue.acquire();
                cashierCount.incrementAndGet();
                System.out.printf("Customer %d leaves Counter %d and joins Cashiers Lane.\n", id, counterIndex + 1);
                Thread.sleep(random.nextInt(10) * TIME_UNIT);
                cashierQueue.release();
                cashierCount.decrementAndGet();
                System.out.printf("Customer %d leaves the food court.\n", id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
