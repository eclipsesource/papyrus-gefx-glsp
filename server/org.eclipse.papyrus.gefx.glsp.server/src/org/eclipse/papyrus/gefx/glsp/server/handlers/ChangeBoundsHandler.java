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
package org.eclipse.papyrus.gefx.glsp.server.handlers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.Size;
import org.eclipse.papyrus.gef4.gmf.parts.NotationDiagramRootPart;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;

import com.eclipsesource.glsp.api.action.kind.AbstractOperationAction;
import com.eclipsesource.glsp.api.action.kind.ChangeBoundsOperationAction;
import com.eclipsesource.glsp.api.handler.OperationHandler;
import com.eclipsesource.glsp.api.model.GraphicalModelState;
import com.eclipsesource.glsp.api.types.ElementAndBounds;
import com.eclipsesource.glsp.graph.GBounds;
import com.eclipsesource.glsp.graph.GModelRoot;

public class ChangeBoundsHandler implements OperationHandler {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;

	@Override
	public Class<?> handlesActionType() {
		return ChangeBoundsOperationAction.class;
	}

	@Override
	public Optional<GModelRoot> execute(AbstractOperationAction action, GraphicalModelState modelState) {
		if (action instanceof ChangeBoundsOperationAction) {
			ChangeBoundsOperationAction cbAction = (ChangeBoundsOperationAction) action;
			String modelId = ModelUtil.getModelId(modelState);
			List<ElementAndBounds> newBounds = cbAction.getNewBounds();
			IViewer viewer = gefSynchronizer.getViewer(modelId);
			IRootPart<?> rootPart = viewer.getRootPart();
			if (rootPart instanceof NotationDiagramRootPart) {
				Diagram modelRoot = ((NotationDiagramRootPart) rootPart).getModelRoot();

				TransactionalEditingDomain editingDomain = (TransactionalEditingDomain) EMFHelper
						.resolveEditingDomain(modelRoot);
				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

					@Override
					protected void doExecute() {
						for (ElementAndBounds moveOperation : newBounds) {
							String elementId = moveOperation.getElementId();
							GBounds bounds = moveOperation.getNewBounds();
							EObject notationElement = modelRoot.eResource().getEObject(elementId);
							// IContentPart<?> contentPart =
							// viewer.getContentPartMap().get(notationElement);
							// MoveHandler moveHandler =contentPart.getAdapter(MoveHandler.class);
							if (notationElement instanceof Node) {
								Node node = (Node) notationElement;
								LayoutConstraint layoutConstraint = node.getLayoutConstraint();
								if (layoutConstraint instanceof Location) {
									((Location) layoutConstraint).setX((int) bounds.getX());
									((Location) layoutConstraint).setY((int) bounds.getY());
								}

								// FIXME Both GLSP/Sprotty and GEF rely on dynamic sizes, but with different
								// algorithms
								// Also, GLSP/Sprotty always sends a Size update on Move, which causes items to
								// be resized
								// on moved; that's not great. Ignore size changes for now.
								if (layoutConstraint instanceof Size) {
									((Size) layoutConstraint).setWidth((int)bounds.getWidth());
									((Size) layoutConstraint).setHeight((int)bounds.getHeight());
								}
							}
						}
					}
				});

			}
			gefSynchronizer.refresh(modelId);
			return Optional.of(gefSynchronizer.getModel(modelId));
		}
		return Optional.empty();
	}

}
