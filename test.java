/**
 * FoodCourt.java Name: [Your Name] UA ID: [Your UA ID]
 */

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class test {
    // Constants
    private static final int COUNTER_CAPACITY = 40;
    private static final int WAITING_AREA_CAPACITY = 100;
    private static final int NUM_COUNTERS = 3;
    private static final int NUM_CASHIERS = 10;
    private static final int TIME_UNIT = 100; // 1 unit = 100ms

    // Shared resources
    private static AtomicInteger waitingAreaCount = new AtomicInteger(0);
    private static Semaphore[] counters = new Semaphore[NUM_COUNTERS];
    private static Semaphore waitingArea = new Semaphore(WAITING_AREA_CAPACITY, true);
    private static Semaphore cashierQueue = new Semaphore(NUM_CASHIERS, true);
    private static AtomicInteger[] counterCounts = new AtomicInteger[NUM_COUNTERS];
    private static AtomicInteger cashierCount = new AtomicInteger(0);

    // Configuration variables
    private static int timeUnits;
    private static int customerLimit;
    private static boolean isOpen = true;

    public static void main(String[] args) {
        // Parse command-line arguments
        parseArguments(args);
        initializeResources();

        // Start the system thread (controls simulation duration)
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

        // Start customer threads
        for (int i = 0; i < customerLimit; i++) {
            new Customer(i).start();
            try {
                Thread.sleep(new Random().nextInt(TIME_UNIT)); // Random delay between customer
                                                               // arrivals
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Parse arguments
    private static void parseArguments(String[] args) {
        if (args.length != 2) {
            System.out
                    .println("Usage: java FoodCourt <simulation time units> <number of customers>");
            System.exit(1);
        }
        timeUnits = Integer.parseInt(args[0]);
        customerLimit = Integer.parseInt(args[1]);
    }

    // Initialize semaphores and counters
    private static void initializeResources() {
        for (int i = 0; i < NUM_COUNTERS; i++) {
            counters[i] = new Semaphore(COUNTER_CAPACITY, true);
            counterCounts[i] = new AtomicInteger(0);
        }
    }

    // Customer class
    static class Customer extends Thread {
        private final int id;
        private final Random random = new Random();
        private final int counterIndex = random.nextInt(NUM_COUNTERS);

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            System.out.println("Customer " + id + ": Arrived at the food court.");
            if (!tryWaitingArea()) {
                System.out
                        .println("Customer " + id + ": Left in frustration, waiting area is full.");
                return;
            }
            chooseCounter();
            handleCashier();
            System.out.println("Customer " + id + ": Has exited the food court.");
        }

        private boolean tryWaitingArea() {
            try {
                if (waitingArea.tryAcquire()) {
                    waitingAreaCount.incrementAndGet();
                    System.out.println(
                            "Customer " + id + ": Entered the waiting area. Current count: "
                                    + waitingAreaCount.get());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void chooseCounter() {
            try {
                counters[counterIndex].acquire();
                counterCounts[counterIndex].incrementAndGet();
                System.out.println("Customer " + id + ": Joined Counter " + (counterIndex + 1)
                        + ". Counter count: " + counterCounts[counterIndex].get());
                Thread.sleep(random.nextInt(10) * TIME_UNIT);
                counters[counterIndex].release();
                counterCounts[counterIndex].decrementAndGet();
                System.out.println("Customer " + id + ": Left Counter " + (counterIndex + 1)
                        + ". Counter count: " + counterCounts[counterIndex].get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void handleCashier() {
            try {
                cashierQueue.acquire();
                cashierCount.incrementAndGet();
                System.out.println("Customer " + id + ": At the cashier. Current cashiers in use: "
                        + cashierCount.get());
                Thread.sleep(random.nextInt(10) * TIME_UNIT);
                cashierQueue.release();
                cashierCount.decrementAndGet();
                System.out.println(
                        "Customer " + id + ": Finished at the cashier. Current cashiers in use: "
                                + cashierCount.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
