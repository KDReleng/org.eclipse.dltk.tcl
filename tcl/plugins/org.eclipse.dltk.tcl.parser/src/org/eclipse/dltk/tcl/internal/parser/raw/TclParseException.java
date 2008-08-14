/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.  
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.parser.raw;

import java.text.ParseException;

public class TclParseException extends ParseException {

	public TclParseException(String s, int errorOffset) {
		super(s, errorOffset);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
