/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *

 *******************************************************************************/
package org.eclipse.dltk.tcl.ast.expressions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.tcl.ast.TclConstants;
import org.eclipse.dltk.tcl.core.ITclSourceParser;
import org.eclipse.dltk.tcl.core.TclNature;
import org.eclipse.dltk.utils.CorePrinter;

public class TclBlockExpression extends Expression {
	private String fBlockContent;
	private char[] fileName = null;

	public TclBlockExpression(int start, int end, String content) {
		this.setStart(start);
		this.setEnd(end);
		this.fBlockContent = content;
	}

	public int getKind() {
		return TclConstants.TCL_BLOCK_EXPRESSION;
	}

	public void traverse(ASTVisitor visitor) throws Exception {
		if (visitor.visit(this)) {
			// List statements = this.parseBlock();
			// if (statements != null) {
			// for (int i = 0; i < statements.size(); i++) {
			// ASTNode node = (ASTNode) statements.get(i);
			// node.traverse(visitor);
			// }
			// }
			visitor.endvisit(this);
		}
	}

	public void printNode(CorePrinter output) {
		output.formatPrintLn("tcl block:" + this.fBlockContent);
	}

	public String toString() {
		return "tcl block:" + this.fBlockContent;
	}

	public String getBlock() {
		return this.fBlockContent;
	}

	public void setFilename(char[] fileName) {
		this.fileName = fileName;
	}

	public List parseBlockSimple() {
		try {
			if (this.fBlockContent == null) {
				return null;
			}
			
			String content = this.fBlockContent.substring(1, this.fBlockContent
					.length() - 1);
			ITclSourceParser parser = null;
			parser = (ITclSourceParser) DLTKLanguageManager
					.getSourceParser(TclNature.NATURE_ID);
			parser.setOffset(this.sourceStart() + 1);
			ModuleDeclaration module = parser.parse(this.fileName, content
					.toCharArray(), null);
			return module.getStatements();
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
