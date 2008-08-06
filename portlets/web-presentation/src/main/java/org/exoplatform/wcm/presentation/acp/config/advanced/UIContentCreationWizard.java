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
package org.exoplatform.wcm.presentation.acp.config.advanced;

import javax.jcr.Node;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.wcm.presentation.acp.config.UIBaseWizard;
import org.exoplatform.wcm.presentation.acp.config.UIContentDialogForm;
import org.exoplatform.wcm.presentation.acp.config.UIMiscellaneousInfo;
import org.exoplatform.wcm.presentation.acp.config.UISocialInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 27, 2008  
 */

@ComponentConfigs( {
  @ComponentConfig(
      template = "app:/groovy/advancedPresentation/config/UIWizard.gtmpl",
      events = {
          @EventConfig(listeners = UIContentCreationWizard.ViewStep1ActionListener.class),  
          @EventConfig(listeners = UIContentCreationWizard.ViewStep2ActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.ViewStep3ActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.ViewStep4ActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.ViewStep5ActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.AbortActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.BackActionListener.class),
          @EventConfig(listeners = UIContentCreationWizard.FinishActionListener.class)
      }
  ),
  @ComponentConfig(
      id = "ViewStep1",
      type = UIContainer.class,
      template = "system:/groovy/portal/webui/page/UIWizardPageWelcome.gtmpl"
  )
} )

public class UIContentCreationWizard extends UIBaseWizard {

  private final int STEP1 = 1;
  private final int STEP2 = 2;
  private final int STEP3 = 3;
  private final int STEP4 = 4;
  private final int STEP5 = 5;

  public UIContentCreationWizard() throws Exception {
    addChild(UIContainer.class, "ViewStep1", null);
    addChild(UIContentDialogForm.class, null, null).setRendered(false);
    addChild(UIMainWebContentData.class, null, null).setRendered(false);
    addChild(UISocialInfo.class, null, null).setRendered(false);
    addChild(UIMiscellaneousInfo.class, null, null).setRendered(false);
    setNumberSteps(5);
  }

  public String[] getActionsByStep() {
    String [] actions = new String [] {};
    switch (getCurrentStep()) {
    case STEP1 :
      actions = new String [] {"ViewStep2", "Abort"};
      break;
    case STEP2 :
      actions = new String[] {"ViewStep3", "Abort"};
      break;
    case STEP3 :
      actions = new String[] {"Back", "ViewStep4", "Finish"};
      break;
    case STEP4 :
      actions = new String[] {"Back","ViewStep5", "Finish"};
      break;
    case STEP5 :
      actions = new String[] {"Back", "Finish"};
      break;
    default :
      break;
    }
    return actions;
  }

  public static class ViewStep1ActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource();
      uiWizard.viewStep(1);
    }
  }

  public static class ViewStep2ActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource();
      UIContentDialogForm uiContentDialogForm = uiWizard.getChild(UIContentDialogForm.class);
      String portalName = Util.getUIPortal().getName();
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      LivePortalManagerService portalManager = uiContentDialogForm.getApplicationComponent(LivePortalManagerService.class);
      Node portalNode = portalManager.getLivePortal(portalName, sessionProvider);
      WebSchemaConfigService webConfigService = uiContentDialogForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler handler = webConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node webContentStored = handler.getWebContentStorage(portalNode);
      NodeLocation storedLocation = NodeLocation.make(webContentStored);
      uiContentDialogForm.setStoredLocation(storedLocation);
      uiContentDialogForm.setContentType("exo:htmlFile");
      uiContentDialogForm.addNew(true);
      uiContentDialogForm.resetProperties();
      uiWizard.viewStep(2);
    }
  }

  public static class ViewStep3ActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource();
      uiWizard.viewStep(3);
    }
  }

  public static class ViewStep4ActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource();
      uiWizard.viewStep(4);
      UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
      WebuiRequestContext context = Util.getPortalRequestContext();
      if (uiWizard.getSelectedStep() < 4 ) {
        uiWizard.updateWizardComponent();
        uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep",null));
        context.addUIComponentToUpdateByAjax(uiPortalApp.getUIPopupMessages());
      }
    }
  }

  public static class ViewStep5ActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIContentCreationWizard uiWizard = event.getSource();
      uiWizard.viewStep(5);
      UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
      WebuiRequestContext context = Util.getPortalRequestContext();
      if (uiWizard.getSelectedStep() < 5 ) {
        uiWizard.updateWizardComponent();
        uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep",null));
        context.addUIComponentToUpdateByAjax(uiPortalApp.getUIPopupMessages());
      }
    }
  }

  public static class FinishActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIBaseWizard uiWizard = event.getSource();
      uiWizard.viewStep(uiWizard.getCurrentStep() -1);
    }
  }

  void updateWizardComponent() {
    UIPortalApplication uiPortalApp = getAncestorOfType(UIPortalApplication.class);
    PortalRequestContext pcontext = Util.getPortalRequestContext();

    UIWorkingWorkspace uiWorkingWS = uiPortalApp
    .getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);

    UIControlWorkspace uiControl = uiPortalApp
    .getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
    pcontext.addUIComponentToUpdateByAjax(uiControl);

    pcontext.setFullRender(true);
  }
}
