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
package org.exoplatform.wcm.webui.clv.config;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.wcm.webui.clv.UIContentListViewerPortlet;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Do Ngoc Anh anh.do@exoplatform.com
 * anhdn86@gmail.com Dec 15, 2008
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  template = "app:/groovy/ContentListViewer/config/UIStartEditionInPageWizard.gtmpl", 
  events = { 
    @EventConfig(name = "Edit", listeners = UIStartEditionInPageWizard.EditPortletActionListener.class) 
  }
)
public class UIStartEditionInPageWizard extends UIContainer {

  /**
   * The listener interface for receiving editPortletAction events. The class
   * that is interested in processing a editPortletAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addEditPortletActionListener<code> method. When
   * the editPortletAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EditPortletActionEvent
   */
  public static class EditPortletActionListener extends EventListener<UIStartEditionInPageWizard> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIStartEditionInPageWizard> event) throws Exception {
      UIStartEditionInPageWizard editMode = event.getSource();
      UIContentListViewerPortlet uiportlet = editMode.getAncestorOfType(UIContentListViewerPortlet.class);
      UIPopupContainer popupContainer = uiportlet.getChild(UIPopupContainer.class);
      UIPortletConfig portletConfig = editMode.getChild(UIPortletConfig.class);
      popupContainer.activate(portletConfig, 1024, 768);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.addUIComponentToUpdateByAjax(popupContainer);
    }
  }
}
