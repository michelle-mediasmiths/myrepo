package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.attributes.shared.BasicAttributeValidator;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MqClient;

public class MayamAttributeController {
	private final AttributeValidator validator;
	private final TasksClient client;
	private AttributeMap attributes;
	
	public MayamAttributeController(TasksClient mayamClient) {
		validator = new BasicAttributeValidator();
		client = mayamClient;
		attributes = client.createAttributeMap();
	}
	
	public MayamAttributeController(AttributeMap attributeMap) {
		validator = new BasicAttributeValidator();
		client = null;
		attributes = attributeMap;
	}
	
	public boolean setAttribute(Attribute attribute, Object value) {
		boolean isValid = validator.isValidValue(attribute, value);
		if (isValid) {
			attributes.setAttribute(attribute, value);
		}
		return isValid;
	}
	
	public AttributeMap getAttributes() {
		return attributes;
	}

}
