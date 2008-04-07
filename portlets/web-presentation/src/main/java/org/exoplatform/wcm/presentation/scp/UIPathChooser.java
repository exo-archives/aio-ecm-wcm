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
import org.exoplatform.dms.webui.form.UISCPForm;
import org.exoplatform.dms.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 27, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPathChooser.ChangeOptionActionListener.class),
      @EventConfig(listeners = UIPathChooser.BrowsePathActionListener.class)
    }
)
//TODO: Tung.Pham says: This is the same as UIContentChooser, need to review
public class UIPathChooser extends UISCPForm implements UISelectable {

  private static String FIELD_REPOSITORY = "Repository" ;
  private static String FIELD_WORKSPACE = "Workspace" ;
  private static String PATH_SELECTION = "PathSelection" ;
  private static String FIELD_PATH = "Path" ;

  private boolean autoPath_ = true ;
  private ContentStorePath contentStorePath_ ;

  public UIPathChooser() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(2) ;
    options.add(new SelectItemOption<String>("Auto", "Auto", null)) ;
    options.add(new SelectItemOption<String>("Specify", "Specify", null)) ;
    UIFormSelectBox uiOptionInput = new UIFormSelectBox("PathOption", null, options).setValue("Auto") ;
    uiOptionInput.setOnChange("ChangeOption") ;
    addUIFormInput(uiOptionInput) ;
    setAutoPath(true) ;
    setActions(new String [] {}) ;
  }

  public void setAutoPath(boolean auto) throws Exception {
    if(autoPath_ == auto) return ;
    autoPath_ = auto ;
    if(autoPath_) {
      removeChildById(FIELD_REPOSITORY) ;
      removeChildById(FIELD_WORKSPACE) ;
      removeChildById(PATH_SELECTION) ;
    } else {
      UIFormSelectBox uiRepoSelectInput = new UIFormSelectBox(FIELD_REPOSITORY, null, getRepoOptions()) ;
      addUIFormInput(uiRepoSelectInput) ;
      addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, null, getWorkspaceOption(uiRepoSelectInput.getValue())).setValue("collaboration")) ;
      UIFormInputSetWithAction uiPathSelection = new UIFormInputSetWithAction(PATH_SELECTION) ;
      uiPathSelection.addUIFormInput(new UIFormStringInput(FIELD_PATH, null, null).setEditable(false)) ;
      uiPathSelection.setActionInfo(FIELD_PATH, new String [] {"BrowsePath"}) ;
      addUIComponentInput(uiPathSelection) ;      
    }
  }

  public boolean isAutoPath() { return autoPath_ ; }

  public ContentStorePath getStorePath() throws Exception { return contentStorePath_ ; }

  public void invokeSetBindingBean() throws Exception {
    contentStorePath_ =  new ContentStorePath() ;
    if(!autoPath_) {
      contentStorePath_.setRepository(getUIStringInput(FIELD_REPOSITORY).getValue()) ;
      contentStorePath_.setWorkspace(getUIStringInput(FIELD_WORKSPACE).getValue()) ;
      contentStorePath_.setPath(getUIStringInput(FIELD_PATH).getValue()) ;
    } else {
      contentStorePath_.setRepository("repository") ;
      contentStorePath_.setWorkspace("collaboration") ;
      String path = "/Web Content/Live/" + Util.getUIPortal().getName() + "/html" ;
      contentStorePath_.setPath(path) ;
    }
  }

  private List<SelectItemOption<String>> getRepoOptions() {
    RepositoryService service = getApplicationComponent(RepositoryService.class) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(RepositoryEntry repo : service.getConfig().getRepositoryConfigurations()) {
      options.add(new SelectItemOption<String>(repo.getName(), repo.getName())) ;
    }
    return options ;
  }

  private List<SelectItemOption<String>> getWorkspaceOption(String repoName) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(3) ;
    Session session ;
    ManageableRepository repo = getApplicationComponent(RepositoryService.class).getRepository(repoName) ;
    for(String name : repo.getWorkspaceNames()) {
      //TODO: use SessionProvider() or createSystemProvider() ?
      //session = SessionProviderFactory.createSessionProvider().getSession(name, repo) ;
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
    UIPortletConfig uiConfig = getAncestorOfType(UIPortletConfig.class) ;
    if(uiComponent == null) {
      uiConfig.removeChild(UIPopupWindow.class) ;
      return ;
    }
    UIPopupWindow uiPopup = uiConfig.getChild(UIPopupWindow.class) ;
    if( uiPopup == null)  uiPopup = uiConfig.addChild(UIPopupWindow.class, null, null) ;
    uiPopup.setUIComponent(uiComponent) ;
    uiPopup.setWindowSize(610, 300);
    uiPopup.setShow(true) ;
  }

  public void doSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value) ;
    SetPopupComponent(null) ;
  }

  public static class ChangeOptionActionListener extends EventListener<UIPathChooser> {

    public void execute(Event<UIPathChooser> event) throws Exception {
      UIPathChooser uiForm = event.getSource() ;
      String selectedOption = uiForm.getUIFormSelectBox("PathOption").getValue() ;
      if("Specify".equals(selectedOption)) uiForm.setAutoPath(false) ;
      else uiForm.setAutoPath(true) ;

    }

  }

  public static class BrowsePathActionListener extends EventListener<UIPathChooser> {

    public void execute(Event<UIPathChooser> event) throws Exception {
      UIPathChooser uiChooser = event.getSource() ;
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
      String [] filterType = new String[] {Utils.NT_FOLDER, Utils.NT_UNSTRUCTURED, "exo:taxonomy"};
      uiExplorer.setFilterType(filterType) ;
      uiExplorer.setComponent(uiChooser, new String [] {UIPathChooser.FIELD_PATH}) ;
      uiChooser.SetPopupComponent(uiExplorer) ;      
    }

  }
  
}
