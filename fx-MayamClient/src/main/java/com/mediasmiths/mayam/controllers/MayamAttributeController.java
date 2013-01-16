package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidationException;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.attributes.shared.BasicAttributeValidator;
import com.mayam.wf.ws.client.TasksClient;

public class MayamAttributeController extends MayamController{
	private final AttributeValidator validator;
	private final TasksClient client;
	private AttributeMap attributes;
	
	// Constructors for creating new Attribute Maps
	public MayamAttributeController(TasksClient mayamClient) {
		validator = new BasicAttributeValidator();
		client = mayamClient;
		attributes = client.createAttributeMap();
	}
	
	public MayamAttributeController(TasksClient mayamClient, AttributeValidator attributeValidator) {
		validator = attributeValidator;
		client = mayamClient;
		attributes = client.createAttributeMap();
	}
	
	// Constructors for updating existing Attribute Maps
	public MayamAttributeController(AttributeMap attributeMap) {
		validator = new BasicAttributeValidator();
		client = null;
		attributes = attributeMap;
	}
	
	public MayamAttributeController(AttributeMap attributeMap, AttributeValidator attributeValidator) {
		validator = attributeValidator;
		client = null;
		attributes = attributeMap;
	}
	
	public void copyAttributes(AttributeMap originalAttributes) {
		attributes.putAll(originalAttributes);
	}
	
	public boolean setAttribute(Attribute attribute, Object value) {
	
		
		boolean isValid = false;
		
		try{
			validator.validate(attribute, value);
			isValid=true;
		}
		catch(AttributeValidationException w){
			log.error(String.format("AttributeValidationException validating value %s for attribute %s", value,attribute.toString(),w));
			isValid=false;
		}
		catch(ClassCastException e){
			log.error(String.format("ClassCastException validating value %s for attribute %s", value,attribute.toString(),e));
			isValid=false;
		}
		
		if (isValid) {
			attributes.setAttribute(attribute, value);
		}

		return isValid;
	}
	
	public AttributeMap getAttributes() {
		return attributes;
	}

	public void copyAttribute(Attribute attribute, AttributeMap source)
	{
		setAttribute(attribute,source.getAttribute(attribute));		
	}

}
