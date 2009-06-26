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
package org.exoplatform.wcm.webui.fastcontentcreator.config.action;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorConstant;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorPortlet;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFastContentCreatorConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    template = "app:/groovy/webui/FastContentCreatorPortlet/UIFastContentCreatorActionList.gtmpl",
    events = {
        @EventConfig(listeners = UIFastContentCreatorActionList.AddActionListener.class),
        @EventConfig(listeners = UIFastContentCreatorActionList.EditActionListener.class),
        @EventConfig(listeners = UIFastContentCreatorActionList.DeleteActionListener.class, confirm = "UIActionList.msg.confirm-delete-action")
    }
)
public class UIFastContentCreatorActionList extends UIContainer {

  final static public String[] ACTIONS = {"Edit", "Delete"} ;

  public UIFastContentCreatorActionList() throws Exception {
    addChild(UIPageIterator.class, null, UIFastContentCreatorConstant.ACTION_PAGE_ITERATOR);
  }
  
  public void updateGrid(Node node, int currentPage) throws Exception {
    UIPageIterator uiIterator = getChild(UIPageIterator.class) ;
    ObjectPageList objPageList = new ObjectPageList(getAllActions(node), 10) ;
    uiIterator.setPageList(objPageList);
    if(currentPage > uiIterator.getAvailablePage())
      uiIterator.setCurrentPage(currentPage-1);
    else
      uiIterator.setCurrentPage(currentPage);
  }

  public String[] getActions() { return ACTIONS ; }

  public boolean hasActions() {
    UIFastContentCreatorConfig fastContentCreatorConfig = getAncestorOfType(UIFastContentCreatorConfig.class) ;
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    try {
      return actionService.hasActions(fastContentCreatorConfig.getSavedLocationNode());
    } catch (Exception e) {
      return false;
    }
  }

  public List<Node> getAllActions(Node node) {
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    try {
      return actionService.getActions(node);
    } catch(Exception e){
      return new ArrayList<Node>() ;
    }
  }

  @SuppressWarnings("unchecked")
  public List getListActions() throws Exception {
    UIPageIterator uiIterator = getChild(UIPageIterator.class) ;
    return uiIterator.getCurrentPageData() ; 
  }
  
  public static class AddActionListener extends EventListener<UIFastContentCreatorActionList> {
    public void execute(Event<UIFastContentCreatorActionList> event) throws Exception {
      UIFastContentCreatorActionList fastContentCreatorActionList = event.getSource();
      UIPopupContainer popupContainer = fastContentCreatorActionList.getAncestorOfType(UIFastContentCreatorPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UIFastContentCreatorConstant.ACTION_POPUP_WINDOW);
      if (popupWindow == null) {
        UIFastContentCreatorActionContainer fastContentCreatorActionContainer = popupContainer.createUIComponent(UIFastContentCreatorActionContainer.class, null, null);
        Utils.createPopupWindow(popupContainer, fastContentCreatorActionContainer, event.getRequestContext(), UIFastContentCreatorConstant.ACTION_POPUP_WINDOW, 200, 200);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }
  
  public static class EditActionListener extends EventListener<UIFastContentCreatorActionList> {
    public void execute(Event<UIFastContentCreatorActionList> event) throws Exception {
      UIFastContentCreatorActionList fastContentCreatorActionList = event.getSource();
      UIPopupContainer popupContainer = fastContentCreatorActionList.getAncestorOfType(UIFastContentCreatorPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UIFastContentCreatorConstant.ACTION_POPUP_WINDOW);
      if (popupWindow == null) {
        UIFastContentCreatorActionContainer fastContentCreatorActionContainer = popupContainer.createUIComponent(UIFastContentCreatorActionContainer.class, null, null);
        Utils.createPopupWindow(popupContainer, fastContentCreatorActionContainer, event.getRequestContext(), UIFastContentCreatorConstant.ACTION_POPUP_WINDOW, 200, 200);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }
  
  public static class DeleteActionListener extends EventListener<UIFastContentCreatorActionList> {
    public void execute(Event<UIFastContentCreatorActionList> event) throws Exception {
      UIFastContentCreatorActionList fastContentCreatorActionList = event.getSource() ;
      UIFastContentCreatorConfig fastContentCreatorConfig = fastContentCreatorActionList.getAncestorOfType(UIFastContentCreatorConfig.class) ;
      ActionServiceContainer actionService = fastContentCreatorActionList.getApplicationComponent(ActionServiceContainer.class) ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer popupContainer = fastContentCreatorActionList.getAncestorOfType(UIFastContentCreatorPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow uiPopup = popupContainer.getChildById(UIFastContentCreatorConstant.ACTION_POPUP_WINDOW) ;
      UIApplication uiApp = fastContentCreatorActionList.getAncestorOfType(UIApplication.class) ;
      if(uiPopup != null && uiPopup.isShow()) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.remove-popup-first", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiPopup != null && uiPopup.isRendered()) popupContainer.removeChildById(UIFastContentCreatorConstant.ACTION_POPUP_WINDOW) ;
      try {
        actionService.removeAction(fastContentCreatorConfig.getSavedLocationNode(), actionName, UIFastContentCreatorUtils.getPreferenceRepository()) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.access-denied", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      fastContentCreatorActionList.updateGrid(fastContentCreatorConfig.getSavedLocationNode(), fastContentCreatorActionList.getChild(UIPageIterator.class).getCurrentPage());
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorConfig) ;
    }
  }
}
