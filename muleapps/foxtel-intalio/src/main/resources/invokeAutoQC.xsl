<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
       <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wor="http://ns.mediasmithsforge.com/foxtel/qc/workflow" xmlns:adap="http://ns.mediasmiths.com/foxtel/wf/adapter">
            <soapenv:Header/>
            <soapenv:Body>
                <wor:startRequest>
                        <adap:assetId><xsl:value-of select="invokeIntalioQCFlow/assetId"/></adap:assetId>
                        <adap:forTXDelivery><xsl:value-of select="invokeIntalioQCFlow/forTXDelivery"/></adap:forTXDelivery>
                </wor:startRequest>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>