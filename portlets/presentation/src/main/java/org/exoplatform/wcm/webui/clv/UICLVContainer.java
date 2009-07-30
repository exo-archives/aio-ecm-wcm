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
import org.exoplatform.wcm.webui.clv.config.UICLVConfig;
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
public abstract class UICLVContainer extends UIContainer implements RefreshDelegateActionListener {

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
    return getPortletPreference().getValue(UICLVPortlet.FORM_VIEW_TEMPLATE_PATH, null);
  }

  public ResourceResolver getTemplateResourceResolver() throws Exception {
    String repository = getPortletPreference().getValue(UICLVPortlet.REPOSITORY, null);
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  public static class QuickEditActionListener extends EventListener<UICLVFolderMode> {
    public void execute(Event<UICLVFolderMode> event) throws Exception {
      UICLVContainer uiListViewerBase = event.getSource();
      UICLVConfig viewerManagementForm = uiListViewerBase.createUIComponent(UICLVConfig.class, null, null);
      Utils.createPopupWindow(uiListViewerBase, viewerManagementForm, "UIViewerManagementPopupWindow", 800, 600);
    }    
  }

  public void onRefresh(Event<UICLVPresentation> event) throws Exception {
    UICLVPresentation contentListPresentation = event.getSource();
    UICLVContainer uiListViewerBase = contentListPresentation.getParent();
    uiListViewerBase.getChildren().clear();
    uiListViewerBase.init();
  }

}
