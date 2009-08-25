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
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCConstant;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCPortlet;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFCCConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/groovy/webui/FastContentCreatorPortlet/UIFCCActionList.gtmpl",
    events = {
        @EventConfig(listeners = UIFCCActionList.AddActionListener.class),
        @EventConfig(listeners = UIFCCActionList.EditActionListener.class),
        @EventConfig(listeners = UIFCCActionList.DeleteActionListener.class, confirm = "UIFCCActionList.msg.confirm-delete-action")
    }
)
public class UIFCCActionList extends UIContainer {

  private static final String[] HEADERS = {"name", "description", "instanceOf"};
  
  private static final String[] ACTIONS = {"Edit", "Delete"};
  
  public UIFCCActionList() throws Exception {
    UIGrid grid = addChild(UIGrid.class, null, null);
    grid.configure(UIFCCConstant.ACTION_GRID, HEADERS , ACTIONS );
  }
  
  public void updateGrid(Node node, int currentPage) throws Exception {
    UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
    ObjectPageList objPageList = new ObjectPageList(getAllActions(node), 10) ;
    uiIterator.setPageList(objPageList);
    if(currentPage > uiIterator.getAvailablePage())
      uiIterator.setCurrentPage(currentPage-1);
    else
      uiIterator.setCurrentPage(currentPage);
  }

  public String[] getActions() { return ACTIONS ; }

  public boolean hasActions() {
    UIFCCConfig fastContentCreatorConfig = getAncestorOfType(UIFCCConfig.class) ;
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
    UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
    return uiIterator.getCurrentPageData() ; 
  }
  
  public static class AddActionListener extends EventListener<UIFCCActionList> {
    public void execute(Event<UIFCCActionList> event) throws Exception {
      UIFCCActionList fastContentCreatorActionList = event.getSource();
      UIFCCActionContainer fastContentCreatorActionContainer = fastContentCreatorActionList.createUIComponent(UIFCCActionContainer.class, null, null);
      Utils.createPopupWindow(fastContentCreatorActionList, fastContentCreatorActionContainer, UIFCCConstant.ACTION_POPUP_WINDOW, 550, 380);
      fastContentCreatorActionContainer.getChild(UIFCCActionTypeForm.class).update();
    }
  }
  
  public static class EditActionListener extends EventListener<UIFCCActionList> {
    public void execute(Event<UIFCCActionList> event) throws Exception {
      UIFCCActionList fastContentCreatorActionList = event.getSource();
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFCCActionContainer fccActionContainer = fastContentCreatorActionList.createUIComponent(UIFCCActionContainer.class, null, null);
      Utils.createPopupWindow(fastContentCreatorActionList, fccActionContainer, UIFCCConstant.ACTION_POPUP_WINDOW, 550, 380);
      UIFCCActionTypeForm fccActionTypeForm = fccActionContainer.getChild(UIFCCActionTypeForm.class);
      
      ActionServiceContainer actionService = fastContentCreatorActionList.getApplicationComponent(ActionServiceContainer.class) ;
      UIFCCConfig fastContentCreatorConfig = fastContentCreatorActionList.getAncestorOfType(UIFCCConfig.class) ;
      Node parentNode = fastContentCreatorConfig.getSavedLocationNode();
      Node actionNode= actionService.getAction(parentNode, actionName);
      fccActionTypeForm.init(actionNode.getPath(), actionNode.getPrimaryNodeType().getName());
      fccActionTypeForm.update();
    }
  }
  
  public static class DeleteActionListener extends EventListener<UIFCCActionList> {
    public void execute(Event<UIFCCActionList> event) throws Exception {
      UIFCCActionList fastContentCreatorActionList = event.getSource() ;
      UIFCCConfig fastContentCreatorConfig = fastContentCreatorActionList.getAncestorOfType(UIFCCConfig.class) ;
      ActionServiceContainer actionService = fastContentCreatorActionList.getApplicationComponent(ActionServiceContainer.class) ;
      String actionName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer popupContainer = fastContentCreatorActionList.getAncestorOfType(UIFCCPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow uiPopup = popupContainer.getChildById(UIFCCConstant.ACTION_POPUP_WINDOW) ;
      UIApplication uiApp = fastContentCreatorActionList.getAncestorOfType(UIApplication.class) ;
      if(uiPopup != null && uiPopup.isShow()) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.remove-popup-first", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiPopup != null && uiPopup.isRendered()) popupContainer.removeChildById(UIFCCConstant.ACTION_POPUP_WINDOW) ;
      try {
        actionService.removeAction(fastContentCreatorConfig.getSavedLocationNode(), actionName, UIFCCUtils.getPreferenceRepository()) ;
      } catch(AccessDeniedException ace) {
        uiApp.addMessage(new ApplicationMessage("UIActionList.msg.access-denied", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      fastContentCreatorActionList.updateGrid(fastContentCreatorConfig.getSavedLocationNode(), fastContentCreatorActionList.getChild(UIGrid.class).getUIPageIterator().getCurrentPage());
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorConfig) ;
    }
  }
}
