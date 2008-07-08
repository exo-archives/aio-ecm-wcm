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
package org.exoplatform.wcm.presentation.acp.config;

import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 26, 2008  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UIPortletConfig extends UIContainer {

  private UIComponent uiBackComponent;
  private boolean isNewConfig;

  public UIPortletConfig() throws Exception {      
    isNewConfig = checkNewConfig();
    UIWelcomeScreen uiWellcomeScreen = createUIComponent(UIWelcomeScreen.class, null, null).setCreateMode(isNewConfig);
    addChild(uiWellcomeScreen) ;
    uiBackComponent = uiWellcomeScreen ;
  }
  private boolean checkNewConfig(){
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences() ;
    String repository = prefs.getValue("repository", null) ;
    String workspace = prefs.getValue("workspace", null) ;
    String nodeUUID = prefs.getValue("nodeUUID", null) ;
    if(repository == null || workspace == null ||nodeUUID ==null)
      return true ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
      Session session = sessionProvider.getSession(workspace, manageableRepository) ;
      session.getNodeByUUID(nodeUUID) ;
      return false ;
    } catch (Exception e) {
    }    
    return true ;
  }

  public UIComponent getBackComponent() { return uiBackComponent; }

  public void setNewConfig(boolean newConfig) { isNewConfig = newConfig; }

  public boolean isNewConfig() { return isNewConfig; }
}
