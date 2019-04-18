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

import org.apache.log4j.Logger;

import com.eclipsesource.glsp.api.action.kind.CollapseExpandAction;
import com.eclipsesource.glsp.api.action.kind.CollapseExpandAllAction;
import com.eclipsesource.glsp.api.action.kind.OpenAction;
import com.eclipsesource.glsp.api.action.kind.SelectAction;
import com.eclipsesource.glsp.api.action.kind.SelectAllAction;
import com.eclipsesource.glsp.api.model.ModelElementOpenListener;
import com.eclipsesource.glsp.api.model.ModelExpansionListener;
import com.eclipsesource.glsp.api.model.ModelSelectionListener;

public class GEFxServerListener
		implements ModelSelectionListener, ModelExpansionListener, ModelElementOpenListener {
	private static Logger log = Logger.getLogger(GEFxServerListener.class);

	@Override
	public void elementOpened(OpenAction action) {
		log.info("HANDLE: OpenAction for element: " + action.getElementId());

	}

	@Override
	public void expansionChanged(CollapseExpandAction action) {
		log.info("HANDLE: CollapseExpandAction for elements: " + action.getCollapseIds());

	}

	@Override
	public void expansionChanged(CollapseExpandAllAction action) {
		log.info("HANDLE: CollapseExpandAllAction");

	}

	@Override
	public void selectionChanged(SelectAction action) {
		log.info("HANDLE: SelectAction for elements: " + String.join(", ", action.getSelectedElementsIDs()));

	}

	@Override
	public void selectionChanged(SelectAllAction action) {
		log.info("HANDLE: SelectAllAction");

	}

}
