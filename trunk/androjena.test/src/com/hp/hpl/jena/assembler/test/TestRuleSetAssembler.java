/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestRuleSetAssembler.java,v 1.1 2009/06/29 08:55:53 castagna Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.RuleSetAssembler;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class TestRuleSetAssembler extends AssemblerTestBase
    {
    public TestRuleSetAssembler( String name )
        { super( name ); }

    @Override
    protected Class<? extends Assembler> getAssemblerClass()
        { return RuleSetAssembler.class; }
    
    public void testRuleSetVocabulary()
        {
        assertSubclassOf( JA.RuleSet, JA.HasRules );
        assertDomain( JA.HasRules, JA.rule );
        assertDomain( JA.HasRules, JA.rulesFrom );
        assertDomain( JA.HasRules, JA.rules );
        assertRange( JA.RuleSet, JA.rules );
        }

    public void testRuleSetAssemblerType()
        { testDemandsMinimalType( new RuleSetAssembler(), JA.RuleSet );  }
    
    public void testEmptyRuleSet() 
        { 
        Assembler a = new RuleSetAssembler();
        Resource root = resourceInModel( "x rdf:type ja:RuleSet" );
        assertEquals( RuleSet.empty, a.open( root ) );
        }
    
    public void testSingleRuleString()
        {
        Assembler a = new RuleSetAssembler();
        String ruleString = "[(?a P ?b) -> (?a Q ?b)]";
        Resource root = resourceInModel( "x rdf:type ja:RuleSet; x ja:rule '" + ruleString.replaceAll( " ", "\\\\s" ) + "'" );
        RuleSet rules = (RuleSet) a.open( root );
        Set<Rule> expected = new HashSet<Rule>( Rule.parseRules( ruleString ) );
        assertEquals( expected, new HashSet<Rule>( rules.getRules() ) );
        }
    
    public void testMultipleRuleStrings()
        {
        Assembler a = new RuleSetAssembler();
        String ruleStringA = "[(?a P ?b) -> (?a Q ?b)]";
        String ruleStringB = "[(?a R ?b) -> (?a S ?b)]";
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet"
            + "; x ja:rule '" + ruleStringA.replaceAll( " ", "\\\\s" ) + "'" 
            + "; x ja:rule '" + ruleStringB.replaceAll( " ", "\\\\s" ) + "'" 
            );
        RuleSet rules = (RuleSet) a.open( root );
        Set<Rule> expected = new HashSet<Rule>( Rule.parseRules( ruleStringA ) );
        expected.addAll( Rule.parseRules( ruleStringB ) );
        assertEquals( expected, new HashSet<Rule>( rules.getRules() ) );
        }
    
    public void testRulesFrom()
        {
        Assembler a = new RuleSetAssembler();
        String rulesA = file( "example.rules" );
        Resource root = resourceInModel( "x rdf:type ja:RuleSet; x ja:rulesFrom " + rulesA );
        Set<Rule> expected = new HashSet<Rule>( Rule.rulesFromURL( rulesA ) );
        RuleSet rules = (RuleSet) a.open( root );
        assertEquals( expected, new HashSet<Rule>( rules.getRules() ) );
        }
    
    public void testSubRules()
        {
        Assembler a = new RuleSetAssembler();
        String ruleStringA = "[(?a P ?b) -> (?a Q ?b)]";
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet; x ja:rules y"
            + "; y rdf:type ja:RuleSet; y ja:rule '" + ruleStringA.replaceAll( " ", "\\\\s" ) + "'" );
        Set<Rule> expected = new HashSet<Rule>( Rule.parseRules( ruleStringA ) );
        RuleSet rules = (RuleSet) a.open( root );
        assertEquals( expected, new HashSet<Rule>( rules.getRules() ) );
        }
    
    public void testTrapsBadRulesObject()
        {
        testTrapsBadRuleObject( "ja:rules", "'y'" );
        testTrapsBadRuleObject( "ja:rulesFrom", "17" );
        testTrapsBadRuleObject( "ja:rule", "aResource" );
        testTrapsBadRuleObject( "ja:rule", "17" );
        testTrapsBadRuleObject( "ja:rule", "'something'xsd:else" );
        }

    private void testTrapsBadRuleObject( String property, String value )
        {
        Assembler a = new RuleSetAssembler();
        Resource root = resourceInModel
            ( "x rdf:type ja:RuleSet; x <property> <value>"
              .replaceAll( "<property>", property ).replaceAll( "<value>", value ) 
            );
        try 
            {
            a.open( root );
            fail( "should trap bad rules object " + value + " for property " + property );
            }
        catch (BadObjectException e) 
            { 
            Model m = e.getRoot().getModel();
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( m, value ), e.getObject() );
            }
        }

    protected static String file( String name )
        { return "file:testing/modelspecs/" + name; }
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