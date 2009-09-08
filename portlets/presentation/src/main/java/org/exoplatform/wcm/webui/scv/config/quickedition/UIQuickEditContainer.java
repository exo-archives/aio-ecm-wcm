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
package org.exoplatform.wcm.webui.scv.config.quickedition;

import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.wcm.webui.scv.config.UIQuickCreationWizard;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Sep 16, 2008
 */
@ComponentConfig (
    lifecycle = Lifecycle.class,
    template = "app:/groovy/SingleContentViewer/config/UIQuickEditContainer.gtmpl",
    events = {
      @EventConfig(listeners = UIQuickEditContainer.BackToNormalWizardActionListener.class)
    }
)
public class UIQuickEditContainer extends UIContainer {
  
  /**
   * Instantiates a new uI quick edit container.
   * 
   * @throws Exception the exception
   */
  public UIQuickEditContainer() throws Exception {
    UIQuickEditWebContentForm quickEditWebContentForm = addChild(UIQuickEditWebContentForm.class, null, null);
    quickEditWebContentForm.init();
  }
  
  /**
   * The listener interface for receiving backToNormalWizardAction events.
   * The class that is interested in processing a backToNormalWizardAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBackToNormalWizardActionListener<code> method. When
   * the backToNormalWizardAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BackToNormalWizardActionEvent
   */
  public static class BackToNormalWizardActionListener extends EventListener<UIQuickEditContainer> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIQuickEditContainer> event) throws Exception {
      UIQuickEditContainer uiQuickEditContainer = event.getSource();
      UISingleContentViewerPortlet viewerPortlet = uiQuickEditContainer.getAncestorOfType(UISingleContentViewerPortlet.class);
      UIPopupContainer popupContainer = viewerPortlet.getChild(UIPopupContainer.class);
      UIPortletConfig uiPortletConfig = uiQuickEditContainer.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.getChildren().clear();
      
      UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
      uiQuickCreationWizard.viewStep(3);
      
      
      WebUIPropertiesConfigService propertiesConfigService = uiQuickEditContainer.getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_EDIT_PORTLET_MODE);
      popupContainer.activate(uiPortletConfig,popupProperties.getWidth(),popupProperties.getHeight());
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }
}
