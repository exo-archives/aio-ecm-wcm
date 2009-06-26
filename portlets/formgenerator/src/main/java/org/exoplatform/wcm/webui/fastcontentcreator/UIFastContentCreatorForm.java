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
package org.exoplatform.wcm.webui.fastcontentcreator;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.ComponentSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.upload.UploadService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIFastContentCreatorForm.SaveActionListener.class),
      @EventConfig(listeners = UIFastContentCreatorForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorForm.ShowComponentActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFastContentCreatorForm.RemoveReferenceActionListener.class, confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE)
    }
)

public class UIFastContentCreatorForm extends UIDialogForm implements UISelectable {

  final static public String FIELD_TAXONOMY = "categories";
  final static public String POPUP_TAXONOMY = "UIPopupTaxonomy";
  
  private List<String> listTaxonomy = new ArrayList<String>();
  private List<String> listTaxonomyName = new ArrayList<String>();
  
  private String documentType_ ;
  private JCRResourceResolver jcrTemplateResourceResolver_ ;

  public UIFastContentCreatorForm() throws Exception {
    setActions(new String[]{"Save"}) ;
  }
  
  public List<String> getListTaxonomy() {
    return listTaxonomy;
  }
  
  public List<String> getlistTaxonomyName() {
    return listTaxonomyName;
  }
  
  public void setListTaxonomy(List<String> listTaxonomyNew) {
    listTaxonomy = listTaxonomyNew;
  }
  
