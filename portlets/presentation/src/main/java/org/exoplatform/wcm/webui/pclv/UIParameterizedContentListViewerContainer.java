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
package org.exoplatform.wcm.webui.pclv;

import java.net.URLDecoder;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
   template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedContentListViewerContainer.gtmpl", 
   events = { 
     @EventConfig(listeners = UIParameterizedContentListViewerContainer.QuickEditActionListener.class) 
   }
 )
public class UIParameterizedContentListViewerContainer extends UIContainer {

  public UIParameterizedContentListViewerContainer() throws Exception {
  }
  
  public void init() throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) portletRequestContext.getRequest();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURI = requestWrapper.getRequestURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getName();
    
    String parameters = null;
    try {
      parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected + "/")),"UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    PortletRequest portletRequest = portletRequestContext.getRequest();
    PortletPreferences portletPreferences = portletRequest.getPreferences();
    String preferenceRepository = portletPreferences.getValue(UIParameterizedContentListViewerConstant.PREFERENCE_REPOSITORY, "");
    String preferenceTreeName = portletPreferences.getValue(UIParameterizedContentListViewerConstant.PREFERENCE_TREE_NAME, "");
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    Node treeNode = taxonomyService.getTaxonomyTree(preferenceRepository, preferenceTreeName);
    String categoryPath = parameters.substring(parameters.indexOf("/") + 1);
    if (preferenceTreeName.equals(categoryPath)) categoryPath = "";
    Node categoryNode = treeNode.getNode(categoryPath);
    NodeIterator categoriesChildNode = this.getListSymlinkNode(portletPreferences, categoryNode.getPath());
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIParameterizedContentListViewerConstant.ITEMS_PER_PAGE, null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(categoriesChildNode, itemsPerPage);
    getChildren().clear();

    UIParameterizedContentListViewerForm parameterizedContentListViewer = addChild(UIParameterizedContentListViewerForm.class, null, UIParameterizedContentListViewerConstant.PARAMETERIZED_CONTENT_LIST_VIEWER_FORM);    
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();    
    parameterizedContentListViewer.init(templatePath, resourceResolver, paginatedNodeIterator); 
    parameterizedContentListViewer.setContentColumn(portletPreferences.getValue(UIParameterizedContentListViewerConstant.HEADER, null));
    parameterizedContentListViewer.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_LINK, null)));
    parameterizedContentListViewer.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_HEADER, null)));
    parameterizedContentListViewer.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_READMORE, null)));
    parameterizedContentListViewer.setHeader(portletPreferences.getValue(UIParameterizedContentListViewerConstant.HEADER, null));
    parameterizedContentListViewer.setAutoDetection(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_AUTO_DETECT, null));
    parameterizedContentListViewer.setShowMoreLink(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_MORE_LINK, null));
    parameterizedContentListViewer.setShowRSSLink(portletPreferences.getValue(UIParameterizedContentListViewerConstant.SHOW_RSS_LINK, null));
  }

  public PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  public String getFormViewTemplatePath() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences references = portletRequestContext.getRequest().getPreferences();
    return references.getValue(UIParameterizedContentListViewerConstant.FORM_VIEW_TEMPLATE_PATH, null);
  }

  public ResourceResolver getTemplateResourceResolver() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences references = portletRequestContext.getRequest().getPreferences();
    String repository = references.getValue(UIParameterizedContentListViewerConstant.PREFERENCE_REPOSITORY, null);
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();

    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }
  
  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {   
    try {
      init(); 
      super.processRender(context);
    } catch (Exception e) {}
  }

  public static class QuickEditActionListener extends EventListener<UIParameterizedContentListViewerContainer> {

    public void execute(Event<UIParameterizedContentListViewerContainer> event) throws Exception {
      UIParameterizedContentListViewerContainer uiContentViewerContainer = event.getSource();

      UIParameterizedContentListViewerPortlet uiListViewerPortlet = uiContentViewerContainer
                                                                        .getAncestorOfType(UIParameterizedContentListViewerPortlet.class);
      
      UIPopupContainer popupContainer = uiListViewerPortlet.getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UIParameterizedContentListViewerConstant.PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW);
      if(popupWindow == null) {

        UIParameterizedManagementForm parameterizedForm = 
          popupContainer.createUIComponent(UIParameterizedManagementForm.class, null, null);
        Utils.createPopupWindow(popupContainer,
                                parameterizedForm,
                                event.getRequestContext(),
                                UIParameterizedContentListViewerConstant
                                .PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW, 650, 800);
      } else {
        popupWindow.setShow(true);
      }
    }
  }

  public void onRefresh(Event<UIParameterizedContentListViewerForm> event) throws Exception {
    UIParameterizedContentListViewerForm contentListPresentation = event.getSource();
    UIParameterizedContentListViewerContainer uiParameterizedContentListontainer = contentListPresentation.getParent();
    uiParameterizedContentListontainer.getChildren().clear();
    uiParameterizedContentListontainer.init();
  }
  
  private NodeIterator getListSymlinkNode(PortletPreferences portletPreferences, String categoryPath) throws RepositoryException, RepositoryConfigurationException {

    String repository = portletPreferences.getValue(UIParameterizedContentListViewerConstant.REPOSITORY, null);
    String worksapce = portletPreferences.getValue(UIParameterizedContentListViewerConstant.WORKSPACE, null);
    String orderType = portletPreferences.getValue(UIParameterizedContentListViewerConstant.ORDER_TYPE, null);
    String orderBy = portletPreferences.getValue(UIParameterizedContentListViewerConstant.ORDER_BY, null);
    if (orderType == null) orderType = "DESC";
    if (orderBy == null) orderBy = "exo:dateCreated";
    
    String orderQuery = " ORDER BY ";
    orderQuery += orderBy + " " + orderType;
    
    ThreadLocalSessionProviderService threadLocalSessionProviderService = ThreadLocalSessionProviderService.class
    .cast(ExoContainerContext.getCurrentContainer()
    .getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);    
    Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(worksapce, manageableRepository);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    StringBuffer sqlQuery = new StringBuffer("select * from exo:taxonomyLink where jcr:path LIKE '").append(categoryPath).append("/%'")
                                 .append(" AND NOT jcr:path LIKE '").append(categoryPath).append("/%/%'").append(" " + orderQuery);
    Query query = queryManager.createQuery(sqlQuery.toString(), Query.SQL);
    QueryResult queryResult = query.execute();
    NodeIterator iterator = queryResult.getNodes();
    return iterator;
  }
}
