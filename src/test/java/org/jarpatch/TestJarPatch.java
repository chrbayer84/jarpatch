/*  jarpatch - https://github.com/chrbayer84/jarpatch
    Copyright (c) 2004 Norbert Barbosa, 2017 Christian Bayer
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
    3. All advertising materials mentioning features or use of this software
       must display the following acknowledgement:
       This product includes software developed by the <organization>.
    4. Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY NORBERT BARBOSA, CHRISTIAN BAYER ''AS IS'' AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL NORBERT BARBOSA, CHRISTIAN BAYER BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jarpatch;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test for {@link org.jarpatch.JarPatch}
 *
 * @author Norbert Barbosa
 * @version $Revision$
 */
public class TestJarPatch {

    @Test
    public void testSimpleNoDiff() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "out-nodiff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        Pattern[] excludes = JarPatch.tokenizePatterns(".*/test1\\.txt, .*/test?\\.txt");
        assertFalse("Difference found", jp.buildPatch(newJar, oldJar, excludes, null, result, false));
        assertFalse(result.exists());
    }

    @Test
    public void testSimpleDiff() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiff-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());

        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, null, result, false));
        assertTrue(result.exists());
    }

    @Test
    public void testSimpleWar() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testSimpleWar-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-new.war").getFile());
        File oldJar = new File(getClass().getResource("/test-old.war").getFile());
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, null, result, false));
        assertTrue(result.exists());
    }

    @Test
    public void testSimpleDiffNoLog() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiffNoLog-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, null, result, true));
        assertTrue(result.exists());
        ZipFile zresult = new ZipFile(result);
        assertNull("log file generated", zresult.getEntry(JarPatch.DELLOG_NAME));
    }

    @Test
    public void testSimpleDiffLog() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiffLog-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-new2.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, null, result, true));
        assertTrue(result.exists());
        ZipFile zresult = new ZipFile(result);
        ZipEntry zlog = zresult.getEntry(JarPatch.DELLOG_NAME);
        assertNotNull("missing log file", zlog);
        BufferedReader br = new BufferedReader(new InputStreamReader(zresult.getInputStream(zlog)));
        assertEquals("log content mismatch", "test/test1.txt", br.readLine());
        br.close();
    }

    @Test
    public void testDefaultMetaInfExclude() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testDefaultMetaInfExclude-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-meta_inf-new.jar").getFile());
        File oldJar = new File(getClass().getResource("/test-meta_inf-old.jar").getFile());
        assertFalse("Difference found", jp.buildPatch(newJar, oldJar, null, null, result, false));
        assertFalse(result.exists());
    }

    @Test
    public void testMetaInfInclude() throws IOException {
        JarPatch jp = new JarPatch();

        File result = new File(getClass().getResource("/").getFile(), "testDefaultMetaInfExclude-diff.zip");
        if (result.exists())
            result.delete();

        File newJar = new File(getClass().getResource("/test-meta_inf-new.jar").getFile());
        File oldJar = new File(getClass().getResource("/test-meta_inf-old.jar").getFile());
        Pattern[] metaInfIncludes = JarPatch.tokenizePatterns(".*Log4j2Plugins\\.dat");
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, metaInfIncludes, result, false));
        assertTrue(result.exists());
        assertTrue( new JarFile( result ).stream().anyMatch( jarEntry -> jarEntry.getName().equals( "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat" ) ) );
        assertFalse( new JarFile( result ).stream().anyMatch( jarEntry -> jarEntry.getName().equals( "META-INF/MANIFEST.MF" ) ) );
    }
}
