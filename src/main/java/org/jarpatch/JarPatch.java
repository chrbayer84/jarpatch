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

import java.io.*;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * build a patch zip that contains the difference between a newJar from an oldJar.
 * <p>
 * The patch will contains all new file introduced on the newJar, and all file that have been 
 * modified from the oldJar. Changed file is determined from computed MD5 hash key.
 *
 * @author Norbert Barbosa
 * @version $Revision$
 */
public class JarPatch {
    static public final String DELLOG_NAME = "jarpatch_deleted.log";

    /** build the resulting patch.
     * 
     * @param newJar - the new jar, use to compute difference
     * @param oldJar - the old jar, use to compute difference
     * @param excludes - an optional (can be null) regexp for excluding ressource(s)
     * @param metaInfIncludes - an optional (can be null) regexp for including META-INF ressource(s)
     * @param zipPatchFile - the result zip file, that contains the difference between newJar and oldJar
     * @param logDeletedFiles - if true then add a file <code>jarpatch_deleted.log</code> on the zipPatchFile,
     * that contains the list of files that are found in oldJar but not in newJar (one line by file) 
     * @return true if a patch can be build, false if no difference have been found
     * @throws IOException - if IO error occur
     */      
    public boolean buildPatch(File newJar, File oldJar, Pattern[] excludes, Pattern[] metaInfIncludes, File zipPatchFile, boolean logDeletedFiles) throws IOException {
        JarContent fnew = new JarContent(newJar);
        JarContent fold = new JarContent(oldJar);
        if(excludes != null){
            fnew.setExcludePattern(excludes);
            fold.setExcludePattern(excludes);
        }
        if(metaInfIncludes != null){
            fnew.setMetaInfIncludePattern(metaInfIncludes);
            fold.setMetaInfIncludePattern(metaInfIncludes);
        }
        fnew.initializeContent();
        fold.initializeContent();
        
        // compute difference of deleted files from oldJar to newJar
        List deldiff = logDeletedFiles? fold.computeDeletedEntry(fnew): Collections.EMPTY_LIST;

        // compute difference
        JarEntry[] diff = fnew.computeNewerEntry(fold);
        if(diff.length == 0 && deldiff.isEmpty())
            return false;

        // create deleted log file in current directory
        StringBuffer delDiffContent = null;
        if (!deldiff.isEmpty()) {
            delDiffContent = new StringBuffer();
            for (int i = 0; i < deldiff.size(); i++) {
                delDiffContent.append(deldiff.get(i));
                delDiffContent.append(System.getProperty("line.separator"));
            }
        }

        // build output zip file
        ZipOutputStream out = null;
        try{
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipPatchFile)));
            for(int i = 0; i < diff.length; i++) {
                out.putNextEntry(diff[i]);
                fnew.writeEntry(diff[i], out);
                out.closeEntry();
            }
            if (delDiffContent != null) {
                out.putNextEntry(new ZipEntry(DELLOG_NAME));
                out.write(delDiffContent.toString().getBytes());
                out.closeEntry( );
            }
            out.finish();
            return true;
        } finally{
            if(out != null) try{out.close();}catch(IOException e){/*ignore*/}
        }
    }

    /** helper method to build an Pattern array from a list of comma separate string pattern.
     * if no pattern, return EMPTY_PATTERNS.
     */
    public static Pattern[] tokenizePatterns(String patterns) throws PatternSyntaxException {
        if(patterns == null) return EMPTY_PATTERN;
        ArrayList ret = new ArrayList();
        StringTokenizer tk = new StringTokenizer(patterns, ",");
        while(tk.hasMoreTokens()){
            ret.add(Pattern.compile(tk.nextToken().trim()));
        }
        Pattern[] aret = new Pattern[ret.size()];
        ret.toArray(aret);
        return aret;
    }
    public static final Pattern[] EMPTY_PATTERN = {};
}
