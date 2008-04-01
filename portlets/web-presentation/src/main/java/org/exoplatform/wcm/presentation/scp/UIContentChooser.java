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
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.dms.webui.form.UIFormInputSetWithAction;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
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
 * Mar 24, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIContentChooser.SaveActionListener.class),
      @EventConfig(listeners = UIContentChooser.BackActionListener.class),
      @EventConfig(listeners = UIContentChooser.BrowseDocumentActionListener.class)
    }
)
public class UIContentChooser extends UIForm implements UISelectable {

  private static String FIELD_REPOSITORY = "Repository" ;
  private static String FIELD_WORKSPACE = "Workspace" ;
  private static String DOCUMENT_SELECTION = "DocumentSelection" ;
  static String FIELD_DOCUMENT_PATH = "DocPath" ;

  public UIContentChooser() throws Exception {
    UIFormSelectBox uiRepoSelectInput = new UIFormSelectBox(FIELD_REPOSITORY, null, getRepoOptions()) ;
    addUIFormInput(uiRepoSelectInput) ;
    addUIFormInput(new UIFormSelectBox(FIELD_WORKSPACE, null, getWorkspaceOption(uiRepoSelectInput.getValue())).setValue("collaboration")) ;
    UIFormInputSetWithAction uiInputSet = new UIFormInputSetWithAction(DOCUMENT_SELECTION) ;
    uiInputSet.addUIFormInput(new UIFormStringInput(FIELD_DOCUMENT_PATH, null, null).setEditable(false)) ;
    uiInputSet.setActionInfo(FIELD_DOCUMENT_PATH, new String [] {"BrowseDocument"}) ;
    addUIComponentInput(uiInputSet) ;
    setActions(new String [] {"Save", "Back"}) ;
  }

  public void doSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value) ;
    SetPopupComponent(null) ;
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

  public static class SaveActionListener extends EventListener<UIContentChooser> {    
    public void execute(Event<UIContentChooser> event) throws Exception {
      UIContentChooser uiForm = event.getSource() ; 
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      String repository = uiForm.getUIFormSelectBox(FIELD_REPOSITORY).getValue();
      String workspace = uiForm.getUIFormSelectBox(FIELD_WORKSPACE).getValue() ;
      String docPath = uiForm.getUIStringInput(FIELD_DOCUMENT_PATH).getValue() ;
      ManageableRepository repo = uiForm.getApplicationComponent(RepositoryService.class).getRepository(repository) ;
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, repo) ;      
      Node node = ((Node) session.getItem(docPath)) ;
      if(!node.isNodeType("mix:referenceable")) {
        node.addMixin("mix:referenceable") ;
        session.save();
      }
      String nodeUUID = node.getUUID() ;
      PortletPreferences prefs = context.getRequest().getPreferences() ;            
      prefs.setValue(UISimplePresentationPortlet.REPOSITORY, repository) ;
      prefs.setValue(UISimplePresentationPortlet.WORKSPACE, workspace) ;
      prefs.setValue(UISimplePresentationPortlet.UUID, nodeUUID) ;
      prefs.store() ;      
      //TODO should use other way to set the application info      
      try{        
        PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance();      
        String instanceId =  portletRequestContext.getApplication().getApplicationId() + "/" + portletRequestContext.getWindowId();
        Value value = session.getValueFactory().createValue(instanceId) ;        
        if(!node.isNodeType("exo:applicationLinkable")) {
          node.addMixin("exo:applicationLinkable");
          node.setProperty("exo:linkedApplications",new Value[]{value}) ;
        }else {
          List<Value> list = new ArrayList<Value>() ;
          list.add(value) ;
          for(Value v: node.getProperty("exo:linkedApplications").getValues()) {
            if(value.getString().equalsIgnoreCase(v.getString())) continue ;
            list.add(v);
          }
          node.setProperty("exo:linkedApplications",list.toArray(new Value[list.size()])) ;         
        }
        session.save();
      }catch (Exception e) {
        e.printStackTrace();
      }      

    }

  }

  public static class BackActionListener extends EventListener<UIContentChooser> {

    public void execute(Event<UIContentChooser> event) throws Exception {
      UIContentChooser uiChooser = event.getSource() ;
      UIPortletConfig uiConfig = uiChooser.getAncestorOfType(UIPortletConfig.class) ;
      uiConfig.getChildren().clear() ;
      uiConfig.addChild(uiConfig.getBackComponent()) ;
    }

  }

  public static class BrowseDocumentActionListener extends EventListener<UIContentChooser> {

    public void execute(Event<UIContentChooser> event) throws Exception {
      UIContentChooser uiChooser = event.getSource() ;
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
      TemplateService templateService = uiChooser.getApplicationComponent(TemplateService.class) ;
      List<String> documents = templateService.getDocumentTemplates(repo) ;
      String [] filterType = new String[documents.size()];
      documents.toArray(filterType) ;
      uiExplorer.setFilterType(filterType) ;
      uiExplorer.setComponent(uiChooser, new String [] {UIContentChooser.FIELD_DOCUMENT_PATH}) ;
      uiChooser.SetPopupComponent(uiExplorer) ;
    }

  }

}
