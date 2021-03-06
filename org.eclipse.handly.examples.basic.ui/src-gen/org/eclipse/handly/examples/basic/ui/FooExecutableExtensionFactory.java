/*
 * generated by Xtext
 */
package org.eclipse.handly.examples.basic.ui;

import com.google.inject.Injector;
import org.eclipse.handly.examples.basic.ui.internal.BasicActivator;
import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class FooExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return BasicActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return BasicActivator.getInstance().getInjector(BasicActivator.ORG_ECLIPSE_HANDLY_EXAMPLES_BASIC_FOO);
	}
	
}
