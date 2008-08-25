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

package org.exoplatform.services.wcm.search.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.search.WcmSearchService;

/**
 * Created by The eXo Platform SARL Author : Pham Xuan Hoa
 * hoa.pham@exoplatform.com Mar 19, 2008
 */
public class WcmSearchServiceImpl implements WcmSearchService {

  private RepositoryService                 repositoryService;

  private String                            defaultRepository;

  private String                            defaultWorksapce;

  private DataStorage                       dataStorage_;

  private HashMap<String, String>           cachedPages_ = new HashMap<String, String>();

  private LivePortalManagerService          livePortalManagerService;

  private WebSchemaConfigService            webSchemaConfigService;

  private ThreadLocalSessionProviderService localSessionProviderService;

  public WcmSearchServiceImpl(RepositoryService repositoryService, DataStorage dataStorage,
      InitParams initParams) {
    this.repositoryService = repositoryService;
    this.dataStorage_ = dataStorage;
    PropertiesParam serviceParams = initParams.getPropertiesParam("service-params");
    defaultRepository = serviceParams.getProperty("defaultRepository");
    defaultWorksapce = serviceParams.getProperty("defaultWorksapce");
  }

  public PageList searchWebContent(String keyword, String portalName, boolean documentSearch,
      boolean pageSearch, SessionProvider sessionProvider) throws Exception {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    livePortalManagerService = (LivePortalManagerService) exoContainer
    .getComponentInstanceOfType(LivePortalManagerService.class);
    webSchemaConfigService = (WebSchemaConfigService) exoContainer
    .getComponentInstanceOfType(WebSchemaConfigService.class);
    localSessionProviderService = (ThreadLocalSessionProviderService) exoContainer
    .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    return searchWebContent(keyword, portalName, defaultRepository, defaultWorksapce,
        documentSearch, pageSearch, sessionProvider);
  }

  public PageList searchWebContent(String keyword, String portalName, String repository,
      String worksapce, boolean documentSeach, boolean pageSearch, SessionProvider sessionProvider)
  throws Exception {
    if (documentSeach && pageSearch) {
      return searchDocumentsAndPages(keyword, portalName, repository, worksapce, sessionProvider);
    } else if (documentSeach) {
      return searchDocuments(keyword, portalName, repository, worksapce, sessionProvider);
    }
    return searchPortalPage(keyword, portalName, repository, worksapce, sessionProvider);
  }

  public void updatePagesCache() throws Exception {
    org.exoplatform.portal.config.Query<Page> pagesQuery = new org.exoplatform.portal.config.Query<Page>(
        null, null, null, Page.class);
    List<Page> allPages = dataStorage_.find(pagesQuery).getAll();
    for (Page page : allPages) {
      String portletId = null;
      for (Object obj : page.getChildren()) {
        if (obj instanceof Application) {
          Application application = (Application) obj;
          String applicationId = application.getInstanceId();
          if (!isWebContentApplication(applicationId))
            continue;
          portletId = applicationId;
          break;
        }
      }
      if (portletId != null) {           
        updateCachedPage(page.getPageId(), portletId);
      }
    }
  }

  private PageList searchDocumentsAndPages(String keyword, String portalName, String repository,
      String workspace, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    List<Object> container = new ArrayList<Object>();
    if (portalName != null) {
      List<Object> documents = findDocuments(queryManager, keyword, portalName, repository);
      List<Object> pageNodes = findPortalPages(keyword, portalName, repository, workspace,
          sessionProvider);
      container.addAll(documents);
      container.addAll(pageNodes);
      return new ObjectPageList(container, 10);
    }
    for (String portal : getPortalNames()) {
      List<Object> documents = findDocuments(queryManager, keyword, portal, repository);
      List<Object> pageNodes = findPortalPages(keyword, portal, repository, workspace,
          sessionProvider);
      container.addAll(documents);
      container.addAll(pageNodes);
    }
    return new ObjectPageList(container, 10);
  }

