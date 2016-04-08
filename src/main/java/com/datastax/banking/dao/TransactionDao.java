package com.datastax.banking.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.datastax.banking.model.Aggregate;
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


	// cql queries - the individual parameters are passed as bind variables to the prepared statement
	//
	//private static final String GET_ALL_TRANSACTIONS_BY_CCNO_AND_DATES = "select * from " + rtfaptransactionTable
	//		+ " where cc_no = ? and year = ? and transaction_time >= ? and transaction_time < ?";

	private static final String GET_ALL_TRANSACTIONS = "select * from " + rtfapTransactionTable + ";";     // SA
	private static final String GET_DAILY_TRANSACTIONS_BY_MERCHANT = "select * from " + merchantDailyRollupTable
			+ " where merchant = ? and day = ?";
	private static final String GET_YEARLY_TRANSACTIONS_BY_CCNO = "select * from " + ccNoYearlyRollupTable
			+ " where cc_No = ? and year = ?";


	// Solr queries - the entire where clause is passed as a parameter to the prepared statement
	//
	private static final String GET_ALL_REJECTED_TRANSACTIONS = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_FACETED_TRANSACTIONS_BY_MERCHANT = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_FRAUDULENT_TRANSACTIONS_BY_CCNO = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";

	private static final String GET_ALL_FRAUDULENT_TRANSACTIONS_IN_LAST_PERIOD = "select * from " + rtfapTransactionTable     // SA
			+ " where solr_query = ?";


	//private PreparedStatement getAllTransactionsByCCnoAndDates;       // SA
	private PreparedStatement getAllTransactions;                       // SA - CQL query
	private PreparedStatement getDailyTransactionsByMerchant;           // SA - CQL query
	private PreparedStatement getYearlyTransactionsByccNo;              // SA - CQL query

	private PreparedStatement getAllRejectedTransactions;               // SA - Solr query
	private PreparedStatement getFacetedTransactionsByMerchant;         // SA - Solr query
	private PreparedStatement getAllFraudulentTransactionsByCCno;       // SA - Solr query
	private PreparedStatement getAllFraudulentTransactionsInLastPeriod; // SA - Solr query

	private AtomicLong count = new AtomicLong(0);

	public TransactionDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();
        // generate a prepared statement based on the contents of string e.g. GET_ALL_FRAUDULENT_TRANSACTIONS_BY_CCNO
		try {


//			this.getAllTransactionsByCCnoAndDates = session.prepare(GET_ALL_TRANSACTIONS_BY_CCNO_AND_DATES);
			this.getAllTransactions = session.prepare(GET_ALL_TRANSACTIONS);    // SA
			this.getDailyTransactionsByMerchant = session.prepare(GET_DAILY_TRANSACTIONS_BY_MERCHANT);    // SA
			this.getYearlyTransactionsByccNo = session.prepare(GET_YEARLY_TRANSACTIONS_BY_CCNO);    // SA
			this.getAllRejectedTransactions = session.prepare(GET_ALL_REJECTED_TRANSACTIONS);    // SA
			this.getFacetedTransactionsByMerchant = session.prepare(GET_FACETED_TRANSACTIONS_BY_MERCHANT);    // SA
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

	public List<Transaction> getAllTransactions() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		ResultSet resultSet = this.session.execute(getAllTransactions.bind());
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getDailyTransactionsByMerchant(String merchant, int day) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		ResultSet resultSet = this.session.execute(getDailyTransactionsByMerchant.bind(merchant, day));
//		ResultSet resultSet = this.session.execute(getDailyTransactionsByMerchant.bind("GAP", 20160309));
		return processTransactionResultSet(resultSet);
	}

	public List<Aggregate> getYearlyTransactionsByccNo(String ccNo, int year) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For cql, specify individual bind variable(s)(or nothing if one isn't required)
		ResultSet resultSet = this.session.execute(getYearlyTransactionsByccNo.bind(ccNo, year));
		return processAggregateResultSet(resultSet);
	}
	public List<Transaction> getAllRejectedTransactions() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"status:Rejected\"}";
		ResultSet resultSet = this.session.execute(getAllRejectedTransactions.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}
	public String getFacetedTransactionsByMerchant() {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"*:*\", \"facet\":{\"field\":\"merchant\"}}";
		ResultSet resultSet = this.session.execute(getFacetedTransactionsByMerchant.bind(solrBindString));
		return processFacetResultSet(resultSet);
	}

	public List<Transaction> getAllFraudulentTransactionsByCCno(String ccNo) {                    // SA
        // execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of e.g. ccNo
		String solrBindString = "{\"q\":\"cc_no: " + ccNo + "\", \"fq\":[\"tags:Fraudulent\"]}";
		ResultSet resultSet = this.session.execute(getAllFraudulentTransactionsByCCno.bind(solrBindString));
		return processTransactionResultSet(resultSet);
	}

	public List<Transaction> getAllFraudulentTransactionsInLastPeriod(String lastPeriod) {                    // SA
		// execute the prepared statement using the supplied bind variable(s)
		// For Solr queries provide the entire WHERE clause as the bind string, not just the value of ccNo
		String solrBindString = "{\"q\":\"*:*\", \"fq\":[\"txn_time:[NOW-1" + lastPeriod + " TO *]\", \"tags:Fraudulent\"]}";
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
		a.setTotal_count(row.getDouble("total_count"));

		return a;
	}
}
