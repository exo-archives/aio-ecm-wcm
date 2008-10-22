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
package org.exoplatform.wcm.webui.clv;

import javax.portlet.PortletMode;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.wcm.webui.clv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, 
    events = { @EventConfig(listeners = UIContentListViewerPortlet.QuickEditActionListener.class) })
public class UIContentListViewerPortlet extends UIPortletApplication {

  private PortletMode        mode            = PortletMode.VIEW;

  public static final String REPOSITORY      = "repository";

  public static final String WORKSPACE       = "workspace";

  public static final String ITEMS_PER_PAGE  = "itemsPerPage";

  public static final String FOLDER_PATH     = "folderPath";

  public static final String TEMPLATE_PATH   = "templatePath";

  public static final String SHOW_QUICK_EDIT = "showQuickEdit";

  public UIContentListViewerPortlet() throws Exception {
    activateMode(mode);
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context;
    PortletMode newMode = pContext.getApplicationMode();
    if (!mode.equals(newMode)) {
      activateMode(newMode);
      mode = newMode;
    }
    super.processRender(app, context);
  }

  public void activateMode(PortletMode mode) throws Exception {
    getChildren().clear();
    if (PortletMode.VIEW.equals(mode)) {
      UIFolderViewer folderViewer = addChild(UIFolderViewer.class, null, UIPortletApplication.VIEW_MODE);
      folderViewer.init();
    } else if (PortletMode.EDIT.equals(mode)) {
      addChild(UIPortletConfig.class, null, UIPortletApplication.EDIT_MODE);
    }
  }

  public boolean canEditPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    return userACL.hasEditPermission(portalConfig, userId);
  }      
  
  public static class QuickEditActionListener extends EventListener<UIContentListViewerPortlet> {
    public void execute(Event<UIContentListViewerPortlet> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }

}