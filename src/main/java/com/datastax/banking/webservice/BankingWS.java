package com.datastax.banking.webservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.banking.model.Transaction;
import com.datastax.banking.service.SearchService;
import com.datastax.banking.service.SearchServiceImpl;

@WebService
@Path("/")
public class BankingWS {

	private Logger logger = LoggerFactory.getLogger(BankingWS.class);
	private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd");

	//Service Layer.
	private SearchService service = new SearchServiceImpl();
	
//	@GET
//	@Path("/gettransactions/{creditcardno}/{from}/{to}")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getMovements(@PathParam("creditcardno") String ccNo, @PathParam("from") String fromDate,
//			@PathParam("to") String toDate) {
		
//		DateTime from = DateTime.now();
//		DateTime to = DateTime.now();
//		try {
//			from = new DateTime(inputDateFormat.parse(fromDate));
//			to = new DateTime(inputDateFormat.parse(toDate));
//		} catch (ParseException e) {
//			String error = "Caught exception parsing dates " + fromDate + "-" + toDate;
			
//			logger.error(error);
//			return Response.status(Status.BAD_REQUEST).entity(error).build();
//		}
				
//		List<Transaction> result = service.getTransactionsByTagAndDate(ccNo, null, from, to);
		
//		return Response.status(Status.OK).entity(result).build();
//	}

//	@GET
//	@Path("/getalltransactions/{creditcardno}")    // SA
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getAllLatestTransactions(@PathParam("creditcardno") String ccNo) {
//		List<Transaction> result = service.getAllLatestTransactionsByCC(ccNo);

//		return Response.status(Status.OK).entity(result).build();
//	}

//	@GET
//	@Path("/getallsuccessfultransactions/{creditcardno}/{from}")    // SA
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getAllRtfapTransactions(@PathParam("creditcardno") String ccNo, @PathParam("from") String fromDate) {
//		DateTime from = DateTime.now();

//		try {
//			from = new DateTime(inputDateFormat.parse(fromDate));
//		} catch (ParseException e) {
//			String error = "Caught exception parsing dates " + fromDate;

//			logger.error(error);
//			return Response.status(Status.BAD_REQUEST).entity(error).build();
//		}
//
//      //logger.info(ccNo + " " + from);
//		List<Transaction> result = service.getAllRtfapTransactionsByCC(ccNo, from);
//
//		return Response.status(Status.OK).entity(result).build();
//	}
    @GET
	@Path("/getalltransactions/")    // SA
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTransactions() {
	List<Transaction> result = service.getAllTransactions();

	return Response.status(Status.OK).entity(result).build();
}


	@GET
	@Path("/getallfraudulenttransactions/{creditcardno}")    // SA
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllFraudulentTransactions(@PathParam("creditcardno") String ccNo) {

		logger.info("WebService: " + ccNo);
		List<Transaction> result = service.getAllFraudulentTransactionsByCC(ccNo);

		return Response.status(Status.OK).entity(result).build();
	}

	@GET
	@Path("/getallfraudulenttransactionsinlastperiod/{period}")    // SA
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllFraudulentTransactionsInLastPeriod(@PathParam("period") String lastPeriod) {

		logger.info("WebService: " + lastPeriod);
		List<Transaction> result = service.getAllFraudulentTransactionsInLastPeriod(lastPeriod);

		return Response.status(Status.OK).entity(result).build();
	}

}
