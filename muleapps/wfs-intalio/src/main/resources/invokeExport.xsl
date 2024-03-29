<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:wfa="http://ns.mediasmiths.com/foxtel/wf/adapter" exclude-result-prefixes="wfa" version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
       <soapenv:Envelope xmlns:adap="http://ns.mediasmiths.com/foxtel/wf/adapter" xmlns:ns2="http://ns.mediasmiths.com/foxtel/tc/adapter" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wor="http://ns.mediasmithsforge.com/foxtel/export/transcode">
		<soapenv:Header/>
		<soapenv:Body>
			<wor:EventStartMessageRequest><xsl:copy-of select="wfa:invokeExport/node()"/></wor:EventStartMessageRequest>
		</soapenv:Body>
	</soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>