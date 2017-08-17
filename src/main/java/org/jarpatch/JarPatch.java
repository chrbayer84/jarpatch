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
