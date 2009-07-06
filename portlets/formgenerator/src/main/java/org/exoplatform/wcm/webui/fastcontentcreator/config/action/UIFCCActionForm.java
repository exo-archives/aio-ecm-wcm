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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCConstant;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCPortlet;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFCCUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.UIFCCConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupContainer;
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
      @EventConfig(listeners = UIFCCActionForm.SaveActionListener.class),
      @EventConfig(listeners = UIDialogForm.OnchangeActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIFCCActionForm.CloseActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFCCActionForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFCCActionForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)
public class UIFCCActionForm extends UIDialogForm implements UISelectable {

  private String parentPath_ ;
  private String nodeTypeName_ = null ;
  private String scriptPath_ = null ;
  private String rootPath_ = null;
  
  private static final String EXO_ACTIONS = "exo:actions".intern();
  
  public UIFCCActionForm() throws Exception {setActions(new String[]{"Save","Close"}) ;}
  
  public void createNewAction(Node parentNode, String actionType, boolean isAddNew) throws Exception {
    reset() ;
    parentPath_ = parentNode.getPath() ;
    nodeTypeName_ = actionType;
    componentSelectors.clear() ;
    properties.clear() ;
    getChildren().clear() ;
  }
  
  private Node getParentNode() throws Exception{ return (Node) getSession().getItem(parentPath_) ; }
  
  public void doSelect(String selectField, Object value) {
    isUpdateSelect = true ;
    getUIStringInput(selectField).setValue(value.toString()) ;
  }
  
  public String getCurrentPath() throws Exception { 
    UIFCCPortlet fastContentCreatorPortlet = getAncestorOfType(UIFCCPortlet.class);
    UIFCCConfig fastContentCreatorConfig = fastContentCreatorPortlet.getChild(UIFCCConfig.class); 
    return fastContentCreatorConfig.getSavedLocationNode().getPath();
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    DMSRepositoryConfiguration repositoryConfiguration = dmsConfiguration.getConfig(repositoryName);
    return new JCRResourceResolver(repositoryName, repositoryConfiguration.getSystemWorkspace(), "exo:templateFile") ;
  }

  public String getTemplate() { return getDialogPath() ; }

