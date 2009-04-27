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
package org.exoplatform.wcm.webui.clv;

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : anh.do
 *          anh.do@exoplatform.com, anhdn86@gmail.com		
 * Feb 23, 2009  
 */
public abstract class UIListViewerBase extends UIContainer implements RefreshDelegateActionListener {

  protected boolean viewAbleContent = false;

  protected String  messageKey;

  public abstract void init() throws Exception;

  public String getMessage() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    return requestContext.getApplicationResourceBundle().getString(messageKey);
  }

  public boolean isViewAbleContent() {
    return viewAbleContent;
  }

  public void setViewAbleContent(boolean bool) {
    viewAbleContent = bool;
  }

  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    if(!Utils.isLiveMode() || context.getFullRender()) {
      init(); 
    }    
    super.processRender(context);
  }
  
  protected PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  protected String getFormViewTemplatePath() {
    return getPortletPreference().getValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH, null);
  }

  public ResourceResolver getTemplateResourceResolver() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = getPortletPreference().getValue(UIContentListViewerPortlet.REPOSITORY, null);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  public static class QuickEditActionListener extends EventListener<UIFolderViewer> {
    public void execute(Event<UIFolderViewer> event) throws Exception {
      UIListViewerBase uiListViewerBase = event.getSource();
      UIContentListViewerPortlet uiListViewerPortlet = uiListViewerBase.getAncestorOfType(UIContentListViewerPortlet.class);
      UIPopupContainer uiMaskPopupContainer = uiListViewerPortlet.getChild(UIPopupContainer.class);
      UIPortletConfig uiPortletConfig = uiMaskPopupContainer.createUIComponent(UIPortletConfig.class, null, null);
      uiListViewerBase.addChild(uiPortletConfig);
      uiPortletConfig.setRendered(true);
      uiMaskPopupContainer.activate(uiPortletConfig, UIContentListViewerPortlet.portletConfigFormWidth, -1);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.addUIComponentToUpdateByAjax(uiMaskPopupContainer);      
    }    
  }

  public void onRefresh(Event<UIContentListPresentation> event) throws Exception {
    UIContentListPresentation contentListPresentation = event.getSource();
    UIListViewerBase uiListViewerBase = contentListPresentation.getParent();
    uiListViewerBase.getChildren().clear();
    uiListViewerBase.init();
  }

}
