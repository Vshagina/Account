package com.company;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private double balance;
    private Lock lock;
    private Condition conclusion;

    public Account(double initialBalance) {
        this.balance = initialBalance;
        this.lock = new ReentrantLock();
        this.conclusion = lock.newCondition();
    }

    public void deposit(double amount) {
        lock.lock();
        try {
            balance += amount;
            System.out.println("Пополнение: " + amount + ", Баланс: " + balance);
            conclusion.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(double amount) throws InterruptedException {
        lock.lock();
        try {
            while (balance < amount) {
                System.out.println("Ожидание пополнения для снятия: " + amount);
                conclusion.await();
            }
            balance -= amount;
            System.out.println("Снятие: " + amount + ", Баланс: " + balance);
        } finally {
            lock.unlock();
        }
    }

    public double getBalance() {
        return balance;
    }

    public static void main(String[] args) {
        Account account = new Account(1000);


        Thread depositThread = new Thread(() -> {
            Random random = new Random();
            while (true) {
                double depositAmount = random.nextDouble() * 100;
                account.deposit(depositAmount);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        Thread withdrawalThread = new Thread(() -> {
            Random random = new Random();
            while (true) {
                double withdrawalAmount = random.nextDouble() * 50;
                try {
                    account.withdraw(withdrawalAmount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        depositThread.start();
        withdrawalThread.start();


        while (true) {
            System.out.println("Текущий баланс: " + account.getBalance());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}