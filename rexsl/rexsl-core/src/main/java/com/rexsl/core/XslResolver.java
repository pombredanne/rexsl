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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

/**
 * Replace standard marshaller.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @link <a href="http://markmail.org/search/?q=list%3Anet.java.dev.jersey.users+ContextResolver%3CMarshaller%3E#query:list%3Anet.java.dev.jersey.users%20ContextResolver%3CMarshaller%3E+page:1+mid:q4fkq6eqlgkzdodc+state:results">discussion</a>
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public final class XslResolver implements ContextResolver<Marshaller> {

    /**
     * XSD locator.
     */
    private static XsdLocator locator = null;

    /**
     * Classes to process.
     */
    private final List<Class> classes = new ArrayList<Class>();

    /**
     * JAXB context.
     */
    private JAXBContext context;

    /**
     * Set locator.
     * @param loc The locator
     */
    public static void setXsdLocator(final XsdLocator loc) {
        XslResolver.locator = loc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Marshaller getContext(final Class<?> type) {
        Marshaller mrsh;
        try {
            mrsh = this.context(type).createMarshaller();
            mrsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final String header = String.format(
                "<?xml version='1.0'?><?xml-stylesheet type='text/xml' href='/xsl/%s.xsl'?>",
                type.getSimpleName()
            );
            mrsh.setProperty("com.sun.xml.bind.xmlHeaders", header);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        if (this.locator != null) {
            final Schema schema = this.locator.locate(type);
            if (schema != null) {
                mrsh.setSchema(schema);
            }
        }
        return mrsh;
    }

    /**
     * Create and return a context.
     * @param cls The class we should process
     * @return The context
     */
    private JAXBContext context(final Class cls) {
        synchronized (this) {
            if (!this.classes.contains(cls)) {
                try {
                    this.classes.add(cls);
                    this.context = JAXBContext.newInstance(
                        this.classes.toArray(new Class[] {})
                    );
                    Logger.info(
                        this,
                        "#context(%s): added to JAXBContext (%d total)",
                        cls.getName(),
                        this.classes.size()
                    );
                } catch (javax.xml.bind.JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return this.context;
        }
    }

}
