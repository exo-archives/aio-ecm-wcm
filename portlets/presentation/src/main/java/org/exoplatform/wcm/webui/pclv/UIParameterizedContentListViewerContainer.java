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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.UIContentListViewerPortlet;
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
   //lifecycle = Lifecycle.class,
   template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedContentListViewerContainer.gtmpl", 
   events = { 
     @EventConfig(listeners = UIParameterizedContentListViewerContainer.QuickEditActionListener.class) 
   }
 )
public class UIParameterizedContentListViewerContainer extends UIContainer {

  public boolean viewAbleContent = false;
  
  public String  messageKey;
  public UIParameterizedContentListViewerContainer() throws Exception {
    //this.addChild(UIParameterizedContentListViewerForm.class, null, "UIParameterizedContentListViewerForm");    
  }
  
  public void init() throws Exception {
    PortletRequestContext porletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext.getRequest();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURI = requestWrapper.getRequestURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getName();
    String parameters = null;
    try {
      parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected + "/")),"UTF-8");
    } catch (UnsupportedEncodingException e) {}
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletRequest portletRequest = portletRequestContext.getRequest();
    PortletPreferences portletPreferences = portletRequest.getPreferences();
    String preferenceRepository = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.PREFERENCE_REPOSITORY, "");
    String preferenceTreeName = portletPreferences.getValue(UIParameterizedContentListViewerPortlet.PREFERENCE_TREE_NAME, "");
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    Node treeNode = taxonomyService.getTaxonomyTree(preferenceRepository, preferenceTreeName);
    String categoryPath = parameters.substring(parameters.indexOf("/") + 1);
    if (preferenceTreeName.equals(categoryPath)) categoryPath = ""; 
    Node categoryNode = treeNode.getNode(categoryPath);
    
    setViewAbleContent(true);
    setViewAbleContent(true);
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.ITEMS_PER_PAGE, null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(categoryNode.getNodes(), itemsPerPage);
    UIParameterizedContentListViewerForm parameterizedContentListViewer = addChild(UIParameterizedContentListViewerForm.class, null, "UIParameterizedContentListViewerForm");    
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();    
    parameterizedContentListViewer.init(templatePath, resourceResolver, paginatedNodeIterator); 
    parameterizedContentListViewer.setContentColumn(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.HEADER, null));
    parameterizedContentListViewer.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_LINK, null)));
    parameterizedContentListViewer.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_HEADER, null)));
    parameterizedContentListViewer.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_READMORE, null)));
    parameterizedContentListViewer.setHeader(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.HEADER, null));
    parameterizedContentListViewer.setAutoDetection(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_AUTO_DETECT, null));
    parameterizedContentListViewer.setShowMoreLink(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_MORE_LINK, null));
    parameterizedContentListViewer.setShowRSSLink(portletPreferences.getValue(UIParameterizedContentListViewerPortlet.SHOW_RSS_LINK, null));
  }

  public PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  public String getFormViewTemplatePath() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences references = portletRequestContext.getRequest().getPreferences();
    return references.getValue(UIParameterizedContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH, null);
  }

  public ResourceResolver getTemplateResourceResolver() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences references = portletRequestContext.getRequest().getPreferences();
    String repository = references.getValue(UIParameterizedContentListViewerPortlet.PREFERENCE_REPOSITORY, null);
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
     
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }
  
  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {   
//    if(!Utils.isLiveMode() || context.getFullRender()) {
      init(); 
//    }
    super.processRender(context);
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
                                .PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW, 850, 800);
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

  public boolean isViewAbleContent() {
    return viewAbleContent;
  }

  public void setViewAbleContent(boolean viewAbleContent) {
    this.viewAbleContent = viewAbleContent;
  }
}
