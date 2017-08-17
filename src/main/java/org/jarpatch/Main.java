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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * A simple main around the {@link JarPatch}.<p>
 * build a patch zip that contains the difference between a newJar from an oldJar.<br>
 * The patch will contains all new file introduced on the newJar, and all file that have been 
 * modified from the oldJar. Changed file is determined from computed MD5 hash key.
 * <p>
 * usage: <b>org.jarpatch.Main -old oldJar -new newJar -out resultZip [-exclude RegexpPattern] [-logDeleteFile]</b>,
 * <br>with:
 * <ul>
 * <li> -old oldJar: the old jar file name
 * <li> -new newJar: the new jar file name
 * <li> -out resultZip: the result patch zp file, witch contains new or modified file from the newJar 
 * to oldJar
 * <li> -excludes regexpPattern,regexpPattern,...: an optional comma separate list of regexp (JDK1.4 regexp), 
 * that specifiy entries to be ignored (like <code>.+\.gif</code>)
 * <li> -logDeleteFile: a flag indicating if need to generate a log file named <code>jarpatch_deleted.log</code>,
 * which contains the list of files that are found in oldJar but not in newJar (one line by file)
 * @author Norbert Barbosa
 * @version $Revision$
 */
public class Main {
    
    /** helper method to print the main usage, and exit */
    private static void printUsageAndExit(){
        System.out.println("usage: java org.jarpatch.Main -old oldJar -new newJar -out resultZip [-excludes RegexpPattern] [-metaInfIncludes RegexpPattern] [-logDeleteFile]");
        System.out.println("with");
        System.out.println("-old oldJar: the old jar/war file name");
        System.out.println("-new newJar: the new jar/war file name");
        System.out.println("-out resultZip: the result patch zp file, witch contains new or modified file from the newJar to oldJar");
        System.out.println("-excludes regexpPattern,regexpPattern,...: an optional comma separate list of regexp (JDK1.4 regexp), that specify entries to be ignored");
        System.out.println("-metaInfIncludes regexpPattern,regexpPattern,...: an optional comma separate list of regexp (JDK1.4 regexp), that specify META-INF entries to be included");
        System.out.println("-logDeleteFile: a flag indicating if need to generate a log file named <code>jarpatch_deleted.log</code>, which contains the list of files that are found in oldJar but not in newJar (one line by file)");
        System.exit(1);
    }
    
    /** parse command line parameter, and proceed */
    public static void main(String[] args) throws IOException {
        File oldJar = null;
        File newJar = null;
        File patch = null;
        Pattern[] exclude = null;
        Pattern[] metaInfIncludes = null;
        boolean logDeleteFile = false;
        for(int i = 0; i < args.length; i++) {
            if("-h".equalsIgnoreCase(args[i]) || "-help".equalsIgnoreCase(args[i]))
                printUsageAndExit();
            else if("-old".equalsIgnoreCase(args[i]))
                oldJar = extractFile(args[++i], "-old", true);
            else if("-new".equalsIgnoreCase(args[i]))
                newJar = extractFile(args[++i], "-new", true);
            else if("-out".equalsIgnoreCase(args[i]))
                patch = extractFile(args[++i], "-out", false);
            else if("-excludes".equalsIgnoreCase(args[i]))
                exclude = JarPatch.tokenizePatterns(args[++i]);
            else if("-metaInfIncludes".equalsIgnoreCase(args[i]))
                metaInfIncludes = JarPatch.tokenizePatterns(args[++i]);
            else if("-logDeleteFile".equalsIgnoreCase(args[i]))
                logDeleteFile = true;
        }
        if(oldJar == null || newJar == null || patch == null)
            printUsageAndExit();
        JarPatch jp = new JarPatch();
        if(!jp.buildPatch(newJar, oldJar, exclude, metaInfIncludes, patch, logDeleteFile))
            System.out.println("jar files contains no difference: no patch build");
    }

    /** helper method to extract a file and check if this file exist */
    private static File extractFile(String arg, String parameter, boolean shouldExist) {
        File ret = new File(arg);
        if(shouldExist && !(ret.isFile() && ret.exists())){
            System.err.println(parameter+" should be an existing file");
            printUsageAndExit();
        }
        return ret; 
    }
}
