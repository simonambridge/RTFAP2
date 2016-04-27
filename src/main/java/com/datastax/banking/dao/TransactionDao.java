package com.datastax.banking.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.banking.model.Aggregate;
import com.datastax.banking.model.Approval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Transaction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;


/**
 * Inserts into 2 tables 
 * @author patrickcallaghan
 * Adapted for SE Banking toolkit by simonambridge
 */
public class TransactionDao {

	private static Logger logger = LoggerFactory.getLogger(TransactionDao.class);
	private Session session;

    private static String rtfapkeyspaceName = "rtfap";                     // SA

    private static String rtfapTransactionTable = rtfapkeyspaceName + ".transactions";   // SA
	private static String merchantDailyRollupTable = rtfapkeyspaceName + ".dailytxns_bymerchant";   // SA
	private static String ccNoYearlyRollupTable = rtfapkeyspaceName + ".yearlyaggregates_bycc";   // SA
	private static String txnCountTable = rtfapkeyspaceName + ".txn_count_min";   // SA


	///////////////////////
	// CQL Queries
	///////////////////////
	// The individual parameters are passed as bind variables to the prepared statement
	//
	//private static final String GET_ALL_TRANSACTIONS_BY_CCNO_AND_DATES = "select * from " + rtfaptransactionTable
	//		+ " where cc_no = ? and year = ? and transaction_time >= ? and transaction_time < ?";

	private static final String GET_ALL_TRANSACTIONS = "select * from " + rtfapTransactionTable + ";";     // SA

	private static final String GET_DAILY_TRANSACTIONS_BY_MERCHANT = "select * from " + merchantDailyRollupTable // SA
			+ " where merchant = ? and day = ?";

	private static final String GET_YEARLY_TRANSACTIONS_BY_CCNO = "select * from " + ccNoYearlyRollupTable  // SA
			+ " where cc_No = ? and year = ?";

	private static final String GET_TRANSACTIONS_APPROVALS_BY_DATE = "select approved_rate_hr, approved_txn_hr," // SA
	        + " approved_rate_min, approved_txn_min"
	        + " from " + txnCountTable
			+ " where year= ? and month= ? and day= ? and hour= ? and minute= ?";


	///////////////////////
	// CQL-Solr Queries
	///////////////////////
	// The entire where clause is passed as a parameter to the prepared statement
	//
	private static final String GET_ALL_TRANSACTIONS_BY_AMOUNT = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ? limit 2147483647";

	private static final String GET_ALL_TRANSACTIONS_BY_CCNO = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_DECLINED_TRANSACTIONS = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_REJECTED_TRANSACTIONS = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_FACETED_TRANSACTIONS_BY_MERCHANT = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_FACETED_TRANSACTIONS_BY_STATUS_IN_LAST_PERIOD = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_FACETED_TRANSACTIONS_BY_CCNO_AND_STATUS_IN_LAST_PERIOD = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_FRAUDULENT_TRANSACTIONS_BY_CCNO = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_FRAUDULENT_TRANSACTIONS_IN_LAST_PERIOD = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";


	//private PreparedStatement getAllTransactionsByCCnoAndDates;       // SA
	private PreparedStatement getAllTransactions;                       // SA - CQL query
	private PreparedStatement getDailyTransactionsByMerchant;           // SA - CQL query
	private PreparedStatement getYearlyTransactionsByCCNo;              // SA - CQL query
	private PreparedStatement getTransactionsApprovalByDate;              // SA - CQL query

