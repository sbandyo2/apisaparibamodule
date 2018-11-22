package com.ibm.bean;

import java.util.List;

import com.ibm.model.MonitorVO;

public class VOWrapperDTO {

	private List<MonitorVO> monitorVOs = null;

	private String fileName = null;

	private StringBuffer requestXml = null;

	private StringBuffer responseXml = null;

	/**
	 * @return the monitorVOs
	 */
	public List<MonitorVO> getMonitorVOs() {
		return monitorVOs;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the requestXml
	 */
	public StringBuffer getRequestXml() {
		return requestXml;
	}

	/**
	 * @param requestXml
	 *            the requestXml to set
	 */
	public void setRequestXml(StringBuffer requestXml) {
		this.requestXml = requestXml;
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
	 * @param monitorVOs
	 *            the monitorVOs to set
	 */
	public void setMonitorVOs(List<MonitorVO> monitorVOs) {
		this.monitorVOs = monitorVOs;
	}

}
