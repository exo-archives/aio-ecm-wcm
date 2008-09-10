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

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.UIBaseWizard;
import org.exoplatform.wcm.presentation.acp.config.UIContentDialogForm;
import org.exoplatform.wcm.presentation.acp.config.UIMiscellaneousInfo;
import org.exoplatform.wcm.presentation.acp.config.UINameWebContentForm;
import org.exoplatform.wcm.presentation.acp.config.UIPermissionManager;
import org.exoplatform.wcm.presentation.acp.config.UISocialInfo;
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
    template = "app:/groovy/advancedPresentation/config/UIWizard.gtmpl",
    events = {
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep4ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep5ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.AbortActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.BackActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.FinishActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.CompleteActionListener.class)
    }
)

public class UIQuickCreationWizard extends UIBaseWizard {

  public UIQuickCreationWizard() throws Exception {
    addChild(UINameWebContentForm.class,null,null).setRendered(true);
    addChild(UIContentDialogForm.class,null,null).setRendered(false);
    addChild(UISocialInfo.class, null, null).setRendered(false);
    addChild(UIPermissionManager.class,null,null).setRendered(false);    
    addChild(UIMiscellaneousInfo.class, null, null).setRendered(false);
    setNumberSteps(5);
  }

  @Override
  public String[] getActionsByStep() {

    final int STEP1 = 1;
    final int STEP2 = 2;
    final int STEP3 = 3;
    final int STEP4 = 4;
    final int STEP5 = 5;

    String[] actions = new String[] {};
    switch(getCurrentStep()) {
    case STEP1:
      actions = new String[] {};
      break;
    case STEP2:
      actions = new String[] {};
      break;
    case STEP3:
      actions = new String[] {"ViewStep4", "Finish"};
      break;
    case STEP4:
      actions = new String[] {"Back","ViewStep5", "Finish"};
      break;
    case STEP5:
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

  public static class ViewStep4ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(4);
      UIContentDialogForm contentDialogForm = uiQuickWizard
      .getChild(UIContentDialogForm.class);
      NodeIdentifier nodeIdentifier = contentDialogForm.getSavedNodeIdentifier();
      UIApplication uiApplication = uiQuickWizard.getAncestorOfType(UIApplication.class);
      if(nodeIdentifier == null) {
        uiApplication.addMessage(new ApplicationMessage("UIQuickCreationWizard.msg.StepbyStep", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
    }
  }

  public static class ViewStep5ActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(5);
      UIContentDialogForm contentDialogForm = uiQuickWizard
      .getChild(UIContentDialogForm.class);
      NodeIdentifier nodeIdentifier = contentDialogForm.getSavedNodeIdentifier();
      UIApplication uiApplication = uiQuickWizard.getAncestorOfType(UIApplication.class);
      if(nodeIdentifier == null) {
        uiApplication.addMessage(new ApplicationMessage("UIQuickCreationWizard.msg.StepbyStep", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
    }
  }

  public static class FinishActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      UIContentDialogForm uiContentDialogForm = uiQuickWizard.getChild(UIContentDialogForm.class);
      NodeIdentifier identifier = uiContentDialogForm.getSavedNodeIdentifier();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue(UIAdvancedPresentationPortlet.REPOSITORY, identifier.getRepository());
      prefs.setValue(UIAdvancedPresentationPortlet.WORKSPACE, identifier.getWorkspace());
      prefs.setValue(UIAdvancedPresentationPortlet.UUID, identifier.getUUID());
      prefs.store();      
      context.setApplicationMode(PortletMode.VIEW);
    }
  }

  public static class CompleteActionListener extends EventListener<UIQuickCreationWizard> {
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickCreationWizard = event.getSource();
      UIContentDialogForm uiContentDialogForm = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
      NodeIdentifier identifier = uiContentDialogForm.getSavedNodeIdentifier();
      UIMiscellaneousInfo uiMiscellaneousInfo = uiQuickCreationWizard.getChild(UIMiscellaneousInfo.class);
      boolean isShowTOC = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTOC").isChecked();
      boolean isQuickEdit = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowQuickEdit").isChecked();
      boolean isShowTags = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTags").isChecked();
      boolean isShowCategories = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowCategories").isChecked();
      boolean isAllowVoting = uiMiscellaneousInfo.getUIFormCheckBoxInput("AllowVoting").isChecked();
      boolean isAllowComment = uiMiscellaneousInfo.getUIFormCheckBoxInput("AllowComment").isChecked();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue(UIAdvancedPresentationPortlet.REPOSITORY, identifier.getRepository());
      prefs.setValue(UIAdvancedPresentationPortlet.WORKSPACE, identifier.getWorkspace());
      prefs.setValue(UIAdvancedPresentationPortlet.UUID, identifier.getUUID());
      prefs.setValue("ShowTOC", Boolean.toString(isShowTOC));
      prefs.setValue("ShowQuickEdit", Boolean.toString(isQuickEdit));
      prefs.setValue("ShowTags", Boolean.toString(isShowTags));
      prefs.setValue("ShowCategories", Boolean.toString(isShowCategories));
      prefs.setValue("AllowVoting", Boolean.toString(isAllowVoting));
      prefs.setValue("AllowComment", Boolean.toString(isAllowComment));
      prefs.store();      
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
