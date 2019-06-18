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
package org.eclipse.papyrus.gefx.glsp.server.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.papyrus.gefx.glsp.server.GEFModelBuilder;
import org.eclipse.swt.widgets.Display;

import com.eclipsesource.glsp.graph.GModelRoot;

import javafx.scene.Node;

public class DiagramsSynchronizer {
	
	private final Map<String, GEFToGraphSynchronizer> handledDiagrams = new HashMap<>();
	
	private final ListenerList<GraphListener> listeners = new ListenerList<>();
	
	public GModelRoot getModel(String modelId) {
		return handledDiagrams.get(modelId).getModel();
	}
	
	public void refresh(String modelId) {
		Display.getDefault().syncExec(() -> {});
		handledDiagrams.get(modelId).refresh();
	}
	
	public IViewer getViewer(String modelId) {
		if (!handledDiagrams.containsKey(modelId)) {
			throw new IllegalStateException("Requested viewer for ID: "+modelId+"; this viewer doesn't exist (Wrong ID, viewer disposed or not initialized?)"); 
		}
		return handledDiagrams.get(modelId).getViewer();
	}

	public String init(IViewer viewer, GEFModelBuilder gefModelBuilder, ThreadSynchronize threadSync) {
		GEFToGraphSynchronizer synchronizer = new GEFToGraphSynchronizer();
		
		// ModelId may be unknown yet; wait until the viewer is fully loaded before returning one
		AtomicReference<String> modelId = new AtomicReference<String>(null);
		getModelId(viewer).ifPresent(modelId::set);
		
		if (modelId.get() == null) {
			System.err.println("Waiting until model is initialized");
			GEFToGraphSynchronizer.GraphListener initializationListener = new GEFToGraphSynchronizer.GraphListener() {
				@Override
				public void graphChanged(GModelRoot graph) {
					System.err.println("Graph changed (init)");
					getModelId(viewer).ifPresent(id -> {
						System.err.println("Id was set; model is ready");
						modelId.set(id);
						synchronizer.removeListener(this);
					});
				}
			};
			synchronizer.addListener(initializationListener);
		}
		synchronizer.addListener(newGraph -> {
			if (modelId.get() != null) {
				System.err.println("Graph changed (runtime)");
				listeners.forEach(listener -> listener.graphChanged(modelId.get(), newGraph));
			}
		});
		synchronizer.init(viewer, gefModelBuilder, threadSync);
		
		long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
		while (modelId.get() == null) {
			if (System.currentTimeMillis() >= timeout) {
				return null;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		
		handledDiagrams.put(modelId.get(), synchronizer);
		System.err.println("Do return");
		return modelId.get();
	}
	
	private Optional<String> getModelId(IViewer viewer){
		if (viewer.isActive()) {
			IRootPart<? extends Node> rootPart = viewer.getRootPart();
			if (rootPart != null && !rootPart.getContentPartChildren().isEmpty()) {
				Object content = rootPart.getContentPartChildren().get(0).getContent();
				if (content instanceof EObject) {
					return Optional.of(EcoreUtil.getURI((EObject)content).toString());
				}
			}
		}
		
		return Optional.empty();
	}
	
	public void addListener(GraphListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(GraphListener listener) {
		listeners.remove(listener);
	}
	
	public static interface GraphListener {
		void graphChanged(String modelId, GModelRoot graph);
	}

	public Collection<String> getModels() {
		return handledDiagrams.keySet();
	}
	
}
