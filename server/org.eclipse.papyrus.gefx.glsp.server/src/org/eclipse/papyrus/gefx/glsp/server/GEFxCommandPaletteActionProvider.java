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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;

import com.eclipsesource.glsp.api.provider.CommandPaletteActionProvider;
import com.eclipsesource.glsp.api.types.LabeledAction;
import com.eclipsesource.glsp.graph.GModelRoot;

public class GEFxCommandPaletteActionProvider implements CommandPaletteActionProvider {

	@SuppressWarnings("unused")
	@Inject
	private DiagramsSynchronizer gefSynchronizer;
	
	@Override
	public Set<LabeledAction> getActions(GModelRoot root, List<String> selectedElementsIds) {
		return new HashSet<>(); //Ctrl+Space Command Actions
	}

}
