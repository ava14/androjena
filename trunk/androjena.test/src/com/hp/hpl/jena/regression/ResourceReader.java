/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ResourceReader.java,v 1.1 2009/06/29 08:55:39 castagna Exp $
*/

/*
 * ResourceReader.java
 *
 * Created on June 29, 2001, 9:54 PM
 */

package com.hp.hpl.jena.regression;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
/**
 * read a data file stored on the class path.
 * To use this, ensure your data is on the class path (e.g. in the
 * program jar, or in a separate data.jar), and give a relative path
 * name to the data.
 * Not intended for an applet environment.
 * 
 * @author  jjc
 * @version  Release='$Name: Jena-2_6_2 $' Revision='$Revision: 1.1 $' Date='$Date: 2009/06/29 08:55:39 $'
 */
class ResourceReader  {
    // If false use FileInputSDtream's assuming we are in the correct directory;
	//ANDROID: changed to true to use classloader to load assets
	static boolean useClassLoader = true;
//    static boolean useClassLoader = false;
    /** Creates new ResourceReader 
     * @param resource The filename of the data file relative to the Java classpath.
     * @exception java.lang.SecurityException If cannot access the classloader, e.g. in applet.
     * @exception java.lang.IllegalArgumentException If file not found.
     */
    private ResourceReader() {}
    /*
    public ResourceReader(String resource) throws IOException {
        super(getInputStream(resource));
    }
    */
    
    static InputStream getInputStream(String prop) throws IOException {
        if ( useClassLoader) {
            ClassLoader loader = ResourceReader.class.getClassLoader();
            if ( loader == null ) 
                throw new SecurityException("Cannot access class loader");
            InputStream in = loader.getResourceAsStream(prop);
            if ( in == null )
                throw new IllegalArgumentException("Resource: " + prop + " not found on class path.");
            return in;
        } else {
            return new FileInputStream(prop);
        }
    }

}

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
