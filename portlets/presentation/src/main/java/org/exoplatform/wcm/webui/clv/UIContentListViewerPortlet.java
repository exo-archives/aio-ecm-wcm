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
import javax.portlet.PortletPreferences;

import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.config.UIPortletConfig;
import org.exoplatform.wcm.webui.clv.config.UIStartEditionInPageWizard;
import org.exoplatform.wcm.webui.clv.config.UIViewerManagementForm;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UIContentListViewerPortlet.
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIContentListViewerPortlet extends UIPortletApplication {

  /** The mode. */
  private PortletMode        mode                    = PortletMode.VIEW;

  /** The Constant REPOSITORY. */
  public final static String REPOSITORY              = "repository";

  /** The Constant WORKSPACE. */
  public final static String WORKSPACE               = "workspace";

  /** The Constant ITEMS_PER_PAGE. */
  public final static String ITEMS_PER_PAGE          = "itemsPerPage";

  /** The Constant FOLDER_PATH. */
  public final static String FOLDER_PATH             = "folderPath";

  /** The Constant HEADER. */
  public final static String HEADER                  = "header";

  /** The Constant FORM_VIEW_TEMPLATE_PATH. */
  public final static String FORM_VIEW_TEMPLATE_PATH = "formViewTemplatePath";

  /** The Constant PAGINATOR_TEMPlATE_PATH. */
  public final static String PAGINATOR_TEMPlATE_PATH = "paginatorTemplatePath";

  /** The Constant SHOW_REFRESH_BUTTON. */
  public final static String SHOW_REFRESH_BUTTON     = "showRefreshButton";

  /** The Constant SHOW_THUMBNAILS_VIEW. */
  public final static String SHOW_THUMBNAILS_VIEW    = "showThumbnailsView";

  /** The Constant SHOW_TITLE. */
  public final static String SHOW_TITLE              = "showTitle";

  /** The Constant SHOW_SUMMARY. */
  public final static String SHOW_SUMMARY            = "showSummary";

  /** The Constant SHOW_DATE_CREATED. */
  public final static String SHOW_DATE_CREATED       = "showDateCreated";

  /** The Constant SHOW_HEADER. */
  public final static String SHOW_HEADER             = "showHeader";
  
  /** The Constant SHOW_LINK. */
  public final static String SHOW_LINK             = "showLink";
  
  public static final String CONTENT_SOURCE = "source";
  
  public static final String VIEWER_MODE = "mode";
  
  public static final String ORDER_BY = "orderBy";
  
  public static final String ORDER_TYPE = "orderType";
  
  public static final String CONTENT_LIST = "contents";
  
  public static final int portletConfigFormWidth = 800;

  /**
   * Instantiates a new uI content list viewer portlet.
   * 
   * @throws Exception the exception
   */
  public UIContentListViewerPortlet() throws Exception {
    activateMode(mode);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPortletApplication#processRender(org.exoplatform.webui.application.WebuiApplication, org.exoplatform.webui.application.WebuiRequestContext)
   */
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
    UIPopupContainer uiPopup = addChild(UIPopupContainer.class, null, "UIViewerManagementPopup");
    uiPopup.getChild(UIPopupWindow.class).setId("UIViewerManagementPopupWindow");
    PortletRequestContext context = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = context.getRequest().getPreferences();
    String viewerMode = preferences.getValue(VIEWER_MODE, null);        
    if (PortletMode.VIEW.equals(mode)) {
      if (viewerMode == null) viewerMode = UIViewerManagementForm.VIEWER_AUTO_MODE;
      if (viewerMode.equals(UIViewerManagementForm.VIEWER_AUTO_MODE)) {        
        UIFolderViewer uiFolderViewer = addChild(UIFolderViewer.class, null, UIPortletApplication.VIEW_MODE);
        uiFolderViewer.init(); 
      } else if (viewerMode.equals(UIViewerManagementForm.VIEWER_MANUAL_MODE)) {        
        UICorrectContentsViewer uiCorrectContentsViewer = addChild(UICorrectContentsViewer.class, null, UIPortletApplication.VIEW_MODE);
        uiCorrectContentsViewer.init();
      }
    } else if (PortletMode.EDIT.equals(mode)) {
      UIPopupContainer maskPopupContainer = getChild(UIPopupContainer.class);
      UIStartEditionInPageWizard portletEditMode = createUIComponent(UIStartEditionInPageWizard.class, null, null);
      addChild(portletEditMode);
      UIPortletConfig portletConfig = portletEditMode.createUIComponent(UIPortletConfig.class, null, null);
      portletEditMode.addChild(portletConfig);
      portletConfig.setRendered(true);
      maskPopupContainer.activate(portletConfig, portletConfigFormWidth, -1);
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      portletRequestContext.addUIComponentToUpdateByAjax(maskPopupContainer);
    }
  }

  /**
   * Can edit portlet.
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public boolean canEditPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();    
    String userId = context.getRemoteUser();       
    return Utils.canEditCurrentPortal(userId);
  }

}
