package org.eclipse.papyrus.gefx.glsp.server.common;

import org.eclipse.fx.core.DisposeableCollector;
import org.eclipse.fx.core.Subscription;

/**
 * <p>
 * A specialization of the {@link DisposeableCollector} that automatically
 * unregisters disposeables when they are disposed, avoiding memory leaks
 * when the collector is meant to be used with many new disposeables over time.
 * </p>
 */
public abstract class CleanedDisposeableCollector extends org.eclipse.fx.core.DisposeableCollector<Subscription> {

	// XXX Number of disposeables for each collector is expected to be small ( < 100 in most cases),
	// but to be frequently added/removed. That's why we can afford keeping the parent's ArrayList
	// implementation. If the number of disposeables is expected to be high, we should consider
	// using a set instead, to speed up removal of disposeables.
	
	private boolean isDisposing = false;
	
	public CleanedDisposeableCollector() {
		super(Subscription::disposeIfExists);
	}
	
	@Override
	protected void register(Subscription o) {
		super.register(wrap(o));
	}

	private Subscription wrap(Subscription o) {
		return () -> {
			if (! isDisposing) {
				unregister(o); // Don't unregister elements during dispose-all, to avoid concurrent modifications
			}
			Subscription.disposeIfExists(o);
		};
	}
	
	@Override
	public void dispose() {
		isDisposing = true;
		super.dispose();
	}

}
