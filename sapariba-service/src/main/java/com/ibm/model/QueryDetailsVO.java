package com.ibm.model;

public class QueryDetailsVO {

	private String param;

	private MonitorVO monitorVO = null;

	private StringBuffer responseXml = null;

	private String cartNumber = null;

	/**
	 * @return the monitorVO
	 */
	public MonitorVO getMonitorVO() {
		return monitorVO;
	}

	/**
	 * @param monitorVO
	 *            the monitorVO to set
	 */
	public void setMonitorVO(MonitorVO monitorVO) {
		this.monitorVO = monitorVO;
	}

	/**
	 * @return the cartNumber
	 */
	public String getCartNumber() {
		return cartNumber;
	}

	/**
	 * @param cartNumber
	 *            the cartNumber to set
	 */
	public void setCartNumber(String cartNumber) {
		this.cartNumber = cartNumber;
	}

	/**
	 * @return the responseXml
	 */
	public StringBuffer getResponseXml() {
		return responseXml;
	}

	/**
	 * @param responseXml
	 *            the responseXml to set
	 */
	public void setResponseXml(StringBuffer responseXml) {
		this.responseXml = responseXml;
	}

	/**
	 * @return the param
	 */
	public String getParam() {
		return param;
	}

	/**
	 * @param param
	 *            the param to set
	 */
	public void setParam(String param) {
		this.param = param;
	}

}
