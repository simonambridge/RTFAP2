package com.datastax.banking.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
//	public List<Transaction> getTransactionsByTagAndDate(String ccNo, Set<String> search, DateTime from, DateTime to) {
		
//		Timer timer = new Timer();
//		List<Transaction> transactions;

//		//If the to and from dates are within the 3 months we can use the latest transactions table as it should be faster.
//		if (from.isAfter(DateTime.now().minusMonths(3))){
//			transactions = dao.getTransactionsForCCNoTagsAndDate(ccNo, search, from, to);
//		}else{
//			transactions = dao.getLatestTransactionsForCCNoTagsAndDate(ccNo, search, from, to);
//		}
			
//		timer.end();
//		timerSum += timer.getTimeTakenMillis();
//		timerCount.incrementAndGet();
//		return transactions;
//	}


//	@Override
//	public List<Transaction> getAllLatestTransactionsByCC(String ccNo) {           // SA

//		Timer timer = new Timer();
//		List<Transaction> transactions;
//		transactions = dao.getAllLatestTransactionsByCC(ccNo);
//		timer.end();
//		timerSum += timer.getTimeTakenMillis();
//		timerCount.incrementAndGet();
//		return transactions;
//	}

//	@Override
//	public List<Transaction> getAllRtfapTransactionsByCC(String ccNo, DateTime from) {           // SA

//		Timer timer = new Timer();
//		List<Transaction> transactions;
//		transactions = dao.getAllRtfapTransactionsByCC(ccNo, from);
//		timer.end();
//		timerSum += timer.getTimeTakenMillis();
//		timerCount.incrementAndGet();
//		return transactions;
//	}

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
	public List<Transaction> getAllFraudulentTransactionsByCC(String ccNo) {           // SA

		Timer timer = new Timer();
		List<Transaction> transactions;
		transactions = dao.getAllFraudulentTransactionsByCC(ccNo);
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
