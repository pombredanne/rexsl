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
package com.rexsl.page;

import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link JaxbBundle}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JaxbBundleTest {

    /**
     * JaxbBundle can be converted to XML text.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToXml() throws Exception {
        final JaxbBundle bundle = new JaxbBundle("root")
            .add("employee")
                .attr("age", "10")
                .add("dept")
                    .add("country", "DE").up()
                    .add("salary", "> \u20AC 50,000").up().up()
                .add(new JaxbBundle("car").add("make", "BMW").up())
                .add(
                    "projects",
                    new JaxbBundle.Group<String>(Arrays.asList("test")) {
                        @Override
                        public JaxbBundle bundle(final String name) {
                            return new JaxbBundle("project").attr("name", name);
                        }
                    }
                )
                .link(new Link("remove", "#del")).up();
        MatcherAssert.assertThat(
            bundle.element(),
            XhtmlMatchers.hasXPaths(
                "/root/employee[@age='10']/dept[country='DE']",
                "/root/employee/links/link[@rel='remove' and @href='#del']",
                "/root/employee/projects/project[name='test']",
                "/root/employee/car[make='BMW']",
                "//dept[salary='> \u20AC 50,000']",
                "//dept[contains(@boss,'Charles')]"
            )
        );
    }

    /**
     * JaxbBundle can be converted to XML through JAXB.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToXmlThroughJaxb() throws Exception {
        final JaxbBundle bundle = new JaxbBundle("alpha")
            .add("beta-1")
                .attr("name", "Joe")
            .up()
            .link(new Link("add", "#add"))
            .add("beta-2")
                .add("gamma", "works fine, isn't it?")
                .up()
            .up();
        final BasePageMocker page = new BasePageMocker()
            .init(new ResourceMocker().mock());
        page.append(bundle.element());
        MatcherAssert.assertThat(
            JaxbConverter.the(page),
            XhtmlMatchers.hasXPaths(
                "/foo/alpha/beta-2/gamma[contains(.,'it')]",
                "/foo/alpha/links/link[@rel='add' and @href='#add']"
            )
        );
    }

}
