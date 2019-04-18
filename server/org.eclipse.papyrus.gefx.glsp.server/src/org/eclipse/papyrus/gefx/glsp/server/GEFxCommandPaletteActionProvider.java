package org.eclipse.papyrus.gefx.glsp.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;
import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.provider.CommandPaletteActionProvider;
import com.eclipsesource.glsp.api.types.LabeledAction;

public class GEFxCommandPaletteActionProvider implements CommandPaletteActionProvider {

	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public Set<LabeledAction> getActions(SModelRoot root, List<String> selectedElementsIds) {
		return new HashSet<>(); //Ctrl+Space Command Actions
	}

}
