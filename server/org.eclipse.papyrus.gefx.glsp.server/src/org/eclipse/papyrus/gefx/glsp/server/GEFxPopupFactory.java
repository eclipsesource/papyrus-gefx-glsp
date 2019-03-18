package org.eclipse.papyrus.gefx.glsp.server;

import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;

import com.eclipsesource.glsp.api.action.kind.RequestPopupModelAction;
import com.eclipsesource.glsp.api.factory.PopupModelFactory;

public class GEFxPopupFactory implements PopupModelFactory {

	@Override
	public SModelRoot createPopuModel(SModelElement arg0, RequestPopupModelAction arg1) {
		return new SModelRoot();
	}

}
