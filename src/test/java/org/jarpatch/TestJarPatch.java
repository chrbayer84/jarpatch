/* jarpatch - http://perso.club-internet.fr/sjobic/jarpatch/
 * Copyright (C) 2004 Norbert Barbosa (norbert.barbosa@laposte.net)
 *
 * An utility, available as standalone application and Ant task,
 * to build zip patch which correspond to the difference between 2 jar files
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jarpatch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;
import org.jarpatch.JarPatch;

/**
 * Unit Test for {@link org.jarpatch.JarPatch}
 *
 * @author Norbert Barbosa
 * @version $Revision$
 */
public class TestJarPatch extends TestCase {

    public void testSimpleNoDiff() throws IOException {
        JarPatch jp = new JarPatch();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        File result = new File(getClass().getResource("/").getFile(), "out-nodiff.zip");
        Pattern[] excludes = JarPatch.tokenizePatterns(".*/test1\\.txt, .*/test?\\.txt");
        assertFalse("Difference found", jp.buildPatch(newJar, oldJar, excludes, result, false));
        assertFalse(result.exists());
    }

    public void testSimpleDiff() throws IOException {
        JarPatch jp = new JarPatch();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiff-diff.zip");
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, result, false));
        assertTrue(result.exists());
    }

    public void testSimpleWar() throws IOException {
        JarPatch jp = new JarPatch();

        File newJar = new File(getClass().getResource("/test-new.war").getFile());
        File oldJar = new File(getClass().getResource("/test-old.war").getFile());
        File result = new File(getClass().getResource("/").getFile(), "testSimpleWar-diff.zip");
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, result, false));
        assertTrue(result.exists());
    }

    public void testSimpleDiffNoLog() throws IOException {
        JarPatch jp = new JarPatch();

        File newJar = new File(getClass().getResource("/test-new.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiffNoLog-diff.zip");
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, result, true));
        assertTrue(result.exists());
        ZipFile zresult = new ZipFile(result);
        assertNull("log file generated", zresult.getEntry(JarPatch.DELLOG_NAME));
    }

    public void testSimpleDiffLog() throws IOException {
        JarPatch jp = new JarPatch();

        File newJar = new File(getClass().getResource("/test-new2.zip").getFile());
        File oldJar = new File(getClass().getResource("/test-old.zip").getFile());
        File result = new File(getClass().getResource("/").getFile(), "testSimpleDiffLog-diff.zip");
        assertTrue("No difference found", jp.buildPatch(newJar, oldJar, null, result, true));
        assertTrue(result.exists());
        ZipFile zresult = new ZipFile(result);
        ZipEntry zlog = zresult.getEntry(JarPatch.DELLOG_NAME);
        assertNotNull("missing log file", zlog);
        BufferedReader br = new BufferedReader(new InputStreamReader(zresult.getInputStream(zlog)));
        assertEquals("log content mismatch", "test/test1.txt", br.readLine());
        br.close();
    }


}
