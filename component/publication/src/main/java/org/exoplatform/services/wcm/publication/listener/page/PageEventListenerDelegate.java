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
package org.exoplatform.services.wcm.publication.listener.page;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationUtil;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Oct 6, 2008
 */
public class PageEventListenerDelegate {

  /** The lifecycle name. */
  private String lifecycleName;

  /**
   * Instantiates a new page event listener delegate.
   * 
   * @param lifecycleName the lifecycle name
   * @param container the container
   */
  public PageEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
  }

  /**
   * Update lifecyle on create page.
   * 
   * @param page the page
   * @param remoteUser TODO
   * 
   * @throws Exception the exception
   */
  public void updateLifecyleOnCreatePage(Page page, String remoteUser) throws Exception { 
    updateAddedApplication(page, remoteUser);
  }

  /**
   * Update lifecyle on change page.
   * 
   * @param page the page
   * @param remoteUser TODO
   * 
   * @throws Exception the exception
   */
  public void updateLifecyleOnChangePage(Page page, String remoteUser) throws Exception {
    updateAddedApplication(page, remoteUser);
    updateRemovedApplication(page, remoteUser);
  }

  /**
   * Update lifecycle on remove page.
   * 
   * @param page the page
   * @param remoteUser TODO
   * 
   * @throws Exception the exception
   */
  public void updateLifecycleOnRemovePage(Page page, String remoteUser) throws Exception {
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    List<String> listPageApplicationId = StageAndVersionPublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET));
    for (String applicationId : listPageApplicationId) {
      Node content = StageAndVersionPublicationUtil.getNodeByApplicationId(applicationId);
      if (content != null) {
        saveRemovedApplication(page, applicationId, content, remoteUser);
      }
    }
  }

  /**
   * Update added application.
   * 
   * @param page the page
   * @param remoteUser TODO
   * 
   * @throws Exception the exception
   */
  private void updateAddedApplication(Page page, String remoteUser) throws Exception {
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    List<String> listPageApplicationId = StageAndVersionPublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET));
    for (String applicationtId : listPageApplicationId) {
      Node content = StageAndVersionPublicationUtil.getNodeByApplicationId(applicationtId);
      if (content != null) saveAddedApplication(page, applicationtId, content, lifecycleName, remoteUser);
    }
  }

  /**
   * Update removed application.
   * 
   * @param page the page
   * @param remoteUser TODO
   * 
   * @throws Exception the exception
   */
  private void updateRemovedApplication(Page page, String remoteUser) throws Exception {
    List<Node> listNode = getListNodeByApplicationId(page);
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    List<String> listApplicationId = StageAndVersionPublicationUtil.getListApplicationIdByPage(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET));
    for (Node content : listNode) {
      for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
        String[] tmp = StageAndVersionPublicationUtil.parseMixedApplicationId(value.getString());
        String nodeApplicationId = tmp[1];
        if (tmp[0].equals(page.getPageId()) && !listApplicationId.contains(nodeApplicationId)) {
          saveRemovedApplication(page, nodeApplicationId, content, remoteUser);
        }
      }
    }
  }

  /**
   * Gets the list node by application id.
   * 
   * @param page the page
   * 
   * @return the list node by application id
   * 
   * @throws Exception the exception
   */
  private List<Node> getListNodeByApplicationId(Page page) throws Exception {
    RepositoryService repositoryService = StageAndVersionPublicationUtil.getServices(RepositoryService.class);
    WCMConfigurationService configurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository.getConfiguration().getName());

    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));

    List<Node> listPublishedNode = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from " + StageAndVersionPublicationConstant.PUBLICATION_LIFECYCLE_TYPE + " where publication:lifecycleName='" + lifecycleName + "' and publication:webPageIDs like '%" + page.getPageId() + "%' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      listPublishedNode.add(nodeIterator.nextNode());
    }
    return listPublishedNode;
  }

  /**
   * Save added application.
   * 
   * @param page the page
   * @param applicationId the application id
   * @param content the content
   * @param lifecycleName the lifecycle name
   * @param remoteUser TODO
   * @throws Exception the exception
   */
  private void saveAddedApplication(Page page, String applicationId, Node content, String lifecycleName, String remoteUser) throws Exception {    
    PublicationService publicationService = StageAndVersionPublicationUtil.getServices(PublicationService.class);                 
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) { return; }
    if (!lifecycleName.equals(nodeLifecycleName)) return;

    WCMPublicationService presentationService = StageAndVersionPublicationUtil.getServices(WCMPublicationService.class);
    StageAndVersionPublicationPlugin publicationPlugin = (StageAndVersionPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    if (content.canAddMixin("publication:webpagesPublication")) 
    	content.addMixin("publication:webpagesPublication");
    
    List<String> nodeAppIds = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    String mixedAppId = StageAndVersionPublicationUtil.setMixedApplicationId(page.getPageId(), applicationId);
    if(nodeAppIds.contains(mixedAppId))
      return;

    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");    
    List<String> listPageNavigationUri = publicationPlugin.getListPageNavigationUri(page, remoteUser);
    if (listPageNavigationUri.isEmpty())  {
      return ;
    }            
    for (String uri : listPageNavigationUri) {
//      if(!listExistedNavigationNodeUri.contains(uri)) {
        listExistedNavigationNodeUri.add(uri);                           
//      }            
    }                   
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));

    List<String> nodeWebPageIds = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    nodeWebPageIds.add(page.getPageId());
    nodeAppIds.add(mixedAppId);
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, nodeAppIds));
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, nodeWebPageIds));
//    HashMap<String,String> context = new HashMap<String,String>();
//    context.put(Constant.CURRENT_VERSION_NAME,content.getName()); 
//    publicationPlugin.changeState(content, Constant.LIVE, context);
    session.save();
  } 

  /**
   * Save removed application.
   * 
   * @param page the page
   * @param applicationId the application id
   * @param content the content
   * @param remoteUser TODO
   * @throws Exception the exception
   */
  private void saveRemovedApplication(Page page, String applicationId, Node content, String remoteUser) throws Exception {
    WCMPublicationService presentationService = StageAndVersionPublicationUtil.getServices(WCMPublicationService.class);
    StageAndVersionPublicationPlugin publicationPlugin = (StageAndVersionPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);

    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    listExistedApplicationId.remove(StageAndVersionPublicationUtil.setMixedApplicationId(page.getPageId(), applicationId));
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedApplicationId));

    List<String> listExistedPageId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    listExistedPageId.remove(page.getPageId());
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedPageId));

    List<String> listPageNavigationUri = publicationPlugin.getListPageNavigationUri(page, remoteUser);
    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = new ArrayList<String>();
    listExistedNavigationNodeUriTmp.addAll(listExistedNavigationNodeUri);    
    for (String existedNavigationNodeUri : listExistedNavigationNodeUriTmp) {
      if (listPageNavigationUri.contains(existedNavigationNodeUri)) {
        listExistedNavigationNodeUri.remove(existedNavigationNodeUri);
        break;
      }
    }
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));

//    if (listExistedPageId.isEmpty()) { 
//      HashMap<String,String> context = new HashMap<String,String>();
//      context.put(Constant.CURRENT_VERSION_NAME,content.getName()); 
//      publicationPlugin.changeState(content, Constant.DRAFT, context);
//    }

    session.save();
  }
}
