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

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Sep 15, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/groovy/SingleContentViewer/config/UIStartEditionInPageWizard.gtmpl",
    events= {
      @EventConfig(name = "Edit", listeners = UIStartEditionInPageWizard.EditPortletActionListener.class)
    }
)
public class UIStartEditionInPageWizard extends UIContainer {
  
  public static class EditPortletActionListener extends EventListener<UIStartEditionInPageWizard> {
    public void execute(Event<UIStartEditionInPageWizard> event) throws Exception {
      UIStartEditionInPageWizard editMode = event.getSource();
      UISingleContentViewerPortlet uiportlet = editMode.getAncestorOfType(UISingleContentViewerPortlet.class);
      UIPopupContainer popupContainer = uiportlet.getChild(UIPopupContainer.class);
      UIPortletConfig portletConfig = editMode.getChild(UIPortletConfig.class);
      WebUIPropertiesConfigService propertiesConfigService = editMode.getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_EDIT_PORTLET_MODE);
      popupContainer.activate(portletConfig,popupProperties.getWidth(),popupProperties.getHeight());            
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.addUIComponentToUpdateByAjax(popupContainer);
    }
  }
}
