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

import javax.portlet.PortletMode;

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
 * Jun 19, 2009  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UIParameterizedContentListViewerPortlet extends UIPortletApplication {

  /** The mode. */
  private PortletMode        mode                    = PortletMode.VIEW;

  /** The Constant REPOSITORY. */
  public final static String REPOSITORY              = "repository";

  /** The Constant WORKSPACE. */
  public final static String WORKSPACE               = "workspace";

  public static final String VIEWER_MODE             = "mode";

  /** The Constant HEADER. */
  public final static String HEADER                  = "header";

  public final static String SHOW_READMORE           = "header";

  public static final String ORDER_TYPE              = "orderType";

  public static final String ORDER_BY                = "orderBy";

  public final static String ITEMS_PER_PAGE          = "itemsPerPage";

  public final static String SHOW_LINK               = "showLink";

  public final static String FORM_VIEW_TEMPLATE_PATH = "formViewTemplatePath";

  public final static String PAGINATOR_TEMPlATE_PATH = "paginatorTemplatePath";

  public final static String SHOW_HEADER             = "showHeader";

  public final static String SHOW_REFRESH_BUTTON     = "showRefreshButton";

  public final static String SHOW_SUMMARY            = "showSummary";

  public final static String SHOW_THUMBNAILS_VIEW    = "showThumbnailsView";

  public final static String SHOW_TITLE              = "showTitle";

  public final static String SHOW_DATE_CREATED       = "showDateCreated";

  public final static String SHOW_MORE_LINK          = "showMoreLink";

  public final static String SHOW_RSS_LINK           = "showRssLink";

  public final static String SHOW_AUTO_DETECT        = "showAutoDetect";

  public final static String TARGET_PAGE             = "targetPage";

  public final static String ADD_DATE_INTO_PAGE      = "addDateIntoPage";

  public final static String FOLDER_PATH             = "folderPath";

  public UIParameterizedContentListViewerPortlet() throws Exception {
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
  
  /**
   * Activate mode.
   * 
   * @param mode the mode
   * 
   * @throws Exception the exception
   */
  private void activateMode(PortletMode mode) throws Exception {
    getChildren().clear();
    addChild(UIPopupContainer.class, null, null);
    if (PortletMode.VIEW.equals(mode)) {
      UIParameterizedContentListViewerContainer container = addChild(UIParameterizedContentListViewerContainer.class, null, null);
      container.init();
    } else if (PortletMode.EDIT.equals(mode)) {

      //addChild(UIParameterizedManagementForm.class, null, null);
    }
  }
}
