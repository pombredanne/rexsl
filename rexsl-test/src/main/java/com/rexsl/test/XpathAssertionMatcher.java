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

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Matches HTTP header against required value.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3.4
 */
@ToString
@EqualsAndHashCode(of = { "xml", "xpath" })
final class XpathAssertionMatcher implements AssertionPolicy {

    /**
     * The source.
     */
    private final transient XML xml;

    /**
     * The matcher to use.
     */
    private final transient String xpath;

    /**
     * Public ctor.
     * @param src The source
     * @param path The XPath to find there
     */
    protected XpathAssertionMatcher(final XML src, final String path) {
        this.xml = src;
        this.xpath = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertThat(final TestResponse response) {
        MatcherAssert.assertThat(
            Logger.format(
                "XPath '%s' has to exist in:\n%s",
                StringEscapeUtils.escapeJava(this.xpath),
                response
            ),
            this.xml.nodes(this.xpath),
            Matchers.not(Matchers.<XML>empty())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRetryNeeded(final int attempt) {
        return false;
    }

}
