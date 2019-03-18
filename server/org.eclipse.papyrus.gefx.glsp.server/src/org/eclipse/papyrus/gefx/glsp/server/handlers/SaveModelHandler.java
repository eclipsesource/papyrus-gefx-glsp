package org.eclipse.papyrus.gefx.glsp.server.handlers;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.fx.core.ServiceUtils;
import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.papyrus.gefx.e3.GEFEditor;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.eclipsesource.glsp.api.action.Action;
import com.eclipsesource.glsp.api.action.kind.SaveModelAction;
import com.eclipsesource.glsp.api.handler.ActionHandler;
import com.eclipsesource.glsp.api.model.ModelState;

public class SaveModelHandler implements ActionHandler {

	@Inject
	private DiagramsSynchronizer synchronizer;
	private ThreadSynchronize threadSync;

	public SaveModelHandler() {
		this.threadSync = ServiceUtils.getService(ThreadSynchronize.class).orElse(null);
	}

	@Override
	public boolean handles(Action object) {
		return object instanceof SaveModelAction;
	}

	@Override
	public Optional<Action> execute(Action action, ModelState modelState) {
		if (action instanceof SaveModelAction) {
			threadSync.syncExec(() -> doSave(modelState));
		}
		return Optional.empty();
	}
	
	private void doSave(ModelState modelState) {
		for (IWorkbenchPage page : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()) {
			for (IEditorPart openEditor : page.getEditors()) {
				if (openEditor instanceof IMultiDiagramEditor) {
					IMultiDiagramEditor editor = (IMultiDiagramEditor)openEditor;
					IEditorPart activeEditor = editor.getActiveEditor();
					if (activeEditor instanceof GEFEditor) {
						GEFEditor<?> activeGEFEditor = (GEFEditor<?>)activeEditor;
						if (activeGEFEditor.getViewer() == synchronizer.getViewer(ModelUtil.getModelId(modelState))) {
							editor.doSave(new NullProgressMonitor());
						}
					}
				}
			}
		}
	}

}
