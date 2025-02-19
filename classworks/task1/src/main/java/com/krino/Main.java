package com.krino;

import java.time.Duration;
import java.util.concurrent.*;

public class Main {

    static CyclicBarrier cbForTransport;
    static Exchanger<Long> stationTransportExchanger;
    static Exchanger<Long> transportStorageExchanger;
    static final Object soutLock = new Object();

    public static class Station implements Runnable {
        long fuelAmount;
        static long fuelConsumptionRate = 5; // per second

        public Station(long fuel) {
            fuelAmount = fuel;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(Duration.ofSeconds(1));
                } catch (InterruptedException ignored) { }
                if (fuelAmount >= fuelConsumptionRate) {
                    synchronized (soutLock) {
                        System.out.println("Doing something, current fuel levels: " + fuelAmount);
                    }
                    fuelAmount -= fuelConsumptionRate;
                } else {
                    synchronized (soutLock) {
                        System.out.println("Fuel low, reloading (fuel levels: " + fuelAmount + ")");
                    }
                    try {
                        cbForTransport.await();
                        long fuelImport = stationTransportExchanger.exchange(null);
                        fuelAmount += fuelImport;
                    } catch (InterruptedException | BrokenBarrierException ignored) { break; }
                }

            }
        }
    }

    public static class Transport implements Runnable {
        static long capacity = 30;
        static Duration transportTime = Duration.ofSeconds(1);

        Long getFuel() throws InterruptedException {
            synchronized (soutLock) {
                System.out.println("Need to get fuel from storage");
            }
            Thread.sleep(transportTime);
            Long fuel = transportStorageExchanger.exchange(null);
            Thread.sleep(transportTime);
            synchronized (soutLock) {
                System.out.println("Got fuel from storage, going to the station");
            }
            Thread.sleep(transportTime);
            return fuel;
        }

        public void run() {
            while (true) {
                try {
                    cbForTransport.await();
                    stationTransportExchanger.exchange(getFuel());
                    synchronized (soutLock) {
                        System.out.println("Deposited fuel");
                    }
                } catch (InterruptedException | BrokenBarrierException ignored) { break; }
            }
        }
    }


    public static class Storage implements Runnable {
        static long fuelExport = 30;
        public void run() {
            while (true) {
                try {
                    transportStorageExchanger.exchange(fuelExport);
                } catch (InterruptedException ignored) { break; }
                synchronized (soutLock) {
                    System.out.println("Exported " + fuelExport + " units of fuel from storage!");
                }
            }
        }
    }



    public static void main(String[] args) throws InterruptedException {
        stationTransportExchanger = new Exchanger<>();
        transportStorageExchanger = new Exchanger<>();
        cbForTransport = new CyclicBarrier(2);
        try (ExecutorService executorService = Executors.newFixedThreadPool(3)) {
            executorService.execute(new Station(20));
            executorService.execute(new Transport());
            executorService.execute(new Storage());
        }
        System.out.println("Exiting main");
    }
}