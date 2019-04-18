package org.eclipse.papyrus.gefx.glsp.server.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.papyrus.gef4.gmf.editor.handlers.CreateNodeHandler;
import org.eclipse.papyrus.gef4.gmf.parts.NotationDiagramRootPart;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.ChildElement;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.Drawer;
import org.eclipse.papyrus.gef4.palette.declarative.IdCreationTool;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.action.kind.AbstractOperationAction;
import com.eclipsesource.glsp.api.action.kind.CreateNodeOperationAction;
import com.eclipsesource.glsp.api.handler.OperationHandler;
import com.eclipsesource.glsp.api.model.GraphicalModelState;
import com.eclipsesource.glsp.api.model.ModelState;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

public class CreateNodeOperationHandler implements OperationHandler {

	private static Logger log = Logger.getLogger(CreateNodeOperationHandler.class);
	
	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public boolean handles(AbstractOperationAction action) {
		return action instanceof CreateNodeOperationAction;
	}

	@Override
	public Optional<SModelRoot> execute(AbstractOperationAction action, GraphicalModelState modelState) {
		CreateNodeOperationAction createAction = (CreateNodeOperationAction)action;
		
		String modelId = ModelUtil.getModelId(modelState);
		IViewer viewer = gefSynchronizer.getViewer(modelId);
		IRootPart<?> rootPart = viewer.getRootPart();
		if (rootPart instanceof NotationDiagramRootPart) {
			Diagram modelRoot = ((NotationDiagramRootPart) rootPart).getModelRoot();

			TransactionalEditingDomain editingDomain = (TransactionalEditingDomain) EMFHelper
					.resolveEditingDomain(modelRoot);
			
			String containerId = createAction.getContainerId();
			String elementTypeId = createAction.getElementTypeId();
			Point location = createAction.getLocation();

			// XXX Workaround: the GEFx CreateNodeHandler is not smart enough yet, and will
			// never delegate to the appropriate parent when the specified container is incorrect
			// (For example, it will never accept creating a Property on a Class or Class Label;
			// it will only accept the Class' Owned Attributes compartment)
			List<IContentPart<?>> possibleParents = new ArrayList<>();
			
			if (containerId == null) {
				possibleParents.add(rootPart.getContentPartChildren().get(0));
			} else {
				// Always try the requested container first
				EObject container = modelRoot.eResource().getEObject(containerId);
				Map<Object, IContentPart<?>> contentPartMap = viewer.getContentPartMap();
				possibleParents.add(contentPartMap.get(container));
				if (container instanceof DecorationNode) {
					// Label: consider the sibling compartments
					container.eContainer().eContents().stream().filter(c -> c instanceof BasicCompartment).map(comp -> contentPartMap.get(comp)).forEach(possibleParents::add);
				} else if (container instanceof Shape && ((Shape)container).getType().contains("Label")) {
					possibleParents.add(contentPartMap.get(container.eContainer()));
				} else if (container instanceof Shape) {
					container.eContents().stream().filter(c -> c instanceof BasicCompartment).map(comp -> contentPartMap.get(comp)).forEach(possibleParents::add);
				}
			}
			
			for (IContentPart<?> parentPart : possibleParents) {
				if (parentPart == null) {
					continue;
				}
				CreateNodeHandler createHandler = parentPart == null ? null : parentPart.getAdapter(CreateNodeHandler.class);
				if (createHandler != null) {
					Point2D loc = getLocationInParent(parentPart, location);
					Dimension size = new Dimension(-1, -1);
					Collection<String> allTypes = getAllTypes(modelState, elementTypeId);
					ICommand command = createHandler.create(loc, size, allTypes);
					if (command != null && command.canExecute()) {
						editingDomain.getCommandStack().execute(new GMFtoEMFCommandWrapper(command));
						gefSynchronizer.refresh(modelId);
						SModelRoot result = gefSynchronizer.getModel(modelId);
						return Optional.of(result);
					} else {
						log.warn("Command is not executable. Resolved Container: "+parentPart+"; element type: "+elementTypeId);
					}
				}
			}
		}
		return Optional.empty();
	}
	
	private Point2D getLocationInParent(IContentPart<?> parent, Point locationInScene) {
		Point2D result = new Point2D(locationInScene.getX(), locationInScene.getY());
		result = parent.getVisual().sceneToLocal(result);
		return result;
	}

	// XXX Workaround for inconsistency between GEFx Palettes and GLSP Palettes
	// Each GLSP Palette Entry contains a single element type, but GEFx/Papyrus have several
	// element types per entry (The correct one is determined at runtime based on the creation container,
	// e.g. Class-as-Shape vs Class-as-ListItem).
	// Reuse the palette to find all element types associated to the selected tool entry.
	// This code must be consistent with GEFxOperationConfiguration
	private Collection<String> getAllTypes(ModelState modelState, String type){
		PaletteDescriptor paletteDescriptor = gefSynchronizer.getViewer(ModelUtil.getModelId(modelState))
				.getAdapter(PaletteDescriptor.class);
		ObservableList<Drawer> drawers = paletteDescriptor.getDrawers();

		for (Drawer drawer : drawers) {
			for (ChildElement child : drawer.getChildren()) {
				if (child instanceof IdCreationTool) {
					IdCreationTool paletteTool = (IdCreationTool) child;
					Collection<String> types = ((IdCreationTool)paletteTool).getElementTypeIds();
					
					if (types.contains(type)) {
						return types;
					}
				}
			}
		}
		return Collections.emptyList();
	}

}
