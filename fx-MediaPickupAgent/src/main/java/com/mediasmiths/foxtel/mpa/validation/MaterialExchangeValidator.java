package com.mediasmiths.foxtel.mpa.validation;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.mpa.Util;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.List;

public class MaterialExchangeValidator extends MessageValidator<Material>
{

	private final MayamClient mayamClient;

	@Inject
	@Named("watchfolder.locations")
	private WatchFolders watchFolders;

	public void setWatchFolders(WatchFolders watchFolders)
	{
		this.watchFolders = watchFolders;
	}

	@Inject
	@Named("ao.aggregator.name")
	private String aoAggregatorName;

	public void setAoAggregatorName(String aoAggregatorName)
	{
		this.aoAggregatorName = aoAggregatorName;
	}

	@Inject
	public MaterialExchangeValidator(
			Unmarshaller unmarshaller,
			MayamClient mayamClient,
			ReceiptWriter receiptWriter,
			SchemaValidator schemaValidator) throws SAXException
	{
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient = mayamClient;
	}

	private static Logger logger = Logger.getLogger(MaterialExchangeValidator.class);

	@Override
	protected MessageValidationResult validateMessage(PickupPackage pp, Material message)
	{
		logger.trace("validateMessage enter");

		boolean folderIsAO = watchFolders.isAo(pp.getRootPath());
		boolean materialIsAO = isMaterialAO(message);
		boolean isAggregatorAO = isAggregatorAO(message);
		boolean withMedia = pp.isComplete();
		logger.debug("Is this AO folder : " + folderIsAO);
		logger.debug("Is the AO material :  " + materialIsAO);
		logger.debug("Is this AO aggregator : " + isAggregatorAO);
		if((folderIsAO != materialIsAO) || (folderIsAO != isAggregatorAO)){
				return MessageValidationResult.AO_MISMATCH; 
		}
		
		return validateTitle(message.getTitle(), message.getDetails(), folderIsAO,withMedia);
	}

		private boolean isAggregatorAO(Material message)
		{
			List<String> locationNames = Arrays.asList(aoAggregatorName.split(","));
			boolean isAO = false;
			for (String location: locationNames)
			{
				logger.debug("Printing the location  :"  + location);
				logger.debug("The aggregator ID from the XML  : " + message.getDetails().getSupplier().getSupplierID());

				isAO = location.equalsIgnoreCase(message.getDetails().getSupplier().getSupplierID());

				if (isAO == true)
				{
					logger.debug("Aggregator is AO " + isAO);
					break;
				}
				else
				{
					continue;
				}

				//isAO = location.toLowerCase().equals(message.getDetails().getSupplier().getSupplierID().toLowerCase());
			}
			return isAO;
		}

	private boolean isMaterialAO(Material message)
	{
		MaterialType material;
		if (Util.isProgramme(message))
		{
			material = message.getTitle().getProgrammeMaterial();
		}
		else
		{
			material = message.getTitle().getMarketingMaterial();
		}

		return material.isAdultMaterial();

	}

	private MessageValidationResult validateTitle(Title title, Details details, boolean expectAOPlaceholder, boolean withMedia)
	{

		final String titleID = title.getTitleID();
		// reject empty titleIDs
		if (StringUtils.isEmpty(titleID))
		{
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}

		if (Util.isProgramme(title))
		{

			// title only has to exist already if we are receiving programme
			// material (not marketing)
			try
			{
				if (!mayamClient.titleExists(title.getTitleID()))
				{
					return MessageValidationResult.TITLE_DOES_NOT_EXIST;
				}
			}
			catch (MayamClientException e)
			{
				logger.error("Mayam client error", e);
				return MessageValidationResult.MAYAM_CLIENT_ERROR;
			}

			try
			{
				if (expectAOPlaceholder)
				{
					if (!mayamClient.titleIsAO(title.getTitleID()))
					{
						return MessageValidationResult.AO_MISMATCH;
					}
				}
				else
				{
					if (mayamClient.titleIsAO(title.getTitleID()))
					{
						return MessageValidationResult.AO_MISMATCH;
					}
				}
			}
			catch (MayamClientException e)
			{
				logger.error("Mayam client error", e);
				return MessageValidationResult.MAYAM_CLIENT_ERROR;
			}

			return validateProgrammeMaterial(title.getProgrammeMaterial(), details, withMedia);
		}
		else
		{

			if (expectAOPlaceholder)
			{
				try
				{
					if (!mayamClient.titleIsAO(title.getTitleID()))
					{
						return MessageValidationResult.AO_MISMATCH;
					}
					else
					{
						return MessageValidationResult.IS_VALID;
					}
				}
				catch (MayamClientException e)
				{
					logger.error("exception validating material exchange message", e);
					return MessageValidationResult.MAYAM_CLIENT_ERROR;
				}
			}
			else
			{
				try
				{
					if (mayamClient.titleIsAO(title.getTitleID()))
					{
						return MessageValidationResult.AO_MISMATCH;
					}
					else
					{
						return MessageValidationResult.IS_VALID;
					}	
				}
				catch (MayamClientException e)
				{
					logger.error("exception validating material exchange message", e);
					return MessageValidationResult.MAYAM_CLIENT_ERROR;
				}
			}
		}

	}

	private MessageValidationResult validateProgrammeMaterial(ProgrammeMaterialType programmeMaterial, Details details, boolean withMedia)
	{

		String materialID = programmeMaterial.getMaterialID();

		// reject empty materialIDs
		if (StringUtils.isEmpty(materialID))
		{
			return MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY;
		}

		try
		{
			if (!mayamClient.materialExists(materialID))
			{
				return MessageValidationResult.MATERIAL_DOES_NOT_EXIST;
			}


			if (mayamClient.materialHasPassedPreview(materialID))
			{
				return MessageValidationResult.MATERIAL_HAS_ALREADY_PASSED_PREVIEW;
			}
			
			int deliveryVersion = details.getDeliveryVersion().intValue();
			int itemDeliveryVersion = mayamClient.getLastDeliveryVersionForMaterial(materialID);

		
			if (itemDeliveryVersion != -1)
			{
				if (itemDeliveryVersion != (deliveryVersion - 1))
				{
					logger.error(String.format("Unexpected delivery version for material {%s} Expected {%d} Actual {%d}", materialID, itemDeliveryVersion+1, deliveryVersion));
					return MessageValidationResult.UNEXPECTED_DELIVERY_VERSION;
				}
			}

			if(withMedia){
				// item has never been updated by a material exchange message, check if it is a placeholder
				if (!mayamClient.isMaterialPlaceholder(materialID))
				{
					return MessageValidationResult.PLACEHOLDER_ALREADY_HAS_MEDIA;
				}
			}
		}
		catch (MayamClientException e)
		{
			logger.error("Mayam client error", e);
			return MessageValidationResult.MAYAM_CLIENT_ERROR;
		}

		// not validating the passed in package information now, CR018
		return MessageValidationResult.IS_VALID;

	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException
	{ // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof Material))
		{
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a Material",
					unmarshalled.getClass().toString()));
		}
	}

}
