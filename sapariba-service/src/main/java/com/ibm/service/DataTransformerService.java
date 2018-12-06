package com.ibm.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.bean.LineItemDTO;
import com.ibm.bean.RequisitionDTO;
import com.ibm.constants.AribaConstants;
import com.ibm.exception.ServiceException;
import com.ibm.utils.ServiceUtils;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

@Service
public class DataTransformerService {

	Logger logger = LoggerFactory.getLogger(DataTransformerService.class);
	
	public static final String SOAP_1_1_PROTOCOL_LOCAL = "SOAP 1.1 Protocol";
	
	
	private StringBuffer sendData = null;
	
	private StringBuffer recievedData = null;
	
	private Map<String,Object> elementValueMap= null;
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * @param requisitionDTO
	 */
	public void postSoapRequest(EurekaClient instanceInfo, RequisitionDTO requisitionDTO) {
		String soapEndpointUrl = null;
		String soapAction =null;

		//initialize
		soapEndpointUrl = ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_ENDPOINT);
		soapAction =ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_ACTION);

		callSoapWebService(instanceInfo, soapEndpointUrl, soapAction,requisitionDTO);
		
	}
	
	/**
	 * @param soapEndpointUrl
	 * @param soapAction
	 */
	private void callSoapWebService(EurekaClient instanceInfo, String soapEndpointUrl, String soapAction,RequisitionDTO requisitionDTO) {
		ByteArrayOutputStream out = null;
		SOAPConnectionFactory soapConnectionFactory = null;
		SOAPConnection soapConnection = null;
		SOAPMessage soapResponse = null;
		try {

			// Create SOAP Connection
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			// Send SOAP Message to SOAP Server
			soapResponse = soapConnection.call(createSOAPRequest(instanceInfo,soapAction,requisitionDTO),soapEndpointUrl);

			// Print the SOAP Response
			out = new ByteArrayOutputStream();

			soapResponse.writeTo(out);
			recievedData = new StringBuffer().append(new String(out.toByteArray()));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!");
		} finally {

			try {
				if (soapConnection != null)
					soapConnection.close();
				if (out != null)
					out.close();
			} catch (SOAPException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		return;
	}

	

	/**
	 * @param soapAction
	 * @return
	 * @throws Exception
	 */
	private SOAPMessage createSOAPRequest(EurekaClient instanceInfo, String soapAction,RequisitionDTO requisitionDTO) throws Exception {

		ByteArrayOutputStream out = null;
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAP_1_1_PROTOCOL_LOCAL);

		SOAPMessage soapMessage = messageFactory.createMessage();

		createSoapEnvelope(instanceInfo, soapMessage,requisitionDTO);

		@SuppressWarnings("restriction")
		String authorization = new sun.misc.BASE64Encoder().encode((ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_CREDENTIAL)).getBytes());

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader(AribaConstants.Authorization, AribaConstants.Basic + authorization);
		headers.addHeader(AribaConstants.SOAPAction, soapAction);

		soapMessage.saveChanges();

		out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);
		sendData = new StringBuffer().append(out.toString());

		logger.info(sendData.toString());
		
		return soapMessage;
	}
	
	/**
	 * @param soapMessage
	 * @throws SOAPException
	 * @throws ServiceException
	 */
	private void createSoapEnvelope(EurekaClient instanceInfo, SOAPMessage soapMessage , RequisitionDTO requisitionDTO)
			throws SOAPException, ServiceException {
		SOAPPart soapPart = soapMessage.getSOAPPart();
		String myNamespace = ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_NS);
		String myNamespaceURI = ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_NS_URI);

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

		SOAPHeader header = envelope.getHeader();

		SOAPElement soapHeaderElem = header.addChildElement("Headers",myNamespace);

		SOAPElement soapHeaderElem1 = soapHeaderElem.addChildElement("variant",	myNamespace);
		soapHeaderElem1.addTextNode(ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_VARIANT));

		soapHeaderElem1 = soapHeaderElem.addChildElement("partition",myNamespace);
		soapHeaderElem1.addTextNode(ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_PARTITION));

		// create SOAP Body
		createSoapBody(instanceInfo, myNamespace, envelope,requisitionDTO);
	}
	
	/**
	 * @param myNamespace
	 * @param envelope
	 * @throws SOAPException
	 * @throws GDException 
	 */
	private void createSoapBody(EurekaClient instanceInfo, String myNamespace, SOAPEnvelope envelope,RequisitionDTO requisitionDTO)
			throws SOAPException, ServiceException {
		SOAPBody soapBody = envelope.getBody();

		//Populate data object

		SOAPElement pullReq = soapBody.addChildElement("RequisitionImportPullRequest", myNamespace);
		QName variant = new QName("variant");
		pullReq.addAttribute(variant, ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_VARIANT));

		QName partition = new QName("partition");
		pullReq.addAttribute(partition, ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_VARIANT));

		SOAPElement reqPullItem = pullReq.addChildElement("Requisition_RequisitionImportPull_Item", myNamespace);
		SOAPElement reqPull = reqPullItem.addChildElement("item", myNamespace);

		// company code
		SOAPElement companyCode = reqPull.addChildElement("CompanyCode",myNamespace);
		SOAPElement companyCodeUniqueName = companyCode.addChildElement("UniqueName", myNamespace);
		setValue(companyCodeUniqueName, requisitionDTO.getCo_cd());

		// default line item
		SOAPElement defaultLineItem = reqPull.addChildElement("DefaultLineItem", myNamespace);
		SOAPElement deliverTo = defaultLineItem.addChildElement("DeliverTo",myNamespace);
		setValue(deliverTo, requisitionDTO.getRequesterName());

		
		SOAPElement importedHeaderCommentStaging = reqPull.addChildElement("ImportedHeaderCommentStaging", myNamespace);
		setValue(importedHeaderCommentStaging, requisitionDTO.getCommentToSupplier());

		SOAPElement importedHeaderExternalCommentStaging = reqPull.addChildElement("ImportedHeaderExternalCommentStaging",myNamespace);
		setValue(importedHeaderExternalCommentStaging, "true");

		// line items
		SOAPElement lineItems = reqPull.addChildElement("LineItems",myNamespace);
		
		// line item
		for(LineItemDTO lineItemDTO : requisitionDTO.getLineItemDTOs()) 
		{
			SOAPElement item = lineItems.addChildElement("item", myNamespace);

			SOAPElement description = item.addChildElement("Description",myNamespace);

			// the UNSPSC code is not required as Ariba will compute the mapping
			// based on the information uploaded udring ERP commodity import
			// from ERP systems
			SOAPElement commonCommodityCode = description.addChildElement("CommonCommodityCode", myNamespace);

			SOAPElement domain = commonCommodityCode.addChildElement("Domain",myNamespace);
			setValue(domain, "unspsc");

			SOAPElement commonCommUniqueName = commonCommodityCode.addChildElement("UniqueName", myNamespace);
			setValue(commonCommUniqueName, lineItemDTO.getUnspsc_cd());

			SOAPElement des = description.addChildElement("Description", myNamespace);
			setValue(des, lineItemDTO.getLineitemDescription());

			SOAPElement price = description.addChildElement("Price", myNamespace);
			SOAPElement amount = price.addChildElement("Amount", myNamespace);
			setValue(amount, lineItemDTO.getLineItemAmount());

			SOAPElement currency = price.addChildElement("Currency", myNamespace);
			SOAPElement currencyUniqueName = currency.addChildElement("UniqueName",myNamespace);
			setValue(currencyUniqueName, lineItemDTO.getLineItemCurrCd());

			// add SupplierPartNumber
			SOAPElement supplierPartNumber =  description.addChildElement("SupplierPartNumber", myNamespace);
			setValue(supplierPartNumber, lineItemDTO.getSupplierpartNumber());

			SOAPElement unitOfMeasure = description.addChildElement("UnitOfMeasure", myNamespace);
			SOAPElement unitOfMeasureUniqueName = unitOfMeasure.addChildElement("UniqueName", myNamespace);
			setValue(unitOfMeasureUniqueName, lineItemDTO.getLineItemUom());
			
			/*If only Cost center is sent Account category/assignment is K.
			  if WBS element is sent account type is P
			  If both sent WBS Element takes precedence over Cost center*/
			
			
			// if the line item has work break down structure element or cost centre then consider the
			// accounting type before sending it to Ariba
			if(!ServiceUtils.isNullOrEmpty(lineItemDTO.getWbsElement()) || !ServiceUtils.isNullOrEmpty(lineItemDTO.getCostCentre())){
				String accountValue = AribaConstants.P;
				boolean isWBS = true;
				
				SOAPElement importedAccountCategoryStaging = item.addChildElement("ImportedAccountCategoryStaging", myNamespace);
				SOAPElement importedACUniqueName = importedAccountCategoryStaging.addChildElement("UniqueName", myNamespace);
				
				if(ServiceUtils.isNullOrEmpty(lineItemDTO.getWbsElement())) {
					accountValue = AribaConstants.K;
					isWBS = false;
				}
				
				setValue(importedACUniqueName, accountValue);
				
				/*If only Cost center is sent Account type is Expense
				  if WBS element is sent account type is ProjectExpense
				  If both sent WBS Element takes precedence over Cost center*/
				SOAPElement importedAccountTypeStaging = item.addChildElement("ImportedAccountTypeStaging", myNamespace);
				String acctype = AribaConstants.PROJECTEXP;
				if(!isWBS) {
					acctype = AribaConstants.EXPENSE;
				}
				setValue(importedAccountTypeStaging, acctype.trim());
				
				SOAPElement importedAccountingsStaging = item.addChildElement("ImportedAccountingsStaging", myNamespace);
				SOAPElement splitAccountings = importedAccountingsStaging.addChildElement("SplitAccountings", myNamespace);

				SOAPElement splitAccountingItem = splitAccountings.addChildElement("item", myNamespace);
				
				/*If both Cost center and WBS element then Cost center should be ignored
				 * else if only Cost center is sent then it should be populated here.*/

				String ccString = lineItemDTO.getWbsElement();
				if(!isWBS) {
					ccString = lineItemDTO.getCostCentre();
				}
				SOAPElement costCenter = splitAccountingItem.addChildElement("CostCenter", myNamespace);
				setValue(costCenter, ccString);
			}
		
			
			SOAPElement importedAccountingsStaging = item.addChildElement("ImportedAccountingsStaging", myNamespace);
			SOAPElement splitAccountings = importedAccountingsStaging.addChildElement("SplitAccountings", myNamespace);

			SOAPElement splitAccountingItem = splitAccountings.addChildElement("item", myNamespace);


			SOAPElement numberInCollectionAcc = splitAccountingItem.addChildElement("NumberInCollection", myNamespace);
			setValue(numberInCollectionAcc, lineItemDTO.getLineItemSeqNo());

			// with the split accounting
			SOAPElement percentage = splitAccountingItem.addChildElement("Percentage", myNamespace);
			setValue(percentage, lineItemDTO.getSplitPercentage());

			SOAPElement QuantityAcc = splitAccountingItem.addChildElement("Quantity", myNamespace);
			setValue(QuantityAcc, lineItemDTO.getLineItemQty());

			// add importedDeliverToStaging
			item.addChildElement("ImportedDeliverToStaging", myNamespace);

			// add importedDeliverToStaging
			SOAPElement importedLineCommentStaging = item.addChildElement("ImportedLineCommentStaging", myNamespace);
			setValue(importedLineCommentStaging, !ServiceUtils.isNullOrEmpty(lineItemDTO.getLineItemComment()) ? lineItemDTO.getLineItemComment() : "");

			SOAPElement importedLineExternalCommentStaging = item.addChildElement("ImportedLineExternalCommentStaging", myNamespace);
			setValue(importedLineExternalCommentStaging, "false");

			SOAPElement importedNeedByStaging = item.addChildElement("ImportedNeedByStaging", myNamespace);
			setValue(importedNeedByStaging, lineItemDTO.getLineitemNeedByDate());

			SOAPElement itemCategory = item.addChildElement("ItemCategory",myNamespace);
			SOAPElement itemCategoryUniqueName = itemCategory.addChildElement("UniqueName", myNamespace);
			setValue(itemCategoryUniqueName, ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_ITEM_CATEGORY_M));

			SOAPElement numberInCollection = item.addChildElement("NumberInCollection", myNamespace);
			setValue(numberInCollection, lineItemDTO.getLineItemSeqNo());

			SOAPElement originatingSystemLineNumber = item.addChildElement("OriginatingSystemLineNumber", myNamespace);
			setValue(originatingSystemLineNumber, lineItemDTO.getOriginatingSystemLineItemNumber());

			SOAPElement quantity = item.addChildElement("Quantity", myNamespace);
			setValue(quantity, lineItemDTO.getLineItemQty());
			
			//fetch VendorId against supplierId
			String vendorId = "";
			vendorId = fetchVendorId(instanceInfo, lineItemDTO.getLineitemSupplierId().trim());
			SOAPElement supplier = item.addChildElement("Supplier", myNamespace);
			SOAPElement supplierUniqueName = supplier.addChildElement("UniqueName",myNamespace);
			setValue(supplierUniqueName, vendorId);

			//custom
			SOAPElement custom = item.addChildElement("custom",myNamespace);

			//contract
			SOAPElement customString  = custom.addChildElement("CustomString", myNamespace);
			QName contractName = new QName("name");
			customString.addAttribute(contractName, "CONTRACTNUMBER");
			setValue(customString, !ServiceUtils.isNullOrEmpty(lineItemDTO.getLineitemContractNo())?lineItemDTO.getLineitemContractNo():"");

			SOAPElement bpCustomString  = custom.addChildElement("CustomString", myNamespace);
			QName byPassString = new QName("name");
			bpCustomString.addAttribute(byPassString, "ByPass");
			setValue(bpCustomString, !ServiceUtils.isNullOrEmpty(lineItemDTO.getLineitemByPassFlag())?lineItemDTO.getLineitemByPassFlag():"");

			SOAPElement scCustomString  = custom.addChildElement("CustomString", myNamespace);
			QName sourceCodeString = new QName("name");
			scCustomString.addAttribute(sourceCodeString, "SOURCECODE");
			setValue(scCustomString, !ServiceUtils.isNullOrEmpty(lineItemDTO.getLineitemSourceCode())?lineItemDTO.getLineitemSourceCode():"" );

			//Value Order
			SOAPElement voCustomString  = custom.addChildElement("CustomString", myNamespace);
			QName valueOrderString = new QName("name");
			voCustomString.addAttribute(valueOrderString, "IsValueOrder");
			setValue(voCustomString, lineItemDTO.getValueOrder());
			
			SOAPElement sdCustomeDate  = custom.addChildElement("CustomString", myNamespace);
			QName startDateMaterial = new QName("name");
			sdCustomeDate.addAttribute(startDateMaterial, "StartDateMaterial");
			setValue(sdCustomeDate, lineItemDTO.getStartDate());

			SOAPElement edCustomeDate  = custom.addChildElement("CustomString", myNamespace);
			QName endDateMaterial = new QName("name");
			edCustomeDate.addAttribute(endDateMaterial, "EndDateMaterial");
			setValue(edCustomeDate, lineItemDTO.getEndDate());
			
		}

		SOAPElement name = reqPull.addChildElement("Name", myNamespace);
		setValue(name, requisitionDTO.getRequisitionName());

		SOAPElement orgSystem = reqPull.addChildElement("OriginatingSystem",myNamespace);
		setValue(orgSystem, requisitionDTO.getApplicationType());
		

		// add OriginatingSystemReferenceID
		SOAPElement originatingSystemReferenceID = reqPull.addChildElement("OriginatingSystemReferenceID", myNamespace);
		setValue(originatingSystemReferenceID,requisitionDTO.getApplicationTransactionNumber());

		SOAPElement preparer = reqPull.addChildElement("Preparer", myNamespace);
		SOAPElement preparerPwd = preparer.addChildElement("PasswordAdapter",myNamespace);
		setValue(preparerPwd, ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_USER_ADAPTER));

		SOAPElement preparerUid = preparer.addChildElement("UniqueName",myNamespace);
		setValue(preparerUid, requisitionDTO.getPreparerWebId());

		SOAPElement requester = reqPull.addChildElement("Requester",myNamespace);
		SOAPElement requesterPwd = requester.addChildElement("PasswordAdapter",myNamespace);
		setValue(requesterPwd, ServiceUtils.getItemsForSoapConnection(AribaConstants.WS_USER_ADAPTER));

		SOAPElement requesterUid = requester.addChildElement("UniqueName",myNamespace);
		setValue(requesterUid, requisitionDTO.getRequesterWebId());

		SOAPElement uniqueName = reqPull.addChildElement("UniqueName",myNamespace);
		setValue(uniqueName, requisitionDTO.getApplicationTransactionNumber());

	}
	
	
	/**
	 * @param param
	 * @param value
	 * @throws SOAPException
	 */
	private void setValue(SOAPElement param, String value) throws SOAPException {
		param.addTextNode(value);
	}
	
	/**
	 * @return the sendData
	 */
	public StringBuffer getSendData() {

		if (sendData == null)
			sendData = new StringBuffer();

		return sendData;
	}
	
	public void setRecievedData(String value) {
		recievedData =  new StringBuffer().append(value);
	}
	/**
	 * @return
	 */
	public StringBuffer getRecievedData() {

		if (recievedData == null)
			recievedData = new StringBuffer();

		return recievedData;
	}
	
	/**
	 * This method validates the response XMl obtained from the requisition
	 * system and parses to get the cart number and respective data required by
	 * the upstream application.Please note no parsing will ahppen if the
	 * response XMl fails the validation with the defined schema
	 * 
	 * @param responseXml
	 * @throws GDException 
	 */
	public void readResponse(String responseXml)  {
		Document xmlDoc = null;
		try {
		
			// prepare the key value pair by parsing the xml
			xmlDoc = ServiceUtils.convertStringToDocument(responseXml);
			
			if(elementValueMap== null)
				elementValueMap = new HashMap<String, Object>();
			
			parseXml(xmlDoc,xmlDoc.getDocumentElement());
		
		
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
		} catch (SecurityException e) {
			logger.error(e.getMessage());
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		} 
		
		
	}
	
	/**
	 * This method parses the xml document to the last child level and get the
	 * values based on the xsd defined elements
	 * @param doc
	 * @param dto
	 * @param e
	 */
	private void parseXml(Document doc, final Element e) {
		
		
		NodeList children = null;
		Node childNode = null;
		children = e.getChildNodes();
		String elemVal = null;

		for (int i = 0; i < children.getLength(); i++) {
			childNode = children.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {

				// get the element value
				elemVal = childNode.getTextContent();
				elementValueMap.put(childNode.getNodeName(), elemVal);

				parseXml(doc, (Element) childNode);
			}
		}
		
	}

	/**
	 * @return the elementValueMap
	 */
	public Map<String, Object> getElementValueMap() {
		return elementValueMap;
	}
	
	/**
	 * @param  requisitionDTO
	 * @param  instanceInfo
	 * @return
	 */
	public String fetchVendorId(EurekaClient eurekaClient,String locationId) {
		String url = null;	
		String jsonString = null;
		String vendorId = null;
		Application backenedApplication = null;
		InstanceInfo instanceInfo = null;
		
		logger.info("Looking up for supplier  ID: " +locationId );
		
		try{

			backenedApplication = eurekaClient.getApplication("backend-service");
			instanceInfo = backenedApplication.getInstances().get(0);
			
			url= "http://" + instanceInfo.getIPAddr() + ":"+ instanceInfo.getPort() + "/" + "/getSuppPartneringInfo/";
			logger.info("Invoking url"+url+" resttemplate"+ restTemplate);
			jsonString = restTemplate.postForObject(url, locationId, String.class);
			
			logger.info("Vendor Id fetched "+jsonString);
			
			JSONObject jsonObj = new JSONObject(jsonString);
			if(jsonObj.has(locationId)) {
				vendorId = jsonObj.get(locationId).toString();
				
			}else {
				vendorId = locationId;
			}
		
		logger.info("vendorId for  supplier id: " + vendorId);
		
		}catch(Exception e) {
			logger.error("Error occurred while fetching the vendor id" );
			e.printStackTrace();
		}
		
		return vendorId;	
	}
	
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}
