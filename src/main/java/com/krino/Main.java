package com.krino;

import com.sun.jdi.Value;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static int symbol_count = 20;
    static CyclicBarrier cyclicBarrier;
    static String os = System.getProperty("os.name");


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
            System.out.println(entry.getKey() + " [" + "#".repeat(entry.getValue()) + " ".repeat(symbol_count - entry.getValue() - 1) + "]");
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
                    Thread.sleep(millisPerSymbol + (new Random()).nextLong(100));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ProgressWriter(name, currentSymbols);
            }
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int thread_count = 5;
        Duration duration = Duration.ofSeconds(5);
        System.out.print("\033[H\033[2J");
        System.out.println("Starting execution!");
        Thread.sleep(1000);
        ExecutorService executorService;
        cyclicBarrier = new CyclicBarrier(thread_count, null);
        executorService = Executors.newFixedThreadPool(thread_count);
        executorService.execute(new MyThread("Thread1", duration));
        executorService.execute(new MyThread("Thread2", duration));
        executorService.execute(new MyThread("Thread3", duration));
        executorService.execute(new MyThread("Thread4", duration));
        executorService.execute(new MyThread("Thread5", duration));

        executorService.shutdown();
        System.out.println("Done");
    }
}