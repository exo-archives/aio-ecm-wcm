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
package org.exoplatform.dms.webui.component;

import org.exoplatform.dms.webui.form.UIWorkspaceListForm;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 18, 2006
 * 2:12:26 PM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UINodesExplorer extends UIContainer implements ComponentSelector{
  
  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  private String repository_ = null;
  private String wsName_ = null ;
  private boolean isDisable_ = false ;  
  private SessionProvider sessionProvider_ ;
  
  //Reference to ecm.UIJCRBrowser
  public UINodesExplorer() throws Exception {
    addChild(UIWorkspaceListForm.class, null, UIWorkspaceListForm.class.getSimpleName()+hashCode()) ;
    addChild(UINodeTreeBuilder.class, null, UINodeTreeBuilder.class.getSimpleName()+hashCode()) ;
    addChild(UITreeNodeList.class, null, UITreeNodeList.class.getSimpleName()+hashCode()) ;        
  }
  
  public void setSessionProvider(SessionProvider provider) { this.sessionProvider_ = provider ; }
  public SessionProvider getSessionProvider() { return this.sessionProvider_ ; }
  
  public void setRootPath(String path) throws Exception{
    getChild(UINodeTreeBuilder.class).setRootPath(path) ;
  }
  
  public void setFilterType(String[] arrType) throws Exception {
    getChild(UIBaseNodeList.class).setFilterType(arrType) ;
  }
  
  public void setMimeTypes(String[] arrMimeType) {
    getChild(UIBaseNodeList.class).setMimeTypes(arrMimeType) ;
  }
  
  public void setIsShowSystem(boolean isShowSystem) {
    getChild(UIWorkspaceListForm.class).setIsShowSystem(isShowSystem) ;
  }
  
  public void setIsTab(boolean isTab) { 
    getChild(UINodeTreeBuilder.class).setIsTab(isTab) ;
  }
  
  public void setIsDisable(String wsName, boolean isDisable) {
    setWorkspace(wsName) ;
    isDisable_ = isDisable ;
    getChild(UIWorkspaceListForm.class).setIsDisable(wsName, isDisable) ;
  }
  
  public boolean isDisable() { return isDisable_ ; }
  
  public void setWorkspace(String wsName) { wsName_ = wsName ; }
  
  public String getWorkspace() throws Exception { 
    if(wsName_ == null || wsName_.trim().length() ==0) {
      return getApplicationComponent(RepositoryService.class).getRepository(repository_).getConfiguration().getDefaultWorkspaceName() ;
    }
    return wsName_ ; 
  }
  
  public void setRepository(String repo) {
    repository_ = repo ; 
    try {
      UIWorkspaceListForm uiWorkspaceList = getChild(UIWorkspaceListForm.class) ;
      uiWorkspaceList.setWorkspaceList(repository_) ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public String getRepository() { return repository_ ; }
  
  public void setShowRootPathSelect(boolean isRendered) {
    UIWorkspaceListForm uiWorkspaceList = getChild(UIWorkspaceListForm.class) ;
    uiWorkspaceList.setShowRootPathSelect(isRendered) ;
  }
  
  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }
  
  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }
  
  public void setAllowedNodes(String [] strs) {
    getChild(UINodeTreeBuilder.class).setAllowedNodes(strs) ;
  }
}
