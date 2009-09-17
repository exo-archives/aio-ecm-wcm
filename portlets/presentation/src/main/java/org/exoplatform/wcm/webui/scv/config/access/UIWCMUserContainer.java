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
 * dzungdev@gmail.com
 * Dec 10, 2008
 */
@SuppressWarnings("deprecation")
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {@EventConfig(listeners = UIWCMUserContainer.AddUserActionListener.class)}
)
public class UIWCMUserContainer extends UIContainer implements UIPopupComponent  {

  /**
   * Instantiates a new uIWCM user container.
   * 
   * @throws Exception the exception
   */
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

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.popup.UIPopupComponent#activate()
   */
  public void activate() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.popup.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {
  }

  /**
   * The listener interface for receiving addUserAction events.
   * The class that is interested in processing a addUserAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAddUserActionListener<code> method. When
   * the addUserAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AddUserActionEvent
   */
  static  public class AddUserActionListener extends EventListener<UIWCMUserContainer> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
