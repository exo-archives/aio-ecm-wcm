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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Oct 3, 2008  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UICloseForm.CloseActionListener.class)
    }
)
public class UICloseForm extends UIForm {
  public UICloseForm() {}

  public static class CloseActionListener extends EventListener<UICloseForm> {
    public void execute(Event<UICloseForm> event) throws Exception {
      UICloseForm closeForm = event.getSource();
      UIPublishingPanel publishingPanel = closeForm.getAncestorOfType(UIPublishingPanel.class);
      UIPopupWindow popupAction = publishingPanel.getAncestorOfType(UIPopupWindow.class) ;
      popupAction.setShow(false);
      popupAction.setRendered(false);
//      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