  public String getDialogPath() {
    repositoryName = UIFCCUtils.getPreferenceRepository() ;
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

  public String getPath() { return scriptPath_ ; }  

  public void setRootPath(String rootPath){
   rootPath_ = rootPath;
  }
  
  public String getRootPath(){return rootPath_;}
  
  public void onchange(Event<?> event) throws Exception {
    event.getRequestContext().addUIComponentToUpdateByAjax(getParent()) ;
  }
  
  static public class SaveActionListener extends EventListener<UIFCCActionForm> {
    public void execute(Event<UIFCCActionForm> event) throws Exception {
      UIFCCActionForm fastContentCreatorActionForm = event.getSource();
      UIApplication uiApp = fastContentCreatorActionForm.getAncestorOfType(UIApplication.class) ;
      
      // Get current node
      UIFCCPortlet fastContentCreatorPortlet = fastContentCreatorActionForm.getAncestorOfType(UIFCCPortlet.class);
      UIFCCConfig fastContentCreatorConfig = fastContentCreatorPortlet.getChild(UIFCCConfig.class) ;   
      Node currentNode = fastContentCreatorConfig.getSavedLocationNode();
      
      // Check permission for current node
      if(!PermissionUtil.canAddNode(currentNode) || !PermissionUtil.canSetProperty(currentNode)) {
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionForm.msg.no-permission-add", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      
      // Add lock token if node is locked
      if (currentNode.isLocked()) {
        String lockToken = LockUtil.getLockToken(currentNode);
        if(lockToken != null) {
          currentNode.getSession().addLockToken(lockToken);
        }
      }

      // Close popup
      UIPopupContainer popupContainer = fastContentCreatorActionForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(popupContainer, UIFCCConstant.ACTION_POPUP_WINDOW);
      
      try{
        Map<String, JcrInputProperty> sortedInputs = DialogFormUtil.prepareMap(fastContentCreatorActionForm.getChildren(), fastContentCreatorActionForm.getInputProperties());
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
        Node parentNode = fastContentCreatorActionForm.getParentNode();
        
        // Check if action existed
        if(parentNode.hasNode(EXO_ACTIONS)) {
          if(parentNode.getNode(EXO_ACTIONS).hasNode(actionName)) { 
            Object[] args = {actionName} ;
            uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionForm.msg.existed-action", args, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return;
          }
        }
        
        // Check parent node
        if(parentNode.isNew()) {
          String[] args = {parentNode.getPath()} ;
          uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionForm.msg.unable-add-action",args)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
        
        // Save to database
        ActionServiceContainer actionServiceContainer = fastContentCreatorActionForm.getApplicationComponent(ActionServiceContainer.class) ;
        String repository = UIFCCUtils.getPreferenceRepository() ;
        actionServiceContainer.addAction(parentNode, repository, fastContentCreatorActionForm.nodeTypeName_, sortedInputs);
        fastContentCreatorActionForm.setIsOnchange(false) ;
        parentNode.getSession().save() ;
        
        // Create action
        fastContentCreatorActionForm.createNewAction(fastContentCreatorConfig.getSavedLocationNode(), fastContentCreatorActionForm.nodeTypeName_, true) ;
        UIFCCActionList fastContentCreatorActionList = fastContentCreatorConfig.findFirstComponentOfType(UIFCCActionList.class) ;  
        fastContentCreatorActionList.updateGrid(parentNode, fastContentCreatorActionList.getChild(UIGrid.class).getUIPageIterator().getCurrentPage());
        fastContentCreatorActionForm.reset() ;
      } catch(RepositoryException repo) {      
        String key = "UIFastContentCreatorActionForm.msg.repository-exception" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(NumberFormatException nume) {
        String key = "UIFastContentCreatorActionForm.msg.numberformat-exception" ;
        uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (NullPointerException nullPointerException) {
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (Exception e) {           
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatorActionForm.msg.unable-add", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }      
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorPortlet) ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIFCCActionForm> {
    public void execute(Event<UIFCCActionForm> event) throws Exception {
      UIFCCActionForm fastContentCreatorActionForm = event.getSource();
      UIPopupContainer popupContainer = fastContentCreatorActionForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(popupContainer, UIFCCConstant.ACTION_POPUP_WINDOW);
    }
  }  
  
  static public class RemoveReferenceActionListener extends EventListener<UIFCCActionForm> {
    public void execute(Event<UIFCCActionForm> event) throws Exception {
      UIFCCActionForm fastContentCreatorActionForm = event.getSource() ;
      fastContentCreatorActionForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      fastContentCreatorActionForm.getUIStringInput(fieldName).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorActionForm.getParent()) ;
    }
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIFCCActionForm> {
    public void execute(Event<UIFCCActionForm> event) throws Exception {
      UIFCCActionForm fastContentCreatorActionForm = event.getSource() ;
      UIContainer uiContainer = fastContentCreatorActionForm.getParent() ;
      fastContentCreatorActionForm.isShowingComponent = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = fastContentCreatorActionForm.componentSelectors.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      String rootPath = (String)fieldPropertiesMap.get("rootPath") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
      if(uiComp instanceof UIOneNodePathSelector) {
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        String wsName = "";
        if(wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String)fastContentCreatorActionForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;          
          ((UIOneNodePathSelector)uiComp).setIsDisable(wsName, true) ;           
        }
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",") ;
          if(arrParams.length == 4) {
            ((UIOneNodePathSelector)uiComp).setAcceptedNodeTypesInPathPanel(new String[] {"nt:file"}) ;
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
        ((UIOneNodePathSelector)uiComp).setRootNodeLocation(UIFCCUtils.getPreferenceRepository(), wsName, rootPath) ;
        ((UIOneNodePathSelector)uiComp).setShowRootPathSelect(true);
        ThreadLocalSessionProviderService threadLocalSessionProviderService = fastContentCreatorActionForm.getApplicationComponent(ThreadLocalSessionProviderService.class);
        SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
        ((UIOneNodePathSelector)uiComp).init(sessionProvider);
      }
      UIPopupContainer popupContainer = fastContentCreatorActionForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.removeChildById(UIFCCConstant.SELECTOR_POPUP_WINDOW);
      Utils.createPopupWindow(popupContainer, uiComp, event.getRequestContext(), UIFCCConstant.SELECTOR_POPUP_WINDOW, 640, 300);
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)uiComp).setSourceComponent(fastContentCreatorActionForm, new String[]{param}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
}
