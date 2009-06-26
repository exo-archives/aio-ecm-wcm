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

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorConstant;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFastContentCreatorConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIFastContentCreatorActionForm.SaveActionListener.class),
      @EventConfig(listeners = UIDialogForm.OnchangeActionListener.class, phase=Phase.DECODE),
//      @EventConfig(listeners = UIFastContentCreatorActionForm.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorActionForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorActionForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)
public class UIFastContentCreatorActionForm extends UIDialogForm implements UISelectable {

  private String parentPath_ ;
  private String nodeTypeName_ = null ;
  private boolean isAddNew_ ;
  private String scriptPath_ = null ;
  private boolean isEditInList_ = false ;
  private String rootPath_ = null;
  
  private static final String EXO_ACTIONS = "exo:actions".intern();
  
  public UIFastContentCreatorActionForm() throws Exception {setActions(new String[]{"Save","Back"}) ;}
  
  public void createNewAction(Node parentNode, String actionType, boolean isAddNew) throws Exception {
    reset() ;
    parentPath_ = parentNode.getPath() ;
    nodeTypeName_ = actionType;
    isAddNew_ = isAddNew ;
    componentSelectors.clear() ;
    properties.clear() ;
    getChildren().clear() ;
  }
  
  private Node getParentNode() throws Exception{ return (Node) getSession().getItem(parentPath_) ; }
  
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true ;
    getUIStringInput(selectField).setValue(value.toString()) ;
    if(isEditInList_) {
//      UIPopupContainer popupContainer = getAncestorOfType(UIPopupContainer.class) ;
//      UIActionListContainer uiActionListContainer = popupContainer.getChild(UIActionListContainer.class) ;
//      uiActionListContainer.removeChildById("PopupComponent") ;
//    } else {
//      UIActionContainer uiActionContainer = getParent() ;
//      uiActionContainer.removeChildById("PopupComponent") ;
    }
  }
  
  public String getCurrentPath() throws Exception { 
    return getAncestorOfType(UIFastContentCreatorConfig.class).getSavedLocationNode().getPath();
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return new JCRResourceResolver(repositoryName, "collaboration", "exo:templateFile") ;
  }

  public String getTemplate() { return getDialogPath() ; }

  public String getDialogPath() {
    repositoryName = UIFastContentCreatorUtils.getPreferenceRepository() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    String dialogPath = null ;
    if (nodeTypeName_ != null) {
      try {
        dialogPath = templateService.getTemplatePathByUser(true, nodeTypeName_, userName, repositoryName);
      } catch (Exception e){
        e.printStackTrace() ;
      }      
    }
    return dialogPath ;    
  }
  
  public String getRepositoryName() { return repositoryName; }

  public String getTemplateNodeType() { return nodeTypeName_ ; }

  private void setPath(String scriptPath) {
    if(scriptPath.indexOf(":") < 0) {
      scriptPath = UIFastContentCreatorUtils.getPreferenceWorkspace() + ":" + scriptPath ;
    }
    scriptPath_ = scriptPath ; 
  }
  public String getPath() { return scriptPath_ ; }  
  public void setRootPath(String rootPath){
   rootPath_ = rootPath;
  }
  public String getRootPath(){return rootPath_;}
  public void setIsEditInList(boolean isEditInList) { isEditInList_ = isEditInList; }
  
  public void onchange(Event<?> event) throws Exception {
    if(isEditInList_ || !isAddNew_) {
      event.getRequestContext().addUIComponentToUpdateByAjax(getParent()) ;
      return ;
    }
    UIPopupContainer popupContainer = getAncestorOfType(UIPopupContainer.class) ;
    popupContainer.setRenderedChild(UIFastContentCreatorActionContainer.class) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
  }
  
  static public class SaveActionListener extends EventListener<UIFastContentCreatorActionForm> {
    public void execute(Event<UIFastContentCreatorActionForm> event) throws Exception {
      UIFastContentCreatorActionForm actionForm = event.getSource();
      UIApplication uiApp = actionForm.getAncestorOfType(UIApplication.class) ;
      ActionServiceContainer actionServiceContainer = actionForm.getApplicationComponent(ActionServiceContainer.class) ;
      UIFastContentCreatorConfig fastContentCreatorConfig = actionForm.getAncestorOfType(UIFastContentCreatorConfig.class) ;   
      String repository = UIFastContentCreatorUtils.getPreferenceRepository() ;
      Map<String, JcrInputProperty> sortedInputs = DialogFormUtil.prepareMap(actionForm.getChildren(), actionForm.getInputProperties());
      Node currentNode = fastContentCreatorConfig.getSavedLocationNode();
      if(!PermissionUtil.canAddNode(currentNode) || !PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.no-permission-add", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      if (currentNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(currentNode);
        if(lockToken != null) {
          currentNode.getSession().addLockToken(lockToken);
        }
      }
      if(!actionForm.isAddNew_) {
        CmsService cmsService = actionForm.getApplicationComponent(CmsService.class) ;      
        Node storedHomeNode = actionForm.getNode().getParent() ;
        cmsService.storeNode(actionForm.nodeTypeName_, storedHomeNode, sortedInputs, false,repository) ;
//        if(!fastContentCreatorConfig.getPreference().isJcrEnable()) currentNode.getSession().save() ;
        if(actionForm.isEditInList_) {
          UIPopupContainer popupContainer = actionForm.getAncestorOfType(UIPopupContainer.class);
          UIPopupWindow uiPopup = popupContainer.getChildById(UIFastContentCreatorConstant.ACTION_POPUP_WINDOW) ;
          uiPopup.setShow(false) ;
          uiPopup.setRendered(false) ;
//          uiManager.setDefaultConfig() ;
          actionForm.isEditInList_ = false ;
          actionForm.isAddNew_ = true ;
          actionForm.setIsOnchange(false) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorConfig) ;
//          fastContentCreatorConfig.setIsHidePopup(true) ;
//          fastContentCreatorConfig.updateAjax(event) ;
        } else {
//          fastContentCreatorConfig.setIsHidePopup(false) ;
//          fastContentCreatorConfig.updateAjax(event) ;
        }
        actionForm.setPath(storedHomeNode.getPath()) ;
        return;
      }
      try{
        JcrInputProperty rootProp = sortedInputs.get("/node");
        if(rootProp == null) {
          rootProp = new JcrInputProperty();
          rootProp.setJcrPath("/node");
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue()) ;
          sortedInputs.put("/node", rootProp) ;
        } else {
          rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
        }
        String actionName = (String)(sortedInputs.get("/node/exo:name")).getValue() ;
        Node parentNode = actionForm.getParentNode();
        if(parentNode.hasNode(EXO_ACTIONS)) {
          if(parentNode.getNode(EXO_ACTIONS).hasNode(actionName)) { 
            Object[] args = {actionName} ;
            uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.existed-action", args, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return;
          }
        }
        if(parentNode.isNew()) {
          String[] args = {parentNode.getPath()} ;
          uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add-action",args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
        actionServiceContainer.addAction(parentNode, repository, actionForm.nodeTypeName_, sortedInputs);
        actionForm.setIsOnchange(false) ;
//        if(!fastContentCreatorConfig.getPreference().isJcrEnable()) parentNode.getSession().save() ;
        actionForm.createNewAction(fastContentCreatorConfig.getSavedLocationNode(), actionForm.nodeTypeName_, true) ;
        UIFastContentCreatorActionList fastContentCreatorActionList = fastContentCreatorConfig.findFirstComponentOfType(UIFastContentCreatorActionList.class) ;  
        fastContentCreatorActionList.updateGrid(parentNode, fastContentCreatorActionList.getChild(UIPageIterator.class).getCurrentPage());
//        uiActionManager.setRenderedChild(UIActionListContainer.class) ;
        actionForm.reset() ;
        actionForm.isEditInList_ = false ;
      } catch(RepositoryException repo) {      
        String key = "UIActionForm.msg.repository-exception" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(NumberFormatException nume) {
        String key = "UIActionForm.msg.numberformat-exception" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (NullPointerException nullPointerException) {
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (Exception e) {           
        uiApp.addMessage(new ApplicationMessage("UIActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }      
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIFastContentCreatorActionForm> {
    public void execute(Event<UIFastContentCreatorActionForm> event) throws Exception {
      UIFastContentCreatorActionForm uiForm = event.getSource() ;
      UIContainer uiContainer = null;
      uiForm.isShowingComponent = true;
      if(uiForm.isEditInList_) {
//        uiContainer = uiForm.getAncestorOfType(UIActionListContainer.class) ;
      } else {
        uiContainer = uiForm.getParent() ;
      }
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = uiForm.componentSelectors.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      String rootPath = (String)fieldPropertiesMap.get("rootPath") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIOneNodePathSelector) {
        String repositoryName = UIFastContentCreatorUtils.getPreferenceRepository() ;
//        SessionProvider provider = fastContentCreatorConfig.getSessionProvider() ;        
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        String wsName = "";
        if(wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String)uiForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;          
          ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true) ;           
        }
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",") ;
          if(arrParams.length == 4) {
            ((UIOneNodePathSelector)uiComp).setAcceptedNodeTypesInPathPanel(new String[] {Utils.NT_FILE}) ;
            wsName = arrParams[1];
            rootPath = arrParams[2];
            ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true) ;
            if(arrParams[3].indexOf(";") > -1) {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(arrParams[3].split(";")) ;
            } else {
              ((UIOneNodePathSelector)uiComp).setAcceptedMimeTypes(new String[] {arrParams[3]}) ;
            }
          }
        }
        if(rootPath == null) rootPath = "/";
        ((UIOneNodePathSelector)uiComp).setRootNodeLocation(repositoryName, wsName, rootPath) ;
        ((UIOneNodePathSelector)uiComp).setShowRootPathSelect(true);
//        ((UIOneNodePathSelector)uiComp).init(provider);
      }
//      if(uiForm.isEditInList_) ((UIActionListContainer) uiContainer).initPopup(uiComp) ;
//      else ((UIActionContainer)uiContainer).initPopup(uiComp) ;
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)uiComp).setSourceComponent(uiForm, new String[]{param}) ;
      if(uiForm.isAddNew_) {
        UIContainer uiParent = uiContainer.getParent() ;
        uiParent.setRenderedChild(uiContainer.getId()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

  static public class RemoveReferenceActionListener extends EventListener<UIFastContentCreatorActionForm> {
    public void execute(Event<UIFastContentCreatorActionForm> event) throws Exception {
      UIFastContentCreatorActionForm fastContentCreatorActionForm = event.getSource() ;
      fastContentCreatorActionForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      fastContentCreatorActionForm.getUIStringInput(fieldName).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorActionForm.getParent()) ;
    }
  }
  
//  static public class BackActionListener extends EventListener<UIFastContentCreatorActionForm> {
//    public void execute(Event<UIFastContentCreatorActionForm> event) throws Exception {
//      UIFastContentCreatorActionForm fastContentCreatorActionForm = event.getSource() ;
//      UIActionManager uiManager = fastContentCreatorActionForm.getAncestorOfType(UIActionManager.class) ;
//      if(fastContentCreatorActionForm.isAddNew_) {
//        uiManager.setRenderedChild(UIActionListContainer.class) ;
//        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
//      } else {
//        if(fastContentCreatorActionForm.isEditInList_) {
//          uiManager.setRenderedChild(UIActionListContainer.class) ;
//          uiManager.setDefaultConfig() ;
//          UIActionListContainer uiActionListContainer = uiManager.getChild(UIActionListContainer.class) ;
//          UIPopupWindow uiPopup = uiActionListContainer.findComponentById("editActionPopup") ;
//          uiPopup.setShow(false) ;
//          uiPopup.setRendered(false) ;
//          fastContentCreatorActionForm.isEditInList_ = false ;
//          event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
//        } else {
//          UIJCRExplorer uiExplorer = fastContentCreatorActionForm.getAncestorOfType(UIJCRExplorer.class) ;
//          uiExplorer.cancelAction() ;
//        }
//      }
//    }
//  }  
  
}
