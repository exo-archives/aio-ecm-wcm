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

import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.UINonEditable;
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
    if(isQuickEditable()) {
      UIWelcomeScreen uiWellcomeScreen = createUIComponent(UIWelcomeScreen.class, null, null).setCreateMode(isNewConfig);
      addChild(uiWellcomeScreen);
      uiBackComponent = uiWellcomeScreen ;
    } else {
      addChild(UINonEditable.class, null, null);
    }
  }

  private boolean isQuickEditable() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    String repositoryName = prefs.getValue(UIAdvancedPresentationPortlet.REPOSITORY, null);
    if(repositoryName == null) return true;
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    String quickEdit = prefs.getValue("ShowQuickEdit", null);
    boolean isQuickEdit = Boolean.parseBoolean(quickEdit);
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    boolean displayQuickEdit = userACL.hasEditPermission(portalConfig, userId);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalNode = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    String UUID = prefs.getValue(UIAdvancedPresentationPortlet.UUID, null);
    try{
      ExtendedNode exWebContentNode = (ExtendedNode) portalNode.getSession().getNodeByUUID(UUID);
      AccessControlList acl = exWebContentNode.getACL();
      List<String> permList = acl.getPermissions(userId);
      if(permList.contains(PermissionType.ADD_NODE) && permList.contains(PermissionType.REMOVE) 
          && permList.contains(PermissionType.SET_PROPERTY)) {
        return (isQuickEdit && displayQuickEdit);
      }
    }catch(ItemNotFoundException e) {
      return true;
    }
    return false;
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

  public UIComponent getBackComponent() {
    uiBackComponent.setRendered(true);
    return uiBackComponent; 
  }

  public void setNewConfig(boolean newConfig) { isNewConfig = newConfig; }

  public boolean isNewConfig() { return isNewConfig; }
}