	private PreparedStatement getAllTransactionsByCCno;                           // SA - Solr query
	private PreparedStatement getAllTransactionsByAmount;                         // SA - Solr query
	private PreparedStatement getAllRejectedTransactions;                         // SA - Solr query
	private PreparedStatement getAllDeclinedTransactions;                         // SA - Solr query
	private PreparedStatement getFacetedTransactionsByMerchant;                   // SA - Solr query
	private PreparedStatement getFacetedTransactionsByStatusInLastPeriod;         // SA - Solr query
	private PreparedStatement getFacetedTransactionsByCCnoAndStatusInLastPeriod;  // SA - Solr query
	private PreparedStatement getAllFraudulentTransactionsByCCno;                 // SA - Solr query
	private PreparedStatement getAllFraudulentTransactionsInLastPeriod;           // SA - Solr query

	private AtomicLong count = new AtomicLong(0);

	public TransactionDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();
        // generate a prepared statement based on the contents of string e.g. GET_ALL_FRAUDULENT_TRANSACTIONS_BY_CCNO
		try {

//			this.getAllTransactionsByCCnoAndDates = session.prepare(GET_ALL_TRANSACTIONS_BY_CCNO_AND_DATES);
			this.getAllTransactions = session.prepare(GET_ALL_TRANSACTIONS);    // SA
			this.getDailyTransactionsByMerchant = session.prepare(GET_DAILY_TRANSACTIONS_BY_MERCHANT);    // SA
			this.getYearlyTransactionsByCCNo = session.prepare(GET_YEARLY_TRANSACTIONS_BY_CCNO);    // SA
			this.getTransactionsApprovalByDate = session.prepare(GET_TRANSACTIONS_APPROVALS_BY_DATE);    // SA

			this.getAllTransactionsByCCno = session.prepare(GET_ALL_TRANSACTIONS_BY_CCNO);    // SA
			this.getAllTransactionsByAmount = session.prepare(GET_ALL_TRANSACTIONS_BY_AMOUNT);    // SA
			this.getAllDeclinedTransactions = session.prepare(GET_ALL_DECLINED_TRANSACTIONS);    // SA
			this.getAllRejectedTransactions = session.prepare(GET_ALL_REJECTED_TRANSACTIONS);    // SA
			this.getFacetedTransactionsByMerchant = session.prepare(GET_FACETED_TRANSACTIONS_BY_MERCHANT);    // SA
			this.getFacetedTransactionsByStatusInLastPeriod = session.prepare(GET_FACETED_TRANSACTIONS_BY_STATUS_IN_LAST_PERIOD);    // SA
			this.getFacetedTransactionsByCCnoAndStatusInLastPeriod = session.prepare(GET_FACETED_TRANSACTIONS_BY_CCNO_AND_STATUS_IN_LAST_PERIOD);    // SA
			this.getAllFraudulentTransactionsByCCno = session.prepare(GET_ALL_FRAUDULENT_TRANSACTIONS_BY_CCNO);    // SA
			this.getAllFraudulentTransactionsInLastPeriod = session.prepare(GET_ALL_FRAUDULENT_TRANSACTIONS_IN_LAST_PERIOD);    // SA

			//logger.info("Query String=" + this.getAllTransactionsByCCno);

		} catch (Exception e) {
			e.printStackTrace();
			session.close();
			cluster.close();
		}
	}


//	public List<Transaction> getAllTransactionsByCC(String ccNo) {                                 // SA
//		ResultSet resultSet = this.session.execute(getAllTransactionsByCCno.bind(ccNo));
//		return processResultSet(resultSet);
//	}

	///////////////////////
	// CQL Queries
	///////////////////////

	public List<Transaction> getAllTransactions() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		logger.info(">> getAllTransactionsApprovalByDate: - <no params>");
		ResultSet resultSet = this.session.execute(getAllTransactions.bind());
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getDailyTransactionsByMerchant(String merchant, int day) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		ResultSet resultSet = this.session.execute(getDailyTransactionsByMerchant.bind(merchant, day));
		logger.info(">> getDailyTransactionsByMerchant: " + merchant + "," + day);
