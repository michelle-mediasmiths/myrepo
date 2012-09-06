package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.Source;

public class MSItem {

	/**
	 * Creates a valid object of type MaterialType 
	 * @param item
	 * @param titleId
	 * @return item
	 * @throws DatatypeConfigurationException
	 */
	public MaterialType validItem (MaterialType item, String titleId) throws DatatypeConfigurationException {
		
		Source source = new Source();
		Aggregation aggregation = new Aggregation();
		aggregation = createAggregation(aggregation);
		//Compile compile = new Compile();
		//Library library = new Library();
		
		//compile.setParentItemID("ABC123");
		
		source.setAggregation(aggregation);;
		//source.setCompile(compile);
		
		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		item.setRequiredBy(xmlCal);
		item.setRequiredFormat("SD");
		item.setSource(source);
		item.setMaterialD("abc123");
		
		return item;
	}
	
	/**
	 * Creates a valid object of type Aggregation to be used as part of an MaterialType
	 * @param aggregation
	 * @return aggregation
	 * @throws DatatypeConfigurationException
	 */
	public Aggregation createAggregation (Aggregation aggregation) throws DatatypeConfigurationException {
		
		Order order = new Order();
		order = createOrder(order);
		Aggregator aggregator = new Aggregator();
		aggregator = createAggregator(aggregator);
		aggregation.setOrder(order);
		aggregation.setAggregator(aggregator);
		
		return aggregation;
	}
	
	/**
	 * Creates a valid object of type Order to be used as part of an aggregation
	 * @param order
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public Order createOrder (Order order) throws DatatypeConfigurationException {
		
		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		order.setOrderCreated(xmlCal);
		order.setOrderReference("ABC123");
		
		return order;
	}
	/**
	 * Creates a valid object of type Aggregator to be used as part of an aggregation
	 * @param aggregator
	 * @return
	 */
	public Aggregator createAggregator (Aggregator aggregator) {
		
		aggregator.setAggregatorID("ABC123");
		aggregator.setAggregatorName("aggregatorName");
		return aggregator;
	}
}
