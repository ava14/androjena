/*	This code is mainly adapated from Xerces 2.6.0 and Jena 2.6.2 
 * Xerces copyright and license: 
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights reserved.
 * License http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Jena copyright and license:
 * Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 * 
 * Specific source classes:
 * 
 * Xerces:
 * org.apache.xerces.impl.dv.xs.AbstractDateTimeDV
 * org.apache.xerces.impl.dv.xs.DayDV
 * 
 * Jena:
 * com.hp.hpl.jena.datatypes.xsd.XSDDatatype
 * com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType
 * com.hp.hpl.jena.datatypes.xsd.impl.XSDDayType
 */

package it.polimi.dei.dbgroup.pedigree.androjena.xsd.impl.validators;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

public class DayValidator extends BaseDateTimeValidator {

	// size without time zone: ---09
	private final static int DAY_SIZE = 5;

	@Override
	protected AbstractDateTime parse(String str) {
		int len = str.length();
		int[] date = new int[TOTAL_SIZE];
		int[] timeZone = new int[2];

		// initialize values
		date[CY] = YEAR;
		date[M] = MONTH;

		date[D] = parseInt(str, 3, 5);

		if (DAY_SIZE < len) {
			if (!isNextCharUTCSign(str, DAY_SIZE, len)) {
				throw new RuntimeException("Error in day parsing");
			} else {
				int sign = findUTCSign(str, DAY_SIZE, len);
				getTimeZone(str, date, sign, len, timeZone);
			}

		}

		// validate and normalize
		validateDateTime(date, timeZone);

		if (date[utc] != 0 && date[utc] != 'Z') {
			AbstractDateTime.normalize(date, timeZone);
		}

		return new XSDDateTime(date, DAY_MASK);
	}

}
