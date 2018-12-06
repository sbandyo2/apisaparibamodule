package com.ibm.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ibm.bean.RequisitionDTO;
import com.ibm.bean.VOWrapperDTO;
import com.ibm.constants.AribaConstants;
import com.ibm.model.MonitorVO;
import com.ibm.service.DataTransformerService;
import com.ibm.utils.ServiceUtils;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

@RestController
public class AribaServiceController {
	Logger logger = LoggerFactory.getLogger(AribaServiceController.class);

	public static final String SOAP_1_1_PROTOCOL_LOCAL = "SOAP 1.1 Protocol";
	public static String RESPONSE_UNIQUE_NAME = "UniqueName";
	public static String RESPONSE_FAULT_CODE = "faultcode";
	public static String RESPONSE_FAULT_STRING = "faultstring";
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EurekaClient eurekaClient;
	

	@RequestMapping(value = "/ariba", method = RequestMethod.POST)
    public String submitToAriba(@RequestBody RequisitionDTO requisitionDTO) {
		
		logger.info("Starting ariba transaction ");
		
		InstanceInfo instanceInfo = null;
		DataTransformerService dataTransformerService = null;
		String url = null;
		MonitorVO monitorVO = null;
		String requestFileID = null;
		String responseFileID = null;
		StringBuffer response = null;
		Application backenedApplication  = null;
		
		response = new StringBuffer();
		backenedApplication = eurekaClient.getApplication("backend-service");
		instanceInfo = backenedApplication.getInstances().get(0);
		
		// post the soap request
		dataTransformerService = new DataTransformerService();
		dataTransformerService.postSoapRequest(instanceInfo,requisitionDTO);
		
		logger.info("Ariba submission completed for transaction  "+requisitionDTO.getApplicationTransactionNumber());
				
		//insert the request Xml  as attachment
		requestFileID = saveAttachements(requisitionDTO, instanceInfo,AribaConstants.SENDDATA,dataTransformerService);
		
		//insert response Xml as attachment
		backenedApplication = eurekaClient.getApplication("backend-service");
		instanceInfo = backenedApplication.getInstances().get(0);
		logger.info("In Controller Response XML"+dataTransformerService.getRecievedData());
		responseFileID = saveAttachements(requisitionDTO, instanceInfo,AribaConstants.RECIEVED,dataTransformerService);
		
		
		//save the transaction data in the monitor data store
		url= "http://" + instanceInfo.getIPAddr() + ":"+ instanceInfo.getPort() + "/" + "/dbinsert/";
		
		// prepare VO for data save
		monitorVO = prepareMonitorDTO(requisitionDTO,  requestFileID, responseFileID,dataTransformerService);
			
		restTemplate.postForObject(url, monitorVO, String.class);
		
		logger.info("DB transaction completed for transaction  "+requisitionDTO.getApplicationTransactionNumber());
	
		//generate the error message
		if(AribaConstants.SUCCESS.equalsIgnoreCase(monitorVO.getStatus())){
			response.append("<returnData><cartID>"+monitorVO.getCartID()+"</cartID></returnData>");
		}else {
			response.append("<returnData><error>"+monitorVO.getErrorMessage()+"</error></returnData>");
		}
		
		
		logger.info("Finishing ariba transaction ");
		
        return response.toString();
    }



	/**
	 * @param requisitionDTO
	 * @param requestFileID
	 * @param responseFileID
	 * @return
	 */
	private MonitorVO prepareMonitorDTO(RequisitionDTO requisitionDTO,String requestFileID, String responseFileID,DataTransformerService dataTransformerService) {
		
		MonitorVO monitorVO = null;
		Object cartNumber = null;
		Object faultCode = null;
		Object faultDesc = null;
		Map<String,Object> elementValueMap= null;
		
		
		monitorVO = new MonitorVO();
		monitorVO.setApplicationTransactionNumber(requisitionDTO.getApplicationTransactionNumber());
		monitorVO.setApplicationType(requisitionDTO.getApplicationType());
		monitorVO.setCreatedTs(ServiceUtils.getCurrentDate());
		monitorVO.setRecievedDataID(requisitionDTO.getApplicationType()+"_"+requisitionDTO.getApplicationTransactionNumber()+"_"+AribaConstants.RECIEVED);
		monitorVO.setRequestXmlID(requestFileID);
		monitorVO.setResponseXmlID(responseFileID);
		
		dataTransformerService.readResponse(dataTransformerService.getRecievedData().toString());
		elementValueMap = dataTransformerService.getElementValueMap();
		if (elementValueMap != null && !elementValueMap.isEmpty()) {
			cartNumber =  elementValueMap.get(RESPONSE_UNIQUE_NAME);
			faultCode = elementValueMap.get(RESPONSE_FAULT_CODE);
			faultDesc = elementValueMap.get(RESPONSE_FAULT_STRING);
		}
		
		if(cartNumber!=null){
			
			monitorVO.setCartID(String.valueOf(cartNumber));
			monitorVO.setErrorMessage(AribaConstants.EMPTY);
			monitorVO.setStatus(AribaConstants.SUCCESS);
		}else {
			monitorVO.setCartID(AribaConstants.EMPTY);
			if(ServiceUtils.isNullOrEmpty(String.valueOf(faultCode)))
				faultCode ="Error Occurred";
			if(ServiceUtils.isNullOrEmpty(String.valueOf(faultDesc)))
				faultDesc ="Refer to recieved data for detailed error message";
			monitorVO.setErrorMessage(String.valueOf(faultCode)+AribaConstants.SEPARATOR_Underscore+String.valueOf(faultDesc));
			monitorVO.setStatus(AribaConstants.ERROR);
		}
		return monitorVO;
	}

	/**
	 * @param requisitionDTO
	 * @param instanceInfo
	 * @return
	 */
	private String saveAttachements(RequisitionDTO requisitionDTO,InstanceInfo instanceInfo,String fNameSuffix,DataTransformerService dataTransformerService) {
		String url = null;
		String fileName = null;
		VOWrapperDTO voWrapperDTO = null;
		
		
		voWrapperDTO = new VOWrapperDTO();
		url= "http://" + instanceInfo.getIPAddr() + ":"+ instanceInfo.getPort() + "/" + "/dbattachinsert/";
		
		if(fNameSuffix.equalsIgnoreCase(AribaConstants.SENDDATA))
			voWrapperDTO.setRequestXml(dataTransformerService.getSendData());
		else if (fNameSuffix.equalsIgnoreCase(AribaConstants.RECIEVED))
			voWrapperDTO.setResponseXml(dataTransformerService.getRecievedData());
		
		fileName = requisitionDTO.getApplicationTransactionNumber()+"_"+fNameSuffix;
		voWrapperDTO.setFileName(fileName);
		voWrapperDTO.setFileType(AribaConstants.XML);
		
		restTemplate.postForObject(url, voWrapperDTO, String.class);
		return fileName;
	}

	

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	
}


