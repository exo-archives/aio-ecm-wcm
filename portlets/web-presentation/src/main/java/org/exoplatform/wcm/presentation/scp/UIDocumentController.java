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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wcm.presentation.scp.UIPathChooser.ContentStorePath;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Nov 8, 2006 10:16:18 AM 
 */

@ComponentConfig (
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UIDocumentController.ViewStep3ActionListener.class)
    }
)
//TODO: Refer to ECM.UIDocumentFormController
public class UIDocumentController extends UIContainer {

  private String defaultDocument_ ;
  private static String DEFAULT_VALUE = "exo:article" ;
  private ContentStorePath storePath_ ;

  public UIDocumentController() throws Exception {
    addChild(UISelectDocumentForm.class, null, null) ;
    UIDocumentForm uiDocumentForm = createUIComponent(UIDocumentForm.class, null, null) ;
    uiDocumentForm.setTemplateNode(DEFAULT_VALUE) ;
    uiDocumentForm.addNew(true) ;
    addChild(uiDocumentForm) ;
  }
  
  public void setStorePath(ContentStorePath storePath) { storePath_ = storePath  ; }
  
  public void init() throws Exception {
    UIDocumentForm uiDocumentForm = getChild(UIDocumentForm.class) ;
    uiDocumentForm.setContentStorePath(storePath_) ;
    uiDocumentForm.setTemplateNode(defaultDocument_) ;
    uiDocumentForm.resetProperties();
  }

  public void initPopup(UIComponent uiComp) throws Exception {
    removeChildById("PopupComponent") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PopupComponent") ;
    uiPopup.setUIComponent(uiComp) ;
    uiPopup.setWindowSize(640, 300) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public List<SelectItemOption<String>> getListFileType() throws Exception {
    String repo = storePath_.getRepository() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    List<String> nodeTypes = new ArrayList<String>() ;
    UISelectDocumentForm uiSelectForm = getChild(UISelectDocumentForm.class) ;
    UIFormSelectBox uiSelectBox = uiSelectForm.getUIFormSelectBox(UISelectDocumentForm.FIELD_SELECT) ;
    boolean hasDefaultDoc = false ;
    Node parentNode = getParentNode() ;
    NodeTypeManager ntManager = parentNode.getSession().getWorkspace().getNodeTypeManager() ; 
    NodeType currentNodeType = parentNode.getPrimaryNodeType() ; 
    NodeDefinition[] childDefs = currentNodeType.getChildNodeDefinitions() ;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    List templates = templateService.getDocumentTemplates(repo) ;
    try {
      for(int i = 0; i < templates.size(); i ++){
        String nodeTypeName = templates.get(i).toString() ;        
        String label = templateService.getTemplateLabel(nodeTypeName, repo) ;
        NodeType nodeType = ntManager.getNodeType(nodeTypeName) ;
        if(nodeType.isMixin()) {
          if(!nodeTypes.contains(nodeTypeName)) {
            options.add(new SelectItemOption<String>(label, nodeTypeName));
            nodeTypes.add(nodeTypeName) ;
            continue;
          }
        }
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
            if(!hasDefaultDoc && nodeTypeName.equals(DEFAULT_VALUE)) {
              defaultDocument_ = DEFAULT_VALUE ;
              hasDefaultDoc = true ;
            }            
            if(!nodeTypes.contains(nodeTypeName)) {
              options.add(new SelectItemOption<String>(label, nodeTypeName));
              nodeTypes.add(nodeTypeName) ;
            }
            isCanCreateDocument = true ;          
          }
        }      
        if(!isCanCreateDocument){
          for(NodeType superType:superTypes) {
            for(NodeDefinition childDef : childDefs){          
              for(NodeType requiredType : childDef.getRequiredPrimaryTypes()) {              
                if (superType.getName().equals(requiredType.getName())) {
                  if(!hasDefaultDoc && nodeTypeName.equals(DEFAULT_VALUE)) {
                    defaultDocument_ = DEFAULT_VALUE ;
                    hasDefaultDoc = true ;
                  }                  
                  if(!nodeTypes.contains(nodeTypeName)) {
                    options.add(new SelectItemOption<String>(label, nodeTypeName));
                    nodeTypes.add(nodeTypeName) ;
                  }
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
      uiSelectBox.setOptions(options) ;
      if(hasDefaultDoc) {
        uiSelectBox.setValue(defaultDocument_);
      } else if(options.size() > 0) {
        defaultDocument_ = options.get(0).getValue() ;
        uiSelectBox.setValue(defaultDocument_);
      } 
    } catch(Exception e) {}
    
    return options ;
  }
  
  private Node getParentNode() throws Exception {
    //TODO: need to review
    //String currentUser = Util.getPortalRequestContext().getRemoteUser();
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider() ; 
//    if(currentUser == null) {
//      sessionProvider = SessionProviderFactory.createSystemProvider() ;
//    }else {
//      sessionProvider = SessionProviderFactory.createSessionProvider();
//    }    
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(storePath_.getRepository());
    Session session = sessionProvider.getSession(storePath_.getWorkspace(), manageableRepository) ;
    return (Node) session.getItem(storePath_.getPath()) ;     
  }
  
  public String event(String name) throws Exception {
    UIDocumentForm uiForm = getChild(UIDocumentForm.class) ;
    return uiForm.event(name) ;
  }
  
  public static class ViewStep3ActionListener extends EventListener<UIDocumentController> {

    public void execute(Event<UIDocumentController> event) throws Exception {
      System.out.println("\n\n\n\n\n\nStep3");
      UIDocumentController uiController = event.getSource() ;
      UIDocumentForm uiForm = uiController.getChild(UIDocumentForm.class) ;
      uiForm.save(event) ;
      UIWizard uiWizard = uiController.getParent() ;
      UIPathChooser uiPathChooser = uiWizard.getChild(UIPathChooser.class) ;
      ContentStorePath storePath = uiPathChooser.getStorePath() ;
      CategoriesService categoriesService = uiWizard.getApplicationComponent(CategoriesService.class) ;
      ManageableRepository manaRepository = uiWizard.getApplicationComponent(RepositoryService.class).getRepository(storePath.getRepository()) ;
      NodeHierarchyCreator nodeHierarchyCreator = uiWizard.getApplicationComponent(NodeHierarchyCreator.class) ;
      UICategoryManager uiManager = uiWizard.getChild(UICategoryManager.class) ;
      UICategoriesAddedList uiCateAddedList = uiManager.getChild(UICategoriesAddedList.class) ;
      uiCateAddedList.updateGrid(categoriesService.getCategories(uiForm.getSavedNode(), storePath.getRepository())) ;
      UINodesExplorer uiJCRBrowser = uiManager.getChild(UINodesExplorer.class) ;
      uiJCRBrowser.setSessionProvider(SessionProviderFactory.createSystemProvider()) ;
      uiJCRBrowser.setFilterType(null) ;
      uiJCRBrowser.setRepository(storePath.getRepository()) ;
      uiJCRBrowser.setIsDisable(manaRepository.getConfiguration().getSystemWorkspaceName(), true) ;
      uiJCRBrowser.setRootPath(nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH)) ;
      uiJCRBrowser.setIsTab(true) ;
      uiJCRBrowser.setComponent(uiCateAddedList, null) ;
      uiWizard.viewStep(3) ;
    }
    
  }


}
