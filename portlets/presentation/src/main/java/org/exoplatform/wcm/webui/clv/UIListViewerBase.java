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
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.config.UIViewerManagementForm;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIContainer;
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
    String repository = getPortletPreference().getValue(UIContentListViewerPortlet.REPOSITORY, null);
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  public static class QuickEditActionListener extends EventListener<UIFolderViewer> {
    public void execute(Event<UIFolderViewer> event) throws Exception {
      UIListViewerBase uiListViewerBase = event.getSource();
      UIViewerManagementForm viewerManagementForm = uiListViewerBase.createUIComponent(UIViewerManagementForm.class, null, null);
      Utils.createPopupWindow(uiListViewerBase, viewerManagementForm, "UIViewerManagementPopupWindow", 800, 600);
    }    
  }

  public void onRefresh(Event<UIContentListPresentation> event) throws Exception {
    UIContentListPresentation contentListPresentation = event.getSource();
    UIListViewerBase uiListViewerBase = contentListPresentation.getParent();
    uiListViewerBase.getChildren().clear();
    uiListViewerBase.init();
  }

}
