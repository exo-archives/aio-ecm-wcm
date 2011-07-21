/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.component.action;

import org.exoplatform.wcm.webui.component.UICommentForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS 
 * Author : Nguyen Anh Vu 
 *          vuna@exoplatform.com
 *          anhvurz90@gmail.com Jul 14, 2011
 */
@ComponentConfig(
     events = { 
         @EventConfig(listeners = CommentActionComponent.CommentActionListener.class) 
     }
)
public class CommentActionComponent extends UIComponent {

  public static class CommentActionListener extends EventListener<CommentActionComponent> {
    public void execute(Event<CommentActionComponent> event) throws Exception {
      UIPopupContainer uiPopupContainer = event.getSource()
                                               .getAncestorOfType(UIPortletApplication.class)
                                               .findFirstComponentOfType(UIPopupContainer.class);
      UICommentForm uiCommentForm = uiPopupContainer.createUIComponent(UICommentForm.class,
                                                                       null,
                                                                       null);
      String commentNodePath = event.getRequestContext().getRequestParameter("nodePath");
      if (commentNodePath != null && commentNodePath.length() > 0) {
        uiCommentForm.setNodeCommentPath(commentNodePath);
        uiCommentForm.setEdit(true);
      }
      uiPopupContainer.activate(uiCommentForm, 750, 700);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }
}
