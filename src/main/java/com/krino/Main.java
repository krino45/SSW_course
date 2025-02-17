package com.krino;

import com.sun.jdi.Value;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    static int symbol_count = 100;
    static CyclicBarrier cyclicBarrier;
    static CountDownLatch cdl;
    static String os = System.getProperty("os.name");
    static final int thread_count = 10;
    static final Duration duration = Duration.ofSeconds(5);



    static HashMap<String, Integer> progressReport = new HashMap<>();

    public synchronized static void ProgressWriter(String name, int currentCount){
        progressReport.put(name, currentCount);
        try {
            if (os.contains("Windows")) {
                System.out.print("\033[H");
                System.out.flush();
            }
            else
                new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("\n=========");
        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>(progressReport.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        for (Map.Entry<String, Integer> entry : entries) {
            System.out.println(entry.getKey() + " ".repeat(16 - entry.getKey().length()) + "[" + "#".repeat(entry.getValue()) + " ".repeat(symbol_count - entry.getValue() - 1) + "]");
        }
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
            for(int currentSymbols = 0; currentSymbols < symbol_count; currentSymbols++) {
                try {
                    Thread.sleep(millisPerSymbol + (((new Random()).nextBoolean()) ? -1L : 1L) * (new Random()).nextLong(50));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ProgressWriter(name, currentSymbols);
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
        System.out.print("\033[H\033[2J");
        System.out.println("Starting execution!");
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