//		ResultSet resultSet = this.session.execute(getDailyTransactionsByMerchant.bind("GAP", 20160309));
		return processTransactionResultSet(resultSet);
	}

	public List<Aggregate> getYearlyTransactionsByccNo(String ccNo, int year) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		logger.info(">> getYearlyTransactionsByccNo: " + ccNo + "," + year);
		ResultSet resultSet = this.session.execute(getYearlyTransactionsByCCNo.bind(ccNo, year));
		return processAggregateResultSet(resultSet);
	}

	public List<Approval> getTransactionsApprovalByDate(String date) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		// logger.info(">> date =" + date);
		// logger.info(">> month =" + Integer.parseInt(date.substring(0, 4)));
		// logger.info(">> month =" + Integer.parseInt(date.substring(4, 6)));
		// logger.info(">> day =" + Integer.parseInt(date.substring(6, 8)));
		// logger.info(">> hour =" + Integer.parseInt(date.substring(8, 10)));
		// logger.info(">> minute =" + Integer.parseInt(date.substring(10, 12)));
		Integer year   = Integer.parseInt(date.substring(0, 4));
		Integer month  = Integer.parseInt(date.substring(4, 6));
		Integer day    = Integer.parseInt(date.substring(6, 8));
		Integer hour   = Integer.parseInt(date.substring(8, 10));
		Integer minute = Integer.parseInt(date.substring(10, 12));
		logger.info(">> getTransactionsApprovalByDate: " + year + "," + month + "," + day + "," + hour + "," + minute);
		ResultSet resultSet = this.session.execute(getTransactionsApprovalByDate.bind(year, month, day, hour, minute));
		return processApprovalResultSet(resultSet);
	}

	///////////////////////
	// CQL-Solr Queries
	///////////////////////

	public List<Transaction> getAllTransactionsByAmount(String amount) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"*:*\", \"fq\":\"amount:[" + amount + " TO *]\"}}";
		logger.info(">> getAllTransactionsByAmount: " + amount);
		ResultSet resultSet = this.session.execute(getAllTransactionsByAmount.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getAllTransactionsByCCno(String ccNo) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"cc_no: " + ccNo + "\"}";
		logger.info(">> getAllTransactionsByCCno: " + ccNo);
		ResultSet resultSet = this.session.execute(getAllTransactionsByCCno.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getAllDeclinedTransactions() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"status:Declined\"}";
		logger.info(">> getAllDeclinedTransactions: - <no params>");
		ResultSet resultSet = this.session.execute(getAllDeclinedTransactions.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}
	public List<Transaction> getAllRejectedTransactions() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"status:Rejected\"}";
		logger.info(">> getAllRejectedTransactions: - <no params>");
		ResultSet resultSet = this.session.execute(getAllRejectedTransactions.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}

	public String getFacetedTransactionsByMerchant() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"*:*\", \"facet\":{\"field\":\"merchant\"}}";
		logger.info(">> getFacetedTransactionsByMerchant: - <no params>");
		ResultSet resultSet = this.session.execute(getFacetedTransactionsByMerchant.bind(solrBindString));
		return processFacetResultSet(resultSet);
	}

	public String getFacetedTransactionsByStatusInLastPeriod(String lastPeriod) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"*:*\", \"fq\":\"txn_time:[NOW-1" + lastPeriod + " TO *]\",\"facet\":{\"field\":\"status\"}}";
		logger.info(">> getFacetedTransactionsByStatusInLastPeriod: " + lastPeriod);
		ResultSet resultSet = this.session.execute(getFacetedTransactionsByStatusInLastPeriod.bind(solrBindString));
		return processFacetResultSet(resultSet);
	}

	public String getFacetedTransactionsByCCnoAndStatusInLastPeriod(String ccNo, String lastPeriod) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"cc_no: " + ccNo + "\", \"fq\":\"txn_time:[NOW-1" + lastPeriod + " TO *]\",\"facet\":{\"field\":\"status\"}}";
		logger.info(">> getFacetedTransactionsByCCnoAndStatusInLastPeriod: " + ccNo + "," + lastPeriod);
		ResultSet resultSet = this.session.execute(getFacetedTransactionsByCCnoAndStatusInLastPeriod.bind(solrBindString));
		return processFacetResultSet(resultSet);
	}

	public List<Transaction> getAllFraudulentTransactionsByCCno(String ccNo) {                    // SA
        // execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"cc_no: " + ccNo + "\", \"fq\":[\"tags:Fraudulent\"]}";
		logger.info(">> getAllFraudulentTransactionsByCCn: " + ccNo);
		ResultSet resultSet = this.session.execute(getAllFraudulentTransactionsByCCno.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getAllFraudulentTransactionsInLastPeriod(String lastPeriod) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of ccNo
		String solrBindString = "{\"q\":\"*:*\", \"fq\":[\"txn_time:[NOW-1" + lastPeriod + " TO *]\", \"tags:Fraudulent\"]}";
		logger.info(">> getAllFraudulentTransactionsInLastPeriod: " + lastPeriod);
		ResultSet resultSet = this.session.execute(getAllFraudulentTransactionsInLastPeriod.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}


    private List<Transaction> processTransactionResultSet(ResultSet resultSet) {   // SA
        List<Row> rows = resultSet.all();
        List<Transaction> transactions = new ArrayList<Transaction>();

        for (Row row : rows) {

            Transaction transaction = rowToTransaction(row);
            transactions.add(transaction);
        }
        return transactions;
	}

	private List<Aggregate> processAggregateResultSet(ResultSet resultSet) {   // SA
		List<Row> rows = resultSet.all();
		List<Aggregate> aggregates = new ArrayList<Aggregate>();

		for (Row row : rows) {

			Aggregate aggregate = rowToAggregate(row);
			aggregates.add(aggregate);
		}
		return aggregates;
	}

	private List<Approval> processApprovalResultSet(ResultSet resultSet) {   // SA
		List<Row> rows = resultSet.all();
		List<Approval> approvals = new ArrayList<Approval>();

		for (Row row : rows) {

			Approval approval = rowToApproval(row);
			approvals.add(approval);
		}
		return approvals;
	}

	private String processFacetResultSet(ResultSet rs) {   // SA
		Row row = rs.one();
		String result = row.getString(0);
		logger.info("Facet: " + result);
		return result;
	}

	private Transaction rowToTransaction(Row row) {

		Transaction t = new Transaction();

		t.setAmount(row.getDouble("amount"));
		t.setCreditCardNo(row.getString("cc_no"));
		t.setMerchant(row.getString("merchant"));
		t.setccProvider(row.getString("cc_provider"));
		t.setLocation(row.getString("location"));
		t.setTransactionId(row.getString("txn_id"));
		t.setTransactionTime(row.getDate("txn_time"));
		t.setUserId(row.getString("user_id"));
		t.setNotes(row.getString("notes"));
		t.setStatus(row.getString("status"));
		t.setTags(row.getSet("tags", String.class));

		return t;
	}
	private Aggregate rowToAggregate(Row row) {

		Aggregate a = new Aggregate();

		a.setCreditCardNo(row.getString("cc_no"));
		a.setYear(row.getInt("year"));
		a.setMin_amount(row.getDouble("min_amount"));
		a.setMax_amount(row.getDouble("max_amount"));
		a.setTotal_amount(row.getDouble("total_amount"));
		a.setTotal_count(row.getLong("total_count"));

		return a;
	}
	private Approval rowToApproval(Row row) {

		Approval a = new Approval();

		a.setApproved_rate_hr(row.getDouble("approved_rate_hr"));
		a.setApproved_rate_min(row.getDouble("approved_rate_min"));
		a.setApproved_txn_hr(row.getInt("approved_txn_hr"));
		a.setApproved_txn_min(row.getInt("approved_txn_min"));

		return a;
	}
}
