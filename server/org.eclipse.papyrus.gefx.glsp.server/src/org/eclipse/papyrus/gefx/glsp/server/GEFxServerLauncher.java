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

import java.util.concurrent.Future;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import com.eclipsesource.glsp.server.ServerLauncher;
import com.google.inject.Guice;

public class GEFxServerLauncher extends AbstractHandler {

	private static ServerLauncher launcher;

	@Override
	public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		BasicConfigurator.configure();
		Shell activeShell = HandlerUtil.getActiveShell(event);
		
		if (launcher == null) {
			startServer(activeShell, false);
		} else {
			
			String[] choices = { "Restart", "Shutdown", "Cancel" };
			int choice = MessageDialog.open(MessageDialog.QUESTION, activeShell, "Server already started", 
					"The GEFx Language Server is already started. Do you want to close it?", SWT.NONE, choices);
			
			switch (choice) {
			case 0: // Restart
				stopServer();
				startServer(activeShell, true);
				break;
			case 1: // Shutdown
				stopServer();
				break;
			case 2: // Cancel
			default:
				break; // Nothing to do
			}
		}
		return null;
	}
	
	private void stopServer() throws ExecutionException {
		try {
			launcher.stop();
			launcher = null;
		} catch (Exception ex) {
			throw new ExecutionException("An error occurred when trying to stop the server", ex);
		}
	}
	
	private void startServer(Shell activeShell, boolean isRestart) {
		GEFxServerRuntimeModule module = new GEFxServerRuntimeModule();
		if (! validateModule(module, activeShell)) {
			return;
		}

		Job job = Job.create("GLSP Server", monitor -> {
			try {
				// Run in a Job to make sure the server can be stopped when we stop Eclipse
				launcher = new ServerLauncher("localhost", 5007, module);
				Future<Void> onClose = launcher.asyncRun(); // This will return quickly
				
				String startMsg = isRestart ? "restarted" : "started";
				String title = String.format("Server %s", startMsg);
				String message = String.format("The GEFx Language Server has been %s", startMsg);
				
				activeShell.getDisplay().asyncExec(() -> MessageDialog.openInformation(activeShell, title,
						message));
				
				while (! onClose.isDone()) {
					if (monitor.isCanceled()) {
						stopServer();
						break;
					}
					Thread.sleep(500);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, "org.eclipse.papyrus.gefx.glsp.server", e.getMessage(), e);
			}
			return Status.OK_STATUS;
		});
		
		job.setSystem(true);
		job.schedule();
	}

	private boolean validateModule(GEFxServerRuntimeModule module, Shell activeShell) {
		try {
			Guice.createInjector(module);
			return true;
		} catch (Exception ex ) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, "org.eclipse.papyrus.gefx.glsp.server", "An error occurred while trying to start the GEFx Language Server", ex), StatusManager.SHOW);
			return false;
		}
	}
}
