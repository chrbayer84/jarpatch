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
import java.util.regex.PatternSyntaxException;

import org.apache.tools.ant.*;

/**
 * An Ant task for building zip patch, corresponding to the difference between 2 jar files.
 * <p>
 * usage:
 * <pre>
    &lt:jarpatch newJar="myNewJar.jar" oldjar="myoldjar.jar" resultPatch="myPatch.zip" excludes=".*\.gif,.*\.jpg" logDeleteFile="true" &gt;
 </pre>
 * @author Norbert Barbosa
 * @version $Revision$
 */
public class JarPatchTask extends Task {
    private File foldJar;
    private File fnewJar;
    private File fresultPatch;
    private String fexcludes;
    private boolean logDeleteFile = false;

    public void setExcludes(String excludes) {
        fexcludes = excludes;
    }

    public void setNewJar(File newJar) {
        fnewJar = newJar;
    }

    public void setOldJar(File oldJar) {
        foldJar = oldJar;
    }

    public void setResultPatch(File resultPatch) {
        fresultPatch = resultPatch;
    }

    public void setLogDeleteFile(boolean v) {
        logDeleteFile = v;
    }

    /** do the task */
    public void execute() throws BuildException {
        if(foldJar == null || !foldJar.exists() || !foldJar.isFile())
            throw new BuildException("oldJar attribute unspecified or invalid");
        if(fnewJar == null || !fnewJar.exists() || !fnewJar.isFile())
            throw new BuildException("newJar attribute unspecified or invalid");
        if(fresultPatch == null)
            throw new BuildException("resultPatch attribute unspecified or invalid");
        Pattern[] excludes = null;
        if(fexcludes != null){
            try{
                excludes = JarPatch.tokenizePatterns(fexcludes);
                if(excludes.length == 0) excludes = null;
            }catch(PatternSyntaxException e){
                throw new BuildException("excludes attribute invalid: "+e.getMessage());
            }
        }
        log("Generating "+fresultPatch+" patch from difference between new "+fnewJar+" and old "+foldJar+" with logDeleteFile="+logDeleteFile, Project.MSG_VERBOSE);
        
        JarPatch jp = new JarPatch();
        try {
            if(!jp.buildPatch(fnewJar, foldJar, excludes, fresultPatch, logDeleteFile))
                log("files  "+fnewJar+" and "+foldJar+" contains no suitable difference: no patch builded");
        } catch(IOException e) {
            e.printStackTrace();
            throw new BuildException("Unexpected IOException: "+e.getMessage());
        }

    }
}
