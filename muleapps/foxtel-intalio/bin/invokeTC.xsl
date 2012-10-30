<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
       <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wor="http://example.com/tc/workflow" xmlns:adap="http://ns.mediasmiths.com/foxtel/wf/adapter">
            <soapenv:Header/>
            <soapenv:Body>
                <wor:startRequest>
                        <adap:inputFile><xsl:value-of select="invokeIntalioTCFlow/inputFile"/></adap:inputFile>
                        <adap:outputFolder><xsl:value-of select="invokeIntalioTCFlow/outputFolder"/></adap:outputFolder>
                        <adap:packageID><xsl:value-of select="invokeIntalioTCFlow/packageID"/></adap:packageID>                        
                </wor:startRequest>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>