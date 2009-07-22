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
package org.exoplatform.services.wcm.publication.listener.navigation;

import java.util.ArrayList;
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
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationUtil;

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
    UserPortalConfigService userPortalConfigService = StageAndVersionPublicationUtil.getServices(UserPortalConfigService.class);
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    for (PageNode pageNode : pageNavigation.getNodes()) {
      Page page = userPortalConfigService.getPage(pageNode.getPageReference(), org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
      if (page != null) {
        for (String applicationId : StageAndVersionPublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))) {
          Node content = StageAndVersionPublicationUtil.getNodeByApplicationId(applicationId);
          if (content != null) {
            List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs"); 
            if (!listExistedApplicationId.contains(StageAndVersionPublicationUtil.setMixedApplicationId(page.getPageId(), applicationId))) {
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
      String mixedNavigationNodeUri = StageAndVersionPublicationUtil.setMixedNavigationUri(portalName, portalPageNode.getUri());
      listPortalNavigationUri.add(mixedNavigationNodeUri);
      listPageReference.add(portalPageNode.getPageReference());
    }

    RepositoryService repositoryService = StageAndVersionPublicationUtil.getServices(RepositoryService.class);
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = wcmConfigurationService.getLivePortalsLocation(repository.getConfiguration().getName());

    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));

    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from publication:wcmPublication where publication:lifecycleName='" + lifecycleName + "' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      Node content = nodeIterator.nextNode();
      String navigationNodeUri = "";
      for (String existedNavigationNodeUri : StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs")) {
        if (existedNavigationNodeUri.startsWith("/" + portalName) && !listPortalNavigationUri.contains(existedNavigationNodeUri)) {
          navigationNodeUri = existedNavigationNodeUri;
        }
      }

      String pageId = "";
      for (String existedPageId : StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs")) {
        if (!listPageReference.contains(existedPageId) && !listPortalNavigationUri.contains(navigationNodeUri)) {
          pageId = existedPageId;
        }
      }

      if (!pageId.equals("")) {
        String applicationId = "";
        UserPortalConfigService userPortalConfigService = StageAndVersionPublicationUtil.getServices(UserPortalConfigService.class);
        Page page = userPortalConfigService.getPage(pageId, org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
        for (String applicationIdTmp : StageAndVersionPublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))) {
          applicationIdTmp = StageAndVersionPublicationUtil.setMixedApplicationId(pageId, applicationIdTmp);
          List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
          if (listExistedApplicationId.contains(applicationIdTmp)) {
            applicationId = applicationIdTmp;
          }
        }

        saveRemovedPageNode(navigationNodeUri, pageId, applicationId, content);
      }
    }
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
    PublicationService publicationService = StageAndVersionPublicationUtil.getServices(PublicationService.class);                 
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) { return; }
    if (!lifecycleName.equals(nodeLifecycleName)) return;

    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    String mixedApplicationId = StageAndVersionPublicationUtil.setMixedApplicationId(pageNode.getPageReference(), applicationId);
    if(listExistedApplicationId.contains(mixedApplicationId)) return ;        
    listExistedApplicationId.add(mixedApplicationId);
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedApplicationId));

    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");    
    String mixedNavigationNodeUri = StageAndVersionPublicationUtil.setMixedNavigationUri(portalName, pageNode.getUri());
    listExistedNavigationNodeUri.add(mixedNavigationNodeUri);    
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));

    List<String> listExistedWebPageId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    listExistedWebPageId.add(pageNode.getPageReference());
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedWebPageId));

//    WCMPublicationService presentationService = Util.getServices(WCMPublicationService.class);
//    StageAndVersionBasedPublicationPlugin publicationPlugin = (StageAndVersionBasedPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(Constant.LIFECYCLE_NAME);
//    HashMap<String,String> context = new HashMap<String,String>();
//    context.put(Constant.CURRENT_VERSION_NAME,content.getName()); 
//    publicationPlugin.changeState(content, Constant.LIVE, context);

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

    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");    
    if (listExistedNavigationNodeUri.contains(navigationNodeUri)) {
      listExistedNavigationNodeUriTmp.remove(navigationNodeUri);            
    }
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUriTmp));

    List<String> listExistedPageId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    List<String> listExistedPageIdTmp = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    if (listExistedPageId.contains(pageId)) {
      listExistedPageIdTmp.remove(pageId);
    }
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedPageIdTmp));

    List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    List<String> listExistedApplicationIdTmp = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    if (listExistedApplicationId.contains(applicationId)) {
      listExistedApplicationIdTmp.remove(applicationId);
    }
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedApplicationIdTmp));

//    if (listExistedNavigationNodeUriTmp.isEmpty()) {
//      WCMPublicationService presentationService = Util.getServices(WCMPublicationService.class);
//      StageAndVersionBasedPublicationPlugin publicationPlugin = (StageAndVersionBasedPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(Constant.LIFECYCLE_NAME);
//      HashMap<String,String> context = new HashMap<String,String>();
//      context.put(Constant.CURRENT_VERSION_NAME,content.getName());
//      publicationPlugin.changeState(content, Constant.DRAFT, context);
//    }

    session.save();
  }
}