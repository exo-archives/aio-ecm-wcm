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
package org.exoplatform.wcm.presentation.scp;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.exoplatform.dms.application.JCRResourceResolver;
import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.dms.webui.form.UIBaseDialogForm;
import org.exoplatform.dms.webui.utils.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.WcmService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : nqhungvn
 *          nguyenkequanghung@yahoo.com
 * July 3, 2006
 * 10:07:15 AM
 * Editor : Pham Tuan
 *        phamtuanchip@yahoo.de
 * Nov 08, 2006  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIDocumentForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIDocumentForm.ShowComponentActionListener.class, phase = Phase.DECODE)  
    }
)

public class UIDocumentForm extends UIBaseDialogForm implements UISelectable {

  private String documentType_ ;
  private boolean isAddNew_ = false ;
  private JCRResourceResolver  resourceResolver ;
  private ContentStorePath contentStorePath_ ;
  private String savedNodeUUID_ ;
  
  public UIDocumentForm() throws Exception {
    setActions(new String [] {}) ;
  }
  
  public void setContentStorePath(ContentStorePath path) {
    contentStorePath_ = path ;
    setRepositoryName(path.getRepository()) ;
    setWorkspace(path.getWorkspace()) ;
    setStoredPath(path.getPath()) ;
  }
  
  public ContentStorePath getContentStorePath() { return contentStorePath_ ; } 
    
  public void setTemplateNode(String type) { documentType_ = type ;}
  
  public boolean isAddNew() {return isAddNew_ ;}
  
  public void addNew(boolean b) {isAddNew_ = b ;}
    
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      resetScriptInterceptor() ;
      return templateService.getTemplatePathByUser(true, documentType_, userName, repositoryName_) ;
    } catch (Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
      Object[] arg = { documentType_ } ;
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, 
                                              ApplicationMessage.ERROR)) ;
      return null ;
    } 
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if(resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName_);
        String worksapce = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(repositoryName_, worksapce, "exo:templateFile");
      }
    }catch (Exception e) {
    }    
    return resourceResolver ;
  }
  
  public boolean isEditing() { return !isAddNew_ ; }
  
  @SuppressWarnings("unchecked")
  public Node storeValue(Event event) throws Exception {
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties()) ;
    Node newNode = null ;
    String nodeType ;
    Node homeNode ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class);
    if(isAddNew()) {
      homeNode = getParentNode() ;
      nodeType = documentType_ ;
    } else { 
      homeNode = getNode().getParent();
      nodeType = getNode().getPrimaryNodeType().getName() ;
    }       
    try {            
      PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance();      
      String instanceId =  portletRequestContext.getApplication().getApplicationId() + "/" + portletRequestContext.getWindowId();      
      WcmService wcmService = getApplicationComponent(WcmService.class) ;
      String addedPath = wcmService.storeNode(nodeType,homeNode,inputProperties,isAddNew(),repositoryName_,instanceId) ;      
      try {
        homeNode.save() ;
        newNode = (Node)homeNode.getSession().getItem(addedPath);
      } catch(Exception e) { return null ; }
    } catch (AccessControlException ace) {
      throw new AccessDeniedException(ace.getMessage());
    } catch(VersionException ve) {
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(ItemNotFoundException item) {
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, 
                                              ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;      
    } catch(RepositoryException repo) {
      repo.printStackTrace() ;
      String key = "UIDocumentForm.msg.repository-exception" ;
      if(ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(NumberFormatException nume) {
      String key = "UIDocumentForm.msg.numberformat-exception" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    } catch(Exception e) {
      e.printStackTrace() ;
      String key = "UIDocumentForm.msg.cannot-save" ;
      uiApp.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return null;
    }
    savedNodeUUID_ = newNode.getUUID() ;
    return newNode ;
  }
  
  public Node getSavedNode() throws Exception {
    return getSesssion().getNodeByUUID(savedNodeUUID_);
  }
  
  @SuppressWarnings("unchecked")
  static public class ShowComponentActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      //TODO: Need to review
      System.out.println("\n\n\n\nNot Implemented yet");
//      UIDocumentForm uiForm = event.getSource() ;
//      UIDocumentController uiContainer = uiForm.getParent() ;
//      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID) ;
//      Map fieldPropertiesMap = uiForm.components.get(fieldName) ;
//      String classPath = (String)fieldPropertiesMap.get("selectorClass") ;
//      ClassLoader cl = Thread.currentThread().getContextClassLoader() ;
//      Class clazz = Class.forName(classPath, true, cl) ;
//      UIComponent uiComp = uiContainer.createUIComponent(clazz, null, null);
//      if(uiComp instanceof UIJCRBrowser) {
//        UIJCRExplorer explorer = uiForm.getAncestorOfType(UIJCRExplorer.class) ;
//        String repositoryName = explorer.getRepositoryName() ;
//        SessionProvider provider = explorer.getSessionProvider() ;                
//        ((UIJCRBrowser)uiComp).setRepository(repositoryName) ;
//        ((UIJCRBrowser)uiComp).setSessionProvider(provider) ;
//        String selectorParams = (String)fieldPropertiesMap.get("selectorParams") ;
//        if(selectorParams != null) {
//          String[] arrParams = selectorParams.split(",") ;
//          if(arrParams.length == 4) {
//            ((UIJCRBrowser)uiComp).setFilterType(new String[] {Utils.NT_FILE}) ;
//            ((UIJCRBrowser)uiComp).setIsDisable(arrParams[1], true) ;
//            ((UIJCRBrowser)uiComp).setRootPath(arrParams[2]) ;
//            ((UIJCRBrowser)uiComp).setMimeTypes(new String[] {arrParams[3]}) ;
//          }
//        }
//      }
//      uiContainer.initPopup(uiComp) ;
//      String param = "returnField=" + fieldName ;
//      ((ComponentSelector)uiComp).setComponent(uiForm, new String[]{param}) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }  

  static public class AddActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  static public class RemoveActionListener extends EventListener<UIDocumentForm> {
    public void execute(Event<UIDocumentForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent()) ;
    }
  }

  @Override
  public void onchange(Event event) throws Exception {
  }

  public void doSelect(String selectField, String value) throws Exception {    
    isUpdateSelect_ = true ;
    getUIStringInput(selectField).setValue(value) ;
    UIContainer uiContainer = getParent() ;
    uiContainer.removeChildById("PopupComponent") ;
  }
  
  public Node getCurrentNode() throws Exception { return getParentNode() ;}
  
  private Node getParentNode() throws Exception {
    return (Node) getSesssion().getItem(contentStorePath_.getPath()) ;     
  }
  
}