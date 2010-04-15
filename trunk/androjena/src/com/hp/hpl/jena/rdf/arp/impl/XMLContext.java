/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * * $Id: XMLContext.java,v 1.1 2009/06/29 08:55:38 castagna Exp $
 
 AUTHOR:  Jeremy J. Carroll
 */
/*
 * XMLContext.java
 *
 * Created on July 10, 2001, 2:35 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;

/**
 * 
 * Both the baseURI and the lang may be tainted with errors. They should not be
 * accessed without providing a taint object to propogate such tainting.
 * 
 * @author jjc
 * 
 */
public class XMLContext extends AbsXMLContext implements ARPErrorNumbers,
        LanguageTagCodes {
    // final private String base;

    /**
     * Creates new XMLContext
     * 
     * @throws SAXParseException
     */
    XMLContext(XMLHandler h, String base) throws SAXParseException {

        this(h, h.iriFactory().create(base));
    }

    protected XMLContext(XMLHandler h, IRI uri, Taint baseT) {
        super(!h.ignoring(IGN_XMLBASE_SIGNIFICANT), null, uri, baseT, "",
                new TaintImpl());
    }

    private XMLContext(XMLHandler h, IRI baseMaybeWithFrag)
            throws SAXParseException {
        this(h, baseMaybeWithFrag.create(""), baseMaybeWithFrag);
    }

    private XMLContext(XMLHandler h, IRI base,
            IRI baseMaybeWithFrag) throws SAXParseException {
        this(h, base, initTaint(h, baseMaybeWithFrag));
    }

    XMLContext(boolean b, AbsXMLContext document, IRI uri,
            Taint baseT, String lang, Taint langT) {
        super(b, document, uri, baseT, lang, langT);
    }

    @Override
    boolean keepDocument(XMLHandler forErrors) {
        return true;
    }

    boolean isSameAsDocument() {
        return this == document
                || (uri == null ? document.uri == null : uri
                        .equals(document.uri));
    }

    @Override
    AbsXMLContext clone(IRI u, Taint baseT, String lng,
            Taint langT) {
        return new XMLContext(true, document, u, baseT, lng, langT);
    }

    void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri,
            String resolvedURI) throws SAXParseException {

        if (document == null || relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.create(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }
    }

    @Override
    void checkBaseUse(XMLHandler forErrors, Taint taintMe, String relUri,
            IRI rslt) throws SAXParseException {
        if (document == null)
            return;

        String resolvedURI = rslt.toString();
        if (relUri.equals(resolvedURI))
            return;
        if (!isSameAsDocument()) {
            String other = document.uri.create(relUri).toString();
            if (!other.equals(resolvedURI)) {
                forErrors.warning(taintMe, IGN_XMLBASE_SIGNIFICANT,
                        "Use of attribute xml:base changes interpretation of relative URI: \""
                                + relUri + "\".");
            }
        }

    }

}
