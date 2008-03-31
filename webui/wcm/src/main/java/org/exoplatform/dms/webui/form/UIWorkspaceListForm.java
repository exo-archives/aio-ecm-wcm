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
package org.exoplatform.dms.webui.form;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;

import org.exoplatform.dms.webui.component.UINodeTreeBuilder;
import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Jun 21, 2007 2:32:49 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/dms/webui/component/UIFormWithoutAction.gtmpl",
    events = { 
      @EventConfig(listeners = UIWorkspaceListForm.ChangeWorkspaceActionListener.class),
      @EventConfig(listeners = UIWorkspaceListForm.AddRootNodeActionListener.class)
    }
)
public class UIWorkspaceListForm extends UIForm {

  static private String WORKSPACE_NAME = "workspaceName" ;
  static private String ROOT_NODE_INFO = "rootNodeInfo" ;
  static private String ROOT_NODE_PATH = "rootNodePath" ;
  
  private List<String> wsList_ ;
  private boolean isShowSystem_ = true ;
  
  //TODO reference to ecm.UIWorkspaceList
  public UIWorkspaceListForm() throws Exception {
    List<SelectItemOption<String>> wsList = new ArrayList<SelectItemOption<String>>() ;
    UIFormSelectBox uiWorkspaceList = new UIFormSelectBox(WORKSPACE_NAME, WORKSPACE_NAME, wsList) ;
    uiWorkspaceList.setOnChange("ChangeWorkspace") ;
    addUIFormInput(uiWorkspaceList) ;
    UIFormInputSetWithAction rootNodeInfo = new UIFormInputSetWithAction(ROOT_NODE_INFO) ;
    rootNodeInfo.addUIFormInput(new UIFormInputInfo(ROOT_NODE_PATH, ROOT_NODE_PATH, null)) ;
    String[] actionInfor = {"AddRootNode"} ;
    rootNodeInfo.setActionInfo(ROOT_NODE_PATH, actionInfor) ;
    rootNodeInfo.showActionInfo(true) ;
    rootNodeInfo.setRendered(false) ;
    addUIComponentInput(rootNodeInfo) ;
  }
  
  public void setIsShowSystem(boolean isShowSystem) { isShowSystem_ = isShowSystem ; }
  
  public void setShowRootPathSelect(boolean isRender) { 
    UIFormInputSetWithAction uiInputAction = getChildById(ROOT_NODE_INFO) ; 
    uiInputAction.setRendered(isRender) ; 
  }
  
  public void setWorkspaceList(String repository) throws Exception {
    wsList_ = new ArrayList<String>() ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    String[] wsNames = repositoryService.getRepository(repository).getWorkspaceNames();
    String systemWsName = 
      repositoryService.getRepository(repository).getConfiguration().getSystemWorkspaceName() ;
    List<SelectItemOption<String>> workspace = new ArrayList<SelectItemOption<String>>() ;
    for(String wsName : wsNames) {
      if(!isShowSystem_) {
        if(!wsName.equals(systemWsName)) {
          workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
          wsList_.add(wsName) ;
        }
      } else {
        workspace.add(new SelectItemOption<String>(wsName,  wsName)) ;
        wsList_.add(wsName) ;
      }
    }
    UIFormSelectBox uiWorkspaceList = getUIFormSelectBox(WORKSPACE_NAME) ;
    uiWorkspaceList.setOptions(workspace) ;
    UINodesExplorer uiBrowser = getParent() ;
    if(uiBrowser.getWorkspace() != null) {
      if(wsList_.contains(uiBrowser.getWorkspace())) {
        uiWorkspaceList.setValue(uiBrowser.getWorkspace()) ; 
      }
    }
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    if(wsList_.contains(wsName)) getUIFormSelectBox(WORKSPACE_NAME).setValue(wsName) ; 
    getUIFormSelectBox(WORKSPACE_NAME).setDisabled(isDisable) ;
  }
  
  static public class ChangeWorkspaceActionListener extends EventListener<UIWorkspaceListForm> {
    public void execute(Event<UIWorkspaceListForm> event) throws Exception {
      UIWorkspaceListForm uiWorkspaceList = event.getSource() ;
      UINodesExplorer uiJBrowser = uiWorkspaceList.getParent() ;
      String wsName = uiWorkspaceList.getUIFormSelectBox(WORKSPACE_NAME).getValue() ;
      uiJBrowser.setWorkspace(wsName) ;
      UINodeTreeBuilder uiTreeJCRExplorer = uiJBrowser.getChild(UINodeTreeBuilder.class) ;
      uiTreeJCRExplorer.setRootNode(null) ;
      uiTreeJCRExplorer.buildTree() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJBrowser) ;
    }
  }
  
  static public class AddRootNodeActionListener extends EventListener<UIWorkspaceListForm> {
    public void execute(Event<UIWorkspaceListForm> event) throws Exception {
      UIWorkspaceListForm uiWorkspaceList = event.getSource() ;
      UINodesExplorer uiJBrowser = uiWorkspaceList.getParent() ;
      String returnField = uiJBrowser.getReturnField() ;
      String workspace = uiJBrowser.getWorkspace() ;
      String repositoryName = uiJBrowser.getRepository() ;
      ManageableRepository repository = 
        uiJBrowser.getChild(UINodeTreeBuilder.class).getRepository(repositoryName) ;
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, repository) ;
      String value = session.getRootNode().getPath() ;
      if(!uiJBrowser.isDisable()) value = uiJBrowser.getWorkspace() + ":" + value ;
      ((UISelectable)uiJBrowser.getReturnComponent()).doSelect(returnField, value) ;
    }
  }
}
