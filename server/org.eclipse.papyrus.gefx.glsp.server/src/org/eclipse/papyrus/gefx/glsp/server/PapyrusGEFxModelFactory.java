package org.eclipse.papyrus.gefx.glsp.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.fx.core.ServiceUtils;
import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.papyrus.gefx.e3.GEFEditor;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.infra.core.sashwindows.di.service.IPageManager;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.sprotty.SModelRoot;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.eclipsesource.glsp.api.action.kind.RequestModelAction;
import com.eclipsesource.glsp.api.factory.ModelFactory;
import com.eclipsesource.glsp.api.utils.ModelOptions;
import com.google.inject.Inject;

public class PapyrusGEFxModelFactory implements ModelFactory {

	private static Logger LOGGER = Logger.getLogger(PapyrusGEFxModelFactory.class);

//	@Inject
//	private ModelTypeConfigurationProvider modelTypeConfigurationProvider;
	
	@Inject
	DiagramsSynchronizer gefSynchronizer;

	private ThreadSynchronize threadSync;
	
	@Inject
	public PapyrusGEFxModelFactory() {
		this.threadSync = ServiceUtils.getService(ThreadSynchronize.class).orElse(null);
	}
	
	@Override
	public SModelRoot loadModel(RequestModelAction action) {
		String sourceURI = action.getOptions().get(ModelOptions.SOURCE_URI);
		try {
			URI absoluteURI = URI.createURI(sourceURI);
			String fileString = absoluteURI.toFileString();
			
			IPath absolutePath = new Path(fileString);
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IPath workspaceLocation = workspaceRoot.getLocation();
			if (workspaceLocation.isPrefixOf(absolutePath)) {
				IPath pathInWs = absolutePath.makeRelativeTo(workspaceLocation);
				CompletableFuture<IViewer> gefViewer = openEditor(pathInWs);
				IViewer viewer = gefViewer.get(10, TimeUnit.SECONDS);
				System.err.println("Viewer will be used");
				String modelId = gefSynchronizer.init(viewer, new GEFModelBuilder(), threadSync);
				Thread.sleep(3000);
				System.err.println("Model factory: model is ready; do return");
				gefSynchronizer.refresh(modelId);
				SModelRoot modelRoot = gefSynchronizer.getModel(modelId);
				LOGGER.info("Opening model: "+modelRoot);
				return modelRoot;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e);
		}
		
		
		return null;
	}

	private CompletableFuture<IViewer> openEditor(IPath pathInWs) {
		CompletableFuture<IViewer> result = new CompletableFuture<>();
		
		threadSync.asyncExec(() -> {
			result.complete(openEditorInUI(pathInWs));
		});
		
		return result.thenApplyAsync(this::waitViewerActive);
	}
	
	private IViewer waitViewerActive(IViewer viewer) {
		while (! viewer.isActive()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		return viewer;
	}

	private IViewer openEditorInUI(IPath wsPath) {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		URI modelURI = URI.createPlatformResourceURI(wsPath.toString(), true);
		try {
			IEditorPart editor = activePage.openEditor(new URIEditorInput(modelURI), "org.eclipse.papyrus.infra.core.papyrusEditor");
			assert editor instanceof IMultiDiagramEditor;
			IMultiDiagramEditor papyrusEditor = (IMultiDiagramEditor)editor;
			IPageManager pageManager = papyrusEditor.getServicesRegistry().getService(IPageManager.class);
			if (pageManager.allPages().isEmpty()) {
				return null;
			}
			
			Object anyPage = pageManager.allPages().get(0);
			pageManager.closeAllOpenedPages(anyPage); 
			pageManager.openPage(anyPage, "org.eclipse.papyrus.infra.gefdiag.common.internal.editor.GEFEditorFactory");
			IEditorPart nestedEditor = papyrusEditor.getActiveEditor();
			assert nestedEditor instanceof GEFEditor;
			return ((GEFEditor<?>)nestedEditor).getViewer();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
