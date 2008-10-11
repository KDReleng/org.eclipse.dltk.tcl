/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.validators.packages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dltk.tcl.ast.StringArgument;
import org.eclipse.dltk.tcl.ast.TclArgument;
import org.eclipse.dltk.tcl.ast.TclCommand;
import org.eclipse.dltk.tcl.definitions.Command;
import org.eclipse.dltk.tcl.parser.TclParserUtils;
import org.eclipse.dltk.tcl.parser.TclVisitor;
import org.eclipse.dltk.tcl.validators.TclValidatorsCore;
import org.eclipse.emf.common.util.EList;

public class PackageCollector extends TclVisitor {

	static final String PACKAGE = "package"; //$NON-NLS-1$
	static final String REQUIRE = "require"; //$NON-NLS-1$
	static final String PROVIDE = "provide"; //$NON-NLS-1$
	static final String IFNEEDED = "ifneeded"; //$NON-NLS-1$
	static final String EXACT = "-exact"; //$NON-NLS-1$

	private final List<PackageRequireRef> requireRefs = new ArrayList<PackageRequireRef>();

	private final Set<String> requirePackages = new HashSet<String>();

	private final Set<String> packagesProvided = new HashSet<String>();

	public void process(List<TclCommand> declaration) {
		try {
			TclParserUtils.traverse(declaration, this);
		} catch (Exception e) {
			TclValidatorsCore.error(e);
		}
	}

	public boolean visit(TclCommand command) {
		Command definition = command.getDefinition();
		if (definition != null && PACKAGE.equals(definition.getName())) {
			processPackageCommand(command);
		}
		return super.visit(command);
	}

	private StringArgument getStringArg(EList<TclArgument> args, int index) {
		if (index < args.size()) {
			final TclArgument argument = args.get(index);
			if (argument instanceof StringArgument) {
				return (StringArgument) argument;
			}
		}
		return null;
	}

	private void processPackageCommand(TclCommand command) {
		EList<TclArgument> args = command.getArguments();
		StringArgument style = getStringArg(args, 0);
		if (style == null) {
			return;
		}
		final String keyword = style.getValue();
		if (REQUIRE.equalsIgnoreCase(keyword)) {
			StringArgument pkgName = getStringArg(args, 1);
			if (pkgName == null) {
				return;
			}
			String packageName = pkgName.getValue();
			if (EXACT.equals(packageName)) {
				pkgName = getStringArg(args, 2);
				if (pkgName == null) {
					return;
				}
				packageName = pkgName.getValue();
			}
			if (isValidPackageName(packageName)) {
				requireRefs.add(new PackageRequireRef(packageName, command
						.getStart(), command.getEnd()));
				requirePackages.add(packageName);
			}
		} else if (IFNEEDED.equalsIgnoreCase(keyword)
				|| PROVIDE.equalsIgnoreCase(keyword)) {
			StringArgument pkgName = getStringArg(args, 1);
			if (pkgName == null) {
				return;
			}
			String pkg = pkgName.getValue();
			if (isValidPackageName(pkg)) {
				packagesProvided.add(pkg);
			}
		}
	}

	/**
	 * @return the packagesRequired
	 */
	public List<PackageRequireRef> getRequireRefs() {
		return requireRefs;
	}

	/**
	 * @return the requirePackages
	 */
	public Set<String> getRequirePackages() {
		return requirePackages;
	}

	/**
	 * @return the packagesProvided
	 */
	public Set<String> getPackagesProvided() {
		return packagesProvided;
	}

	private static boolean isValidPackageName(String packageName) {
		return packageName != null && packageName.length() != 0
				&& packageName.indexOf('$') == -1
				&& packageName.indexOf('[') == -1
				&& packageName.indexOf(']') == -1;
	}
}