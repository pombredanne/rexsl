/**
 * Copyright (c) 2011, ReXSL.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the ReXSL.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rexsl.core;

import com.ymock.util.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Makes a decision whether page should be transformed to HTML or returned
 * to the user as untouched XML (or anything else).
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class PageAnalyzer {

    /**
     * The page.
     */
    private final transient String page;

    /**
     * The request.
     */
    private final transient HttpServletRequest request;

    /**
     * Public ctor.
     * @param text The text of response
     * @param rqst The request
     */
    public PageAnalyzer(final String text, final HttpServletRequest rqst) {
        this.page = text;
        this.request = rqst;
    }

    /**
     * Do we need filtering?
     * @return Do we need to transform to XHTML?
     */
    public boolean needsTransformation() {
        final String agent = this.request.getHeader(HttpHeaders.USER_AGENT);
        final String accept = this.request.getHeader(HttpHeaders.ACCEPT);
        // let's check whether we should transform or not
        // @checkstyle BooleanExpressionComplexity (1 line)
        final boolean dontTouch =
            // page is empty
            this.page.isEmpty()
            // it doesn't look like XML
            || !this.page.startsWith("<?xml ")
            // it doesn't refer to any stylesheet
            || !this.page.contains("<?xml-stylesheet ")
            // it's a pure XML client, requesting XML format
            || this.isXmlExplicitlyRequested(accept)
            // the browser supports XSTL 2.0
            || (this.isXsltCapable(agent) && this.acceptsXml(accept));
        if (dontTouch) {
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#filter('%s': %d chars): User-Agent='%s', Accept='%s', no need to transform",
                this.request.getRequestURI(),
                this.page.length(),
                agent,
                accept
            );
        }
        return !dontTouch;
    }

    /**
     * Check if the XSLT transformation is required on the server side.
     * @param agent User agent string from the request.
     * @return If the transformation is needed.
     * @todo #7 The implementation is very preliminary and should be refined.
     *  Not all Chrome or Safari versions support XSLT 2.0. We should properly
     *  parse the "Agent" header and understand versions.
     */
    private Boolean isXsltCapable(final String agent) {
        final Boolean cap = (agent != null)
            && agent.matches(".*(Chrome|Safari).*");
        Logger.debug(this, "#isXsltCapable('%s'): %b", agent, cap);
        return cap;
    }

    /**
     * Check if the application/xml MIME type is the only one there.
     * @param header Accept header string from the request.
     * @return If the application/XML MIME type is the one
     */
    private Boolean isXmlExplicitlyRequested(final String header) {
        final Boolean requested = (header != null)
            && (MediaType.APPLICATION_XML.equals(header));
        Logger.debug(
            this,
            "#isXmlExplicitlyRequested('%s'): %b",
            header,
            requested
        );
        return requested;
    }

    /**
     * Check if the "application/xml" MIME type is accepted.
     * @param header Accept header string from the request.
     * @return If the application/XML MIME type is present
     * @todo #7 This implemetation is very very preliminary and should
     *  be replaced with something more decent. I don't like the idea
     *  of implementing this parsing functionality here. We should better
     *  use some library: http://stackoverflow.com/questions/7705979
     */
    private Boolean acceptsXml(final String header) {
        final Boolean accepts = (header != null)
            && (header.contains(MediaType.APPLICATION_XML));
        Logger.debug(
            this,
            "#acceptsXml('%s'): %b",
            header,
            accepts
        );
        return accepts;
    }

}