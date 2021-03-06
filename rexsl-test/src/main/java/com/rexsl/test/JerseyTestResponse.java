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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import java.io.StringReader;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.json.Json;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.NamespaceContext;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.w3c.dom.Node;

/**
 * Implementation of {@link TestResponse}.
 *
 * <p>Objects of this class are mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(of = "fetcher")
@SuppressWarnings("PMD.TooManyMethods")
@Loggable(Loggable.DEBUG)
final class JerseyTestResponse implements TestResponse {

    /**
     * Fetcher of response (buffered).
     */
    private final transient BufferedJerseyFetcher fetcher;

    /**
     * Decorated request.
     */
    private final transient RequestDecor request;

    /**
     * The XML document in the body, should be loaded on demand in
     * {@link #getXml()}.
     * @see #getXml()
     */
    private transient XML xml;

    /**
     * Public ctor.
     * @param ftch Response fetcher
     * @param rqst Decorated request, for logging purposes
     */
    protected JerseyTestResponse(@NotNull final JerseyFetcher ftch,
        @NotNull final RequestDecor rqst) {
        this.fetcher = new BufferedJerseyFetcher(ftch);
        this.request = rqst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "HTTP request:\n%s\nHTTP response (%d):\n%s",
            this.request,
            this.getStatus(),
            new ClientResponseDecor(this.fetcher.fetch(), this.getBody())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestClient rel(@NotNull final String query) {
        final List<String> links = this.xpath(query);
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' not found in:\n%s\nHTTP request:\n%s",
                StringEscapeUtils.escapeJava(query),
                new ClientResponseDecor(this.fetcher.fetch(), this.getBody()),
                this.request
            ),
            links,
            Matchers.hasSize(1)
        );
        return this.follow(UriBuilder.fromUri(links.get(0)).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestClient follow() {
        return this.follow(this.fetcher.fetch().getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getBody() {
        try {
            return this.fetcher.body();
        } catch (java.io.IOException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public JsonReader getJson() {
        try {
            return Json.createReader(new StringReader(this.fetcher.body()));
        } catch (java.io.IOException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return this.fetcher.fetch().getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Node node() {
        return this.getXml().node();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> xpath(@NotNull final String query) {
        return Collections.unmodifiableList(this.getXml().xpath(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getStatusLine() {
        return Logger.format(
            "%d %s",
            this.fetcher.fetch().getStatus(),
            this.fetcher.fetch().getClientResponseStatus()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public MultivaluedMap<String, String> getHeaders() {
        return this.fetcher.fetch().getHeaders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Cookie cookie(@NotNull final String name) {
        final MultivaluedMap<String, String> headers = this.getHeaders();
        MatcherAssert.assertThat(
            "cookies should be set in HTTP header",
            headers.containsKey(HttpHeaders.SET_COOKIE)
        );
        final String header = StringUtils.join(
            headers.get(HttpHeaders.SET_COOKIE), ", "
        );
        Cookie cookie = null;
        for (HttpCookie candidate : HttpCookie.parse(header)) {
            if (candidate.getName().equals(name)) {
                cookie = new Cookie(
                    candidate.getName(),
                    candidate.getValue(),
                    candidate.getPath(),
                    candidate.getDomain(),
                    candidate.getVersion()
                );
                break;
            }
        }
        MatcherAssert.assertThat(
            Logger.format(
                "cookie '%s' not found in Set-Cookie header: '%s'",
                name, header
            ),
            cookie,
            Matchers.notNullValue()
        );
        assert cookie != null;
        return cookie;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse registerNs(@NotNull final String prefix,
        @NotNull final Object uri) {
        synchronized (this.fetcher) {
            this.xml = this.getXml().registerNs(prefix, uri);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse merge(@NotNull final NamespaceContext ctx) {
        synchronized (this.fetcher) {
            this.xml = this.getXml().merge(ctx);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<XML> nodes(@NotNull final String query) {
        return Collections.unmodifiableList(this.getXml().nodes(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertThat(@NotNull final AssertionPolicy assertion) {
        synchronized (this.fetcher) {
            int attempt = 0;
            while (true) {
                try {
                    assertion.assertThat(this);
                    break;
                } catch (AssertionError ex) {
                    ++attempt;
                    if (!assertion.isRetryNeeded(attempt)) {
                        throw ex;
                    }
                    if (attempt >= JerseyTestResponse.MAX_ATTEMPTS) {
                        this.fail(
                            Logger.format("failed after %d attempt(s)", attempt)
                        );
                    }
                    if (assertion.getClass()
                        .getAnnotation(AssertionPolicy.Quiet.class) == null) {
                        Logger.warn(
                            this,
                            // @checkstyle LineLength (1 line)
                            "#assertThat(%[type]s): attempt #%d failed, re-trying: %[exception]s",
                            assertion, attempt, ex
                        );
                    } else {
                        Logger.warn(
                            this,
                            "#assertThat(%[type]s): attempt #%d: %s",
                            assertion, attempt, ex.getMessage()
                        );
                    }
                    this.fetcher.reset();
                }
            }
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public void fail(@NotNull final String reason) {
        this.assertThat(new Failure(reason));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertStatus(final int status) {
        return this.assertStatus(Matchers.equalTo(status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertStatus(@NotNull final Matcher<Integer> matcher) {
        this.assertThat(new StatusMatcher(matcher));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertHeader(@NotNull final String name,
        @NotNull final Matcher<Iterable<String>> matcher) {
        this.assertThat(new HeaderMatcher(name, matcher));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertBody(@NotNull final Matcher<String> matcher) {
        this.assertThat(new BodyMatcher(matcher));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertXPath(@NotNull final String xpath) {
        this.assertThat(new XpathAssertionMatcher(this.getXml(), xpath));
        return this;
    }

    /**
     * Get XmlDocument of the body.
     *
     * <p>It uses {@code this.fetcher} as a synchronization lock because at
     * the time of method execution {@code this.xml} may be {@code NULL}.
     *
     * @return The XML document
     */
    @NotNull
    public XML getXml() {
        synchronized (this.fetcher) {
            if (this.xml == null) {
                this.xml = new LazyXml(this);
            }
            return this.xml;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public TestResponse assertJson(@NotNull final String element) {
        Logger.warn(this, "method #assertJson() is not implemented yet");
        return this;
    }

    /**
     * Follow the URI provided.
     * @param uri The URI to follow
     * @return New client
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private TestClient follow(final URI uri) {
        final TestClient client = RestTester.start(uri);
        for (NewCookie cookie : this.fetcher.fetch().getCookies()) {
            if (!cookie.getValue().isEmpty()) {
                client.header(
                    HttpHeaders.COOKIE,
                    new Cookie(cookie.getName(), cookie.getValue()).toString()
                );
            }
        }
        return client;
    }

}
