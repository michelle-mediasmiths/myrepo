package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.Source;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods.Relative;
import org.apache.commons.lang.RandomStringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Random;
import java.util.UUID;

public class MSItem {

	protected final String[] formats = new String[]{"SD","HD","3D"};
	
	/**
	 * Creates a valid object of type MaterialType 
	 * @param item
	 * @param titleId
	 * @return item
	 * @throws DatatypeConfigurationException
	 */
	public MaterialType validItem (MaterialType item, String titleId) throws DatatypeConfigurationException {
		
		HelperMethods method = new HelperMethods();		
		XMLGregorianCalendar requiredBy = method.giveValidDate();
		XMLGregorianCalendar orderCreated = method.giveValidDate(Relative.BEFORE,requiredBy.toGregorianCalendar());
		
		
		Source source = new Source();
		Aggregation aggregation = new Aggregation();
		aggregation = createAggregation(aggregation,orderCreated);
		//Compile compile = new Compile();
		//Library library = new Library();
		
		//compile.setParentItemID("ABC123");
		
		source.setAggregation(aggregation);;
		//source.setCompile(compile);
		item.setRequiredBy(requiredBy);
		item.setRequiredFormat(formats[new Random(System.currentTimeMillis()).nextInt(formats.length)]);
		item.setSource(source);
		item.setMaterialID(RandomStringUtils.randomAlphanumeric(6));
		
		return item;
	}
	
	/**
	 * Creates a valid object of type Aggregation to be used as part of an MaterialType
	 * @param aggregation
	 * @param orderCreated 
	 * @return aggregation
	 * @throws DatatypeConfigurationException
	 */
	public Aggregation createAggregation (Aggregation aggregation, XMLGregorianCalendar orderCreated) throws DatatypeConfigurationException {
		
		Order order = new Order();
		order = createOrder(order, orderCreated);
		Aggregator aggregator = new Aggregator();
		aggregator = createAggregator(aggregator);
		aggregation.setOrder(order);
		aggregation.setAggregator(aggregator);
		
		return aggregation;
	}
	
	/**
	 * Creates a valid object of type Order to be used as part of an aggregation
	 * @param order
	 * @param orderCreated 
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public Order createOrder (Order order, XMLGregorianCalendar orderCreated) throws DatatypeConfigurationException {
		
		order.setOrderCreated(orderCreated);
		order.setOrderReference(UUID.randomUUID().toString());
		
		return order;
	}
	/**
	 * Creates a valid object of type Aggregator to be used as part of an aggregation
	 * @param aggregator
	 * @return
	 */
	public Aggregator createAggregator (Aggregator aggregator) {
		
		aggregator.setAggregatorID(RandomStringUtils.randomAlphanumeric(6));
		aggregator.setAggregatorName(RandomStringUtils.randomAlphanumeric(20));
		return aggregator;
	}
	
}
