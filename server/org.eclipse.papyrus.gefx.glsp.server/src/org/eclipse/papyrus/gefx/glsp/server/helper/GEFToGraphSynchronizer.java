package org.eclipse.papyrus.gefx.glsp.server.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fx.core.ThreadSynchronize;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.gef4.gmf.parts.NotationDiagramRootPart;
import org.eclipse.papyrus.gefx.glsp.server.common.CleanedDisposeableCollector;
import org.eclipse.sprotty.SModelElement;
import org.eclipse.sprotty.SModelRoot;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

class GEFToGraphSynchronizer extends CleanedDisposeableCollector {

	private TreeRoot treeRoot;

	private ModelBuilder modelBuilder;

	private final Runnable refreshNotifier = this::notifyListeners;

	private final AtomicBoolean isDirty = new AtomicBoolean(false);
	private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

	private final ListenerList<GraphListener> listeners = new ListenerList<>();

	// During a reparent, a Part may be removed from a parent and added
	// to a new parent. To avoid disposing the corresponding branch,
	// we store removed branches here during a transaction, so we might
	// reuse them if it turns out they are being moved rather than deleted.
	// This set is cleared at the end of each transaction.
	private final Map<IVisualPart<?>, ContentBranch> detachedBranches = new HashMap<>();

	private ThreadSynchronize threadSync;

	private IViewer viewer;

	public GEFToGraphSynchronizer() {
		super();
	}

	public void init(IViewer viewer, ModelBuilder modelBuilder, ThreadSynchronize threadSync) {
		this.modelBuilder = modelBuilder;
		this.threadSync = threadSync;
		this.viewer = viewer;
		treeRoot = new TreeRoot(viewer.getRootPart());
		register(treeRoot::dispose);
		register(() -> {
			detachedBranches.values().forEach(ContentBranch::dispose);
			detachedBranches.clear();
		});
		register(listeners::clear);
		markDirty();
		refresh();
	}

	public IViewer getViewer() {
		return this.viewer;
	}

