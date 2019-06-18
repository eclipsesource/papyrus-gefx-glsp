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
import java.util.Arrays;
import java.util.List;

import com.eclipsesource.glsp.api.diagram.DiagramConfiguration;
import com.eclipsesource.glsp.api.operations.Operation;
import com.eclipsesource.glsp.api.types.EdgeTypeHint;
import com.eclipsesource.glsp.api.types.NodeTypeHint;

public class GEFxDiagramConfiguration implements DiagramConfiguration {
	
	/**
	 * The GEFx-GLSP Integration Diagram type
	 * 
	 * This must be the same value as the client's gefx-language.ts#DiagramType
	 */
	public static final String DIAGRAM_TYPE = "gefx-diagram"; 

	@Override
	public List<EdgeTypeHint> getEdgeTypeHints() {
		List<EdgeTypeHint> edgeHints = new ArrayList<>();
		edgeHints.add(new EdgeTypeHint(GEFxTypes.EDGE, true, true, true, Arrays.asList(GEFxTypes.NODE, GEFxTypes.COMP), Arrays.asList(GEFxTypes.NODE, GEFxTypes.COMP)));
		return edgeHints;
	}

	@Override
	public List<NodeTypeHint> getNodeTypeHints() {
		ArrayList<NodeTypeHint> nodeHints = new ArrayList<>();
		nodeHints.add(new NodeTypeHint(GEFxTypes.NODE, true, true, true, Arrays.asList(GEFxTypes.NODE, GEFxTypes.COMP)));
		nodeHints.add(new NodeTypeHint(GEFxTypes.COMP, false, false, false, Arrays.asList(GEFxTypes.NODE)));
		nodeHints.add(new NodeTypeHint(GEFxTypes.LABEL, true, false, false, Arrays.asList(GEFxTypes.NODE)));
		return nodeHints;
	}

	@Override
	public String getDiagramType() {
		return DIAGRAM_TYPE; // Single DiagramManager proxy for generic GEFx Diagrams
	}

	@Override
	public List<Operation> getOperations() {
		throw new UnsupportedOperationException("This method is not supported for the GEFx GLSP Server. Use "+RequestPaletteOperationsHandler.class.getName()+" instead");
	}


}
