package com.datastax.banking.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.banking.model.Aggregate;
import com.datastax.banking.model.Approval;
import org.joda.time.DateTime;

import com.datastax.banking.dao.TransactionDao;
import com.datastax.banking.model.Transaction;
import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;

public class SearchServiceImpl implements SearchService {

	private TransactionDao dao;
	private long timerSum = 0;
	private AtomicLong timerCount= new AtomicLong();

	public SearchServiceImpl() {		
		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");
		this.dao = new TransactionDao(contactPointsStr.split(","));
	}	

	@Override
	public double getTimerAvg(){
		return timerSum/timerCount.get();
	}

//	@Override
//	public List<Transaction> getAllTransactionsByCCnoAndDates(String ccNo, DateTime from, DateTime to) {
		
//		Timer timer = new Timer();
//		List<Transaction> transactions;

//		//If the to and from dates are within the 3 months we can use the latest transactions table as it should be faster.
//		if (from.isAfter(DateTime.now().minusMonths(3))){
//			transactions = dao.getAllTransactionsByCCnoAndDates(ccNo, from, to);
//		}else{
//			transactions = dao.getAllTransactionsByCCnoAndDates(ccNo, from, to);
//		}
			
//		timer.end();
//		timerSum += timer.getTimeTakenMillis();
//		timerCount.incrementAndGet();
//		return transactions;
//	}

	///////////////////////
    // CQL Queries
	///////////////////////
	@Override
	public List<Transaction> getAllTransactions() {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllTransactions();
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Transaction> getDailyTransactionsByMerchant(String merchant, int day) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getDailyTransactionsByMerchant(merchant, day);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Aggregate> getYearlyTransactionsByccNo(String ccNo, int year) {           // SA

		Timer timer = new Timer();
		List<Aggregate> aggregates;
		aggregates = dao.getYearlyTransactionsByccNo(ccNo, year);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return aggregates;
	}

	@Override
	public List<Approval> getTransactionsApprovalByDate(String date) {           // SA

		Timer timer = new Timer();
		List<Approval> approvals;
		approvals = dao.getTransactionsApprovalByDate(date);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return approvals;
	}

	///////////////////////
	// CQL-Solr Queries
	///////////////////////
	@Override
	public List<Transaction> getAllTransactionsByAmount(String amount) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllTransactionsByAmount(amount);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Transaction> getAllRejectedTransactions() {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllRejectedTransactions();
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public String getFacetedTransactionsByMerchant() {           // SA

		Timer timer = new Timer();
		String transactions = dao.getFacetedTransactionsByMerchant();
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public String getFacetedTransactionsByStatusInLastPeriod(String lastPeriod) {           // SA

		Timer timer = new Timer();
		String transactions = dao.getFacetedTransactionsByStatusInLastPeriod(lastPeriod);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public String getFacetedTransactionsByCCnoAndStatusInLastPeriod(String ccNo, String lastPeriod) {           // SA

		Timer timer = new Timer();
		String transactions = dao.getFacetedTransactionsByCCnoAndStatusInLastPeriod(ccNo, lastPeriod);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Transaction> getAllTransactionsByCCno(String ccNo) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllTransactionsByCCno(ccNo);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Transaction> getAllFraudulentTransactionsByCCno(String ccNo) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllFraudulentTransactionsByCCno(ccNo);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

	@Override
	public List<Transaction> getAllFraudulentTransactionsInLastPeriod(String lastPeriod) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllFraudulentTransactionsInLastPeriod(lastPeriod);
		timer.end();
		timerSum += timer.getTimeTakenMillis();
		timerCount.incrementAndGet();
		return transactions;
	}

}
