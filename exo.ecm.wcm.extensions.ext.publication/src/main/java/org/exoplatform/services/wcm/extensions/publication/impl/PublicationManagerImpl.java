package org.exoplatform.services.wcm.extensions.publication.impl;

import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.wcm.extensions.publication.PublicationManager;
import org.exoplatform.services.wcm.extensions.publication.context.ContextPlugin;
import org.exoplatform.services.wcm.extensions.publication.context.impl.ContextConfig.Context;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.StatesLifecyclePlugin;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.picocontainer.Startable;

public class PublicationManagerImpl implements PublicationManager, Startable {

    private StatesLifecyclePlugin statesLifecyclePlugin;
    private ContextPlugin contextPlugin;

    public void addLifecycle(ComponentPlugin plugin) {
	if (plugin instanceof StatesLifecyclePlugin) {
	    statesLifecyclePlugin = (StatesLifecyclePlugin) plugin;
	}
    }

    public void addContext(ComponentPlugin plugin) {
	if (plugin instanceof ContextPlugin) {
	    contextPlugin = (ContextPlugin) plugin;
	}
    }

    public void start() {
	// TODO Auto-generated method stub

    }

    public void stop() {
	// TODO Auto-generated method stub

    }

    public Context getContext(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    public List<Context> getContexts() {
	if (contextPlugin != null && contextPlugin.getContextConfig() != null)
	    return contextPlugin.getContextConfig().getContexts();
	return null;
    }

    public Lifecycle getLifecycle(String name) {
	if (name != null && statesLifecyclePlugin != null && statesLifecyclePlugin.getLifecyclesConfig() != null) {
	    for (Lifecycle lifecycle : statesLifecyclePlugin.getLifecyclesConfig().getLifecycles()) {
		if (name.equals(lifecycle.getName())) {
		    return lifecycle;
		}
	    }
	}

	return null;
    }

    public List<Lifecycle> getLifecycles() {
	if (statesLifecyclePlugin != null && statesLifecyclePlugin.getLifecyclesConfig() != null)
	    return statesLifecyclePlugin.getLifecyclesConfig().getLifecycles();
	return null;
    }

    public List<Lifecycle> getLifecyclesFromUser(String remoteUser, String state) {
	// TODO
	return null;
    }
}
