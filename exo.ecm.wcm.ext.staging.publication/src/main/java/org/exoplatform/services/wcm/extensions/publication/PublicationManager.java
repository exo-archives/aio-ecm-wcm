package org.exoplatform.services.wcm.extensions.publication;

import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.wcm.extensions.publication.context.impl.ContextConfig.Context;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
public interface PublicationManager {
  public void addLifecycle(ComponentPlugin plugin);

  public void addContext(ComponentPlugin plugin);

  public List<Lifecycle> getLifecycles();

  public List<Context> getContexts();

  public Context getContext(String name);

  public Lifecycle getLifecycle(String name);

  public List<Lifecycle> getLifecyclesFromUser(String remoteUser, String state);

}
