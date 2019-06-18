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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.fx.nodes.Connection;
import org.eclipse.gef.mvc.fx.parts.IBendableContentPart.BendPoint;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.gef4.parts.AbstractLabelContentPart;
import org.eclipse.papyrus.gef4.parts.BaseContentPart;
import org.eclipse.papyrus.gef4.parts.CompartmentContentPart;
import org.eclipse.papyrus.gef4.parts.ConnectionContentPart;
import org.eclipse.papyrus.gef4.parts.LabelContentPart;
import org.eclipse.papyrus.gef4.parts.ListCompartmentContentPart;
import org.eclipse.papyrus.gef4.parts.NodeContentPart;
import org.eclipse.papyrus.gef4.services.LabelService;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelBuilder;

import com.eclipsesource.glsp.graph.GCompartment;
import com.eclipsesource.glsp.graph.GDimension;
import com.eclipsesource.glsp.graph.GEdge;
import com.eclipsesource.glsp.graph.GLabel;
import com.eclipsesource.glsp.graph.GLayoutOptions;
import com.eclipsesource.glsp.graph.GModelElement;
import com.eclipsesource.glsp.graph.GNode;
import com.eclipsesource.glsp.graph.GPoint;
import com.eclipsesource.glsp.graph.GraphFactory;
import com.eclipsesource.glsp.server.util.GModelUtil;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class GEFModelBuilder implements ModelBuilder {

	@Override
	public GModelElement createElement(IVisualPart<?> gefPart) {
		GModelElement result = doCreateElement(gefPart);

		if (result == null) {
			System.err.println("UNKNOWN PART: " + gefPart + ". Fallback to simple GNode");
			result = GraphFactory.eINSTANCE.createGNode();
		}
		
		String id = getId(gefPart);
		if (id != null) {
			result.setId(id);
		} else {
			System.err.println("NO ID: "+gefPart);
		}

		return result;
	}

	@Override
	public void refreshElement(IVisualPart<?> gefPart, GModelElement modelElement) {
		String id = getId(gefPart);
		if (id != null) {
			modelElement.setId(id);
		}

		if (modelElement instanceof GNode) {
			refreshNode((GNode) modelElement, gefPart);
		}

		if (modelElement instanceof GLabel) {
			refreshLabel((GLabel) modelElement, gefPart);
		}

		if (modelElement instanceof GEdge) {
			refreshConnection((GEdge) modelElement, gefPart);
		}

		if (modelElement instanceof GCompartment) {
			refreshCompartment((GCompartment) modelElement, gefPart);
		}

//		setType(modelElement, gefPart);
	}

	private void refreshCompartment(GCompartment sCompartment, IVisualPart<?> gefPart) {
		if (gefPart instanceof CompartmentContentPart) {
			GLayoutOptions layoutOptions = GraphFactory.eINSTANCE.createGLayoutOptions();
			if (gefPart instanceof ListCompartmentContentPart) {
				sCompartment.setLayout("vbox");
				layoutOptions.setHAlign("left");
			}
			layoutOptions.setResizeContainer(true);
			if (gefPart.getVisual() instanceof Region) {
				Region region = (Region) gefPart.getVisual();
				Insets padding = region.getPadding();
				if (padding != null) {
					setPaddingIfSet(layoutOptions::setPaddingLeft, padding::getLeft);
					setPaddingIfSet(layoutOptions::setPaddingTop, padding::getTop);
					setPaddingIfSet(layoutOptions::setPaddingRight, padding::getRight);
					setPaddingIfSet(layoutOptions::setPaddingBottom, padding::getBottom);
				}
			}
			layoutOptions.setVGap(1.);
			sCompartment.setLayoutOptions(layoutOptions);
		}
	}

	private void setPaddingIfSet(Consumer<Double> setter, Supplier<Double> getter) {
		Double value = getter.get();
		if (value > 0) {
			setter.accept(value);
		}
	}

	private String getId(IVisualPart<?> gefPart) {
		if (gefPart instanceof IContentPart) {
			Object content = ((IContentPart<?>) gefPart).getContent();
			if (content instanceof EObject) {
				return EcoreUtil.getURI((EObject) content).fragment();
			}
		}
		return null;
	}

	private void refreshNode(GNode modelElement, IVisualPart<?> gefPart) {
		Node visual = gefPart.getVisual();
		if (visual == null || visual.getParent() == null) {
			return;
		}

		modelElement.setLayout("vbox");
		GLayoutOptions layout = GraphFactory.eINSTANCE.createGLayoutOptions();
		// layout.setVGap(1.);
		layout.setResizeContainer(false);
		modelElement.setLayoutOptions(layout);

		Bounds boundsInParent = visual.getBoundsInParent();
		modelElement.setPosition(GModelUtil.point(boundsInParent.getMinX(), boundsInParent.getMinY()));

		GDimension dimension = GraphFactory.eINSTANCE.createGDimension();
		dimension.setWidth(boundsInParent.getWidth());
		dimension.setHeight(boundsInParent.getHeight());
		modelElement.setSize(dimension);
	}

	private GModelElement doCreateElement(IVisualPart<?> gefPart) {
		if (gefPart instanceof CompartmentContentPart) {
			return GraphFactory.eINSTANCE.createGCompartment();
		}
		if (gefPart instanceof LabelContentPart) {
			return GraphFactory.eINSTANCE.createGLabel();
		}
		if (gefPart instanceof ConnectionContentPart) {
			return GraphFactory.eINSTANCE.createGEdge();
		}
		if (gefPart instanceof NodeContentPart) {
			if (((NodeContentPart<?>) gefPart).getLocator() != null) {
				return GraphFactory.eINSTANCE.createGPort();
			}
			return GraphFactory.eINSTANCE.createGNode();
		}

		return null;
	}

	private void refreshConnection(GEdge modelElement, IVisualPart<?> gefPart) {
		if (gefPart instanceof ConnectionContentPart) {
			ConnectionContentPart<?> connection = (ConnectionContentPart<?>) gefPart;
			// Source & Target, Anchors
			Connection gefVisual = connection.getConnection();

			Node sourceNode = gefVisual.getStartAnchor().getAnchorage();
			Node targetNode = gefVisual.getEndAnchor().getAnchorage();

			IViewer viewer = gefPart.getViewer();
			IVisualPart<?> sourcePart = viewer.getVisualPartMap().get(sourceNode);
			modelElement.setSourceId(getId(sourcePart));
			IVisualPart<?> targetPart = viewer.getVisualPartMap().get(targetNode);
			modelElement.setTargetId(getId(targetPart));

			modelElement.getRoutingPoints().clear(); // TODO Verify
			modelElement.getRoutingPoints().addAll(geometryToGraph(connection.getContentBendPoints()));
		}
	}

	private List<GPoint> geometryToGraph(List<BendPoint> contentBendPoints) {
		List<GPoint> result = new ArrayList<>();
		for (BendPoint bendpoint : contentBendPoints) {
			result.add(geometryToGraph(bendpoint.getPosition()));
		}
		return result;
	}

	private GPoint geometryToGraph(org.eclipse.gef.geometry.planar.Point startPoint) {
		return GModelUtil.point(startPoint.x(), startPoint.y());
	}

	private void refreshLabel(GLabel modelElement, IVisualPart<?> gefPart) {
		setLabel(modelElement, gefPart);
		if (gefPart.getParent() instanceof ConnectionContentPart) {
			refreshConnectionLabel(modelElement, gefPart);
		}
	}

	private void refreshConnectionLabel(GLabel modelElement, IVisualPart<?> labelPart) {
		Node visual = labelPart.getVisual();
		Bounds boundsInParent = visual.getBoundsInParent();
		modelElement.setPosition(GModelUtil.point(boundsInParent.getMinX(), boundsInParent.getMinY()));

		GDimension dimension = GraphFactory.eINSTANCE.createGDimension();
		dimension.setWidth(boundsInParent.getWidth());
		dimension.setHeight(boundsInParent.getHeight());
		modelElement.setSize(dimension);
		// modelElement.setEdgePlacement(EdgePlacement.);
	}

	// Unused for now; because the client only supports generic types (Node, Edge,
	// Label, Comp).
	// We should probably add a new notationType property rather than reuse the
	// generic type,
	// since we will be communicating with a generic client that can only understand
	// the generic
	// type.
	@SuppressWarnings("unused")
	private void setType(GModelElement sElement, IVisualPart<?> gefPart) {
		if (gefPart instanceof BaseContentPart) {
			Object content = ((BaseContentPart<?, ?>) gefPart).getContent();
			if (content instanceof View) {
				String type = ((View) content).getType();
				System.err.println("TODO Handle types");
//				String mainType = sElement.getType();
//				int separatorIndex = mainType.indexOf(":");
//				if (separatorIndex > 0) {
//					mainType = mainType.substring(0, separatorIndex);
//				}
//				sElement.setType(mainType+":"+type);
			}
		}
	}

	private void setLabel(GModelElement sElement, IVisualPart<?> gefPart) {
		if (sElement instanceof GLabel && gefPart instanceof AbstractLabelContentPart) {
			LabelService labelService = ((AbstractLabelContentPart<?, ?>) gefPart).getAdapter(LabelService.class);
			if (labelService != null) {
				// Label
				String text = labelService.getText();
				((GLabel) sElement).setText(text == null ? "" : text);
			}
		}
	}

}
