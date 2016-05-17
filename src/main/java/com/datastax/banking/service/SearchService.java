package com.datastax.banking.service;

import java.util.List;
import java.util.Set;

import com.datastax.banking.model.Aggregate;
import com.datastax.banking.model.Approval;
import org.joda.time.DateTime;

import com.datastax.banking.model.Transaction;

public interface SearchService {

	public double getTimerAvg();


	///////////////////////
	// CQL Queries
	///////////////////////
	//	List<Transaction> getAllTransactionsByCCnoAndDates(String ccNo, DateTime from, DateTime to); // SA - CQL only
	List<Transaction> getAllTransactions();                                                       // SA - CQL only
	List<Transaction> getDailyTransactionsByMerchant(String merchant, int day);                   // SA - CQL only
	List<Aggregate> getYearlyTransactionsByccNo(String ccNo, int year);                           // SA - CQL only
	List<Approval> getTransactionsApprovalByDate(String date);                                    // SA - CQL only

	///////////////////////
	// CQL-Solr Queries
	///////////////////////
	List<Transaction> getAllTransactionsByAmount(String amount);                                  // SA - Solr query
	List<Transaction> getAllRejectedTransactions();                                               // SA - Solr query
	String getFacetedTransactionsByMerchant();                                                    // SA - Solr query
	String getFacetedTransactionsByStatusInLastPeriod(String lastPeriod);                         // SA - Solr query
	String getFacetedTransactionsByCCnoAndStatusInLastPeriod(String ccNo, String lastPeriod);     // SA - Solr query
	List<Transaction> getAllTransactionsByCCno(String ccNo);                                      // SA - Solr query
	List<Transaction> getAllFraudulentTransactionsByCCno(String ccNo);                            // SA - Solr query
	List<Transaction> getAllFraudulentTransactionsInLastPeriod(String lastPeriod);                // SA - Solr query
}