  private PageList searchDocuments(String keyword, String portalName, String repository,
      String workspace, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    if (portalName != null) {
      return new ObjectPageList(findDocuments(queryManager, keyword, portalName, repository), 10);
    }
    List<Object> allDocuments = new ArrayList<Object>();
    for (String portal : getPortalNames()) {
      List<Object> list = findDocuments(queryManager, keyword, portal, repository);
      allDocuments.addAll(list);
    }
    return new ObjectPageList(allDocuments, 10);
  }

  private List<String> getPortalNames() throws Exception {
    org.exoplatform.portal.config.Query<PortalConfig> query = new org.exoplatform.portal.config.Query<PortalConfig>(
        null, null, null, PortalConfig.class);
    List<PortalConfig> list = dataStorage_.find(query).getAll();
    List<String> portalNames = new ArrayList<String>();
    for (PortalConfig config : list) {
      portalNames.add(config.getName());
    }
    return portalNames;
  }

  private List<Object> findDocuments(QueryManager queryManager, String keyword, String portalName,
      String repository) throws Exception {
    Node webContentStorage = getWebContentStorage(portalName);
    String sql = "select * from nt:base where contains(*,'" + keyword + "') and jcr:path like '"
    + webContentStorage.getPath() + "/%' order by exo:dateCreated DESC";
    Query query = queryManager.createQuery(sql, Query.SQL);
    QueryResult queryResult = query.execute();
    HashSet<Node> hashSet = new HashSet<Node>();
    for (NodeIterator iterator = queryResult.getNodes(); iterator.hasNext();) {
      Node node = iterator.nextNode();
      if (node.getPrimaryNodeType().isNodeType("nt:resource"))
        node = node.getParent();
      hashSet.add(node);
    }
    List<Object> documents = Arrays.asList(hashSet.toArray());
    return documents;
  }

  private PageList searchPortalPage(String keyword, String portalName, String repository,
      String workspace, SessionProvider sessionProvider) throws Exception {
    if (portalName != null) {
      List<Object> list = findPortalPages(keyword, portalName, repository, workspace,
          sessionProvider);
      return new ObjectPageList(list, 10);
    }
    List<Object> container = new ArrayList<Object>();
    for (String portal : getPortalNames()) {
      List<Object> list = findPortalPages(keyword, portal, repository, workspace, sessionProvider);
      container.addAll(list);
    }
    return new ObjectPageList(container, 10);
  }

  private List<Object> findPortalPages(String keyword, String portalName, String repository,
      String workspace, SessionProvider sessionProvider) throws Exception {
    Node webContentStorage = getWebContentStorage(portalName);
    String sql = "select * from nt:base where contains(*,'" + keyword + "') and jcr:path like '"
    + webContentStorage.getPath() + "/%' order by exo:dateCreated DESC";
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String userId = session.getUserID();
    if (SystemIdentity.ANONIM.endsWith(userId))
      userId = null;
    Query query = queryManager.createQuery(sql, Query.SQL);
    QueryResult queryResult = query.execute();
    List<Object> pageNodes = new ArrayList<Object>();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService portalConfigService = (UserPortalConfigService) container
    .getComponentInstanceOfType(UserPortalConfigService.class);
    UserPortalConfig userPortalConfig = portalConfigService.getUserPortalConfig(portalName, userId);
    for (NodeIterator iterator = queryResult.getNodes(); iterator.hasNext();) {
      Node webContent = iterator.nextNode();
      String key = null;
      if (webContent.getPrimaryNodeType().isNodeType("nt:resource")) {
        Node documentNode = webContent.getParent();
        key = buildKey(repository, workspace, documentNode.getParent().getUUID());
      } else {
        key = buildKey(repository, workspace, webContent.getUUID());
      }
      String referencedPage = cachedPages_.get(key);
      if (!hasAccessPermission(referencedPage, userId, portalConfigService))
        continue;
      pageNodes.addAll(findPageNodes(userPortalConfig, referencedPage));
    }
    return pageNodes;
  }

