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
import org.eclipse.sprotty.Dimension;
import org.eclipse.sprotty.LayoutOptions;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SCompartment;
import org.eclipse.sprotty.SEdge;
import org.eclipse.sprotty.SLabel;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SNode;
import org.eclipse.sprotty.SPort;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class GEFModelBuilder implements ModelBuilder {

	@Override
	public SModelElement createElement(IVisualPart<?> gefPart) {
		SModelElement result = doCreateElement(gefPart);

		if (result == null) {
			System.err.println("UNKNOWN PART: " + gefPart + ". Fallback to simple SNode");
			result = new SNode();
		}

		return result;
	}

	@Override
	public void refreshElement(IVisualPart<?> gefPart, SModelElement modelElement) {
		String id = getId(gefPart);
		if (id != null) {
			modelElement.setId(id);
		}
		
		if (modelElement instanceof SNode) {
			refreshNode((SNode) modelElement, gefPart);
		}

		if (modelElement instanceof SLabel) {
			refreshLabel((SLabel) modelElement, gefPart);
		}

		if (modelElement instanceof SEdge) {
			refreshConnection((SEdge) modelElement, gefPart);
		}
		
		if (modelElement instanceof SCompartment) {
			refreshCompartment((SCompartment) modelElement, gefPart);
		}
		
//		setType(modelElement, gefPart);
	}
	
	private void refreshCompartment(SCompartment sCompartment, IVisualPart<?> gefPart) {
		if (gefPart instanceof CompartmentContentPart) {
			LayoutOptions layoutOptions = new LayoutOptions();
			if (gefPart instanceof ListCompartmentContentPart) {
				sCompartment.setLayout("vbox");
				layoutOptions.setHAlign("left");
			}
			layoutOptions.setResizeContainer(true);
			if (gefPart.getVisual() instanceof Region) {
				Region region = (Region)gefPart.getVisual();
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

	private void refreshNode(SNode modelElement, IVisualPart<?> gefPart) {
		Node visual = gefPart.getVisual();
		if (visual == null || visual.getParent() == null) {
			return;
		}
		
		modelElement.setLayout("vbox");
		LayoutOptions layout = new LayoutOptions();
		//layout.setVGap(1.);
		layout.setResizeContainer(false);
		modelElement.setLayoutOptions(layout);
		
		Bounds boundsInParent = visual.getBoundsInParent();
		modelElement.setPosition(new Point(boundsInParent.getMinX(), boundsInParent.getMinY()));
		modelElement.setSize(new Dimension(boundsInParent.getWidth(), boundsInParent.getHeight()));
	}

	private SModelElement doCreateElement(IVisualPart<?> gefPart) {
		if (gefPart instanceof CompartmentContentPart) {
			return new SCompartment();
		}
		if (gefPart instanceof LabelContentPart) {
			return new SLabel();
		}
		if (gefPart instanceof ConnectionContentPart) {
			return new SEdge();
		}
		if (gefPart instanceof NodeContentPart) {
			if (((NodeContentPart<?>) gefPart).getLocator() != null) {
				return new SPort();
			}
			return new SNode();
		}

		return null;
	}

	private void refreshConnection(SEdge modelElement, IVisualPart<?> gefPart) {
		if (gefPart instanceof ConnectionContentPart) {
			ConnectionContentPart<?> connection = (ConnectionContentPart<?>)gefPart;
			// Source & Target, Anchors
			Connection gefVisual = connection.getConnection();
			
			Node sourceNode = gefVisual.getStartAnchor().getAnchorage();
			Node targetNode = gefVisual.getEndAnchor().getAnchorage();
			
			IViewer viewer = gefPart.getViewer();
			IVisualPart<?> sourcePart = viewer.getVisualPartMap().get(sourceNode);
			modelElement.setSourceId(getId(sourcePart));
			IVisualPart<?> targetPart = viewer.getVisualPartMap().get(targetNode);
			modelElement.setTargetId(getId(targetPart));
			
			modelElement.setRoutingPoints(geometryToSprotty(connection.getContentBendPoints()));
		}
	}

	private List<Point> geometryToSprotty(List<BendPoint> contentBendPoints) {
		List<Point> result = new ArrayList<>();
		for (BendPoint bendpoint : contentBendPoints) {
			result.add(geometryToSprotty(bendpoint.getPosition()));
		}
		return result.isEmpty() ? null : result;
	}

	private Point geometryToSprotty(org.eclipse.gef.geometry.planar.Point startPoint) {
		return new Point(startPoint.x(), startPoint.y());
	}

	private void refreshLabel(SLabel modelElement, IVisualPart<?> gefPart) {
		setLabel(modelElement, gefPart);
		if (gefPart.getParent() instanceof ConnectionContentPart) {
			refreshConnectionLabel(modelElement, gefPart);
		}
	}

	private void refreshConnectionLabel(SLabel modelElement, IVisualPart<?> labelPart) {
		Node visual = labelPart.getVisual();
		Bounds boundsInParent = visual.getBoundsInParent();
		modelElement.setPosition(new Point(boundsInParent.getMinX(), boundsInParent.getMinY()));
		modelElement.setSize(new Dimension(boundsInParent.getWidth(), boundsInParent.getHeight()));
		//modelElement.setEdgePlacement(EdgePlacement.);
	}

	// Unused for now; because the client only supports generic types (Node, Edge, Label, Comp).
	// We should probably add a new notationType property rather than reuse the generic type,
	// since we will be communicating with a generic client that can only understand the generic
	// type.
	private void setType(SModelElement sElement, IVisualPart<?> gefPart) {
		if (gefPart instanceof BaseContentPart) {
			Object content = ((BaseContentPart<?, ?>) gefPart).getContent();
			if (content instanceof View) {
				String type = ((View) content).getType();
				String mainType = sElement.getType();
				int separatorIndex = mainType.indexOf(":");
				if (separatorIndex > 0) {
					mainType = mainType.substring(0, separatorIndex);
				}
				sElement.setType(mainType+":"+type);
			}
		}
	}

	private void setLabel(SModelElement sElement, IVisualPart<?> gefPart) {
		if (sElement instanceof SLabel && gefPart instanceof AbstractLabelContentPart) {
			LabelService labelService = ((AbstractLabelContentPart<?, ?>) gefPart).getAdapter(LabelService.class);
			if (labelService != null) {
				// Label
				String text = labelService.getText();
				((SLabel) sElement).setText(text == null ? "" : text);
			}
		}
	}

}
