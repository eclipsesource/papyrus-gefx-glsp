package org.eclipse.papyrus.gefx.glsp.server.handlers;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;
import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.action.kind.AbstractOperationAction;
import com.eclipsesource.glsp.api.action.kind.CreateConnectionOperationAction;
import com.eclipsesource.glsp.api.handler.OperationHandler;
import com.eclipsesource.glsp.api.model.GraphicalModelState;

public class CreateEdgeHandler implements OperationHandler {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public boolean handles(AbstractOperationAction action) {
		return action instanceof CreateConnectionOperationAction;
	}

	@Override
	public Optional<SModelRoot> execute(AbstractOperationAction action, GraphicalModelState modelState) {
		// TODO Auto-generated method stub
		
		String modelId = ModelUtil.getModelId(modelState);
		gefSynchronizer.refresh(modelId);
		return Optional.of(gefSynchronizer.getModel(modelId));
	}

}
