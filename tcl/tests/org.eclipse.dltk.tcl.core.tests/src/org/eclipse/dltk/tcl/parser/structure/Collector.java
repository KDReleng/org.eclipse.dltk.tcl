/*******************************************************************************
 * Copyright (c) 2009 xored software, Inc.  
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.tcl.parser.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.compiler.ISourceElementRequestorExtension;
import org.eclipse.dltk.compiler.SourceElementRequestorAdaptor;
import org.eclipse.dltk.core.caching.StructureModelCollector;

public class Collector extends StructureModelCollector implements
		ISourceElementRequestorExtension {

	/**
	 * @param requestor
	 */
	public Collector() {
		super(new SourceElementRequestorAdaptor());
	}

	static class Tag {
		final int tag;
		final int offset;

		public Tag(int tag, int offset) {
			this.tag = tag;
			this.offset = offset;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Tag) {
				final Tag other = (Tag) obj;
				return tag == other.tag && offset == other.offset;
			}
			return false;
		}

		@Override
		public String toString() {
			return tag + "@" + offset; //$NON-NLS-1$
		}
	}

	final List<Tag> tags = new ArrayList<Tag>();

	@Override
	protected void writeTag(int tag) throws IOException {
		tags.add(new Tag(tag, out.size()));
		super.writeTag(tag);
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			saveDataTo(stream);
		} catch (IOException e) {
			// should not happen
		}
		return stream.toByteArray();
	}

	@Override
	public void acceptFieldReference(String fieldName, int sourcePosition) {
		// ignore
	}

	@Override
	public void acceptMethodReference(String methodName, int argCount,
			int sourcePosition, int sourceEndPosition) {
		// ignore
	}

	@Override
	public void acceptTypeReference(char[] typeName, int sourcePosition) {
		// ignore
	}

	@Override
	public void acceptTypeReference(char[][] typeName, int sourceStart,
			int sourceEnd) {
		// ignore
	}

	public int getMode() {
		return MODE_STRUCTURE;
	}

}
