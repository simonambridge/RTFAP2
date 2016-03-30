package com.datastax.banking.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Transaction {

	private String creditCardNo;
	private String userId;
	private Date transactionTime;
	private String transactionId;
	private Map<String, Double> items;
	private String location;
	private String merchant;
	private String ccProvider;    // SA
	private Double amount;
	private String status;	
	private String notes;
	private Set<String> tags;

	public Transaction() {
		super();
	}

	public String getCreditCardNo() {
		return creditCardNo;
	}

	public void setCreditCardNo(String creditCardNo) {
		this.creditCardNo = creditCardNo;
	}

	public Date getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Map<String, Double> getItems() {
		return items;
	}

	public void setItems(Map<String, Double> items) {
		this.items = items;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getccProvider() {
		return ccProvider;
	}                              // SA

	public void setccProvider(String ccProvider) {
		this.ccProvider = ccProvider;
	}    // SA

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getNotes() {
		if(notes==null){
			return "";
		}
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getStatus() {
		
		if(status==null){
			return "";
		}
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isApproved(){
		return this.getStatus().equalsIgnoreCase(Status.CLIENT_APPROVED.toString()) ||
				this.getStatus().equalsIgnoreCase(Status.APPROVED.toString());			
	}
	public boolean isDeclined(){
		return this.getStatus().equalsIgnoreCase(Status.DECLINED.toString()) ||
				this.getStatus().equalsIgnoreCase(Status.CLIENT_DECLINED.toString());
	}
	
	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Transaction [creditCardNo=" + creditCardNo + ", userId=" + userId + ", transactionTime="
				+ transactionTime + ", transactionId=" + transactionId + ", items=" + items + ", location=" + location
				+ ", merchant=" + merchant + ", amount=" + amount + ", status=" + status + ", notes=" + notes
				+ ", tags=" + tags + "]";
	}

	public enum Status {
		CHECK, APPROVED, DECLINED, CLIENT_APPROVED, CLIENT_DECLINED, CLIENT_APPROVAL, TIMEOUT
	}
}
