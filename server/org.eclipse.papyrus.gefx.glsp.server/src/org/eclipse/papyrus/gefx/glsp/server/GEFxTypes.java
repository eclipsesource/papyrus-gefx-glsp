package org.eclipse.papyrus.gefx.glsp.server;

import static com.eclipsesource.glsp.graph.DefaultTypes.GRAPH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;

import com.eclipsesource.glsp.graph.GraphPackage;
import com.google.common.collect.ImmutableMap;

public final class GEFxTypes {

	public static final String ROOT = "root";
	public static final String LABEL = "label";
	public static final String COMP = "comp";
	public static final String EDGE = "edge";
	public static final String NODE = "node";
	public static final Map<String, EClass> TYPE_MAP = getTypeMappings();
	
	private static Map<String, EClass> getTypeMappings() {
		HashMap<String, EClass> types = new HashMap<>();
		
		types.put(GRAPH, GraphPackage.Literals.GGRAPH);
		types.put(NODE, GraphPackage.Literals.GNODE);
		types.put(EDGE, GraphPackage.Literals.GEDGE);
		types.put(COMP, GraphPackage.Literals.GCOMPARTMENT);
		types.put(LABEL, GraphPackage.Literals.GLABEL);
		types.put(ROOT, GraphPackage.Literals.GMODEL_ROOT);
		
		return ImmutableMap.copyOf(types);
	}
	
}
