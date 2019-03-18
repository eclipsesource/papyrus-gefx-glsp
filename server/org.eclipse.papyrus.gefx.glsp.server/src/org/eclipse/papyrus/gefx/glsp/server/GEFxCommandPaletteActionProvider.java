package org.eclipse.papyrus.gefx.glsp.server;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.fx.core.command.Command;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.ChildElement;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.Drawer;
import org.eclipse.papyrus.gef4.palette.PaletteDescriptor.ToolEntry;
import org.eclipse.papyrus.gef4.tools.Tool;
import org.eclipse.papyrus.gef4.tools.ToolManager;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.papyrus.gefx.glsp.server.helper.ModelUtil;
import org.eclipse.papyrus.infra.gefdiag.common.palette.CreateConnectionTool;
import org.eclipse.papyrus.infra.gefdiag.common.palette.CreateNodeTool;
import org.eclipse.sprotty.Point;
import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.action.kind.CreateConnectionOperationAction;
import com.eclipsesource.glsp.api.action.kind.CreateNodeOperationAction;
import com.eclipsesource.glsp.api.provider.CommandPaletteActionProvider;
import com.eclipsesource.glsp.api.types.LabeledAction;

import javafx.collections.ObservableList;

public class GEFxCommandPaletteActionProvider implements CommandPaletteActionProvider {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public Set<LabeledAction> getActions(SModelRoot root, String[] selectedElementsIds) {
		return new HashSet<>(); //Ctrl+Space Command Actions
	}

}
