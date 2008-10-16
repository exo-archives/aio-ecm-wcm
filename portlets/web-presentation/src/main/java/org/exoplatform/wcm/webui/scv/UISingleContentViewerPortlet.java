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
package org.exoplatform.wcm.webui.scv;

import java.security.AccessControlException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jun 9, 2008  
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    events = {
      @EventConfig(listeners = UISingleContentViewerPortlet.QuickEditActionListener.class)
    }
)

public class UISingleContentViewerPortlet extends UIPortletApplication {

  public static String REPOSITORY = "repository" ;
  public static String WORKSPACE = "workspace" ;
  public static String UUID = "nodeUUID" ;

  private PortletMode mode_ = PortletMode.VIEW ;

  public UISingleContentViewerPortlet() throws Exception {
    activateMode(mode_) ;
  }

  public void activateMode(PortletMode mode) throws Exception {
    getChildren().clear() ;
    if(PortletMode.VIEW.equals(mode)) {
      addChild(UIPresentationContainer.class, null, UIPortletApplication.VIEW_MODE) ;
    } else if (PortletMode.EDIT.equals(mode)) {      
      UIPortletConfig portletConfig = addChild(UIPortletConfig.class, null, UIPortletApplication.EDIT_MODE) ;
      portletConfig.init();
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context ;
    PortletMode newMode = pContext.getApplicationMode() ;
    if(!mode_.equals(newMode)) {
      activateMode(newMode) ;
      mode_ = newMode ;
    }
    super.processRender(app, context) ;
  }

  public boolean canEditPortlet() throws Exception{
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    return userACL.hasEditPermission(portalConfig, userId);
  }

  public Node getReferencedContent() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UISingleContentViewerPortlet.REPOSITORY, "repository");    
    String worksapce = preferences.getValue(UISingleContentViewerPortlet.WORKSPACE, "collaboration");
    String uuid = preferences.getValue(UISingleContentViewerPortlet.UUID, "") ;
    if(repository == null || worksapce == null || uuid == null) 
      throw new ItemNotFoundException();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    SessionProvider sessionProvider = null;
    if(userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    }else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    Session session = sessionProvider.getSession(worksapce, manageableRepository);
    return session.getNodeByUUID(uuid) ;
  }

  public boolean canEditContent(final Node content) throws Exception{
    if(!content.isNodeType("exo:webContent")) return false;
    try {
      ((ExtendedNode)content).checkPermission(PermissionType.ADD_NODE);
      ((ExtendedNode)content).checkPermission(PermissionType.REMOVE);
      ((ExtendedNode)content).checkPermission(PermissionType.SET_PROPERTY);
    } catch (AccessControlException e) {
      return false;
    }
    return true;
  }

  public static class QuickEditActionListener extends EventListener<UISingleContentViewerPortlet> {
    public void execute(Event<UISingleContentViewerPortlet> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }
}
