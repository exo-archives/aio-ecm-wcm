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
package org.exoplatform.wcm.webui.scv.config;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.social.UISocialInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jun 9, 2008  
 */

@ComponentConfig(
    template = "app:/groovy/SingleContentViewer/config/UIWizard.gtmpl",
    events = {
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.BackActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.CompleteActionListener.class)
    }
)

public class UIQuickCreationWizard extends UIBaseWizard {

  public UIQuickCreationWizard() throws Exception {
    addChild(UIWebConentNameTabForm.class,null,null).setRendered(true);
    addChild(UIContentDialogForm.class,null,null).setRendered(false);
    addChild(UISocialInfo.class, null, null).setRendered(false);
    setNumberSteps(3);
  }

  @Override
  public String[] getActionsByStep() {

    final int STEP1 = 1;
    final int STEP2 = 2;
    final int STEP3 = 3;

    String[] actions = new String[] {};
    switch(getCurrentStep()) {
    case STEP1:
      actions = new String[] {};
      break;
    case STEP2:
      actions = new String[] {};
      break;
    case STEP3:
      actions = new String[] {"Back", "Complete"};
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
      UIContentDialogForm contentDialogForm = uiQuickWizard
      .getChild(UIContentDialogForm.class);
      NodeIdentifier nodeIdentifier = contentDialogForm.getSavedNodeIdentifier();
      uiQuickWizard.viewStep(2);
      UIApplication uiApplication = uiQuickWizard.getAncestorOfType(UIApplication.class);
      if(nodeIdentifier == null ) {
        uiApplication.addMessage(new ApplicationMessage("UIQuickCreationWizard.msg.StepbyStep", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
    }
  }

  public static class ViewStep3ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(3);
      UIContentDialogForm contentDialogForm = uiQuickWizard
      .getChild(UIContentDialogForm.class);
      NodeIdentifier nodeIdentifier = contentDialogForm.getSavedNodeIdentifier();
      UIApplication uiApplication = uiQuickWizard.getAncestorOfType(UIApplication.class);
      if(nodeIdentifier == null ) {
        uiApplication.addMessage(new ApplicationMessage("UIQuickCreationWizard.msg.StepbyStep", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
    }
  }

  public static class CompleteActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickCreationWizard = event.getSource();
      UISocialInfo uiSocialInfo = uiQuickCreationWizard.getChild(UISocialInfo.class);
      UIMiscellaneousInfo uiMiscellaneousInfo = uiSocialInfo.getChild(UIMiscellaneousInfo.class);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      UIContentDialogForm uiContentDialogForm = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
      NodeIdentifier identifier = uiContentDialogForm.getSavedNodeIdentifier();
      prefs.setValue(UISingleContentViewerPortlet.REPOSITORY, identifier.getRepository());
      prefs.setValue(UISingleContentViewerPortlet.WORKSPACE, identifier.getWorkspace());
      prefs.setValue(UISingleContentViewerPortlet.IDENTIFIER, identifier.getUUID());
      prefs.store();      
      UIPortletConfig uiPortletConfig = uiQuickCreationWizard.getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isEditPortletInCreatePageWizard()) {
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
      } else {        
        context.setApplicationMode(PortletMode.VIEW);
        Utils.refreshBrowser(context);
      }
    }
  }
}
