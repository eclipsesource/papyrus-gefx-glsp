package org.eclipse.papyrus.gefx.glsp.server.helper;

import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.sprotty.SModelElement;

public interface ModelBuilder {
	public SModelElement createElement(IVisualPart<?> gefPart);
	
	public void refreshElement(IVisualPart<?> gefPart, SModelElement modelElement);
}
