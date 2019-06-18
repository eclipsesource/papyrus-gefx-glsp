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

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;

import com.eclipsesource.glsp.api.action.kind.AbstractOperationAction;
import com.eclipsesource.glsp.api.action.kind.CreateConnectionOperationAction;
import com.eclipsesource.glsp.api.handler.OperationHandler;
import com.eclipsesource.glsp.api.model.GraphicalModelState;
import com.eclipsesource.glsp.graph.GModelRoot;

public class CreateEdgeHandler implements OperationHandler {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public boolean handles(AbstractOperationAction action) {
		return action instanceof CreateConnectionOperationAction;
	}

	@Override
	public Optional<GModelRoot> execute(AbstractOperationAction action, GraphicalModelState modelState) {
		// TODO Auto-generated method stub
		
		String modelId = ModelUtil.getModelId(modelState);
		gefSynchronizer.refresh(modelId);
		return Optional.of(gefSynchronizer.getModel(modelId));
	}

}
