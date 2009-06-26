/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.fastcontentcreator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorConstant;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorPortlet;
import org.exoplatform.wcm.webui.fastcontentcreator.UIFastContentCreatorUtils;
import org.exoplatform.wcm.webui.fastcontentcreator.config.action.UIFastContentCreatorActionList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIFastContentCreatorConfig.SaveActionListener.class),
      @EventConfig(listeners = UIFastContentCreatorConfig.SelectPathActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorConfig.ChangeWorkspaceActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorConfig.ChangeRepositoryActionListener.class, phase=Phase.DECODE)
    }
)
public class UIFastContentCreatorConfig extends UIForm implements UISelectable {

  private Node savedLocationNode;
  
  public UIFastContentCreatorConfig() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;

    UIFormInputSet saveLocationField = new UIFormInputSet(UIFastContentCreatorConstant.SAVE_LOCATION_FIELD);
    
    UIFormSelectBox repositorySelectBox = new UIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX, UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX, options) ; 
    repositorySelectBox.setOnChange("ChangeRepository") ;
    saveLocationField.addUIFormInput(repositorySelectBox) ;
    
    UIFormSelectBox workspaceSelectBox = new UIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX, UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX, options) ; 
    workspaceSelectBox.setOnChange("ChangeWorkspace") ;
    saveLocationField.addUIFormInput(workspaceSelectBox) ;
    
    UIFormInputSetWithAction folderSelectorInput = new UIFormInputSetWithAction(UIFastContentCreatorConstant.LOCATION_FORM_INPUT_ACTION) ;
    folderSelectorInput.addUIFormInput(new UIFormStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT, UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT, null).setEditable(false)) ;
    folderSelectorInput.setActionInfo(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT, new String[] {"SelectPath"}) ;
    saveLocationField.addChild(folderSelectorInput) ;
    
    addUIFormInput(saveLocationField);
    
    UIFormInputSet templateField = new UIFormInputSet(UIFastContentCreatorConstant.TEMPLATE_FIELD);
    templateField.addUIFormInput(new UIFormSelectBox(UIFastContentCreatorConstant.TEMPLATE_FORM_SELECTBOX, UIFastContentCreatorConstant.TEMPLATE_FORM_SELECTBOX, options));
    templateField.addUIFormInput(new UIFormStringInput(UIFastContentCreatorConstant.SAVE_FORM_STRING_INPUT, UIFastContentCreatorConstant.SAVE_FORM_STRING_INPUT, null));
    templateField.addUIFormInput(new UIFormTextAreaInput(UIFastContentCreatorConstant.MESSAGE_FORM_TEXTAREA_INPUT, UIFastContentCreatorConstant.MESSAGE_FORM_TEXTAREA_INPUT, null));
    templateField.addUIFormInput(new UIFormCheckBoxInput<Boolean>(UIFastContentCreatorConstant.REDIRECT_FORM_CHECKBOX_INPUT, UIFastContentCreatorConstant.REDIRECT_FORM_CHECKBOX_INPUT, false));
    templateField.addUIFormInput(new UIFormStringInput(UIFastContentCreatorConstant.REDIRECT_PATH_FORM_STRING_INPUT, UIFastContentCreatorConstant.REDIRECT_PATH_FORM_STRING_INPUT, null));

    addUIFormInput(templateField);
    
    UIFormInputSet actionField = new UIFormInputSet(UIFastContentCreatorConstant.ACTION_FIELD);
    actionField.addChild(UIFastContentCreatorActionList.class, null, null);
    
    addUIFormInput(actionField);
    
    setActions(new String[] {"Save"}) ;
  }
  
  public void initEditMode() throws Exception {
    PortletPreferences preferences = UIFastContentCreatorUtils.getPortletPreferences();
    String preferenceRepository = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, "") ;
    boolean isDefaultWorkspace = false ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;  
    List<SelectItemOption<String>> repositories = new ArrayList<SelectItemOption<String>>() ;
    for(RepositoryEntry repositoryEntry : repositoryService.getConfig().getRepositoryConfigurations()) {
      repositories.add(new SelectItemOption<String>(repositoryEntry.getName())) ;
    }
    UIFormSelectBox uiRepositoryList = getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX) ;
    uiRepositoryList.setOptions(repositories) ;
    uiRepositoryList.setValue(preferenceRepository) ;
    try {
      ManageableRepository repository = getApplicationComponent(RepositoryService.class).getRepository(preferenceRepository) ;
      String[] workspaceNames = repository.getWorkspaceNames();
      String systemWsName = repository.getConfiguration().getSystemWorkspaceName() ;
      List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
      String preferenceWorkspace = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, "") ;
      setTemplateOptions(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_PATH, ""), preferenceRepository, preferenceWorkspace) ;
      for(String workspaceName : workspaceNames) {
        if(!workspaceName.equals(systemWsName)) {
          if(workspaceName.equals(preferenceWorkspace)) isDefaultWorkspace = true ;
          workspace.add(new SelectItemOption<String>(workspaceName)) ;
        }
      }
      UIFormSelectBox uiWorkspaceList = getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX) ; 
      uiWorkspaceList.setOptions(workspace) ;
      if(isDefaultWorkspace) {
        uiWorkspaceList.setValue(preferenceWorkspace);
      } else if(workspace.size() > 0) {
        uiWorkspaceList.setValue(workspace.get(0).getValue());
      }
      getUIStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT).setValue(preferences.getValue("path", "")) ;
    } catch(RepositoryException repo) {
      PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
      ResourceBundle resourceBundle = portletRequestContext.getApplicationResourceBundle() ;
      String label = resourceBundle.getString(getId() + ".label.select-repository") ;
      repositories.add(new SelectItemOption<String>(label, "")) ;
      uiRepositoryList.setValue("") ;
    }
  }
  
  private void setTemplateOptions(String nodePath, String repositoryName, String workspaceName) throws Exception {
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository repository = repositoryService.getRepository(repositoryName);
      ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspaceName, repository);
      Node currentNode = null ;
      UIFormSelectBox uiSelectTemplate = getUIFormSelectBox(UIFastContentCreatorConstant.TEMPLATE_FORM_SELECTBOX) ;
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
      boolean hasDefaultDoc = false ;
      String defaultValue = UIFastContentCreatorUtils.getPreferenceType();
      try {
        currentNode = (Node)session.getItem(nodePath) ;
        setSavedLocationNode(currentNode);
      } catch(PathNotFoundException ex) {
        UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.item-not-found", null, ApplicationMessage.WARNING)) ;
        session.logout();
        return ;
      }
      NodeTypeManager ntManager = currentNode.getSession().getWorkspace().getNodeTypeManager() ; 
      NodeType currentNodeType = currentNode.getPrimaryNodeType() ; 
      NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
      TemplateService templateService = getApplicationComponent(TemplateService.class) ;
      List templates = templateService.getDocumentTemplates(repositoryName) ;
      List<String> labels = new ArrayList<String>() ;
      try {
        for(int i = 0; i < templates.size(); i ++){
          String nodeTypeName = templates.get(i).toString() ; 
          NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
          NodeType[] superTypes = nodeType.getSupertypes() ;
          boolean isCanCreateDocument = false ;
          for(NodeDefinition childDef : childDefs){
            NodeType[] requiredChilds = childDef.getRequiredPrimaryTypes() ;
            for(NodeType requiredChild : requiredChilds) {          
              if(nodeTypeName.equals(requiredChild.getName())){            
                isCanCreateDocument = true ;
                break ;
              }            
            }
            if(nodeTypeName.equals(childDef.getName()) || isCanCreateDocument) {
              if(!hasDefaultDoc && nodeTypeName.equals(defaultValue)) hasDefaultDoc = true ;
              String label = templateService.getTemplateLabel(nodeTypeName, repositoryName) ;
              if(!labels.contains(label)) {
                options.add(new SelectItemOption<String>(label, nodeTypeName));          
              }
              labels.add(label) ;
              isCanCreateDocument = true ;          
            }
          }      
          if(!isCanCreateDocument){
            for(NodeType superType:superTypes) {
              for(NodeDefinition childDef : childDefs){          
                for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                  if (superType.getName().equals(requiredType.getName())) {
                    if(!hasDefaultDoc && nodeTypeName.equals(defaultValue)) {
                      hasDefaultDoc = true ;
                    }
                    String label = templateService.getTemplateLabel(nodeTypeName, repositoryName) ;
                    if(!labels.contains(label)) {
                      options.add(new SelectItemOption<String>(label, nodeTypeName));                
                    }
                    labels.add(label) ;
                    isCanCreateDocument = true ;
                    break;
                  }
                }
                if(isCanCreateDocument) break ;
              }
              if(isCanCreateDocument) break ;
            }
          }            
        }
        uiSelectTemplate.setOptions(options) ;
        if(hasDefaultDoc) {
          uiSelectTemplate.setValue(defaultValue);
        } else if(options.size() > 0) {
          defaultValue = options.get(0).getValue() ;
          uiSelectTemplate.setValue(defaultValue);
        } 
      } catch(Exception e) {
        e.printStackTrace() ;
      }
      session.logout();
    } catch(Exception ex) {
      ex.printStackTrace() ;
    }
  }
  
  public void doSelect(String selectField, Object value) {
    getUIStringInput(selectField).setValue(value.toString()) ;
    String repositoryName = getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX).getValue() ;
    String workspaceName = getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX).getValue() ;
    String savedLocationPath = value.toString();
    try {
      setTemplateOptions(savedLocationPath, repositoryName, workspaceName) ;
    } catch(Exception ex) {
      ex.printStackTrace() ;
    }
    UIFastContentCreatorPortlet fastContentCreatorPortlet = getAncestorOfType(UIFastContentCreatorPortlet.class);
    UIPopupContainer popupContainer = fastContentCreatorPortlet.getChild(UIPopupContainer.class);
    Utils.closePopupWindow(popupContainer, UIFastContentCreatorConstant.SELECTOR_POPUP_WINDOW);
  }

  public Node getSavedLocationNode() {
    return savedLocationNode;
  }

  public void setSavedLocationNode(Node savedLocationNode) {
    this.savedLocationNode = savedLocationNode;
  }
  
  static public class SelectPathActionListener extends EventListener<UIFastContentCreatorConfig> {
    public void execute(Event<UIFastContentCreatorConfig> event) throws Exception {
      UIFastContentCreatorConfig fastContentCreatorConfig = event.getSource() ;
      UIFastContentCreatorPortlet fastContentCreatorPortlet = fastContentCreatorConfig.getParent() ;
      String repositoryName = fastContentCreatorConfig.getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX).getValue() ;
      String workspaceName = fastContentCreatorConfig.getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX).getValue() ;
      
      RepositoryService repositoryService = fastContentCreatorConfig.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      String systemWorkspaceName = manageableRepository.getConfiguration().getSystemWorkspaceName();
      
      UIOneNodePathSelector uiOneNodePathSelector = fastContentCreatorConfig.createUIComponent(UIOneNodePathSelector.class, null, null);
      uiOneNodePathSelector.setIsDisable(workspaceName, true) ;
      uiOneNodePathSelector.setShowRootPathSelect(true) ;
      uiOneNodePathSelector.setRootNodeLocation(repositoryName, workspaceName, "/");
      if(SessionProviderFactory.isAnonim()) {
        uiOneNodePathSelector.init(SessionProviderFactory.createAnonimProvider()) ;
      } else if(workspaceName.equals(systemWorkspaceName)){
        uiOneNodePathSelector.init(SessionProviderFactory.createSystemProvider()) ;
      } else {
        uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider()) ;
      }
      uiOneNodePathSelector.setSourceComponent(fastContentCreatorConfig, new String[] {UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT}) ;
      UIPopupContainer popupContainer = fastContentCreatorPortlet.getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UIFastContentCreatorConstant.SELECTOR_POPUP_WINDOW);
      if(popupWindow == null) {
        Utils.createPopupWindow(popupContainer, uiOneNodePathSelector, event.getRequestContext(), UIFastContentCreatorConstant.SELECTOR_POPUP_WINDOW, 610, 300);
      } else {
        popupWindow.setShow(true);
      }
    }
  }
  
  static public class ChangeWorkspaceActionListener extends EventListener<UIFastContentCreatorConfig> {
    public void execute(Event<UIFastContentCreatorConfig> event) throws Exception {
      UIFastContentCreatorConfig uiTypeForm = event.getSource() ;
      uiTypeForm.getUIStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT).setValue("/") ;
      String repoName = uiTypeForm.getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX).getValue() ;
      String wsName = uiTypeForm.getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX).getValue() ;
      uiTypeForm.setTemplateOptions(uiTypeForm.getUIStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT).getValue(), repoName, wsName) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTypeForm) ;
    }
  }
  
  static public class ChangeRepositoryActionListener extends EventListener<UIFastContentCreatorConfig> {
    public void execute(Event<UIFastContentCreatorConfig> event) throws Exception {
      UIFastContentCreatorConfig uiTypeForm = event.getSource() ;
      RepositoryService repositoryService = uiTypeForm.getApplicationComponent(RepositoryService.class) ;
      uiTypeForm.getUIStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT).setValue("/") ;
      String repoName = uiTypeForm.getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX).getValue() ;
      String[] wsNames = repositoryService.getRepository(repoName).getWorkspaceNames();
      String systemWsName = repositoryService.getRepository(repoName).getConfiguration().getSystemWorkspaceName() ;
      List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
      for(String ws : wsNames) {
        if(!ws.equals(systemWsName)) workspace.add(new SelectItemOption<String>(ws, ws)) ;
      }
      if(workspace.size() > 0) {
        uiTypeForm.setTemplateOptions("/", repoName, workspace.get(0).getLabel()) ;
      }
      uiTypeForm.getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX).setOptions(workspace) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTypeForm) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIFastContentCreatorConfig> {
    public void execute(Event<UIFastContentCreatorConfig> event) throws Exception {
      UIFastContentCreatorConfig uiEditModeConfiguration = event.getSource() ;
      UIApplication uiApp = uiEditModeConfiguration.getAncestorOfType(UIApplication.class) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletRequest request = context.getRequest() ; 
      PortletPreferences preferences = request.getPreferences() ;
      String fileType = uiEditModeConfiguration.getUIFormSelectBox(UIFastContentCreatorConstant.TEMPLATE_FORM_SELECTBOX).getValue() ;
      String location = uiEditModeConfiguration.getUIStringInput(UIFastContentCreatorConstant.LOCATION_FORM_STRING_INPUT).getValue() ;
      String wsName = uiEditModeConfiguration.getUIFormSelectBox(UIFastContentCreatorConstant.WORKSPACE_FORM_SELECTBOX).getValue() ;
      if(wsName == null || wsName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.ws-empty", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      String repoName = uiEditModeConfiguration.getUIFormSelectBox(UIFastContentCreatorConstant.REPOSITORY_FORM_SELECTBOX).getValue() ;
      if(fileType == null || fileType.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.fileType-empty", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      preferences.setValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, wsName) ;
      preferences.setValue(UIFastContentCreatorConstant.PREFERENCE_PATH, location) ;
      preferences.setValue(UIFastContentCreatorConstant.PREFERENCE_TYPE, fileType) ;
      preferences.setValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, repoName) ;
      preferences.store() ;
      uiApp.addMessage(new ApplicationMessage("UIEditModeConfiguration.msg.save-successfully", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }
  
}
