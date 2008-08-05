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
package org.exoplatform.wcm.presentation.acp.config.quickcreation;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.UIBaseWizard;
import org.exoplatform.wcm.presentation.acp.config.UIContentDialogForm;
import org.exoplatform.wcm.presentation.acp.config.UIMiscellaneousInfo;
import org.exoplatform.wcm.presentation.acp.config.UIPortletConfig;
import org.exoplatform.wcm.presentation.acp.config.UISocialInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jun 9, 2008  
 */

@ComponentConfigs({
  @ComponentConfig(
      template = "app:/groovy/presentation/webui/component/UIWizard.gtmpl",
      events = {
          @EventConfig(listeners = UIQuickCreationWizard.ViewStep1ActionListener.class),  
          @EventConfig(listeners = UIQuickCreationWizard.ViewStep2ActionListener.class),
          @EventConfig(listeners = UIQuickCreationWizard.ViewStep3ActionListener.class),
          @EventConfig(listeners = UIQuickCreationWizard.ViewStep4ActionListener.class),
          @EventConfig(listeners = UIQuickCreationWizard.AbortActionListener.class),
          @EventConfig(listeners = UIQuickCreationWizard.BackActionListener.class),
          @EventConfig(listeners = UIQuickCreationWizard.FinishActionListener.class)
      }
  ),
  @ComponentConfig(
      id = "ViewStep1",
      type = UIContainer.class,
      template = "system:/groovy/portal/webui/page/UIWizardPageWelcome.gtmpl"
  )
})

public class UIQuickCreationWizard extends UIBaseWizard {

  public UIQuickCreationWizard() throws Exception {
    addChild(UIContainer.class, "ViewStep1", null);
    addChild(UIContentDialogForm.class, null, null).setRendered(false);    
    addChild(UISocialInfo.class, null, null).setRendered(false);
    addChild(UIMiscellaneousInfo.class, null, null).setRendered(false);
    setNumberSteps(4);
  }

  @Override
  public String[] getActionsByStep() {

    final int STEP1 = 1;
    final int STEP2 = 2;
    final int STEP3 = 3;
    final int STEP4 = 4;

    String[] actions = new String[] {};
    switch(getCurrentStep()) {
    case STEP1:
      actions = new String[] {"ViewStep2", "Abort"};
      break;
    case STEP2:
      actions = new String[] {"Abort"};
      break;
    case STEP3:
      actions = new String[] {"Back", "ViewStep4", "Finish"};
      break;
    case STEP4:
      actions = new String[] {"Back", "Finish"};
      break;
    default:
      break;
    }
    return actions;
  }

  public static class ViewStep1ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(1);
    }
  }

  public static class ViewStep2ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      UIPortletConfig uiPortletConfig = uiQuickWizard.getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isNewConfig()) {
        String portalName = Util.getUIPortal().getName();
        LivePortalManagerService portalManagerService = uiQuickWizard.getApplicationComponent(LivePortalManagerService.class);
        SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
        Node portalNode = portalManagerService.getLivePortal(portalName, sessionProvider);
        WebSchemaConfigService configService = uiQuickWizard.getApplicationComponent(WebSchemaConfigService.class);
        PortalFolderSchemaHandler handler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
        Node webContentStorage = handler.getWebContentStorage(portalNode);
        NodeLocation storedLocation = NodeLocation.make(webContentStorage);
        UIContentDialogForm uiCDForm = uiQuickWizard.getChild(UIContentDialogForm.class);
        uiCDForm.setStoredLocation(storedLocation);
        uiCDForm.setContentType(uiQuickWizard.EXO_WEB_CONTENT);
        uiCDForm.addNew(true);
        uiCDForm.resetProperties();
        uiQuickWizard.viewStep(2);
      } else {
        PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
        PortletPreferences prefs = context.getRequest().getPreferences();
        String repositoryName = prefs.getValue(UIAdvancedPresentationPortlet.REPOSITORY, null);
        String workspace = prefs.getValue(UIAdvancedPresentationPortlet.WORKSPACE, null);
        String UUID = prefs.getValue(UIAdvancedPresentationPortlet.UUID, null);
        RepositoryService repositoryService = uiQuickWizard.getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
        Session session = SessionProvider.createSystemProvider().getSession(workspace, manageableRepository);
        Node currentNode = session.getNodeByUUID(UUID);
        NodeLocation nodeLocation = new NodeLocation();
        nodeLocation.setRepository(repositoryName);
        nodeLocation.setWorkspace(workspace);
        nodeLocation.setPath(currentNode.getPath());
        UIContentDialogForm uiCDForm = uiQuickWizard.getChild(UIContentDialogForm.class);
        TemplateService templateService = uiQuickWizard.getApplicationComponent(TemplateService.class);
        List documentNodeType = templateService.getDocumentTemplates(repositoryName);
        String nodeType = currentNode.getPrimaryNodeType().getName();
        if(documentNodeType.contains(nodeType)) {
          uiCDForm.setStoredLocation(nodeLocation);
          uiCDForm.setNodePath(currentNode.getPath());
          uiCDForm.setContentType(uiQuickWizard.EXO_WEB_CONTENT);
          uiCDForm.addNew(false);
          uiCDForm.resetProperties();
          uiQuickWizard.viewStep(2);
        } else {
          UIApplication uiApp = uiQuickWizard.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.not-support", null)) ;
          context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }        
      }
    }
  }

  public static class ViewStep3ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(3);
    }
  }

  public static class ViewStep4ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(4);
    }
  }

  public static class FinishActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      UIContentDialogForm uiContentDialogForm = uiQuickWizard.getChild(UIContentDialogForm.class) ;
      NodeIdentifier identifier = uiContentDialogForm.getSavedNodeIdentifier() ;
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext() ;
      PortletPreferences prefs = context.getRequest().getPreferences() ;
      prefs.setValue(UIAdvancedPresentationPortlet.REPOSITORY, identifier.getRepository()) ;
      prefs.setValue(UIAdvancedPresentationPortlet.WORKSPACE, identifier.getWorkspace()) ;
      prefs.setValue(UIAdvancedPresentationPortlet.UUID, identifier.getUUID()) ;
      prefs.store() ;      
      context.setApplicationMode(PortletMode.VIEW) ;
    }
  }
}
