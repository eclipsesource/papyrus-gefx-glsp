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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.eclipse.papyrus.gefx.glsp.server.handlers.ChangeBoundsHandler;
import org.eclipse.papyrus.gefx.glsp.server.handlers.CreateEdgeHandler;
import org.eclipse.papyrus.gefx.glsp.server.handlers.CreateNodeOperationHandler;
import org.eclipse.papyrus.gefx.glsp.server.handlers.SaveModelHandler;
import org.eclipse.papyrus.gefx.glsp.server.helper.DiagramsSynchronizer;

import com.eclipsesource.glsp.api.diagram.DiagramManager;
import com.eclipsesource.glsp.api.factory.ModelFactory;
import com.eclipsesource.glsp.api.factory.PopupModelFactory;
import com.eclipsesource.glsp.api.handler.ActionHandler;
import com.eclipsesource.glsp.api.handler.OperationHandler;
import com.eclipsesource.glsp.api.model.ModelElementOpenListener;
import com.eclipsesource.glsp.api.model.ModelExpansionListener;
import com.eclipsesource.glsp.api.model.ModelSelectionListener;
import com.eclipsesource.glsp.api.operations.OperationConfiguration;
import com.eclipsesource.glsp.api.provider.CommandPaletteActionProvider;
import com.eclipsesource.glsp.server.DefaultGLSPModule;
import com.eclipsesource.glsp.server.actionhandler.CollapseExpandActionHandler;
import com.eclipsesource.glsp.server.actionhandler.ComputedBoundsActionHandler;
import com.eclipsesource.glsp.server.actionhandler.ExecuteServerCommandActionHandler;
import com.eclipsesource.glsp.server.actionhandler.OpenActionHandler;
import com.eclipsesource.glsp.server.actionhandler.OperationActionHandler;
import com.eclipsesource.glsp.server.actionhandler.RequestCommandPaletteActionsHandler;
import com.eclipsesource.glsp.server.actionhandler.RequestModelActionHandler;
import com.eclipsesource.glsp.server.actionhandler.RequestOperationsHandler;
import com.eclipsesource.glsp.server.actionhandler.RequestPopupModelActionHandler;
import com.eclipsesource.glsp.server.actionhandler.RequestTypeHintsActionHandler;
import com.eclipsesource.glsp.server.actionhandler.SelectActionHandler;
import com.google.inject.Provides;

public class GEFxServerRuntimeModule extends DefaultGLSPModule {

	@Provides
	@Singleton
	protected DiagramsSynchronizer bindSynchronizer() {
		return new DiagramsSynchronizer();
	}

	@Override
	protected Collection<Class<? extends DiagramManager>> bindDiagramManagers() {
		return Collections.singletonList(GEFxDiagramManager.class);
	}

	@Override
	protected Class<? extends ModelFactory> bindModelFactory() {
		return PapyrusGEFxModelFactory.class;
	}

	@Override
	public Class<? extends PopupModelFactory> bindPopupModelFactory() {
		return GEFxPopupFactory.class;
	}

	@Override
	public Class<? extends ModelSelectionListener> bindModelSelectionListener() {
		return GEFxServerListener.class;
	}

	@Override
	public Class<? extends ModelElementOpenListener> bindModelElementOpenListener() {
		return GEFxServerListener.class;
	}

	@Override
	public Class<? extends ModelExpansionListener> bindModelExpansionListener() {
		return GEFxServerListener.class;
	}

	@Override
	public Class<? extends OperationConfiguration> bindOperationConfiguration() {
		return GEFxOperationConfiguration.class;
	}

	@Override
	protected Class<? extends CommandPaletteActionProvider> bindCommandPaletteActionProvider() {
		// return GEFxCommandPaletteActionProvider.class;
		return super.bindCommandPaletteActionProvider(); // Ctrl+Space command list
	}

	@Override
	protected Collection<Class<? extends OperationHandler>> bindOperationHandlers() {
		return Arrays.asList(
			ChangeBoundsHandler.class,
			CreateNodeOperationHandler.class,
			CreateEdgeHandler.class
		);
	}

	@Override
	protected Collection<Class<? extends ActionHandler>> bindActionHandlers() {
		return Arrays.asList(
				// Inherited
				CollapseExpandActionHandler.class,
				ComputedBoundsActionHandler.class,
				OpenActionHandler.class,
				OperationActionHandler.class,
				RequestModelActionHandler.class,
				RequestOperationsHandler.class,
				RequestPopupModelActionHandler.class,
				SelectActionHandler.class,
				ExecuteServerCommandActionHandler.class,
				RequestTypeHintsActionHandler.class,
				RequestCommandPaletteActionsHandler.class,
				
				// Custom
				SaveModelHandler.class
		);
	}

}
