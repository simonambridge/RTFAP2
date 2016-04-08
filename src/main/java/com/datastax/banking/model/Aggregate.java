package com.datastax.banking.model;

/**
 * Created by dse on 4/8/16.
 */
public class Aggregate {

    private String creditCardNo;
    private int year;
    private double max_amount;
    private double min_amount;
    private double total_amount;
    private long total_count;

    public Aggregate() {
        super();
    }

    public String getCreditCardNo() {
        return creditCardNo;
    }

    public void setCreditCardNo(String creditCardNo) {
        this.creditCardNo = creditCardNo;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getMax_amount() {
        return max_amount;
    }

    public void setMax_amount(double max_amount) {
        this.max_amount = max_amount;
    }

    public double getMin_amount() {
        return min_amount;
    }

    public void setMin_amount(double min_amount) {
        this.min_amount = min_amount;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public long getTotal_count() {
        return total_count;
    }

    public void setTotal_count(long total_count) {
        this.total_count = total_count;
    }
}
