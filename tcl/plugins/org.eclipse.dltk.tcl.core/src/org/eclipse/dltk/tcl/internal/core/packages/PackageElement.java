package org.eclipse.dltk.tcl.internal.core.packages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.WorkingCopyOwner;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.dltk.internal.core.ExternalEntryFile;
import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.dltk.internal.core.MementoModelElementUtil;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.ModelElementInfo;
import org.eclipse.dltk.internal.core.Openable;
import org.eclipse.dltk.internal.core.OpenableElementInfo;
import org.eclipse.dltk.internal.core.ScriptFolderInfo;
import org.eclipse.dltk.internal.core.util.MementoTokenizer;
import org.eclipse.dltk.internal.core.util.Util;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.tcl.internal.core.packages.PackagesManager.PackageInformation;
import org.eclipse.dltk.utils.CorePrinter;

public class PackageElement extends Openable implements IScriptFolder {
	private String packageName;
	private String packageVersion;

	protected PackageElement(ModelElement parent, String name, String version) {
		super(parent);
		this.packageName = name;
		this.packageVersion = version;
	}

	public String getElementName() {
		return this.packageName;
	}

	protected Object createElementInfo() {
		return new OpenableElementInfo();
	}

	public int getElementType() {
		return SCRIPT_FOLDER;
	}

	public int getKind() throws ModelException {
		return IProjectFragment.K_SOURCE;
	}

	public IResource getResource() {
		return null;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PackageElement))
			return false;

		PackageElement other = (PackageElement) o;
		return this.packageName.equals(other.packageName);
	}

	public int hashCode() {
		return this.packageName.hashCode();
	}

	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm, Map newElements, IResource underlyingResource)
			throws ModelException {
		// check whether this folder can be opened
		IInterpreterInstall install = null;
		try {
			install = ScriptRuntime.getInterpreterInstall(getScriptProject());
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		if (install != null) {
			// add modules from resources
			HashSet vChildren = new HashSet();
			// Add external source module here.
			PackageInformation information = PackagesManager.getInstance()
					.getPacakgeInfo(this.packageName, install);
			Set sources = information.getSources();
			if (!sources.isEmpty()) {
				String[] paths = (String[]) sources.toArray(new String[sources
						.size()]);
				IEnvironment environment = EnvironmentManager
						.getEnvironment(getScriptProject());
				for (int i = 0; i < paths.length; i++) {
					IPath path = new Path(paths[i]);
					ExternalEntryFile storage = new ExternalEntryFile(
							EnvironmentPathUtils.getFile(environment, path));
					ExternalSourceModule module = new ExternalSourceModule(
							this, path.lastSegment(),
							DefaultWorkingCopyOwner.PRIMARY, storage) {
						public IPath getPath() {
							IEnvironment environment = EnvironmentManager
									.getEnvironment(this);
							return EnvironmentPathUtils.getFullPath(
									environment, getStorage().getFullPath());
						}

						public IPath getFullPath() {
							return getPath();
						}
					};
					vChildren.add(module);
				}
			}
			info.setChildren((IModelElement[]) vChildren
					.toArray(new IModelElement[vChildren.size()]));
		}
		return true;
	}

	protected char getHandleMementoDelimiter() {
		return JEM_USER_ELEMENT;
	}

	public IModelElement getHandleFromMemento(String token,
			MementoTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
		case JEM_SOURCEMODULE:
			if (!memento.hasMoreTokens())
				return this;
			String classFileName = memento.nextToken();
			ModelElement classFile = (ModelElement) getSourceModule(classFileName);
			return classFile.getHandleFromMemento(memento, owner);
		case JEM_USER_ELEMENT:
			return MementoModelElementUtil.getHandleFromMememento(memento,
					this, owner);
		}
		return null;
	}

	public boolean containsScriptResources() throws ModelException {
		Object elementInfo = getElementInfo();
		if (!(elementInfo instanceof ScriptFolderInfo))
			return false;
		ScriptFolderInfo scriptElementInfo = (ScriptFolderInfo) elementInfo;
		return scriptElementInfo.containsScriptResources();
	}

	public boolean hasChildren() throws ModelException {
		return getChildren().length > 0;
	}

	public void printNode(CorePrinter output) {
		output.formatPrint("DLTK TCL Package:" + getElementName()); //$NON-NLS-1$
		output.indent();
		try {
			IModelElement modelElements[] = this.getChildren();
			for (int i = 0; i < modelElements.length; ++i) {
				IModelElement element = modelElements[i];
				if (element instanceof ModelElement) {
					((ModelElement) element).printNode(output);
				} else {
					output.print("Unknown element:" + element); //$NON-NLS-1$
				}
			}
		} catch (ModelException ex) {
			output.formatPrint(ex.getLocalizedMessage());
		}
		output.dedent();
	}

	public ISourceModule createSourceModule(String name, String contents,
			boolean force, IProgressMonitor monitor) throws ModelException {
		return null;
	}

	public Object[] getForeignResources() throws ModelException {
		return ModelElementInfo.NO_NON_SCRIPT_RESOURCES;
	}

	public boolean exists() {
		return true;
	}

	public ISourceModule[] getSourceModules() throws ModelException {
		ArrayList list = getChildrenOfType(SOURCE_MODULE);
		ISourceModule[] array = new ISourceModule[list.size()];
		list.toArray(array);
		return array;
	}

	public boolean hasSubfolders() throws ModelException {
		return false;
	}

	public boolean isRootFolder() {
		return false;
	}

	public IPath getPath() {
		return getParent().getPath().append(packageName);
	}

	public void copy(IModelElement container, IModelElement sibling,
			String rename, boolean replace, IProgressMonitor monitor)
			throws ModelException {
	}

	public void delete(boolean force, IProgressMonitor monitor)
			throws ModelException {
	}

	public void move(IModelElement container, IModelElement sibling,
			String rename, boolean replace, IProgressMonitor monitor)
			throws ModelException {
	}

	public void rename(String name, boolean replace, IProgressMonitor monitor)
			throws ModelException {
	}

	public ISourceModule getSourceModule(String name) {
		IModelElement[] children = null;
		try {
			children = getChildren();
			for (int i = 0; i < children.length; i++) {
				IModelElement child = children[i];
				if (child instanceof IScriptFolder) {
					if (name.equals(child.getElementName())) {
						return (ISourceModule) child;
					}
				}
			}
		} catch (ModelException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getVersion() {
		return this.packageVersion;
	}
}
