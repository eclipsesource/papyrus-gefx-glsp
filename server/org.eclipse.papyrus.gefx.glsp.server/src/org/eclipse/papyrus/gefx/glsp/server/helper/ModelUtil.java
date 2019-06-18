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

import com.eclipsesource.glsp.api.model.ModelState;
import com.eclipsesource.glsp.graph.GModelRoot;

public class ModelUtil {
	
	public static String getModelId(ModelState<GModelRoot> modelState) {
		return modelState == null ? null : getModelId(modelState.getRoot());
	}

	public static String getModelId(GModelRoot root) {
		return root == null ? null : root.getId();
	}
	
}
