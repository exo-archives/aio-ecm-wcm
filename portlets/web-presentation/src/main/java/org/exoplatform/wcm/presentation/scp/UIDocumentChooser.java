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
package org.exoplatform.wcm.presentation.scp;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Session;

import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.dms.webui.form.UIFormInputSetWithAction;
import org.exoplatform.dms.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Apr 7, 2008  
 */

public class UIDocumentChooser extends UIForm implements UISelectable {

  static public short SELECT_DOCUMENT_MODE = 1 ;
  static public short SELECT_PATH_MODE = 2 ;
  static String FIELD_REPOSITORY = "repository" ;
  static String FIELD_WORKSPACE = "workspace" ;
  static String FIELD_DOCUMENT = "Document" ;
  static String FIELD_PATH = "Location" ;
  static String PATH = "path" ;
  private short mode_ ;
  protected ContentStorePath contentStorePath_ ;

  public UIDocumentChooser() throws Exception {
    this(SELECT_DOCUMENT_MODE) ;
  }
  
  public UIDocumentChooser(short mode) throws Exception {
    mode_ = mode ;
    activateMode(mode) ;
  }
  
  protected void activateMode(short mode) throws Exception {
    UIFormSelectBox uiRepoSelectInput = new UIFormSelectBox(FIELD_REPOSITORY, FIELD_REPOSITORY, getRepoOptions()) ;
    addUIFormInput(uiRepoSelectInput) ;
    addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, FIELD_WORKSPACE, getWorkspaceOption(uiRepoSelectInput.getValue())).setValue("collaboration")) ;
    if(SELECT_DOCUMENT_MODE == mode) {
      UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(FIELD_DOCUMENT) ;
      uiInputSet.addUIFormInput(new UIFormStringInput(PATH, PATH, null).setEditable(false)) ;
      uiInputSet.setActionInfo(PATH, new String [] {"Browse"}) ;
      addUIComponentInput(uiInputSet) ;          
    } else if (SELECT_PATH_MODE == mode) {
      UIFormInputSetWithAction uiPathSelection = new UIFormInputSetWithAction(FIELD_PATH) ;
      uiPathSelection.addUIFormInput(new UIFormStringInput(PATH, PATH, null).setEditable(false)) ;
      uiPathSelection.setActionInfo(PATH, new String [] {"Browse"}) ;
      addUIComponentInput(uiPathSelection) ;      
    }    
  }
  
  public void setValues(ContentStorePath storePath) throws Exception {
    contentStorePath_ = storePath ;
    if(contentStorePath_ == null) return ;
    invokeGetBindingBean(contentStorePath_) ;
  }
  
  public ContentStorePath getContentStorePath() { return contentStorePath_ ; }
  
  public short getMode() { return mode_ ; }

  public void invokeSetBindingBean(Object bean) throws Exception {
    ContentStorePath path = (ContentStorePath) bean ;
    path.setRepository(getUIFormSelectBox(FIELD_REPOSITORY).getValue()) ;
    path.setWorkspace(getUIFormSelectBox(FIELD_WORKSPACE).getValue()) ;
    path.setPath(getUIStringInput(PATH).getValue()) ;
  }
  public void doSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value) ;
    SetPopupComponent(null) ;
  }

  protected List<SelectItemOption<String>> getRepoOptions() {
    RepositoryService service = getApplicationComponent(RepositoryService.class) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(RepositoryEntry repo : service.getConfig().getRepositoryConfigurations()) {
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
    }
    return options ;
  }

  protected List<SelectItemOption<String>> getWorkspaceOption(String repoName) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(3) ;
    Session session ;
    ManageableRepository repo = getApplicationComponent(RepositoryService.class).getRepository(repoName) ;
    for(String name : repo.getWorkspaceNames()) {
      session = SessionProviderFactory.createSystemProvider().getSession(name, repo) ;
      try {
        session.getRootNode() ;
        options.add(new SelectItemOption<String>(name, name)) ;
      } catch (AccessDeniedException acde) {
        continue ;
      }
    }
    return options ;
  }

  public void SetPopupComponent(UIComponent uiComponent) throws Exception {
    UIContainer uiParent = getParent() ;
    if(uiComponent == null) {
      uiParent.removeChild(UIPopupWindow.class) ;
      return ;
    }
    UIPopupWindow uiPopup = uiParent.getChild(UIPopupWindow.class) ;
    if( uiPopup == null)  uiPopup = uiParent.addChild(UIPopupWindow.class, null, null) ;
    uiPopup.setUIComponent(uiComponent) ;
    uiPopup.setWindowSize(610, 300);
    uiPopup.setShow(true) ;
  }
  
  public static class BrowseActionListener extends EventListener<UIDocumentChooser> {

    public void execute(Event<UIDocumentChooser> event) throws Exception {
     UIDocumentChooser uiChooser = event.getSource() ;
     String repo = uiChooser.getUIFormSelectBox(FIELD_REPOSITORY).getValue() ;
     String workspace = uiChooser.getUIFormSelectBox(FIELD_WORKSPACE).getValue() ;
     String defaultPath = "/Web Content/Live" ;
     if(!"collaboration".equals(workspace)) defaultPath = "/" ;
     UINodesExplorer uiExplorer = uiChooser.createUIComponent(UINodesExplorer.class, null, null) ;
     uiExplorer.setRepository(repo) ;
     uiExplorer.setIsDisable(workspace, true) ;
     uiExplorer.setRootPath(defaultPath) ;    
     if(event.getRequestContext().getRemoteUser() == null) {
       uiExplorer.setSessionProvider(SessionProviderFactory.createAnonimProvider()) ;
     }     
     if(SELECT_DOCUMENT_MODE == uiChooser.getMode()) {
       TemplateService templateService = uiChooser.getApplicationComponent(TemplateService.class) ;
       List<String> documents = templateService.getDocumentTemplates(repo) ;
       String [] filterType = new String[documents.size()];
       documents.toArray(filterType) ;
       uiExplorer.setFilterType(filterType) ;
       uiExplorer.setComponent(uiChooser, new String [] {UIDocumentChooser.PATH}) ;       
     } else if (SELECT_PATH_MODE == uiChooser.getMode()) {
       String [] filterType = new String[] {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED, "exo:taxonomy"};
       uiExplorer.setFilterType(filterType) ;
       uiExplorer.setComponent(uiChooser, new String [] {UIDocumentChooser.PATH}) ;
     }
     uiChooser.SetPopupComponent(uiExplorer) ;             
    }
    
  }

}
