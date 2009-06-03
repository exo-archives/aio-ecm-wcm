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
package org.exoplatform.wcm.webui.clv.config;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UIPortletConfig.
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIPortletConfig extends UIContainer implements UIPopupComponent {

  /**
   * Instantiates a new uI portlet config.
   * 
   * @throws Exception the exception
   */
  public UIPortletConfig() throws Exception {
    addChild(UIViewerManagementForm.class, null, null);
    UIPopupContainer uiPopup = addChild(UIPopupContainer.class, null, "UIFolderPathSelecctPopup");
    uiPopup.getChild(UIPopupWindow.class).setId("UIFolderPathSelectPopupWindow");

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.webui.core.UIPopupComponent#activate()
   */
  public void activate() throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.webui.core.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {
  }

}
