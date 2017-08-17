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
    private String fmetaInfIncludes;
    private boolean logDeleteFile = false;

    public void setExcludes(String excludes) {
        fexcludes = excludes;
    }

    public void setMetaInfIncludes(String metaInfIncludes) {
        fmetaInfIncludes = metaInfIncludes;
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
        Pattern[] metaInfIncludes = null;
        if(fmetaInfIncludes != null){
            try{
                metaInfIncludes = JarPatch.tokenizePatterns(fmetaInfIncludes);
                if(metaInfIncludes.length == 0) metaInfIncludes = null;
            }catch(PatternSyntaxException e){
                throw new BuildException("metaInfIncludes attribute invalid: "+e.getMessage());
            }
        }
        log("Generating "+fresultPatch+" patch from difference between new "+fnewJar+" and old "+foldJar+" with logDeleteFile="+logDeleteFile, Project.MSG_VERBOSE);
        
        JarPatch jp = new JarPatch();
        try {
            if(!jp.buildPatch(fnewJar, foldJar, excludes, metaInfIncludes, fresultPatch, logDeleteFile))
                log("files  "+fnewJar+" and "+foldJar+" contains no suitable difference: no patch builded");
        } catch(IOException e) {
            e.printStackTrace();
            throw new BuildException("Unexpected IOException: "+e.getMessage());
        }

    }
}
