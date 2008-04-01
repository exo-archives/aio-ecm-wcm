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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.wcm.presentation.scp.UIPathChooser.ContentStorePath;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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
 * Mar 31, 2008  
 */

@ComponentConfig(
    template = "app:/groovy/presentation/webui/component/UIWizard.gtmpl",
    events = {
        @EventConfig(listeners = UIContentEditWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIContentEditWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIContentEditWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIContentEditWizard.ViewStep4ActionListener.class),
        @EventConfig(listeners = UIContentWizard.AbortActionListener.class)
    }
)

public class UIContentEditWizard extends UIContentWizard {

  public UIContentEditWizard() throws Exception {
    UIDocumentForm uiDocumentForm = addChild(UIDocumentForm.class, null, null) ;
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences prefs = context.getRequest().getPreferences() ;
    String repoName = prefs.getValue(UISimplePresentationPortlet.REPOSITORY, null) ;
    String workspace = prefs.getValue(UISimplePresentationPortlet.WORKSPACE, null) ;
    String uuid = prefs.getValue(UISimplePresentationPortlet.UUID, null) ;
    RepositoryService repoService = getApplicationComponent(RepositoryService.class) ;
    ManageableRepository repo = repoService.getRepository(repoName) ;
    Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, repo) ;
    Node currentNode = session.getNodeByUUID(uuid);
    TemplateService tservice = getApplicationComponent(TemplateService.class) ;
    List documentNodeType = tservice.getDocumentTemplates(repoName) ;
    String nodeType = currentNode.getPrimaryNodeType().getName() ;
    if(documentNodeType.contains(nodeType)){
      ContentStorePath storePath = new ContentStorePath() ;
      storePath.setRepository(repoName) ;
      storePath.setWorkspace(workspace) ;
      storePath.setPath(currentNode.getPath()) ;
      uiDocumentForm.setContentStorePath(storePath) ;
      uiDocumentForm.setTemplateNode(nodeType) ;
      uiDocumentForm.setNodePath(currentNode.getPath()) ;
      uiDocumentForm.addNew(false) ;
    } else {
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-support", null)) ;
      context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    }
    addChild(UICategoryManager.class, null, null).setRendered(false) ;
    addChild(UIContentOptionForm.class, null, null).setRendered(false) ; 
    setNumberSteps(3) ;
  }
  
  public static class ViewStep1ActionListener extends EventListener<UIContentEditWizard> {

    public void execute(Event<UIContentEditWizard> event) throws Exception {
      UIContentEditWizard uiWizard = event.getSource() ;
      System.out.println("\n\n\n\n\n\nstep 1");
    }
    
  }

  public static class ViewStep2ActionListener extends EventListener<UIContentEditWizard> {

    public void execute(Event<UIContentEditWizard> event) throws Exception {
      UIContentEditWizard uiWizard = event.getSource() ;
      System.out.println("\n\n\n\n\n\nstep 2");
      UIDocumentForm uiDocumentForm = uiWizard.getChild(UIDocumentForm.class) ;
      uiDocumentForm.save(event) ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences prefs = context.getRequest().getPreferences() ;
      String repoName = prefs.getValue(UISimplePresentationPortlet.REPOSITORY, null) ;
      CategoriesService categoriesService = uiWizard.getApplicationComponent(CategoriesService.class) ;
      ManageableRepository manaRepository = uiWizard.getApplicationComponent(RepositoryService.class).getRepository(repoName) ;
      NodeHierarchyCreator nodeHierarchyCreator = uiWizard.getApplicationComponent(NodeHierarchyCreator.class) ;
      UICategoryManager uiManager = uiWizard.getChild(UICategoryManager.class) ;
      UICategoriesAddedList uiCateAddedList = uiManager.getChild(UICategoriesAddedList.class) ;
      uiCateAddedList.updateGrid(categoriesService.getCategories(uiDocumentForm.getSavedNode(), repoName)) ;
      UINodesExplorer uiJCRBrowser = uiManager.getChild(UINodesExplorer.class) ;
      uiJCRBrowser.setSessionProvider(SessionProviderFactory.createSystemProvider()) ;
      uiJCRBrowser.setFilterType(null) ;
      uiJCRBrowser.setRepository(repoName) ;
      uiJCRBrowser.setIsDisable(manaRepository.getConfiguration().getSystemWorkspaceName(), true) ;
      uiJCRBrowser.setRootPath(nodeHierarchyCreator.getJcrPath(BasePath.EXO_TAXONOMIES_PATH)) ;
      uiJCRBrowser.setIsTab(true) ;
      uiJCRBrowser.setComponent(uiCateAddedList, null) ;      
      uiWizard.viewStep(2) ;
    }
    
  }

  public static class ViewStep3ActionListener extends EventListener<UIContentEditWizard> {

    public void execute(Event<UIContentEditWizard> event) throws Exception {
      UIContentEditWizard uiWizard = event.getSource() ;
      uiWizard.viewStep(3) ;
      System.out.println("\n\n\n\n\n\nstep 3");
    }
    
  }


  public static class ViewStep4ActionListener extends EventListener<UIContentEditWizard> {

    public void execute(Event<UIContentEditWizard> event) throws Exception {
      UIContentEditWizard uiWizard = event.getSource() ;
      System.out.println("\n\n\n\n\n\nsave");
      uiWizard.createEvent("Abort", Phase.PROCESS, event.getRequestContext()).broadcast() ;
    }
    
  }

}
