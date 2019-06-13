/*******************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *  
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *  
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *  
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
package org.eclipse.papyrus.gefx.glsp.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.papyrus.gef4.palette.PaletteDescriptor;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.ChildElement;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.Drawer;
import org.eclipse.papyrus.gef4.palette.declarative.IdCreationTool;
import org.eclipse.papyrus.gef4.palette.declarative.IdCreationTool.CreationKind;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;

import com.eclipsesource.glsp.api.action.Action;
import com.eclipsesource.glsp.api.action.kind.RequestOperationsAction;
import com.eclipsesource.glsp.api.action.kind.SetOperationsAction;
import com.eclipsesource.glsp.api.model.GraphicalModelState;
import com.eclipsesource.glsp.api.operations.Operation;
import com.eclipsesource.glsp.server.actionhandler.AbstractActionHandler;

/**
 * GEFx-GLSP Handler for {@link RequestOperationsAction}
 * 
 * Return a list of GLSP {@link Operation}s from the GEFx {@link PaletteDescriptor}.
 * 
 * @see RequestOperationsAction
 * @see SetOperationsAction
 */
// We can't use the default RequestOperationsActionHandler because it relies on the 
// parameter-less DiagramConfiguration#getOperations.
// Our palettes depend on the current model state
public class RequestPaletteOperationsHandler extends AbstractActionHandler {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public boolean handles(Action action) {
		return action instanceof RequestOperationsAction;
	}
	
	@Override
	protected Optional<Action> execute(Action action, GraphicalModelState modelState) {
		List<Operation> paletteOperations = new ArrayList<>();

		PaletteDescriptor paletteDescriptor = gefSynchronizer.getViewer(ModelUtil.getModelId(modelState))
				.getAdapter(PaletteDescriptor.class);
		List<Drawer> drawers = paletteDescriptor.getDrawers();

		for (Drawer drawer : drawers) {
			for (ChildElement child : drawer.getChildren()) {
				// XXX The current GEF Palette is not (really) declarative, so we have to cheat a little
				// For now we add a declarative descriptor interface (IdCreationTool) to the imperative tools
				if (child instanceof IdCreationTool) {
					IdCreationTool paletteTool = (IdCreationTool) child;
					Collection<String> types = ((IdCreationTool)paletteTool).getElementTypeIds();
					if (types.isEmpty()) {
						continue;
					}
					
					String type = types.iterator().next(); // Take any type; we'll retrieve the associated types at runtime if we receive a create action
					if (paletteTool.getCreationKind() == CreationKind.CREATE_NODE) {
						paletteOperations.add(createAction(paletteTool.getName().get().toString(), type, Operation.Kind.CREATE_NODE));
					} else if (paletteTool.getCreationKind() == CreationKind.CREATE_EDGE) {
						paletteOperations.add(createAction(paletteTool.getName().get().toString(), type, Operation.Kind.CREATE_CONNECTION));
					}
				}
			}
		}

		return Optional.of(new SetOperationsAction(paletteOperations));
	}

	private Operation createAction(String label, String elementType, String kind) {
		return new Operation(label, elementType, kind);
	}

}
