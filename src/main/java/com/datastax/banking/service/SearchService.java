package com.datastax.banking.service;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.datastax.banking.model.Transaction;

public interface SearchService {

	public double getTimerAvg();


	//	List<Transaction> getAllTransactionsByCCnoAndDates(String ccNo, DateTime from, DateTime to); // SA - CQL only
	List<Transaction> getAllTransactions();                            // SA - CQL only
	List<Transaction> getDailyTransactionsByMerchant(String merchant, int day);    // SA - CQL only
	List<Transaction> getYearlyTransactionsByccNo(String ccNo, int year);          // SA - CQL only
	List<Transaction> getAllRejectedTransactions();                                // SA - Solr query
	String getFacetedTransactionsByMerchant();                                     // SA - Solr query
	List<Transaction> getAllFraudulentTransactionsByCCno(String ccNo);             // SA - Solr query
	List<Transaction> getAllFraudulentTransactionsInLastPeriod(String lastPeriod); // SA - Solr query
}
