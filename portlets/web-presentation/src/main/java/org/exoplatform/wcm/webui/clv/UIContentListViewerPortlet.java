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
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIContentListViewerPortlet extends UIPortletApplication {

  private PortletMode        mode                    = PortletMode.VIEW;

  public final static String REPOSITORY              = "repository";

  public final static String WORKSPACE               = "workspace";

  public final static String ITEMS_PER_PAGE          = "itemsPerPage";

  public final static String FOLDER_PATH             = "folderPath";

  public final static String FORM_VIEW_TEMPLATE_PATH = "formViewTemplatePath";

  public final static String PAGINATOR_TEMPlATE_PATH = "paginatorTemplatePath";

  public final static String SHOW_QUICK_EDIT_BUTTON  = "showQuickEditButton";

  public final static String SHOW_REFRESH_BUTTON     = "showRefreshButton";

  public final static String SHOW_THUMBNAILS_VIEW    = "showThumbnailsView";

  public final static String SHOW_TITLE              = "showTitle";

  public final static String SHOW_SUMMARY            = "showSummary";

  public final static String SHOW_DATE_CREATED        = "showDateCreated";

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
      UIFolderViewer folderViewer = addChild(UIFolderViewer.class, null,
          UIPortletApplication.VIEW_MODE);
      folderViewer.init();
    } else if (PortletMode.EDIT.equals(mode)) {
      addChild(UIPortletConfig.class, null, UIPortletApplication.EDIT_MODE);
    }
  }

  public boolean canEditPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    return userACL.hasEditPermission(portalConfig, userId);
  }
  
}