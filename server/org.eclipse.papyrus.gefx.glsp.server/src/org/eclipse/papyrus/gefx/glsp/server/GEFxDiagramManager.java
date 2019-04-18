package org.eclipse.papyrus.gefx.glsp.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SNode;

import com.eclipsesource.glsp.api.types.EdgeTypeHint;
import com.eclipsesource.glsp.api.types.NodeTypeHint;
import com.eclipsesource.glsp.server.AbstractDiagramManager;

public class GEFxDiagramManager extends AbstractDiagramManager {
	
	/**
	 * The GEFx-GLSP Integration Diagram type
	 * 
	 * This must be the same value as the client's gefx-language.ts#DiagramType
	 */
	public static final String DIAGRAM_TYPE = "gefx-diagram"; 

	@Override
	public Map<String, Class<? extends SModelElement>> getTypeMappings() {
		HashMap<String, Class<? extends SModelElement>> types = new HashMap<>();
		
		types.put("node", SNode.class);
		types.put("edge", SEdge.class);
		types.put("comp", SCompartment.class);
		types.put("label", SLabel.class);
		
		return types;
	}
	
	@Override
	public List<EdgeTypeHint> getEdgeTypeHints() {
		return new ArrayList<>();
	}

	@Override
	public List<NodeTypeHint> getNodeTypeHints() {
		ArrayList<NodeTypeHint> nodeHints = new ArrayList<>();
		nodeHints.add(new NodeTypeHint("node", true, true, true, Arrays.asList("node", "comp")));
		nodeHints.add(new NodeTypeHint("comp", false, false, false));
		nodeHints.add(new NodeTypeHint("label", true, false, false));
		return nodeHints;
	}

	@Override
	public String getDiagramType() {
		return DIAGRAM_TYPE; // Single DiagramManager proxy for generic GEFx Diagrams
	}


}
