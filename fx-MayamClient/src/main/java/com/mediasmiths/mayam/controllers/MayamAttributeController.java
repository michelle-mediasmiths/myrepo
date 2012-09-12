package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.attributes.shared.BasicAttributeValidator;
import com.mayam.wf.ws.client.TasksClient;

public class MayamAttributeController {
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
	
	public boolean setAttribute(Attribute attribute, Object value) {
		boolean isValid = validator.isValidValue(attribute, value);
		if (isValid) {
			attributes.setAttributeFromString(attribute, value.toString());
		}
		return isValid;
	}
	
	public AttributeMap getAttributes() {
		return attributes;
	}

}
