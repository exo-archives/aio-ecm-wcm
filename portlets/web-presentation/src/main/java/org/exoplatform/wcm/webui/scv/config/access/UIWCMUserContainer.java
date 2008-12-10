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
package org.exoplatform.wcm.webui.scv.config.access;

import org.exoplatform.ecm.webui.popup.UIPopupComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Dec 10, 2008  
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {@EventConfig(listeners = UIWCMUserContainer.AddUserActionListener.class)}
)

public class UIWCMUserContainer extends UIContainer implements UIPopupComponent  {

  public UIWCMUserContainer() throws Exception {
    UIUserSelector uiUserSelector = getChild(UIUserSelector.class);
    if (uiUserSelector == null) {
      uiUserSelector = addChild(UIUserSelector.class, null, null);
    }
    uiUserSelector.setMulti(false);
    uiUserSelector.setShowSearchGroup(true);
    uiUserSelector.setShowSearchUser(true);
    uiUserSelector.setShowSearch(true);
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  static  public class AddUserActionListener extends EventListener<UIWCMUserContainer> {
    public void execute(Event<UIWCMUserContainer> event) throws Exception {
      UIWCMUserContainer uiUserContainer = event.getSource();
      UIPopupWindow uiPopup = uiUserContainer.getAncestorOfType(UIPopupWindow.class);
      UIPermissionManager uiPermissionManager = uiPopup.getAncestorOfType(UIPermissionManager.class);
      UIUserSelector uiUserSelector = uiUserContainer.getChild(UIUserSelector.class);
      UIPermissionSetting uiPermissionSetting = uiPermissionManager.getChild(UIPermissionSetting.class);
      uiPermissionSetting.doSelect(UIPermissionSetting.USERS_STRINGINPUT, uiUserSelector.getSelectedUsers());
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionManager);
    }  
  }
}
