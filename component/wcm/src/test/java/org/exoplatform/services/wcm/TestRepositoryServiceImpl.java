/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.wcm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.impl.AddNamespacesPlugin;
import org.exoplatform.services.jcr.impl.AddNodeTypePlugin;
import org.exoplatform.services.jcr.impl.RepositoryContainer;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Dec 3, 2009  
 */
public class TestRepositoryServiceImpl implements RepositoryService, Startable {

	protected static Log                               log                   = ExoLogger.getLogger("jcr.RepositoryService");

  private final RepositoryServiceConfiguration       config;

  private final ThreadLocal<String>                  currentRepositoryName = new ThreadLocal<String>();

  private final HashMap<String, RepositoryContainer> repositoryContainers  = new HashMap<String, RepositoryContainer>();

  private final List<ComponentPlugin>                addNodeTypePlugins;

  private final List<ComponentPlugin>                addNamespacesPlugins;

  private final ExoContainerContext                  containerContext;

  private ExoContainer                               parentContainer;

  public TestRepositoryServiceImpl(RepositoryServiceConfiguration configuration) {
    this(configuration, null);
  }

  public TestRepositoryServiceImpl(RepositoryServiceConfiguration configuration,
                               ExoContainerContext context) {
    this.config = configuration;
    addNodeTypePlugins = new ArrayList<ComponentPlugin>();
    addNamespacesPlugins = new ArrayList<ComponentPlugin>();
    containerContext = context;
    currentRepositoryName.set(config.getDefaultRepositoryName());
  }

  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof AddNodeTypePlugin)
      addNodeTypePlugins.add(plugin);
    else if (plugin instanceof AddNamespacesPlugin)
      addNamespacesPlugins.add(plugin);
  }

  public boolean canRemoveRepository(String name) throws RepositoryException {
    RepositoryImpl repo = (RepositoryImpl) getRepository(name);
    try {
      RepositoryEntry repconfig = config.getRepositoryConfiguration(name);

      for (WorkspaceEntry wsEntry : repconfig.getWorkspaceEntries()) {
        // Check non system workspaces
        if (!repo.getSystemWorkspaceName().equals(wsEntry.getName())
            && !repo.canRemoveWorkspace(wsEntry.getName()))
          return false;
      }
      // check system workspace
      RepositoryContainer repositoryContainer = repositoryContainers.get(name);
      SessionRegistry sessionRegistry = (SessionRegistry) repositoryContainer.getComponentInstance(SessionRegistry.class);
      if (sessionRegistry == null || sessionRegistry.isInUse(repo.getSystemWorkspaceName()))
        return false;

    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    }

    return true;
  }

  /**
   * Create repository. <br>
   * Init worksapces for initial start or them load from persistence. <br>
   * Add namespaces and nodetypes from service plugins.
   * 
   */
  public void createRepository(RepositoryEntry rEntry) throws RepositoryConfigurationException,
                                                      RepositoryException {
    if (repositoryContainers.containsKey(rEntry.getName()))
      throw new RepositoryConfigurationException("Repository container " + rEntry.getName()
          + " already started");

    RepositoryContainer repositoryContainer = new RepositoryContainer(parentContainer, rEntry);
    // Storing and starting the repository container under
    // key=repository_name
    repositoryContainers.put(rEntry.getName(), repositoryContainer);
    repositoryContainer.start();

    if (!config.getRepositoryConfigurations().contains(rEntry)) {
      config.getRepositoryConfigurations().add(rEntry);
    }
    addNamespaces(rEntry.getName());
    registerNodeTypes(rEntry.getName());

    // turn on Repository ONLINE
    ManageableRepository mr = (ManageableRepository) repositoryContainer.getComponentInstanceOfType(ManageableRepository.class);
    mr.setState(ManageableRepository.ONLINE);
  }

  public RepositoryServiceConfiguration getConfig() {
    return config;
  }

  public ManageableRepository getCurrentRepository() throws RepositoryException {
    if (currentRepositoryName.get() == null)
      return getDefaultRepository();
    return getRepository(currentRepositoryName.get());
  }

  public ManageableRepository getDefaultRepository() throws RepositoryException {
    return getRepository(config.getDefaultRepositoryName());
  }

  /**
   * @deprecated use getDefaultRepository() instead
   */
  @Deprecated
  public ManageableRepository getRepository() throws RepositoryException {
    return getDefaultRepository();
  }

  // ------------------- Startable ----------------------------

  public ManageableRepository getRepository(String name) throws RepositoryException {
    RepositoryContainer repositoryContainer = repositoryContainers.get(name);
    log.debug("RepositoryServiceimpl() getRepository " + name);
    if (repositoryContainer == null)
      throw new RepositoryException("Repository '" + name + "' not found.");

    return (ManageableRepository) repositoryContainer.getComponentInstanceOfType(ManageableRepository.class);
  }

  public void removeRepository(String name) throws RepositoryException {
    if (!canRemoveRepository(name))
      throw new RepositoryException("Repository " + name + " in use. If you want to "
          + " remove repository close all open sessions");

    try {
      RepositoryEntry repconfig = config.getRepositoryConfiguration(name);
      RepositoryImpl repo = (RepositoryImpl) getRepository(name);
      for (WorkspaceEntry wsEntry : repconfig.getWorkspaceEntries()) {
        repo.internalRemoveWorkspace(wsEntry.getName());
      }
      repconfig.getWorkspaceEntries().clear();
      RepositoryContainer repositoryContainer = repositoryContainers.get(name);
      repositoryContainer.stopContainer();
      repositoryContainer.stop();
      repositoryContainers.remove(name);
      config.getRepositoryConfigurations().remove(repconfig);
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public void setCurrentRepositoryName(String repositoryName) throws RepositoryConfigurationException {
    if (!repositoryContainers.containsKey(repositoryName))
      throw new RepositoryConfigurationException("Repository is not configured. Name "
          + repositoryName);
    currentRepositoryName.set(repositoryName);
  }

  public void start() {
    try {

      ExoContainer container = null;
      if (containerContext == null)
        container = PortalContainer.getInstance();
      else
        container = containerContext.getContainer();

      init(container);
    } catch (RepositoryException e) {
      log.error("Error start repository service", e);
    } catch (RepositoryConfigurationException e) {
//      log.error("Error start repository service", e);
    } catch (Throwable e) {
      log.error("Error start repository service", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    for (Entry<String, RepositoryContainer> entry : repositoryContainers.entrySet()) {
      entry.getValue().stop();
    }
    repositoryContainers.clear();
    addNamespacesPlugins.clear();
    addNodeTypePlugins.clear();
  }

//  private void addNamespaces() throws RepositoryException {
//
//    for (RepositoryEntry repoConfig : config.getRepositoryConfigurations()) {
//      addNamespaces(repoConfig.getName());
//    }
//  }

  private void addNamespaces(String repositoryName) throws RepositoryException {

    ManageableRepository repository = getRepository(repositoryName);
    NamespaceRegistry nsRegistry = repository.getNamespaceRegistry();

    for (int j = 0; j < addNamespacesPlugins.size(); j++) {
      AddNamespacesPlugin plugin = (AddNamespacesPlugin) addNamespacesPlugins.get(j);
      Map<String, String> namespaces = plugin.getNamespaces();
      try {
        for (Map.Entry<String, String> namespace : namespaces.entrySet()) {

          String prefix = namespace.getKey();
          String uri = namespace.getValue();

          // register namespace if not found
          try {
            nsRegistry.getURI(prefix);
          } catch (NamespaceException e) {
            nsRegistry.registerNamespace(prefix, uri);
          }
          log.info("Namespace is registered " + prefix + " = " + uri);
        }
      } catch (Exception e) {
        log.error("Error load namespaces ", e);
      }
    }
  }

  private void init(ExoContainer container) throws RepositoryConfigurationException,
                                           RepositoryException {
    this.parentContainer = container;
    List<RepositoryEntry> rEntries = config.getRepositoryConfigurations();
    for (int i = 0; i < rEntries.size(); i++) {
      RepositoryEntry rEntry = rEntries.get(i);
      // Making new repository container as portal's subcontainer
      createRepository(rEntry);
    }
  }

//  private void registerNodeTypes() throws RepositoryException {
//    for (RepositoryEntry repoConfig : config.getRepositoryConfigurations()) {
//      registerNodeTypes(repoConfig.getName());
//    }
//
//  }

  private void registerNodeTypes(String repositoryName) throws RepositoryException {
    ConfigurationManager configService = (ConfigurationManager) parentContainer.getComponentInstanceOfType(ConfigurationManager.class);

    ExtendedNodeTypeManager ntManager = getRepository(repositoryName).getNodeTypeManager();
    //
    for (int j = 0; j < addNodeTypePlugins.size(); j++) {
      AddNodeTypePlugin plugin = (AddNodeTypePlugin) addNodeTypePlugins.get(j);
      List<String> autoNodeTypesFiles = plugin.getNodeTypesFiles(AddNodeTypePlugin.AUTO_CREATED);
      if (autoNodeTypesFiles != null && autoNodeTypesFiles.size() > 0) {
        for (String nodeTypeFilesName : autoNodeTypesFiles) {

          InputStream inXml;
          try {
            inXml = configService.getInputStream(nodeTypeFilesName);
          } catch (Exception e) {
            throw new RepositoryException(e);
          }
          log.info("Trying register node types from xml-file " + nodeTypeFilesName);
          ntManager.registerNodeTypes(inXml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
          log.info("Node types is registered from xml-file " + nodeTypeFilesName);
        }
        List<String> defaultNodeTypesFiles = plugin.getNodeTypesFiles(repositoryName);
        if (defaultNodeTypesFiles != null && defaultNodeTypesFiles.size() > 0) {
          for (String nodeTypeFilesName : defaultNodeTypesFiles) {

            InputStream inXml;
            try {
              inXml = configService.getInputStream(nodeTypeFilesName);
            } catch (Exception e) {
              throw new RepositoryException(e);
            }
            log.info("Trying register node types (" + repositoryName + ") from xml-file "
                + nodeTypeFilesName);
            ntManager.registerNodeTypes(inXml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
            log.info("Node types is registered (" + repositoryName + ") from xml-file "
                + nodeTypeFilesName);
          }
        }
      }
    }
  }
	
}
