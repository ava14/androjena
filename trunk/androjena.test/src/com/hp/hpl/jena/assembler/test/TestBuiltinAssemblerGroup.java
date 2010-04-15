/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestBuiltinAssemblerGroup.java,v 1.1 2009/06/29 08:55:53 castagna Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestBuiltinAssemblerGroup extends AssemblerTestBase
    {
    public TestBuiltinAssemblerGroup( String name )
        { super( name ); }

    @Override protected Class<? extends Assembler> getAssemblerClass()
        { return null; }

    public void testGeneralRegistration()
        {
        assertAssemblerClass( JA.DefaultModel, DefaultModelAssembler.class );
        assertAssemblerClass( JA.PrefixMapping, PrefixMappingAssembler.class );      
        assertAssemblerClass( JA.SinglePrefixMapping, PrefixMappingAssembler.class );      
        assertAssemblerClass( JA.FileModel, FileModelAssembler.class );       
        assertAssemblerClass( JA.OntModel, OntModelAssembler.class );       
        assertAssemblerClass( JA.OntModelSpec, OntModelSpecAssembler.class );     
        assertAssemblerClass( JA.RDBModel, RDBModelAssembler.class );       
        assertAssemblerClass( JA.Connection, ConnectionAssembler.class );       
        assertAssemblerClass( JA.Content, ContentAssembler.class );         
        assertAssemblerClass( JA.ContentItem, ContentAssembler.class );       
        assertAssemblerClass( JA.ReasonerFactory, ReasonerFactoryAssembler.class );  
        assertAssemblerClass( JA.InfModel, InfModelAssembler.class );       
        assertAssemblerClass( JA.MemoryModel, MemoryModelAssembler.class );       
        assertAssemblerClass( JA.RuleSet, RuleSetAssembler.class );
        assertAssemblerClass( JA.LocationMapper, LocationMapperAssembler.class );
        assertAssemblerClass( JA.FileManager, FileManagerAssembler.class );
        assertAssemblerClass( JA.DocumentManager, DocumentManagerAssembler.class );
        assertAssemblerClass( JA.UnionModel, UnionModelAssembler.class );
        assertAssemblerClass( JA.ModelSource, ModelSourceAssembler.class );
        assertAssemblerClass( JA.RDBModelSource, ModelSourceAssembler.class );
        }
    
    public void testVariables()
        {
        assertInstanceOf( DefaultModelAssembler.class, Assembler.defaultModel );
        assertInstanceOf( PrefixMappingAssembler.class, Assembler.prefixMapping );
        assertInstanceOf( FileModelAssembler.class, Assembler.fileModel );
        assertInstanceOf( OntModelAssembler.class, Assembler.ontModel );
        assertInstanceOf( OntModelSpecAssembler.class, Assembler.ontModelSpec );
        assertInstanceOf( RDBModelAssembler.class, Assembler.rdbModel );
        assertInstanceOf( ConnectionAssembler.class, Assembler.connection );
        assertInstanceOf( ContentAssembler.class, Assembler.content );
        assertInstanceOf( ReasonerFactoryAssembler.class, Assembler.reasonerFactory );
        assertInstanceOf( InfModelAssembler.class, Assembler.infModel );
        assertInstanceOf( MemoryModelAssembler.class, Assembler.memoryModel );
        assertInstanceOf( RuleSetAssembler.class, Assembler.ruleSet );
        assertInstanceOf( LocationMapperAssembler.class, Assembler.locationMapper );
        assertInstanceOf( FileManagerAssembler.class, Assembler.fileManager );
        assertInstanceOf( DocumentManagerAssembler.class, Assembler.documentManager );
        assertInstanceOf( UnionModelAssembler.class, Assembler.unionModel );
        }
    
    public void testRecognisesAndAssemblesSinglePrefixMapping()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create().setNsPrefix( "P", "spoo:/" );
        Resource r = resourceInModel( "x ja:prefix 'P'; x ja:namespace 'spoo:/'" );
        assertEquals( wanted, Assembler.general.open( r ) );
        }
    
    public void testRecognisesAndAssemblesMultiplePrefixMappings()
        {
        PrefixMapping wanted = PrefixMapping.Factory.create()
            .setNsPrefix( "P", "spoo:/" ).setNsPrefix( "Q", "flarn:/" );
        Resource r = resourceInModel
            ( "x ja:includes y; x ja:includes z; y ja:prefix 'P'; y ja:namespace 'spoo:/'; z ja:prefix 'Q'; z ja:namespace 'flarn:/'" );
        assertEquals( wanted, Assembler.general.open( r ) );
        }

    public static void assertEquals( PrefixMapping wanted, Object got )
        {
        if (got instanceof PrefixMapping && wanted.samePrefixMappingAs( (PrefixMapping) got ))
            pass();
        else
            fail( "expected " + wanted + " but was: " + got );
        }
    
    private void assertAssemblerClass( Resource type, Class<?> C )
        {
        assertInstanceOf( C, Assembler.general.assemblerFor( type ) );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
*/