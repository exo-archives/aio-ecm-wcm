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

import java.util.Map;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.dms.webui.component.UICategoriesAddedList;
import org.exoplatform.dms.webui.component.UICategoryManager;
import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.dms.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 24, 2008  
 */

@ComponentConfig(
    template = "app:/groovy/presentation/webui/component/UIWizard.gtmpl",
    events = {
        @EventConfig(listeners = UIContentCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.ViewStep4ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.CompleteActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.FinishActionListener.class),
        @EventConfig(listeners = UIContentWizard.BackActionListener.class),
        @EventConfig(listeners = UIContentWizard.AbortActionListener.class)
    }
)

public class UIContentCreationWizard extends UIContentWizard {

  public UIContentCreationWizard() throws Exception {
    addChild(UIPathChooser.class, null, null) ;
    addChild(UIDocumentForm.class, null, null).setRendered(false) ;
    addChild(UICategoryManager.class, null, null).setRendered(false) ;
    addChild(UIContentOptionForm.class, null, null).setRendered(false) ;
    setNumberSteps(4) ;
  }
  
  public String [] getActionsByStep() {
    String [] actions = new String [] {} ;
    switch (getCurrentStep()) {
    case 1 :
      actions = new String [] {"ViewStep2", "Abort"} ;
      break ;
    case 2 :
      actions = new String [] {"Back", "ViewStep3", "Complete"} ;
      break ;
    case 3 :
      actions = new String [] {"ViewStep4", "Finish"} ;
      break ;
    case 4 :
      actions = new String [] {"Back", "Finish"} ;
      break ;
    default :
      break ;
    }
    return actions ;
  }
  
  public void saveContent(Event event) throws Exception {
    UIDocumentForm uiForm = getChild(UIDocumentForm.class) ;
    uiForm.save(event) ;
    UIPathChooser uiPathChooser = getChild(UIPathChooser.class) ;
    ContentStorePath storePath = uiPathChooser.getContentStorePath() ;
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class) ;
    ManageableRepository manaRepository = getApplicationComponent(RepositoryService.class).getRepository(storePath.getRepository()) ;
    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class) ;
    UICategoryManager uiManager = getChild(UICategoryManager.class) ;
    UICategoriesAddedList uiCateAddedList = uiManager.getChild(UICategoriesAddedList.class) ;
    Node node = uiForm.getSavedNode() ; 
    ContentStorePath documentStorePath = new ContentStorePath() ;
    documentStorePath.setRepository(storePath.getRepository()) ;
    documentStorePath.setWorkspace(storePath.getWorkspace()) ;
    documentStorePath.setPath(node.getPath()) ;
    uiCateAddedList.setStorePath(documentStorePath) ;
    uiCateAddedList.updateGrid(categoriesService.getCategories(node, documentStorePath.getRepository())) ;
    UINodesExplorer uiJCRBrowser = uiManager.getChild(UINodesExplorer.class) ;
    uiJCRBrowser.setSessionProvider(SessionProviderFactory.createSystemProvider()) ;
    uiJCRBrowser.setFilterType(null) ;
    uiJCRBrowser.setRepository(documentStorePath.getRepository()) ;
    uiJCRBrowser.setIsDisable(manaRepository.getConfiguration().getSystemWorkspaceName(), true) ;
    uiJCRBrowser.setRootPath(nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH)) ;
    uiJCRBrowser.setIsTab(true) ;
    uiJCRBrowser.setComponent(uiCateAddedList, null) ;      
  }
      
  public static class ViewStep2ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource() ;
      UIPathChooser uiChooser = uiWizard.getChild(UIPathChooser.class) ;
      uiChooser.invokeSetBindingBean() ;
      ContentStorePath storePath = uiChooser.getContentStorePath() ;
      Map<String, String> templates = Utils.getListFileType(storePath) ;
      if(templates.size() == 0) {
        UIApplication uiApp = uiWizard.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.empty-file-type", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(!templates.containsKey("exo:htmlFile")) {
        UIApplication uiApp = uiWizard.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContentCreationWizard.msg.no-support-html", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      UIDocumentForm uiDocForm = uiWizard.getChild(UIDocumentForm.class) ;
      uiDocForm.setContentStorePath(storePath) ;
      uiDocForm.addNew(true) ;
      uiDocForm.setTemplateNode("exo:htmlFile") ;
      uiDocForm.resetProperties() ;
      uiWizard.viewStep(2) ;
    }
    
  }

  public static class ViewStep3ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource() ;
      uiWizard.saveContent(event) ;
      uiWizard.viewStep(3) ;
    }
  }

  public static class ViewStep4ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource() ;
      uiWizard.viewStep(4) ;
    }
    
  }
  
  public static class CompleteActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource() ;
      uiWizard.saveContent(event) ;
      uiWizard.createEvent("Finish", Phase.PROCESS, event.getRequestContext()).broadcast() ;
    }
    
  }
  
  public static class FinishActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource() ;
      UIDocumentForm uiDocumentForm = uiWizard.findFirstComponentOfType(UIDocumentForm.class) ;
      ContentStorePath storePath = uiDocumentForm.getContentStorePath() ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences prefs = context.getRequest().getPreferences() ;
      prefs.setValue(UISimplePresentationPortlet.REPOSITORY, storePath.getRepository()) ;
      prefs.setValue(UISimplePresentationPortlet.WORKSPACE, storePath.getWorkspace()) ;
      prefs.setValue(UISimplePresentationPortlet.UUID, uiDocumentForm.getSavedNode().getUUID()) ;
      prefs.store() ;
      uiWizard.createEvent("Abort", Phase.PROCESS, event.getRequestContext()).broadcast() ;
    }
    
  }
  
}
