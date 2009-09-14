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
package org.exoplatform.services.wcm.publication.lifecycle.simple;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.PublicationUtil;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Oct 6, 2008
 */
public class NavigationEventListenerDelegate {

  /** The lifecycle name. */
  private String lifecycleName;

  /**
   * Instantiates a new navigation event listener delegate.
   * 
   * @param lifecycleName the lifecycle name
   * @param container the container
   */
  public NavigationEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
  }

  /**
   * Update lifecyle on create navigation.
   * 
   * @param pageNavigation the page navigation
   * 
   * @throws Exception the exception
   */
  public void updateLifecyleOnCreateNavigation(PageNavigation pageNavigation) throws Exception {
    // TODO: Don't support in this version
  }

  /**
   * Update lifecycle on change navigation.
   * 
   * @param pageNavigation the page navigation
   * 
   * @throws Exception the exception
   */
  public void updateLifecycleOnChangeNavigation(PageNavigation pageNavigation) throws Exception {
    if (pageNavigation.getOwnerType().equals(PortalConfig.PORTAL_TYPE)) {
      updateRemovedPageNode(pageNavigation);
      updateAddedPageNode(pageNavigation);
    }
  }

  /**
   * Update lifecyle on remove navigation.
   * 
   * @param pageNavigation the page navigation
   * 
   * @throws Exception the exception
   */
  public void updateLifecyleOnRemoveNavigation(PageNavigation pageNavigation) throws Exception {
    // TODO: Don't support in this version
  }

  /**
   * Update added page node.
   * 
   * @param pageNavigation the page navigation
   * 
   * @throws Exception the exception
   */
  private void updateAddedPageNode(PageNavigation pageNavigation) throws Exception {
  	WCMConfigurationService wcmConfigurationService = PublicationUtil.getServices(WCMConfigurationService.class);
  	UserPortalConfigService userPortalConfigService = PublicationUtil.getServices(UserPortalConfigService.class);
    for (PageNode pageNode : pageNavigation.getNodes()) {
      Page page = userPortalConfigService.getPage(pageNode.getPageReference(), org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
      if (page != null) {
        for (String applicationId : PublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))) {
          Node content = PublicationUtil.getNodeByApplicationId(applicationId);
          if (content != null) {
            List<String> listExistedApplicationId = PublicationUtil.getValuesAsString(content, "publication:applicationIDs"); 
            if (!listExistedApplicationId.contains(PublicationUtil.setMixedApplicationId(page.getPageId(), applicationId))) {
              saveAddedPageNode(pageNavigation.getOwnerId(), pageNode, applicationId, content);
            }
          }
        }
      }
    }
  }

  /**
   * Update removed page node.
   * 
   * @param pageNavigation the page navigation
   * 
   * @throws Exception the exception
   */
  private void updateRemovedPageNode(PageNavigation pageNavigation) throws Exception {
    String portalName = pageNavigation.getOwnerId();
    List<PageNode> listPortalPageNode = pageNavigation.getNodes();
    List<String> listPortalNavigationUri = new ArrayList<String>();
    List<String> listPageReference = new ArrayList<String>();
    for (PageNode portalPageNode : listPortalPageNode) {
      String mixedNavigationNodeUri = PublicationUtil.setMixedNavigationUri(portalName, portalPageNode.getUri());
      listPortalNavigationUri.add(mixedNavigationNodeUri);
      listPageReference.add(portalPageNode.getPageReference());
    }

    RepositoryService repositoryService = PublicationUtil.getServices(RepositoryService.class);
    WCMConfigurationService configurationService = PublicationUtil.getServices(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository.getConfiguration().getName());

    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));

    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from publication:simplePublication where publication:lifecycleName='" + lifecycleName + "' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      Node content = nodeIterator.nextNode();
      String navigationNodeUri = "";
      for (String existedNavigationNodeUri : PublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs")) {
        if (existedNavigationNodeUri.startsWith("/" + portalName) && !listPortalNavigationUri.contains(existedNavigationNodeUri)) {
          navigationNodeUri = existedNavigationNodeUri;
        }
      }

      String pageId = "";
      for (String existedPageId : PublicationUtil.getValuesAsString(content, "publication:webPageIDs")) {
        if (!listPageReference.contains(existedPageId) && !listPortalNavigationUri.contains(navigationNodeUri)) {
          pageId = existedPageId;
        }
      }

      if (!pageId.equals("")) {
        String applicationId = "";
        UserPortalConfigService userPortalConfigService = PublicationUtil.getServices(UserPortalConfigService.class);
        Page page = userPortalConfigService.getPage(pageId, org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
        for (String applicationIdTmp : PublicationUtil.getListApplicationIdByPage(page, configurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))) {
          applicationIdTmp = PublicationUtil.setMixedApplicationId(pageId, applicationIdTmp);
          List<String> listExistedApplicationId = PublicationUtil.getValuesAsString(content, "publication:applicationIDs");
          if (listExistedApplicationId.contains(applicationIdTmp)) {
            applicationId = applicationIdTmp;
          }
        }
        saveRemovedPageNode(navigationNodeUri, pageId, applicationId, content);
      }
    }
    sessionProvider.close();
  }

  /**
   * Save added page node.
   * 
   * @param portalName the portal name
   * @param pageNode the page node
   * @param applicationId the application id
   * @param content the content
   * 
   * @throws Exception the exception
   */
  private void saveAddedPageNode(String portalName, PageNode pageNode, String applicationId, Node content) throws Exception {    
    PublicationService publicationService = PublicationUtil.getServices(PublicationService.class);                 
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) { return; }
    if (!lifecycleName.equals(nodeLifecycleName)) return;

    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> listExistedApplicationId = PublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    String mixedApplicationId = PublicationUtil.setMixedApplicationId(pageNode.getPageReference(), applicationId);
    if(listExistedApplicationId.contains(mixedApplicationId)) return ;        
    listExistedApplicationId.add(mixedApplicationId);
    content.setProperty("publication:applicationIDs", PublicationUtil.toValues(valueFactory, listExistedApplicationId));

    List<String> listExistedNavigationNodeUri = PublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");    
    String mixedNavigationNodeUri = PublicationUtil.setMixedNavigationUri(portalName, pageNode.getUri());
    listExistedNavigationNodeUri.add(mixedNavigationNodeUri);    
    String nodeURILogs = mixedNavigationNodeUri + PublicationUtil.HISTORY_SEPARATOR;
    content.setProperty("publication:navigationNodeURIs", PublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));

    List<String> listExistedWebPageId = PublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    listExistedWebPageId.add(pageNode.getPageReference());
    content.setProperty("publication:webPageIDs", PublicationUtil.toValues(valueFactory, listExistedWebPageId));

    WCMPublicationService presentationService = PublicationUtil.getServices(WCMPublicationService.class);
    SimplePublicationPlugin publicationPlugin = (SimplePublicationPlugin) presentationService.getWebpagePublicationPlugins().get(SimplePublicationPlugin.LIFECYCLE_NAME);
    publicationPlugin.changeState(content, "published", null);

    String[] logs = new String[] {new Date().toString(), PublicationDefaultStates.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodePublished", nodeURILogs};
    publicationService.addLog(content, logs);
    session.save();
  }

  /**
   * Save removed page node.
   * 
   * @param navigationNodeUri the navigation node uri
   * @param pageId the page id
   * @param applicationId the application id
   * @param content the content
   * 
   * @throws Exception the exception
   */
  private void saveRemovedPageNode(String navigationNodeUri, String pageId, String applicationId, Node content) throws Exception {
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> listExistedNavigationNodeUri = PublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = PublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");    
    if (listExistedNavigationNodeUri.contains(navigationNodeUri)) {
      listExistedNavigationNodeUriTmp.remove(navigationNodeUri);            
    }
    content.setProperty("publication:navigationNodeURIs", PublicationUtil.toValues(valueFactory, listExistedNavigationNodeUriTmp));

    List<String> listExistedPageId = PublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    List<String> listExistedPageIdTmp = PublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    if (listExistedPageId.contains(pageId)) {
      listExistedPageIdTmp.remove(pageId);
    }
    content.setProperty("publication:webPageIDs", PublicationUtil.toValues(valueFactory, listExistedPageIdTmp));

    List<String> listExistedApplicationId = PublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    List<String> listExistedApplicationIdTmp = PublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    if (listExistedApplicationId.contains(applicationId)) {
      listExistedApplicationIdTmp.remove(applicationId);
    }
    content.setProperty("publication:applicationIDs", PublicationUtil.toValues(valueFactory, listExistedApplicationIdTmp));

    String nodeURILogs = "";
    if (!navigationNodeUri.equals("")) {
      nodeURILogs = navigationNodeUri + PublicationUtil.HISTORY_SEPARATOR;
      String[] logs = new String[] {new Date().toString(), PublicationDefaultStates.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodeRemoved", nodeURILogs};
      PublicationService publicationService = PublicationUtil.getServices(PublicationService.class);
      publicationService.addLog(content, logs);
    }

    if (listExistedNavigationNodeUriTmp.isEmpty()) {
      WCMPublicationService presentationService = PublicationUtil.getServices(WCMPublicationService.class);
      SimplePublicationPlugin publicationPlugin = (SimplePublicationPlugin) presentationService.getWebpagePublicationPlugins().get(SimplePublicationPlugin.LIFECYCLE_NAME);
      publicationPlugin.changeState(content, "unpublished", null);
    }
    session.save();
  }
}