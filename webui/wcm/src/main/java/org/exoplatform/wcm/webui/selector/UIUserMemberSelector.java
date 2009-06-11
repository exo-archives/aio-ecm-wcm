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
package org.exoplatform.wcm.webui.selector;

import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * June 10, 2009  
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {@EventConfig(listeners = UIUserMemberSelector.AddUserActionListener.class)}
)

public class UIUserMemberSelector extends UIContainer implements ComponentSelector  {

  private UIComponent uiComponent;
  
  private String returnField;
  
  private boolean isUsePopup = true;
  
  private boolean isMulti = true;
  
  private boolean isShowSearchGroup = true;
  
  private boolean isShowSearchUser = true;
  
  private boolean isShowSearch;
  
  /**
   * Instantiates a new uIWCM user container.
   * 
   * @throws Exception the exception
   */
  public UIUserMemberSelector() {}
  
  public void init() throws Exception {
    UIUserSelector uiUserSelector = getChild(UIUserSelector.class);
    if (uiUserSelector == null) {
      uiUserSelector = addChild(UIUserSelector.class, null, null);
    }
    uiUserSelector.setMulti(isMulti);
    uiUserSelector.setShowSearchGroup(isShowSearchGroup);
    uiUserSelector.setShowSearchUser(isShowSearchUser);
    uiUserSelector.setShowSearch(isShowSearch);
  }
  
  public boolean isUsePopup() {
    return isUsePopup;
  }
  
  public void setUsePopup(boolean isUsePopup) {
    this.isUsePopup = isUsePopup;
  }
  
  public boolean isMulti() {
    return isMulti;
  }
  
  public void setMulti(boolean isMulti) {
    this.isMulti = isMulti;
  }
  
  public boolean isShowSearchGroup() {
    return isShowSearchGroup;
  }
  
  public void setShowSearchGroup(boolean isShowSearchGroup) {
    this.isShowSearchGroup = isShowSearchGroup;
  }
  
  public boolean isShowSearchUser() {
    return isShowSearchUser;
  }
  
  public void setShowSearchUser(boolean isShowSearchUser) {
    this.isShowSearchUser = isShowSearchUser;
  }
  
  public boolean isShowSearch() {
    return isShowSearch;
  }
  
  public void setShowSearch(boolean isShowSearch) {
    this.isShowSearch = isShowSearch;
  }
  
  public UIComponent getSourceComponent() {
    return uiComponent;
  }
  
  public String getReturnField() {
    return returnField;
  }
  
  public void setSourceComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent;
    if (initParams == null || initParams.length == 0)
      return;
    for (int i = 0; i < initParams.length; i++) {
      if (initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=");
        returnField = array[1];
        break;
      }
      returnField = initParams[0];
    }
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
  static  public class AddUserActionListener extends EventListener<UIUserMemberSelector> {

    public void execute(Event<UIUserMemberSelector> event) throws Exception {
      UIUserMemberSelector userMemberSelector = event.getSource();
      UIUserSelector userSelector = userMemberSelector.getChild(UIUserSelector.class);
      String returnField = userMemberSelector.getReturnField();
      ((UISelectable)userMemberSelector.getSourceComponent()).doSelect(returnField, userSelector.getSelectedUsers());
      if (userMemberSelector.isUsePopup) {
        UIPopupWindow uiPopup = userMemberSelector.getParent();
        uiPopup.setShow(false);
        UIComponent uicomp = userMemberSelector.getSourceComponent().getParent();
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);
        if (!uiPopup.getId().equals("PopupComponent"))
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(
            userMemberSelector.getSourceComponent());
      }
    }  
  }


}
