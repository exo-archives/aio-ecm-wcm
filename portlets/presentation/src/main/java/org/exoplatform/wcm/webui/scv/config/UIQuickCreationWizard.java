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

import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.wcm.webui.scv.config.social.UISocialInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */
@ComponentConfig(
    template = "app:/groovy/SingleContentViewer/config/UIWizard.gtmpl",
    events = {
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIQuickCreationWizard.BackActionListener.class)
    }
)
public class UIQuickCreationWizard extends UIBaseWizard {

  /**
   * Instantiates a new uI quick creation wizard.
   * 
   * @throws Exception the exception
   */
  public UIQuickCreationWizard() throws Exception {
    addChild(UINameWebContentForm.class,null,null).setRendered(true);
    addChild(UIContentDialogForm.class,null,null).setRendered(false);
    addChild(UISocialInfo.class, null, null).setRendered(false);
    setNumberSteps(3);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.webui.scv.config.UIBaseWizard#getActionsByStep()
   */
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
      actions = new String[] {"Back"};
      break;
    default:
      break;
    }
    return actions;
  }

  /**
   * The listener interface for receiving viewStep1Action events.
   * The class that is interested in processing a viewStep1Action
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addViewStep1ActionListener<code> method. When
   * the viewStep1Action event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ViewStep1ActionEvent
   */
  public static class ViewStep1ActionListener extends EventListener<UIQuickCreationWizard> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIQuickCreationWizard> event) throws Exception {
      UIQuickCreationWizard uiQuickWizard = event.getSource();
      uiQuickWizard.viewStep(1);
    }
  }

  /**
   * The listener interface for receiving viewStep2Action events.
   * The class that is interested in processing a viewStep2Action
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addViewStep2ActionListener<code> method. When
   * the viewStep2Action event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ViewStep2ActionEvent
   */
  public static class ViewStep2ActionListener extends EventListener<UIQuickCreationWizard> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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

  /**
   * The listener interface for receiving viewStep3Action events.
   * The class that is interested in processing a viewStep3Action
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addViewStep3ActionListener<code> method. When
   * the viewStep3Action event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ViewStep3ActionEvent
   */
  public static class ViewStep3ActionListener extends EventListener<UIQuickCreationWizard> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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
}