	/**
	 * Register a listener that will be notified when the graph structure changes.
	 * Notifications are sent in a batch, so if several branches of the graph change
	 * during the same transaction, only one event will be issued.
	 * 
	 * @param listener
	 */
	public void addListener(GraphListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a {@link GraphListener} installed with
	 * {@link #addListener(GraphListener)}
	 * 
	 * @param listener
	 */
	public void removeListener(GraphListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners() {
		if (isDirty.getAndSet(false)) {
			isRefreshing.set(true);
			try {
				detachedBranches.values().forEach(TreeBranch::dispose);
				detachedBranches.clear();
				listeners.forEach(listener -> listener.graphChanged(treeRoot.getElement()));
			} finally {
				isRefreshing.set(false);
			}
		}
	}

	private void markDirty() {
		if (!isDirty.getAndSet(true)) {
			threadSync.asyncExec(refreshNotifier);
		}
	}

	public SModelRoot getModel() {
		while (isDirty.get() || isRefreshing.get()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		threadSync.syncExec(() -> {});
		SModelRoot model = internalGetModel();
//		System.out.println(model);
		return model;
	}

	private SModelRoot internalGetModel() {
		return this.treeRoot.getElement();
	}

	public void refresh() {
		isRefreshing.set(true);
		try {
			this.treeRoot.refresh();
		} finally {
			isRefreshing.set(false);
		}
	}

	protected abstract class TreeBranch<SMODEL extends SModelElement> extends CleanedDisposeableCollector {

		private final SMODEL modelElement;

		private final IVisualPart<?> part;

		private final Map<IVisualPart<?>, ContentBranch> childBranches = new HashMap<>();

		public TreeBranch(IVisualPart<?> part) {
			super();
			this.part = part;
			this.modelElement = createElement(part);

			ObservableList<IVisualPart<?>> children = getContentChildren(part);
			createChildren(part, children);

			ListChangeListener<IVisualPart<?>> listener = this::contentsChanged;
			children.addListener(listener);
			register(() -> children.removeListener(listener));
		}

		private void createChildren(IVisualPart<?> part, List<IVisualPart<?>> children) {
			for (IVisualPart<?> childPart : children) {
				childBranches.computeIfAbsent(childPart, this::getOrRestore);
			}

			refreshChildren(children);
		}

		private ContentBranch getOrRestore(IVisualPart<?> gefPart) {
			if (detachedBranches.containsKey(gefPart)) {
				return detachedBranches.remove(gefPart);
			} else {
				return new ContentBranch(gefPart);
			}
		}

		protected abstract ObservableList<IVisualPart<?>> getContentChildren(IVisualPart<?> part);

		public SMODEL getElement() {
			return modelElement;
		}

		public abstract SMODEL createElement(IVisualPart<?> part);

		private void contentsChanged(Change<? extends IVisualPart<?>> childrenChange) {
			while (childrenChange.next()) {
				if (childrenChange.wasRemoved()) {
					for (IVisualPart<?> removedPart : childrenChange.getRemoved()) {
						ContentBranch detachedBranch = childBranches.remove(removedPart);
						detachedBranches.put(removedPart, detachedBranch);
					}
				}
				if (childrenChange.wasAdded()) {
					for (IVisualPart<?> addedPart : childrenChange.getAddedSubList()) {
						childBranches.computeIfAbsent(addedPart, ContentBranch::new);
					}
				}
				if (childrenChange.wasUpdated()) {
					// TODO Do we need this?
					System.err.println("UNSUPPORTED: Children List was Updated");
				}
				if (childrenChange.wasPermutated()) {
					// TODO Do we need this?
					System.err.println("UNSUPPORTED: Children List was Permutated");
				}
			}

			refreshChildren(childrenChange.getList());

			markDirty();
		}

		protected void refreshChildren(List<? extends IVisualPart<?>> children) {
			List<SModelElement> childElements = new ArrayList<>();
			for (IVisualPart<?> childPart : children) {
				childElements.add(childBranches.get(childPart).getElement());
			}
			modelElement.setChildren(childElements);
		}

		public void refresh() {
			modelBuilder.refreshElement(part, modelElement);
			for (ContentBranch child : childBranches.values()) {
				child.refresh();
			}
		}
	}

	public class ContentBranch extends TreeBranch<SModelElement> {

		public ContentBranch(IVisualPart<?> part) {
			super(part);
		}

		@Override
		protected ObservableList<IVisualPart<?>> getContentChildren(IVisualPart<?> part) {
			return part.getChildrenUnmodifiable();
		}

		@Override
		public SModelElement createElement(IVisualPart<?> part) {
			return modelBuilder.createElement(part);
		}

	}

	public class TreeRoot extends TreeBranch<SModelRoot> {

		private int revision;

		public TreeRoot(IRootPart<?> part) {
			super(part);
		}

		@Override
		protected ObservableList<IVisualPart<?>> getContentChildren(IVisualPart<?> part) {
			// return part.getChildrenUnmodifiable();
			return part.getChildrenUnmodifiable().filtered(IContentPart.class::isInstance).get(0)
					.getChildrenUnmodifiable();
		}

		@Override
		public SModelRoot createElement(IVisualPart<?> part) {
			SModelRoot sModelRoot = new SModelRoot();
			String id;
			if (part instanceof NotationDiagramRootPart) {
				Diagram modelRoot = ((NotationDiagramRootPart) part).getModelRoot();
				id = EcoreUtil.getURI(modelRoot).toString();
			} else {
				id = "GEFxDiagramRoot";
			}
			sModelRoot.setId(id);
			return sModelRoot;
		}

		@Override
		public void refresh() {
			super.refresh();
			internalGetModel().setRevision(revision++);
		}

	}

	public static interface GraphListener {
		void graphChanged(SModelRoot graph);
	}
}
