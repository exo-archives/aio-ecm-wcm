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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Aug 15, 2008  
 */

/**
 * The Class UIPermissionManager.
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIPermissionManager extends UIContainer {
  
  /**
   * Instantiates a new uI permission manager.
   * 
   * @throws Exception the exception
   */
  public UIPermissionManager() throws Exception {
    addChild(UIPermissionInfo.class, null, null);
    addChild(UIPermissionSetting.class, null, null);
  }

  /**
   * Inits the popup permission.
   * 
   * @param uiSelector the ui selector
   * 
   * @throws Exception the exception
   */
  public void initPopupPermission(UIComponent uiSelector) throws Exception {
    UIPopupWindow uiPopup = getChildById(UIPermissionSetting.POPUP_SELECT);
    if (uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, UIPermissionSetting.POPUP_SELECT);
      uiPopup.setWindowSize(560, 300);
    } else {
      uiPopup.setRendered(true);
    }
    uiPopup.setUIComponent(uiSelector);
    uiPopup.setShow(true);
    uiPopup.setResizable(true);
  }

}
