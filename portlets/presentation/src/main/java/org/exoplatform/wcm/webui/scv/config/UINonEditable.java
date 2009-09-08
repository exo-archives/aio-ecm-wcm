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

import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Sep 15, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/SingleContentViewer/config/UINonEditable.gtmpl",
    events= {
      @EventConfig(listeners = UINonEditable.BackToViewActionListener.class)
    }
)
public class UINonEditable extends UIForm {

  /**
   * The listener interface for receiving backToViewAction events.
   * The class that is interested in processing a backToViewAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBackToViewActionListener<code> method. When
   * the backToViewAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BackToViewActionEvent
   */
  public static class BackToViewActionListener extends EventListener<UINonEditable> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UINonEditable> event) throws Exception {
      @SuppressWarnings("unused")
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
    }
  }
}
