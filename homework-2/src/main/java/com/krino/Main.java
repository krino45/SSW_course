package com.krino;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    static int symbol_count = 40;
    static CountDownLatch cdl;
    static final int thread_count = 10;
    static final Duration loading_duration = Duration.ofSeconds(2);

    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    static Map<String,  Tuple<Integer, Long>> progressReport = new ConcurrentHashMap<>();

    public synchronized static void ProgressWriter(String name, int currentCount, long startingTime){
        progressReport.put(name, new Tuple<>(currentCount, startingTime));
        int uplines = progressReport.size() + 3; // 1 accounts for the ======== line
        ArrayList<Map.Entry<String, Tuple<Integer, Long>>> entries = new ArrayList<>(progressReport.entrySet());
        entries.sort(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replaceAll("\\D+", ""))));
        System.out.print("\033["+uplines+"A");
        System.out.println(((cdl.getCount() == 0) ? "All" : thread_count - cdl.getCount())+ " threads finished");
        for (Map.Entry<String, Tuple<Integer, Long>> entry : entries) {
            Duration duration = Duration.ofNanos(System.nanoTime() - entry.getValue().y);
            System.out.println(entry.getKey() +
                    " ".repeat(16 - entry.getKey().length()) +
                    "[" + "\033[32m" + "#".repeat(entry.getValue().x) + "\033[39m" +
                    "~".repeat(symbol_count - entry.getValue().x - 1) + "]" + (((entry.getValue().x + 1) >= symbol_count) ? "" :
                    " " + duration.toSeconds() +"."+ duration.toMillis() + "s")
                    );
        }
        int average = progressReport.values().stream().map(t -> t.x).reduce(0,Integer::sum) / thread_count;
        long max = progressReport.values().stream().map(t -> t.y).max(Long::compare).orElseThrow();
        Duration duration = Duration.ofNanos(System.nanoTime() - max);
        System.out.println();
        System.out.println("Main" +
                " ".repeat(12) +
                "[" + "\033[32m" + "#".repeat(average) + "\033[39m" +
                "~".repeat(symbol_count - average - 1) + "]" + (((average) >= symbol_count) ? "" :
                " " + duration.toSeconds() +"."+ duration.toMillis() + "s")
        );
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
                    Thread.sleep(millisPerSymbol + (((new Random()).nextBoolean()) ? -1L : 1L) * (new Random()).nextLong(millisPerSymbol / 2,millisPerSymbol));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ProgressWriter(name, currentSymbols, startTime);
            }
            cdl.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting execution!" + "\n".repeat(thread_count + 2));
        Thread.sleep(1000);
        ExecutorService executorService;
        cdl = new CountDownLatch(thread_count);
        executorService = Executors.newFixedThreadPool(thread_count);
        for (int i = 1; i <= thread_count; i++) {
            executorService.execute(new MyThread("Thread" + i, loading_duration));
        }
        cdl.await();
        executorService.shutdown();
        System.out.println("Done");
    }
}