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
package com.rexsl.maven;

import com.rexsl.maven.checks.BinaryFilesCheck;
import com.rexsl.maven.checks.CssStaticCheck;
import com.rexsl.maven.checks.FilesStructureCheck;
import com.rexsl.maven.checks.InContainerScriptsCheck;
import com.rexsl.maven.checks.JSStaticCheck;
import com.rexsl.maven.checks.JigsawCssCheck;
import com.rexsl.maven.checks.WebXmlCheck;
import com.rexsl.maven.checks.XhtmlOutputCheck;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider of checks.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (100 lines)
 */
public final class ChecksProvider {

    /**
     * Get full collection of checks.
     * @return List of checks
     */
    public Set<Check> all() {
        final Set<Check> checks = new LinkedHashSet<Check>();
        checks.add(new BinaryFilesCheck());
        checks.add(new CssStaticCheck());
        checks.add(new JigsawCssCheck());
        checks.add(new JSStaticCheck());
        checks.add(new FilesStructureCheck());
        checks.add(new XhtmlOutputCheck());
        checks.add(new InContainerScriptsCheck());
        checks.add(new WebXmlCheck());
        return checks;
    }

}
