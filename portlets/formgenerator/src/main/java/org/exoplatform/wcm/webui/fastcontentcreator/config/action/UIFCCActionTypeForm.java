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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCPortlet;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFCCConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/FastContentCreatorPortlet/UIFCCActionTypeForm.gtmpl",
    events = {
      @EventConfig(listeners = UIFCCActionTypeForm.ChangeActionTypeActionListener.class) 
    }
)
public class UIFCCActionTypeForm extends UIForm {

  final static public String ACTION_TYPE = "actionType" ;
  final static public String CHANGE_ACTION = "ChangeActionType" ;

  private List<SelectItemOption<String>> typeList_ ;

  public String defaultActionType_ ;

  public UIFCCActionTypeForm() throws Exception {
    typeList_ = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(ACTION_TYPE, ACTION_TYPE, new ArrayList<SelectItemOption<String>>()) ;
    uiSelectBox.setOnChange(CHANGE_ACTION) ;
    addUIFormInput(uiSelectBox) ;
  }

  private Iterator<NodeType> getCreatedActionTypes() throws Exception {
    ActionServiceContainer actionService = getApplicationComponent(ActionServiceContainer.class) ;
    return actionService.getCreatedActionTypes(UIFCCUtils.getPreferenceRepository()).iterator();
  }

  public void setDefaultActionType() throws Exception{    
    if(defaultActionType_ == null) {
      defaultActionType_ = "exo:sendMailAction" ;
      UIFCCPortlet fastContentCreatorPortlet = getAncestorOfType(UIFCCPortlet.class);
      UIFCCConfig fastContentCreatorConfig = fastContentCreatorPortlet.getChild(UIFCCConfig.class);
      Node savedLocationNode = fastContentCreatorConfig.getSavedLocationNode() ;
      UIFCCActionContainer fastContentCreatorActionContainer = getParent() ;
      UIFCCActionForm fastContentCreatorActionForm = fastContentCreatorActionContainer.getChild(UIFCCActionForm.class) ;
      fastContentCreatorActionForm.createNewAction(savedLocationNode, defaultActionType_, true) ;
      fastContentCreatorActionForm.setNodePath(null) ;
      fastContentCreatorActionForm.setWorkspace(savedLocationNode.getSession().getWorkspace().getName()) ;
      fastContentCreatorActionForm.setStoredPath(savedLocationNode.getPath()) ;
      getUIFormSelectBox(ACTION_TYPE).setValue(defaultActionType_) ;
    }
  }  

  public void update() throws Exception {
    Iterator<NodeType> actions = getCreatedActionTypes(); 
    while(actions.hasNext()){
      String action =  actions.next().getName();
      typeList_.add(new SelectItemOption<String>(action, action));
    }    
    getUIFormSelectBox(ACTION_TYPE).setOptions(typeList_) ;
    setDefaultActionType() ;
  }

  public void init() {
    
  }
  
  static public class ChangeActionTypeActionListener extends EventListener<UIFCCActionTypeForm> {
    public void execute(Event<UIFCCActionTypeForm> event) throws Exception {
      UIFCCActionTypeForm fastContentCreatorActionTypeForm = event.getSource() ;
      UIFCCPortlet fastContentCreatorPortlet = fastContentCreatorActionTypeForm.getAncestorOfType(UIFCCPortlet.class);
      UIFCCConfig fastContentCreatorConfig = fastContentCreatorPortlet.getChild(UIFCCConfig.class);
      Node currentNode = fastContentCreatorConfig.getSavedLocationNode() ;
      String actionType = fastContentCreatorActionTypeForm.getUIFormSelectBox(ACTION_TYPE).getValue() ;
      TemplateService templateService = fastContentCreatorActionTypeForm.getApplicationComponent(TemplateService.class) ;
      String repository = UIFCCUtils.getPreferenceRepository() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      UIApplication uiApp = fastContentCreatorActionTypeForm.getAncestorOfType(UIApplication.class) ;
      try {
        String templatePath = templateService.getTemplatePathByUser(true, actionType, userName, repository) ;
        if(templatePath == null) {
          Object[] arg = { actionType } ;
          uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionTypeForm.msg.access-denied", arg, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          actionType = "exo:sendMailAction" ;
          fastContentCreatorActionTypeForm.getUIFormSelectBox(UIFCCActionTypeForm.ACTION_TYPE).setValue(actionType) ;
          UIFCCActionContainer fastContentCreatorActionContainer = fastContentCreatorActionTypeForm.getAncestorOfType(UIFCCActionContainer.class) ;
          UIFCCActionForm fastContentCreatorActionForm = fastContentCreatorActionContainer.getChild(UIFCCActionForm.class) ;
          fastContentCreatorActionForm.createNewAction(currentNode, actionType, true) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorActionContainer) ;
          return ;
        }
      } catch(PathNotFoundException path) {
        Object[] arg = { actionType } ;
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionTypeForm.msg.not-support", arg, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        actionType = "exo:sendMailAction" ;
        fastContentCreatorActionTypeForm.getUIFormSelectBox(UIFCCActionTypeForm.ACTION_TYPE).setValue(actionType) ;
        UIFCCActionContainer fastContentCreatorActionContainer = fastContentCreatorActionTypeForm.getAncestorOfType(UIFCCActionContainer.class) ;
        UIFCCActionForm fastContentCreatorActionForm = fastContentCreatorActionContainer.getChild(UIFCCActionForm.class) ;
        fastContentCreatorActionForm.createNewAction(currentNode, actionType, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorActionContainer) ;
      } 
      UIFCCActionContainer fastContentCreatorActionContainer = fastContentCreatorActionTypeForm.getParent() ;
      UIFCCActionForm uiActionForm = fastContentCreatorActionContainer.getChild(UIFCCActionForm.class) ;
      uiActionForm.createNewAction(currentNode, actionType, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorActionContainer) ;
    }
  }
  
}
