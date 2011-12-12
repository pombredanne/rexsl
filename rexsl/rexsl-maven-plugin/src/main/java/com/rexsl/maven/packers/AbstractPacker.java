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
package com.rexsl.maven.packers;

import com.rexsl.maven.Environment;
import com.rexsl.maven.Packer;
import com.ymock.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 * Abstract packer.
 *
 * @author Yegor Bugayenko (yegor@rexsl.com)
 * @version $Id$
 */
abstract class AbstractPacker implements Packer {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public final void pack(final Environment env) {
        final File srcdir = new File(
            env.basedir(),
            String.format("src/main/webapp/%s", this.extension())
        );
        final File destdir = this.ddir(env);
        if (srcdir.exists()) {
            final Collection<File> files = FileUtils.listFiles(
                srcdir,
                new String[] {this.extension()},
                true
            );
            for (File src : files) {
                final File dest = new File(destdir, src.getName());
                try {
                    this.pack(src, dest);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        } else {
            Logger.info(this, "#pack(): %s directory is absent", srcdir);
        }
    }

    /**
     * Get extension of files (and directory name).
     * @return The suffix
     */
    protected abstract String extension();

    /**
     * Pack one file from source to destination.
     * @param src Source file
     * @param dest Destination file
     * @throws IOException If some IO problem inside
     */
    protected abstract void pack(File src, File dest) throws IOException;

    /**
     * Prepare and return destination dir.
     * @param env The environment
     * @return The directory ready to save files
     */
    private File ddir(final Environment env) {
        final File dir = new File(env.webdir(), this.extension());
        if (dir.mkdirs()) {
            Logger.info(this, "#ddir(): %s directory created", dir);
        }
        return dir;
    }

}
