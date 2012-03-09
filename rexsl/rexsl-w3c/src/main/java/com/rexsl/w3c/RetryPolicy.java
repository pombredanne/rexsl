/**
 * Copyright (c) 2011-2012, ReXSL.com
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
package com.rexsl.w3c;

import com.rexsl.test.AssertionPolicy;
import com.rexsl.test.TestResponse;
import com.ymock.util.Logger;

/**
 * Retry policy of assertion.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
final class RetryPolicy implements AssertionPolicy {

    /**
     * XPath to look for.
     */
    private final transient String xpath;

    /**
     * Was it an invalid XML response?
     */
    private transient boolean invalid;

    /**
     * Public ctor.
     * @param addr The XPath addr to check
     */
    public RetryPolicy(final String addr) {
        this.xpath = addr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertThat(final TestResponse response) {
        this.invalid = response.nodes(this.xpath).isEmpty();
        if (this.invalid) {
            throw new AssertionError("invalid XML from W3C server");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean again(final int attempt) {
        Logger.warn(
            this,
            "#again(#%d): W3C XML response is broken",
            attempt
        );
        return this.invalid;
    }

}
