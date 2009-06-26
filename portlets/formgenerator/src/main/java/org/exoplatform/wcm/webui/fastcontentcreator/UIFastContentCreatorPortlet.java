/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.fastcontentcreator;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFastContentCreatorConfig;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UIFastContentCreatorPortlet extends UIPortletApplication {

  public UIFastContentCreatorPortlet() throws Exception {}

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    context.getJavascriptManager().importJavascript("eXo.ecm.ECMUtils","/ecm/javascript/");
    context.getJavascriptManager().addJavascript("eXo.ecm.ECMUtils.init('UIFastContentCreatorPortlet') ;");
    PortletRequestContext portletRequestContext = (PortletRequestContext)  context ;
    addChild(UIPopupContainer.class, null, null);
    if (portletRequestContext.getApplicationMode() == PortletMode.VIEW) {
      if(getChild(UIFastContentCreatorConfig.class) != null) {
        removeChild(UIFastContentCreatorConfig.class) ;
      }
      if(getChild(UIFastContentCreatorForm.class) == null) {
        UIFastContentCreatorForm fastContentCreatorForm = addChild(UIFastContentCreatorForm.class, null, null) ;
        PortletPreferences preferences = UIFastContentCreatorUtils.getPortletPreferences();
        fastContentCreatorForm.setTemplateNode(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_TYPE, "")) ;
        fastContentCreatorForm.setWorkspace(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, "")) ;
        fastContentCreatorForm.setStoredPath(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_PATH, "")) ;
        fastContentCreatorForm.setRepositoryName(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, "")) ;
      }
    } else if(portletRequestContext.getApplicationMode() == PortletMode.EDIT) {
      if(getChild(UIFastContentCreatorForm.class) != null) {
        removeChild(UIFastContentCreatorForm.class) ;
      }
      if(getChild(UIFastContentCreatorConfig.class) == null) {
        UIFastContentCreatorConfig fastContentCreatorConfig = addChild(UIFastContentCreatorConfig.class, null, null) ;
        fastContentCreatorConfig.initEditMode() ;
      }
    }
    super.processRender(app, context) ;
  }  
}