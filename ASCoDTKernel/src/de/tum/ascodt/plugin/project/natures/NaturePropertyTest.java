package de.tum.ascodt.plugin.project.natures;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class NaturePropertyTest extends PropertyTester {

	public NaturePropertyTest() {

	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		IResource res = (IResource) receiver;


		IProject project=res.getProject();
		try {
			if(project.isOpen()&&project.hasNature(expectedValue.toString()))
				return true;
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return false;
	}

}
