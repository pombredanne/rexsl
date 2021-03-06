/**
 * Copyright (c) 2011-2013, ReXSL.com
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
package com.rexsl.test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link ClientResponse}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class ClientResponseMocker {

    /**
     * Paths.
     */
    private final transient ClientResponse response =
        Mockito.mock(ClientResponse.class);

    /**
     * Headers.
     */
    private final transient MultivaluedMap<String, String> headers =
        new MultivaluedMapImpl();

    /**
     * Public ctor.
     */
    public ClientResponseMocker() {
        Mockito.doAnswer(
            new Answer<URI>() {
                public URI answer(final InvocationOnMock invocation) {
                    return URI.create(
                        ClientResponseMocker.this.headers
                            .getFirst(HttpHeaders.LOCATION)
                    );
                }
            }
        ).when(this.response).getLocation();
        Mockito.doAnswer(
            new Answer<List<NewCookie>>() {
                public List<NewCookie> answer(
                    final InvocationOnMock invocation) {
                    final List<NewCookie> cookies = new LinkedList<NewCookie>();
                    for (String txt
                        : ClientResponseMocker.this.headers
                        .get(HttpHeaders.SET_COOKIE)) {
                        cookies.add(NewCookie.valueOf(txt));
                    }
                    return cookies;
                }
            }
        ).when(this.response).getCookies();
        Mockito.doReturn(this.headers).when(this.response).getHeaders();
        this.withStatus(HttpURLConnection.HTTP_OK);
        this.withEntity("");
    }

    /**
     * Return this status code.
     * @param code The code
     * @return This object
     */
    public ClientResponseMocker withStatus(final int code) {
        Mockito.doReturn(code).when(this.response).getStatus();
        return this;
    }

    /**
     * Return this header.
     * @param name The name of ti
     * @param value The value
     * @return This object
     */
    public ClientResponseMocker withHeader(final String name,
        final String value) {
        if (!this.headers.containsKey(name)) {
            this.headers.put(name, new ArrayList<String>(0));
        }
        this.headers.get(name).add(value);
        return this;
    }

    /**
     * Return this entity.
     * @param entity The entity
     * @return This object
     */
    public ClientResponseMocker withEntity(final String entity) {
        try {
            return this.withEntity(
                IOUtils.toInputStream(entity, CharEncoding.UTF_8)
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Return this entity.
     * @param entity The entity
     * @return This object
     */
    public ClientResponseMocker withEntity(final byte[] entity) {
        return this.withEntity(new ByteArrayInputStream(entity));
    }

    /**
     * Return this entity.
     * @param entity The entity
     * @return This object
     */
    public ClientResponseMocker withEntity(final InputStream entity) {
        Mockito.doReturn(entity).when(this.response)
            .getEntityInputStream();
        Mockito.doReturn(true).when(this.response).hasEntity();
        return this;
    }

    /**
     * Mock it.
     * @return Mocked class
     */
    public ClientResponse mock() {
        return this.response;
    }

}
