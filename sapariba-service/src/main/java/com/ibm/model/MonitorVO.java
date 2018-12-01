package com.ibm.model;

public class MonitorVO extends BaseVO {

	private Long transactionID;
	private String applicationTransactionNumber;
	private String applicationType;
	private String cartID;
	private String createdTs;
	private String status;
	private String errorMessage;
	private String responseXmlID;
	private String requestXmlID;
	private String recievedDataID;
	
	
	/**
	 * @return the recievedDataID
	 */
	public String getRecievedDataID() {
		return recievedDataID;
	}
	/**
	 * @param recievedDataID the recievedDataID to set
	 */
	public void setRecievedDataID(String recievedDataID) {
		this.recievedDataID = recievedDataID;
	}
	/**
	 * @return the transactionID
	 */
	public Long getTransactionID() {
		return transactionID;
	}
	/**
	 * @param transactionID the transactionID to set
	 */
	public void setTransactionID(Long transactionID) {
		this.transactionID = transactionID;
	}
	/**
	 * @return the applicationTransactionNumber
	 */
	public String getApplicationTransactionNumber() {
		return applicationTransactionNumber;
	}
	/**
	 * @param applicationTransactionNumber the applicationTransactionNumber to set
	 */
	public void setApplicationTransactionNumber(String applicationTransactionNumber) {
		this.applicationTransactionNumber = applicationTransactionNumber;
	}
	/**
	 * @return the applicationType
	 */
	public String getApplicationType() {
		return applicationType;
	}
	/**
	 * @param applicationType the applicationType to set
	 */
	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}
	/**
	 * @return the cartID
	 */
	public String getCartID() {
		return cartID;
	}
	/**
	 * @param cartID the cartID to set
	 */
	public void setCartID(String cartID) {
		this.cartID = cartID;
	}
	/**
	 * @return the createdTs
	 */
	public String getCreatedTs() {
		return createdTs;
	}
	/**
	 * @param createdTs the createdTs to set
	 */
	public void setCreatedTs(String createdTs) {
		this.createdTs = createdTs;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * @return the responseXmlID
	 */
	public String getResponseXmlID() {
		return responseXmlID;
	}
	/**
	 * @param responseXmlID the responseXmlID to set
	 */
	public void setResponseXmlID(String responseXmlID) {
		this.responseXmlID = responseXmlID;
	}
	/**
	 * @return the requestXmlID
	 */
	public String getRequestXmlID() {
		return requestXmlID;
	}
	/**
	 * @param requestXmlID the requestXmlID to set
	 */
	public void setRequestXmlID(String requestXmlID) {
		this.requestXmlID = requestXmlID;
	}
	
}
