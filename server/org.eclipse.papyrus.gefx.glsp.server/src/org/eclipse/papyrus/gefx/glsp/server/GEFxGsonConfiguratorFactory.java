package org.eclipse.papyrus.gefx.glsp.server;

import com.eclipsesource.glsp.api.factory.GraphGsonConfiguratorFactory;
import com.eclipsesource.glsp.graph.gson.GGraphGsonConfigurator;

public class GEFxGsonConfiguratorFactory implements GraphGsonConfiguratorFactory {

	@Override
	public GGraphGsonConfigurator create() {
		return new GGraphGsonConfigurator().withTypes(GEFxTypes.TYPE_MAP);
	}

}
