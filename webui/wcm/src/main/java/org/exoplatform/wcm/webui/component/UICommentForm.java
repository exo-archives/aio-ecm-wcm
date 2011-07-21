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
package org.exoplatform.wcm.webui.component;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Anh Vu
 *          vuna@exoplatform.com
 *          anhvurz90@gmail.com
 * Jul 14, 2011  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "system:/groovy/webui/form/UIForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UICommentForm.SaveActionListener.class),
                   @EventConfig(listeners = UICommentForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
             ) 

public class UICommentForm extends org.exoplatform.ecm.webui.presentation.comment.UICommentForm {

  public UICommentForm() throws Exception {
    super();
  }

  @Override
  public Node getCommentNode() throws Exception {
    UIBaseNodePresentation uiComponent = 
      getAncestorOfType(UIPortletApplication.class).
      findFirstComponentOfType(UIBaseNodePresentation.class);
    int index1 = nodeCommentPath.lastIndexOf('/');
    int index2 = nodeCommentPath.lastIndexOf('/', index1 - 1);
    return uiComponent.getOriginalNode().getNode(
                       nodeCommentPath.substring(index2 + 1));
  }

  @Override
  public Node getCurrentNode() throws Exception {
    UIBaseNodePresentation uiComponent = 
            getAncestorOfType(UIPortletApplication.class).
            findFirstComponentOfType(UIBaseNodePresentation.class);
    return (uiComponent == null ? null : uiComponent.getOriginalNode());
  }

  @Override
  public String getLanguage() {
    UIBaseNodePresentation uiComponent = 
      getAncestorOfType(UIPortletApplication.class).
      findFirstComponentOfType(UIBaseNodePresentation.class);
    return (uiComponent == null ? null : uiComponent.getLanguage());
  }

  @Override
  public void updateAjax(Event<org.exoplatform.ecm.webui.presentation.comment.UICommentForm> event) throws Exception {
    UIApplication uiApp = getAncestorOfType(UIPortletApplication.class);
    UIBaseNodePresentation uiComponent = uiApp.findFirstComponentOfType(UIBaseNodePresentation.class);

    UIPopupContainer uiPopupContainer = 
      event.getSource().getAncestorOfType(UIPortletApplication.class)
                       .findFirstComponentOfType(UIPopupContainer.class);
    uiPopupContainer.cancelPopupAction();

    event.getRequestContext().addUIComponentToUpdateByAjax(
               uiComponent != null ? uiComponent : uiApp);
    event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
  }
  
  

}
