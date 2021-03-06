<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs xsl xhtml">
    <xsl:output method="xhtml"/>
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template name="content">
        <xsl:value-of select="/page/text" />
        <xsl:copy-of select="/page/img" />
    </xsl:template>
</xsl:stylesheet>
