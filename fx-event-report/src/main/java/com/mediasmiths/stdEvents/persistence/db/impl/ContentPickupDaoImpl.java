package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;

public class ContentPickupDaoImpl extends HibernateDao<ContentPickup, Long> implements EventMarshaller, QueryDao<ContentPickup>
{
	public static final transient Logger logger = Logger.getLogger(ContentPickupDaoImpl.class);

	public ContentPickupDaoImpl()
	{
		super(ContentPickup.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		ContentPickup material = get(event);
		material.event = event;
		logger.info("Saving material");
		saveOrUpdate(material);
	}

	@Transactional
	public List<ContentPickup> getAll()
	{
		logger.info("Getting all content pickup...");
		List<ContentPickup> material = super.getAll();
		logger.info("Content found");
		return material;
	}

	@Transactional
	public ContentPickup get(EventEntity event)
	{
		logger.info("Setting content pickup for event");
		ContentPickup material = new ContentPickup();
		String matStr = event.getPayload();
		logger.info("Payload recieved: " + matStr);

		if (matStr.contains("AdultMaterial"))
		{
			material.setAdultMaterial(Boolean.parseBoolean(matStr.substring(
					matStr.indexOf("AdultMaterial") + 15,
					matStr.indexOf("</AdultMaterial"))));
		}
		if (matStr.contains("Format"))
			material.setFormat(matStr.substring(matStr.indexOf("Format") + 7, matStr.indexOf("</Format")));
		if (matStr.contains("AspectRatio"))
			material.setAspectRatio(matStr.substring(matStr.indexOf("AspectRatio") + 12, matStr.indexOf("</AspectRatio")));
		if (matStr.contains("FirstFrameTimecode"))
			material.setFirstFrameTimecode(matStr.substring(
					matStr.indexOf("FirstFrameTimecode") + 19,
					matStr.indexOf("</FirstFrameTimecode")));
		if (matStr.contains("LastFrameTimecode"))
			material.setLastFrameTimecode(matStr.substring(
					matStr.indexOf("LastFrameTimecode") + 19,
					matStr.indexOf("</LastFrameTimecode")));
		if (matStr.contains("Duration"))
			material.setDuration(matStr.substring(matStr.indexOf("Duration") + 9, matStr.indexOf("</Duration")));
		if (matStr.contains("</Media"))
			material.setMedia(matStr.substring(matStr.indexOf("Media") + 6, matStr.indexOf("</Media")));
		if (matStr.contains("OriginalConform"))
			material.setOriginalConform(matStr.substring(
					matStr.indexOf("OriginalConform") + 16,
					matStr.indexOf("</OriginalConform")));
		if (matStr.contains("Presentation"))
			material.setPresentation(matStr.substring(matStr.indexOf("Presentation") + 13, matStr.indexOf("</Presentation")));
		if (matStr.contains("AdditionalProgrammeDetail"))
			material.setAdditionalProgrammeDetail(matStr.substring(
					matStr.indexOf("AdditionalProgrammeDetail") + 26,
					matStr.indexOf("</AdditionalProgrammeDetail")));
		if (matStr.contains("materialId"))
			material.setMaterialID(matStr.substring(matStr.indexOf("materialId") + 11, matStr.indexOf("</materialId")));
		if (matStr.contains("UnmatchedPath"))
			material.setUnmatchedPath(matStr.substring(matStr.indexOf("UnmatchedPath") + 14, matStr.indexOf("</UnmatchedPath")));
		logger.info("MaterialType constructed");
		return material;
	}

	@Override
	public String getNamespace(EventEntity event)
	{
		String namespace = event.getNamespace();
		return namespace;
	}

	@Override
	public String getInfo(EventEntity event)
	{
		String namespace = event.getNamespace().substring(25);
		String info = "Translating from EventEntity to " + namespace;
		return info;
	}
}