  private List<PageNode> findPageNodes(UserPortalConfig userPortalConfig, String pageReferencedId)
  throws Exception {
    List<PageNode> result = new ArrayList<PageNode>();
    for (PageNavigation navigation : userPortalConfig.getNavigations()) {
      result.addAll(filter(navigation, pageReferencedId));
    }
    return result;
  }

  private boolean hasAccessPermission(String pageId, String accessUser,
      UserPortalConfigService portalConfigService) throws Exception {
    if (pageId == null)
      return false;
    Page page = portalConfigService.getPage(pageId, accessUser);
    if (page != null)
      return true;
    return false;
  }

  private List<PageNode> filter(PageNavigation nav, String pageReferencedId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>();
    for (PageNode node : nav.getNodes()) {
      filter(node, pageReferencedId, list);
    }
    return list;
  }

  private void filter(PageNode node, String pageReferencedId, List<PageNode> allPageNode)
  throws Exception {
    if (pageReferencedId.equals(node.getPageReference())) {
      allPageNode.add(node.clone());
    }
    List<PageNode> children = node.getChildren();
    if (children == null)
      return;
    for (PageNode child : children) {
      filter(child, pageReferencedId, allPageNode);
    }
  }

  private boolean isWebContentApplication(String applicationId) {
    // TODO this code use for SimpleContentPresentation portlet
    if (applicationId.contains("/web-presentation/AdvancedPresentationPortlet/"))
      return true;
    return false;
  }

  private void updateCachedPage(String pageId, String portletId) throws Exception {
    PortletPreferences portletPreferences = dataStorage_.getPortletPreferences(new ExoWindowID(
        portletId));
    String repository = null, worksapce = null, nodeUUID = null;
    // TODO this code use for SimpleContentPresentation portlet
    for (Object obj : portletPreferences.getPreferences()) {
      Preference preference = (Preference) obj;
      if ("repository".equals(preference.getName())) {
        repository = (String) preference.getValues().get(0);
      } else if ("workspace".equals(preference.getName())) {
        worksapce = (String) preference.getValues().get(0);
      } else if ("nodeUUID".equals(preference.getName())) {
        nodeUUID = (String) preference.getValues().get(0);
      }
    }
    if (repository == null || worksapce == null || nodeUUID == null)
      return;
    String key = buildKey(repository, worksapce, nodeUUID);
    cachedPages_.put(key, pageId);
  }

  private String buildKey(String repository, String worksapce, String nodeUUID) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(repository).append("::").append(worksapce).append("::").append(nodeUUID);
    return stringBuffer.toString();
  }

  public Node getAssociatedDocument(PageNode pageNode, SessionProvider sessionProvider)
  throws Exception {
    String pageReferenced = pageNode.getPageReference();
    String nodeInfo = null;
    for (Iterator<String> iterator = cachedPages_.keySet().iterator(); iterator.hasNext();) {
      String key = iterator.next();
      String value = cachedPages_.get(key);
      if (value.equals(pageReferenced)) {
        nodeInfo = key;
        break;
      }
    }
    if (nodeInfo == null)
      return null;
    String[] temp = nodeInfo.split("::");
    String repository = temp[0];
    String worksapce = temp[1];
    String nodeUUID = temp[2];
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(worksapce, manageableRepository);
    return session.getNodeByUUID(nodeUUID);
  }

  private Node getSelectedPortal(String portalName) throws Exception {
    List<Node> livePortals = livePortalManagerService.getLivePortals(localSessionProviderService
        .getSessionProvider(null));
    for (Node portal : livePortals) {
      if (portal.getName().equals(portalName)) {
        return portal;
      }
    }
    return null;
  }

  private Node getWebContentStorage(String portalName) throws Exception {
    Node portal = getSelectedPortal(portalName);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService
    .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return portalFolderSchemaHandler.getWebContentStorage(portal);
  }

}
