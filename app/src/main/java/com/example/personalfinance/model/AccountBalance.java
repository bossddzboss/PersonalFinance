package com.example.personalfinance.model;

public class AccountBalance {

    public final int balance;
    public final String lastUpdate;

    public AccountBalance(int balance, String lastUpdate) {
        this.balance = balance;
        this.lastUpdate = lastUpdate;
    }
}
