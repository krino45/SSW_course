package com.krino;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    static int symbol_count = 120;
    static CyclicBarrier cyclicBarrier;
    static CountDownLatch cdl;
    static String os = System.getProperty("os.name");
    static final int thread_count = 10;
    static final Duration duration = Duration.ofSeconds(2);

    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    static HashMap<String,  Tuple<Integer, Long>> progressReport = new HashMap<>();

    public synchronized static void ProgressWriter(String name, int currentCount, long startingTime){
        progressReport.put(name, new Tuple<Integer, Long>(currentCount, startingTime));
        try {
            if (os.contains("Windows")) {
                System.out.print("\033["+(thread_count + 3)+"A");
            }
            else
                new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("\n=========");
        ArrayList<Map.Entry<String, Tuple<Integer, Long>>> entries = new ArrayList<>(progressReport.entrySet());
        entries.sort(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replaceAll("\\D+", ""))));
        for (Map.Entry<String, Tuple<Integer, Long>> entry : entries) {
            Duration duration = Duration.ofNanos(System.nanoTime() - entry.getValue().y);
            System.out.println(entry.getKey() +
                    " ".repeat(16 - entry.getKey().length()) +
                    "[" + "#".repeat(entry.getValue().x) +
                    " ".repeat(symbol_count - entry.getValue().x - 1) + "]" + (((entry.getValue().x + 1) >= symbol_count) ? "" :
                    " " + duration.toSeconds() +"."+ duration.toMillis() + "s")
                    );
        }
        System.out.print("\033["+(thread_count + 3)+"B");
    }

    static class MyThread implements Runnable {
        String name;
        Duration time;
        long millisPerSymbol;

        public MyThread(String name, Duration time) {
            this.name = name;
            this.time = time;
            millisPerSymbol = time.toMillis() / symbol_count;
        }

        public void run()
        {
            long startTime = System.nanoTime();
            for(int currentSymbols = 0; currentSymbols < symbol_count; currentSymbols++) {
                try {
                    Thread.sleep(millisPerSymbol + (((new Random()).nextBoolean()) ? -1L : 1L) * (new Random()).nextLong(millisPerSymbol / 2));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ProgressWriter(name, currentSymbols, startTime);
            }
            try {
                cyclicBarrier.await();
                cdl.countDown();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting execution!" + "\n".repeat(thread_count + 2));
        Thread.sleep(1000);
        ExecutorService executorService;
        cyclicBarrier = new CyclicBarrier(thread_count, null);
        cdl = new CountDownLatch(1);
        executorService = Executors.newFixedThreadPool(thread_count);
        for (int i = 1; i <= thread_count; i++) {
            executorService.execute(new MyThread("Thread" + i, duration));
        }
        cdl.await();
        executorService.shutdown();
        System.out.println("Done");
    }
}