  public void setListTaxonomyName(List<String> listTaxonomyNameNew) {
    listTaxonomyName = listTaxonomyNameNew;
  }
  
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    String repository = UIFastContentCreatorUtils.getPreferenceRepository() ;
    try {      
      if(SessionProviderFactory.isAnonim()) {
        return templateService.getTemplatePathByAnonymous(true, documentType_, repository);
      }
      return templateService.getTemplatePathByUser(true, documentType_, userName, repository) ;
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { documentType_ } ;
      uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.not-support", arg, ApplicationMessage.ERROR)) ;
      return null ;
    }
  }
  
  public void doSelect(String selectField, Object value) throws Exception {
    this.isUpdateSelect = true;
    UIFormInput formInput = getUIInput(selectField);
    if(formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value.toString());
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput;
      UIFormInputBase input = inputSet.getChild(inputSet.getChildren().size()-1);      
      String valueTaxonomy = String.valueOf(value).trim();
      List taxonomylist = inputSet.getValue();
      if (!taxonomylist.contains(valueTaxonomy)) {
        listTaxonomy.add(valueTaxonomy);
        listTaxonomyName.add(valueTaxonomy);
        taxonomylist.add(valueTaxonomy);
      }
      inputSet.setValue(taxonomylist);
    }
    UIFastContentCreatorPortlet uiContainer = getParent();
    uiContainer.removeChildById("PopupComponent");    
  }

  public Node getCurrentNode() throws Exception {  
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    PortletPreferences preferences = UIFastContentCreatorUtils.getPortletPreferences() ;
    Session session = 
      SessionProviderFactory.createSystemProvider().getSession(
        preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, ""), 
        repositoryService.getRepository(preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, ""))) ;
    return (Node) session.getItem(preferences.getValue("path", ""));
  }

  public void setTemplateNode(String type) { documentType_ = type ; }

  public boolean isEditing() { return false ; }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    if(jcrTemplateResourceResolver_ == null) newJCRTemplateResourceResolver() ; 
    return jcrTemplateResourceResolver_; 
  }

  public void newJCRTemplateResourceResolver() {
    try {
      jcrTemplateResourceResolver_ = new JCRResourceResolver(repositoryName, getDMSWorkspace(), "exo:templateFile") ;
    } catch(Exception e) { }
  }
  
  private String getDMSWorkspace() {
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    return dmsConfiguration.getConfig(repositoryName).getSystemWorkspace();   
  }
  
  private Session getSession(String repository, String workspace) throws RepositoryException, RepositoryConfigurationException {
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class);      
    return SessionProviderFactory.createSessionProvider().getSession(workspace, repositoryService.getRepository(repository));
  }
  
  @SuppressWarnings("unchecked")
  static public class SaveActionListener extends EventListener<UIFastContentCreatorForm> {
    public void execute(Event<UIFastContentCreatorForm> event) throws Exception {
      UIFastContentCreatorForm fastContentCreatorForm = event.getSource() ;
      PortletPreferences preferences = UIFastContentCreatorUtils.getPortletPreferences();
      String preferenceRepository = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, "");
      String preferencePath = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_PATH, "") ;
      String preferenceType = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_TYPE, "") ;
      String preferenceWorkspace = preferences.getValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, "") ;
      Session session = fastContentCreatorForm.getSession(preferenceRepository, preferenceWorkspace);
      CmsService cmsService = fastContentCreatorForm.getApplicationComponent(CmsService.class) ;
      TaxonomyService taxonomyService = fastContentCreatorForm.getApplicationComponent(TaxonomyService.class);      
      UIApplication uiApp = fastContentCreatorForm.getAncestorOfType(UIApplication.class);
      boolean hasCategories = false;
      String categoriesPath = "";
      String[] categoriesPathList = null;
      List inputs = fastContentCreatorForm.getChildren();
      for (int i = 0; i < inputs.size(); i++) {
        UIFormInput input = (UIFormInput) inputs.get(i);
        if((input.getName() != null) && input.getName().equals("name")) {
          String[] arrFilterChar = {"&", "$", "@", ":", "]", "[", "*", "%", "!", "+", "(", ")", "'", "#", ";", "}", "{", "/", "|", "\""};          
          String valueName = input.getValue().toString().trim();          
          if (!org.exoplatform.ecm.webui.utils.Utils.isNameValid(valueName, arrFilterChar)) {
            uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.name-not-allowed", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      if(fastContentCreatorForm.isReference) {
        UIFormMultiValueInputSet uiSet = fastContentCreatorForm.getChild(UIFormMultiValueInputSet.class);
        if((uiSet != null) && (uiSet.getName() != null) && uiSet.getName().equals("categories")) {
          hasCategories = true;
          List<UIComponent> listChildren = uiSet.getChildren();         
          for (UIComponent component : listChildren) {
            UIFormStringInput uiStringInput = (UIFormStringInput)component;          
            if(uiStringInput.getValue() != null) {
              String value = uiStringInput.getValue().trim();            
              categoriesPath += value + ",";
            }
          }
          if (categoriesPath.endsWith(",")) categoriesPath = categoriesPath.substring(0, categoriesPath.length()-1).trim();
          categoriesPathList = categoriesPath.split(",");
          if ((categoriesPathList == null) || (categoriesPathList.length == 0)) {
            uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          
          for(String categoryPath : categoriesPathList) {              
            if((categoryPath != null) && (categoryPath.trim().length() > 0)){
              if (categoryPath.indexOf("/") == -1) {
                uiApp.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.non-categories", null, 
                    ApplicationMessage.WARNING));
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
                return;
              }
            }
          }
        }
      }      
      Map inputProperties = DialogFormUtil.prepareMap(fastContentCreatorForm.getChildren(), fastContentCreatorForm.getInputProperties()) ;
      Node homeNode = null;
      Node newNode = null ;
      try {
        homeNode = (Node) session.getItem(preferencePath);
      } catch (AccessDeniedException ade){
        Object[] args = { preferencePath } ;
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.access-denied", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(PathNotFoundException pnfe) {
        Object[] args = { preferencePath } ;
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.path-not-found", args, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      String lockToken = LockUtil.getLockToken(homeNode);
      if(lockToken != null) homeNode.getSession().addLockToken(lockToken);
      try {
        String addedPath = cmsService.storeNode(preferenceType, homeNode, inputProperties, true, preferenceRepository);
        homeNode.getSession().save() ;
        int index = 0;
        if(homeNode.hasNode(addedPath.substring(addedPath.lastIndexOf("/") + 1))) {
          newNode = homeNode.getNode(addedPath.substring(addedPath.lastIndexOf("/") + 1));
          if (hasCategories && (newNode != null) && ((categoriesPath != null) && (categoriesPath.length() > 0))){
            for(String categoryPath : categoriesPathList) {    
              index = categoryPath.indexOf("/");
              taxonomyService.addCategory(newNode, categoryPath.substring(0, index), categoryPath.substring(index + 1));
            }
          }
          event.getRequestContext().setAttribute("nodePath", newNode.getPath());
        }
        fastContentCreatorForm.reset() ;
        fastContentCreatorForm.setIsResetForm(true) ;
        for(UIComponent uiChild : fastContentCreatorForm.getChildren()) {
          if(uiChild instanceof UIFormMultiValueInputSet) {
            ((UIFormMultiValueInputSet)uiChild).setValue(new ArrayList<Value>()) ;
          } else if(uiChild instanceof UIFormUploadInput) {
            UploadService uploadService = fastContentCreatorForm.getApplicationComponent(UploadService.class) ;
            uploadService.removeUpload(((UIFormUploadInput)uiChild).getUploadId()) ;
          }
        }
        session.save() ;
        session.refresh(false) ;
        homeNode.getSession().refresh(false) ;
        Object[] args = { preferencePath } ;
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.saved-successfully", args)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorForm.getParent()) ;
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch(VersionException ve) {
        uiApp.addMessage(new ApplicationMessage("UIFastContentCreatortForm.msg.in-versioning", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(AccessDeniedException e) {
        Object[] args = { preferencePath } ;
        String key = "UIFastContentCreatortForm.msg.access-denied" ;
        uiApp.addMessage(new ApplicationMessage(key, args, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(LockException lock) {
        Object[] args = { preferencePath } ;
        String key = "UIFastContentCreatortForm.msg.node-locked" ;
        uiApp.addMessage(new ApplicationMessage(key, args, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch(ItemExistsException item) {
        Object[] args = { preferencePath } ;
        String key = "UIFastContentCreatortForm.msg.node-isExist" ;
        uiApp.addMessage(new ApplicationMessage(key, args, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } finally {
        if(session != null) {
          session.logout();
        }
      }
    }
  }  
    
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIFastContentCreatorForm> {
    public void execute(Event<UIFastContentCreatorForm> event) throws Exception {
      UIFastContentCreatorForm fastContentCreatorForm = event.getSource() ;
      UIFastContentCreatorPortlet fastContentCreatorPortlet = fastContentCreatorForm.getParent() ;
      fastContentCreatorForm.isShowingComponent = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Map fieldPropertiesMap = fastContentCreatorForm.componentSelectors.get(fieldName) ;
      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
      String rootPath = (String)fieldPropertiesMap.get("rootPath") ;
      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
      Class clazz = Class.forName(classPath, true, cl) ;
      UIComponent component = fastContentCreatorPortlet.createUIComponent(clazz, null, null);
      NodeHierarchyCreator nodeHierarchyCreator = fastContentCreatorForm.getApplicationComponent(NodeHierarchyCreator.class);  
      String value = fastContentCreatorForm.getUIStringInput(fieldName).getValue();
      String[] arrayTaxonomy = new String[1];
      if (value != null && !value.equals("")) {
        arrayTaxonomy = value.split(",");
        if (arrayTaxonomy.length > 0) {
          if (arrayTaxonomy[0].startsWith("[")) arrayTaxonomy[0] = arrayTaxonomy[0].substring(1, arrayTaxonomy[0].length());
          if (arrayTaxonomy[arrayTaxonomy.length - 1].endsWith("]")) {
            arrayTaxonomy[arrayTaxonomy.length - 1] = arrayTaxonomy[arrayTaxonomy.length - 1].substring(0, arrayTaxonomy[arrayTaxonomy.length - 1].length() - 1);  
          }
        }
      }
      if(component instanceof UIOneNodePathSelector) {
        String repositoryName = UIFastContentCreatorUtils.getPreferenceRepository() ;
        SessionProvider provider = SessionProviderFactory.createSystemProvider() ;                
        String wsFieldName = (String)fieldPropertiesMap.get("workspaceField") ;
        String wsName = "";
        if(wsFieldName != null && wsFieldName.length() > 0) {
          wsName = (String)fastContentCreatorForm.<UIFormInputBase>getUIInput(wsFieldName).getValue() ;
          ((UIOneNodePathSelector)component).setIsDisable(wsName, true) ;      
        }
        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
        if(selectorParams != null) {
          String[] arrParams = selectorParams.split(",") ;
          if(arrParams.length == 4) {
            ((UIOneNodePathSelector)component).setAcceptedNodeTypesInPathPanel(new String[] {"nt:file"}) ;
            wsName = arrParams[1];
            rootPath = arrParams[2];
            ((UIOneNodePathSelector)component).setIsDisable(wsName, true) ;
            if(arrParams[3].indexOf(";") > -1) {
              ((UIOneNodePathSelector)component).setAcceptedMimeTypes(arrParams[3].split(";")) ;
            } else {
              ((UIOneNodePathSelector)component).setAcceptedMimeTypes(new String[] {arrParams[3]}) ;
            }
          }
        }
        if(rootPath == null) rootPath = "/";
        ((UIOneNodePathSelector)component).setRootNodeLocation(repositoryName, wsName, rootPath) ;
        ((UIOneNodePathSelector)component).setShowRootPathSelect(true);
        ((UIOneNodePathSelector)component).init(provider);
      } else if (component instanceof UIOneTaxonomySelector) {
        String workspaceName = fastContentCreatorForm.getDMSWorkspace();
        ((UIOneTaxonomySelector)component).setIsDisable(workspaceName, false);
        String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);      
        Session session = fastContentCreatorForm.getSession(fastContentCreatorForm.repositoryName, workspaceName);
        Node rootTree = (Node) session.getItem(rootTreePath);      
        NodeIterator childrenIterator = rootTree.getNodes();
        while (childrenIterator.hasNext()) {
          Node childNode = childrenIterator.nextNode();
          rootTreePath = childNode.getPath();
          break;
        }
        
        ((UIOneTaxonomySelector)component).setRootNodeLocation(fastContentCreatorForm.repositoryName, workspaceName, rootTreePath);
        ((UIOneTaxonomySelector)component).init(SessionProviderFactory.createSystemProvider());
        
      }
      UIPopupContainer popupContainer = fastContentCreatorPortlet.getChild(UIPopupContainer.class);
      Utils.createPopupWindow(popupContainer, component, event.getRequestContext(), UIFastContentCreatorConstant.TAXONOMY_POPUP_WINDOW, 640, 300);
      String param = "returnField=" + fieldName ;
      ((ComponentSelector)component).setSourceComponent(fastContentCreatorForm, new String[]{param}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorPortlet) ;
    }
  }      
  
  static public class RemoveReferenceActionListener extends EventListener<UIFastContentCreatorForm> {
    public void execute(Event<UIFastContentCreatorForm> event) throws Exception {
      UIFastContentCreatorForm fastContentCreatorForm = event.getSource() ;
      fastContentCreatorForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      fastContentCreatorForm.getUIStringInput(fieldName).setValue(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorForm.getParent()) ;
    }
  }  

  static public class AddActionListener extends EventListener<UIFastContentCreatorForm> {
    public void execute(Event<UIFastContentCreatorForm> event) throws Exception {
      UIFastContentCreatorForm fastContentCreatorForm = event.getSource();
      UIFastContentCreatorPortlet fastContentCreatorPortlet = fastContentCreatorForm.getParent();
      String clickedField = event.getRequestContext().getRequestParameter(OBJECTID);
      if (fastContentCreatorForm.isReference) {
        UIFormMultiValueInputSet uiSet = fastContentCreatorForm.getChildById(FIELD_TAXONOMY);
        if((uiSet != null) && (uiSet.getName() != null) && uiSet.getName().equals(FIELD_TAXONOMY)) {
          if ((clickedField != null) && (clickedField.equals(FIELD_TAXONOMY))){
            NodeHierarchyCreator nodeHierarchyCreator = fastContentCreatorForm.getApplicationComponent(NodeHierarchyCreator.class);        
            if(uiSet.getValue().size() == 0) uiSet.setValue(new ArrayList<Value>());
            String workspaceName = fastContentCreatorForm.getDMSWorkspace();
            UIOneTaxonomySelector uiOneTaxonomySelector = fastContentCreatorForm.createUIComponent(UIOneTaxonomySelector.class, null, null);
            uiOneTaxonomySelector.setIsDisable(workspaceName, false);
            String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);      
            Session session = fastContentCreatorForm.getSession(fastContentCreatorForm.repositoryName, workspaceName);
            Node rootTree = (Node) session.getItem(rootTreePath);      
            NodeIterator childrenIterator = rootTree.getNodes();
            while (childrenIterator.hasNext()) {
              Node childNode = childrenIterator.nextNode();
              rootTreePath = childNode.getPath();
              break;
            }
            uiOneTaxonomySelector.setRootNodeLocation(fastContentCreatorForm.repositoryName, workspaceName, rootTreePath);
            uiOneTaxonomySelector.init(SessionProviderFactory.createSystemProvider());
            String param = "returnField=" + FIELD_TAXONOMY;        
            uiOneTaxonomySelector.setSourceComponent(fastContentCreatorForm, new String[]{param});
            UIPopupContainer popupContainer = fastContentCreatorPortlet.getChild(UIPopupContainer.class);
            UIPopupWindow popupWindow = popupContainer.getChildById(UIFastContentCreatorConstant.TAXONOMY_POPUP_WINDOW);
            if(popupWindow == null) {
              Utils.createPopupWindow(popupContainer, uiOneTaxonomySelector, event.getRequestContext(), UIFastContentCreatorConstant.TAXONOMY_POPUP_WINDOW, 640, 300);
            } else {
              popupWindow.setShow(true);
            }
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorPortlet);
      } else {        
        event.getRequestContext().addUIComponentToUpdateByAjax(fastContentCreatorForm.getParent());
      }
    }
  }

  static public class RemoveActionListener extends EventListener<UIFastContentCreatorForm> {
    public void execute(Event<UIFastContentCreatorForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }  
}
