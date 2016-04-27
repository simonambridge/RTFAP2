package com.datastax.banking.model;

/**
 * Created by dse on 4/27/16.
 */
public class Approval {

    private double approved_rate_hr;
    private double approved_rate_min;
    private int approved_txn_hr;
    private int approved_txn_min;

    public double getApproved_rate_hr() {
        return approved_rate_hr;
    }

    public void setApproved_rate_hr(double approved_rate_hr) {
        this.approved_rate_hr = approved_rate_hr;
    }

    public double getApproved_rate_min() {
        return approved_rate_min;
    }

    public void setApproved_rate_min(double approved_rate_min) {
        this.approved_rate_min = approved_rate_min;
    }

    public int getApproved_txn_hr() {
        return approved_txn_hr;
    }

    public void setApproved_txn_hr(int approved_txn_hr) {
        this.approved_txn_hr = approved_txn_hr;
    }

    public int getApproved_txn_min() {
        return approved_txn_min;
    }

    public void setApproved_txn_min(int approved_txn_min) {
        this.approved_txn_min = approved_txn_min;
    }

}
