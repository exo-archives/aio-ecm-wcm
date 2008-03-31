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

import javax.portlet.PortletPreferences;

import org.exoplatform.wcm.presentation.scp.UIPathChooser.ContentStorePath;
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
        @EventConfig(listeners = UIContentCreationWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.ViewStep4ActionListener.class),
        @EventConfig(listeners = UIContentCreationWizard.ViewStep5ActionListener.class),
        @EventConfig(listeners = UIContentWizard.AbortActionListener.class)
    }
)

public class UIContentCreationWizard extends UIContentWizard {

  public UIContentCreationWizard() throws Exception {
    addChild(UIPathChooser.class, null, null) ;
    addChild(UIDocumentController.class, null, null).setRendered(false) ;
    addChild(UICategoryManager.class, null, null).setRendered(false) ;
    addChild(UIContentOptionForm.class, null, null).setRendered(false) ;
    setNumberSteps(4) ;
  }
  
  public static class ViewStep1ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      System.out.println("\n\n\n\n\n\nStep1");
      UIContentCreationWizard uiWizard = event.getSource() ;
      uiWizard.viewStep(1) ;
    }
    
  }
  
  public static class ViewStep2ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      System.out.println("\n\n\n\n\n\nStep2");
      UIContentCreationWizard uiWizard = event.getSource() ;
      UIPathChooser uiChooser = uiWizard.getChild(UIPathChooser.class) ;
      UIDocumentController uiDocumentController = uiWizard.getChild(UIDocumentController.class) ;
      uiChooser.invokeSetBindingBean() ;
      ContentStorePath storePath = uiChooser.getStorePath() ;
      uiDocumentController.setStorePath(storePath) ;
      if(uiDocumentController.getListFileType().size() == 0) {
        UIApplication uiApp = uiWizard.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.empty-file-type", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiDocumentController.init() ;
      uiWizard.viewStep(2) ;
    }
    
  }
  
  public static class ViewStep4ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      System.out.println("\n\n\n\n\n\nStep4");
      UIContentCreationWizard uiWizard = event.getSource() ;
      uiWizard.viewStep(4) ;
    }
    
  }
  
  public static class ViewStep5ActionListener extends EventListener<UIContentCreationWizard> {

    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      System.out.println("\n\n\n\n\n\nsave");
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
