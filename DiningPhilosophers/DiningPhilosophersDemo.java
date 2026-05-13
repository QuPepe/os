import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dining Philosophers Demo
 * Demonstrates three classical concurrency solutions to the Dining Philosophers
 * problem:
 * 1. Synchronized monitor
 * 2. Semaphores
 * 3. ReentrantLock + Condition variables
 * 
 * Author: Frank Chuang
 */
public class DiningPhilosophersDemo {
    public static final int NUM_PHILOSOPHERS = 5;
    public static final int THINKING_TIME = 2000; // Max thinking time in ms
    public static final int EATING_TIME = 1000; // Max eating time in ms

    public static void main(String[] args) {
        System.out.println("Choose a Dining Philosophers implementation:");
        System.out.println("(1) Synchronized monitor");
        System.out.println("(2) Semaphores");
        System.out.println("(3) ReentrantLock + Condition variables");

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            int choice = scanner.nextInt();
            DiningPhilosophers table;

            // Select one of the three implementations
            switch (choice) {
                case 1 -> {
                    table = new DiningPhilosophersSync();
                    System.out.println("Using synchronized monitor.");
                }
                case 2 -> {
                    table = new DiningPhilosophersSem();
                    System.out.println("Using semaphores.");
                }
                case 3 -> {
                    table = new DiningPhilosophersLock();
                    System.out.println("Using ReentrantLock + Condition variables.");
                }
                default -> {
                    System.out.println("Invalid choice. Defaulting to ReentrantLock + Condition variables.");
                    table = new DiningPhilosophersLock();
                }
            }

            // Start philosopher threads
            for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                int id = i;
                new Thread(() -> {
                    try {
                        while (true) {
                            System.out.println("Philosopher " + id + " is thinking...");
                            Thread.sleep((long) (Math.random() * THINKING_TIME));

                            table.pickup(id);
                            System.out.println("Philosopher " + id + " is eating...");
                            Thread.sleep((long) (Math.random() * EATING_TIME));

                            table.putdown(id);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    // Common interface for all implementations
    interface DiningPhilosophers {
        void pickup(int i) throws InterruptedException;

        void putdown(int i);
    }

    // --------------------------------------------------------------------
    // 1. Solution using a synchronized monitor
    // --------------------------------------------------------------------
    static class DiningPhilosophersSync implements DiningPhilosophers {
        private enum State {
            THINKING, HUNGRY, EATING
        }

        private final State[] state = new State[NUM_PHILOSOPHERS];

        public DiningPhilosophersSync() {
            for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                state[i] = State.THINKING;
            }
        }

        public synchronized void pickup(int i) throws InterruptedException {
            state[i] = State.HUNGRY;
            while (!canEat(i)) {
                wait(); // Wait until the philosopher can eat
            }
            state[i] = State.EATING;
        }

        public synchronized void putdown(int i) {
            state[i] = State.THINKING;
            notifyAll(); // Wake up all threads (some may not be eligible to eat)
        }

        // A philosopher can eat if they are hungry, and both neighbors are not eating.
        private boolean canEat(int i) {
            int prevNeighbor = (i + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS;
            int nextNeighbor = (i + 1) % NUM_PHILOSOPHERS;
            return state[i] == State.HUNGRY &&
                    state[prevNeighbor] != State.EATING &&
                    state[nextNeighbor] != State.EATING;
        }
    }

    // --------------------------------------------------------------------
    // 2. Solution using semaphores
    // --------------------------------------------------------------------
    static class DiningPhilosophersSem implements DiningPhilosophers {
        // There is no need for an internal state variable to track whether a
        // philosopher is thinking, hungry, or eating.
        // The semaphore's lock status effectively enforces mutual exclusion.

        private final Semaphore[] chopsticks = new Semaphore[NUM_PHILOSOPHERS];

        public DiningPhilosophersSem() {
            for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                chopsticks[i] = new Semaphore(1); // One binary semaphore per chopstick
            }
        }

        public void pickup(int i) throws InterruptedException {
            int firstChopstick = (i + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS;
            int secondChopstick = i;

            // Alternate acquisition order based on philosopher index to avoid circular
            // wait.
            if (i % 2 == 0) {
                chopsticks[firstChopstick].acquire();
                chopsticks[secondChopstick].acquire();
            } else {
                chopsticks[secondChopstick].acquire();
                chopsticks[firstChopstick].acquire();
            }
        }

        public void putdown(int i) {
            int firstChopstick = (i + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS;
            int secondChopstick = i;
            chopsticks[firstChopstick].release();
            chopsticks[secondChopstick].release();
        }
    }

    // --------------------------------------------------------------------
    // 3. Solution using ReentrantLock and Condition variables
    // --------------------------------------------------------------------
    static class DiningPhilosophersLock implements DiningPhilosophers {
        private enum State {
            THINKING, HUNGRY, EATING
        }

        private final State[] state = new State[NUM_PHILOSOPHERS];
        private final Lock lock = new ReentrantLock(true); // Fair lock to reduce starvation
        private final Condition[] cond = new Condition[NUM_PHILOSOPHERS];

        public DiningPhilosophersLock() {
            for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                state[i] = State.THINKING;
                cond[i] = lock.newCondition(); // Each philosopher has its own condition
            }
        }

        public void pickup(int i) throws InterruptedException {
            lock.lock();
            try {
                state[i] = State.HUNGRY;
                while (!canEat(i)) {
                    cond[i].await(); // Wait until eligible to eat
                }
                state[i] = State.EATING;
            } finally {
                lock.unlock();
            }
        }

        public void putdown(int i) {
            lock.lock();
            try {
                state[i] = State.THINKING;

                // Check and notify neighbors
                int prevNeighbor = (i + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS;
                int nextNeighbor = (i + 1) % NUM_PHILOSOPHERS;

                if (canEat(prevNeighbor))
                    cond[prevNeighbor].signal();
                if (canEat(nextNeighbor))
                    cond[nextNeighbor].signal();
            } finally {
                lock.unlock();
            }
        }

        // A philosopher can eat if they are hungry and both neighbors are not eating.
        private boolean canEat(int i) {
            int prevNeighbor = (i + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS;
            int nextNeighbor = (i + 1) % NUM_PHILOSOPHERS;
            return state[i] == State.HUNGRY &&
                    state[prevNeighbor] != State.EATING &&
                    state[nextNeighbor] != State.EATING;
        }
    }
}
