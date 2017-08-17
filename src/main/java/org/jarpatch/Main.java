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
        System.out.println("usage: java org.jarpatch.Main -old oldJar -new newJar -out resultZip [-except RegexpPattern]");
        System.out.println("with");
        System.out.println("-old oldJar: the old jar/war file name");
        System.out.println("-new newJar: the new jar/war file name");
        System.out.println("-out resultZip: the result patch zp file, witch contains new or modified file from the newJar to oldJar");
        System.out.println("-excludes regexpPattern,regexpPattern,...: an optional comma separate list of regexp (JDK1.4 regexp), that specifiy entries to be ignored");
        System.out.println("-logDeleteFile: a flag indicating if need to generate a log file named <code>jarpatch_deleted.log</code>, which contains the list of files that are found in oldJar but not in newJar (one line by file)");
        System.exit(1);
    }
    
    /** parse command line parameter, and proceed */
    public static void main(String[] args) throws IOException {
        File oldJar = null;
        File newJar = null;
        File patch = null;
        Pattern[] exclude = null;
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
            else if("-logDeleteFile".equalsIgnoreCase(args[i]))
                logDeleteFile = true;
        }
        if(oldJar == null || newJar == null || patch == null)
            printUsageAndExit();
        JarPatch jp = new JarPatch();
        if(!jp.buildPatch(newJar, oldJar, exclude, patch, logDeleteFile))
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
