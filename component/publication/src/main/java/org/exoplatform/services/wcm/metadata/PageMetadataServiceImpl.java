/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.wcm.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 3, 2008  
 */
public class PageMetadataServiceImpl implements PageMetadataService, Startable {
  private static Log log = ExoLogger.getLogger(PageMetadataServiceImpl.class); 
  private HashSet<String> publishingNavigationNodeURIs = new HashSet<String>();  
  private ExoCache pageMetadataCache;   
  private RepositoryService repositoryService;
  private LivePortalManagerService livePortalManagerService;
  private WCMConfigurationService configurationService;

  public PageMetadataServiceImpl(RepositoryService repositoryService, LivePortalManagerService livePortalManagerService, WCMConfigurationService configurationService,CacheService cacheService) throws Exception {
    this.repositoryService = repositoryService;
    this.pageMetadataCache = cacheService.getCacheInstance(PageMetadataServiceImpl.class.getName());
    this.livePortalManagerService = livePortalManagerService;
    this.configurationService = configurationService;
  }    

  public Map<String,String> getMetadata(String pageUri,SessionProvider sessionProvider) throws Exception {
    HashMap<String,String> metadata = (HashMap<String,String>)pageMetadataCache.get(pageUri);
    if(metadata != null) return metadata;
    if(!publishingNavigationNodeURIs.contains(pageUri)) 
      throw new UnsupportedOperationException("the service unsupported action for page with URI"+ pageUri);        
    Node content = findNodeByNavigationNodeURI(pageUri,sessionProvider);
    if(content == null)
      return metadata;
    metadata = extractMetadata(content);
    addMetadata(pageUri,metadata);
    return metadata;
  }

  private Node findNodeByNavigationNodeURI(String uri, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();
    String repository = manageableRepository.getConfiguration().getName();
    NodeLocation storageLocation = configurationService.getLivePortalsLocation(repository);
    String queryStatement = "select * from publication:webpagesPublication where jcr:path like '" +
    storageLocation.getPath() + "/%' and publication:currentState='Published' and publication:navigationNodeURIs like '" + uri + "'";
    Session session = sessionProvider.getSession(storageLocation.getWorkspace(),manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryStatement,Query.SQL);    
    NodeIterator nodeIterator = query.execute().getNodes();
    if(nodeIterator.getSize() == 0)
      return null;
    return nodeIterator.nextNode();
  }


  public HashMap<String, String> extractMetadata(Node node) throws Exception {
    HashMap<String, String> medatata = new HashMap<String,String>();
    Node portalNode = findPortal(node);
    NodeTypeManager nodeTypeManager = node.getSession().getWorkspace().getNodeTypeManager();
    String siteTitle = null;
    if(portalNode != null) {
      siteTitle = getProperty(portalNode,"metadata:siteTitle");
      if(siteTitle == null) siteTitle = portalNode.getName();
      NodeType siteMedata = nodeTypeManager.getNodeType("metadata:siteMetadata");
      for(PropertyDefinition pdef: siteMedata.getDeclaredPropertyDefinitions()) {
        String metadataName = pdef.getName();
        String metadataValue = getProperty(portalNode,metadataName);
        if(metadataValue != null) 
          medatata.put(metadataName,metadataValue);              
      }
      medatata.remove("metadata:siteTitle");
    }
    String pageTitle = getProperty(node,"exo:title");
    if(pageTitle == null)
      pageTitle = node.getName();    
    if(siteTitle != null) {
      pageTitle = pageTitle + "-" + siteTitle;
    }    
    String description = getProperty(node,"exo:summary");    
    medatata.put(PAGE_TITLE,pageTitle);
    if(description != null) {
      medatata.put("description",description); 
    }    
    return medatata;
  }

  private String getProperty(Node node, String propertyName) throws Exception {
    return node.hasProperty(propertyName)?node.getProperty(propertyName).getString():null;
  }

  protected Node findPortal(Node child) throws Exception{                       
    try {
      return livePortalManagerService.getLivePortalByChild(child);
    } catch (Exception e) {
    }
    return null;
  }

  public void addMetadata(String pageURI, HashMap<String, String> metadata) throws Exception {
    publishingNavigationNodeURIs.add(pageURI);
    pageMetadataCache.put(pageURI,metadata);
  }

  public void removeMetadata(String pageURI) throws Exception {
    publishingNavigationNodeURIs.remove(pageURI);
    pageMetadataCache.remove(pageURI);
  }

  private void initialize(String repoName, SessionProvider sessionProvider) throws Exception {    
    NodeLocation siteContentStorage = configurationService.getLivePortalsLocation(repoName);
    String queryStatement = "select * from publication:webpagesPublication where jcr:path like '" +
    siteContentStorage.getPath() + "/%' and publication:currentState='Published'";
    ManageableRepository manageableRepository = repositoryService.getRepository(repoName);
    Session session = sessionProvider.getSession(siteContentStorage.getWorkspace(),manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryStatement,Query.SQL);
    for(NodeIterator iterator = query.execute().getNodes();iterator.hasNext();) {
      Node node = iterator.nextNode();
      if(!node.hasProperty("publication:navigationNodeURIs")) 
        continue;
      Value[]values = node.getProperty("publication:navigationNodeURIs").getValues();
      if(values.length == 0)
        continue;        
      HashMap<String,String> metadata = extractMetadata(node);
      for(Value value: values) {
        addMetadata(value.getString(),metadata);
      }
    }    
  }

  public void start() {
    log.info("Starting PageMetadataService...");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    for(RepositoryEntry repositoryEntry : repositoryService.getConfig().getRepositoryConfigurations()) {
      try {
        initialize(repositoryEntry.getName(),sessionProvider);
      } catch (Exception e) {
        log.error("Exception when initialize metadata for reoisitory: "+ repositoryEntry.getName(),e);
      }
    }
    sessionProvider.close();
  }

  public void stop() {
    log.info("Stoping PageMetadataService...");
  }         
}
