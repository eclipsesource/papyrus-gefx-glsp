package org.eclipse.papyrus.gefx.glsp.server.helper;

import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.model.ModelState;

public class ModelUtil {
	
	public static String getModelId(ModelState<SModelRoot> modelState) {
		return modelState == null ? null : getModelId(modelState.getRoot());
	}

	public static String getModelId(SModelRoot root) {
		return root == null ? null : root.getId();
	}
	
}
