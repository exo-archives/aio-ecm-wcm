/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.pclv;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
   //lifecycle = Lifecycle.class,
   template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedContentListViewerContainer.gtmpl", 
   events = { 
     @EventConfig(listeners = UIParameterizedContentListViewerContainer.QuickEditActionListener.class) 
   }
 )
public class UIParameterizedContentListViewerContainer extends UIContainer {

  public UIParameterizedContentListViewerContainer() throws Exception {
    this.addChild(UIParameterizedContentListViewerForm.class, null, "UIParameterizedContentListViewerForm");    
  }

  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {   
    super.processRender(context);
  }

  public static class QuickEditActionListener extends EventListener<UIParameterizedContentListViewerContainer> {

    public void execute(Event<UIParameterizedContentListViewerContainer> event) throws Exception {
      UIParameterizedContentListViewerContainer uiContentViewerContainer = event.getSource();

      UIParameterizedContentListViewerPortlet uiListViewerPortlet = uiContentViewerContainer
                                                                        .getAncestorOfType(UIParameterizedContentListViewerPortlet.class);
      for(UIComponent component : uiListViewerPortlet.getChildren()){
        System.out.println("----------------> child name: " + component.getName());
      }
      
      UIPopupContainer popupContainer = uiListViewerPortlet.getChild(UIPopupContainer.class);
      System.out.println("============================ Get container =========================== ");
      UIPopupWindow popupWindow = popupContainer.getChildById(UIParameterizedContentListViewerConstant.PARAMETERIZED_MANAGEMENT_POPUP_WINDOW);
      if(popupWindow == null) {

        UIParameterizedManagementForm parameterizedForm = 
          popupContainer.createUIComponent(UIParameterizedManagementForm.class, null, null);
        Utils.createPopupWindow(popupContainer,
                                parameterizedForm,
                                event.getRequestContext(),
                                UIParameterizedContentListViewerConstant
                                .PARAMETERIZED_MANAGEMENT_POPUP_WINDOW, 800, 600);
        PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
        context.addUIComponentToUpdateByAjax(popupContainer);      
      } else {
        
        popupWindow.setShow(true);
      }
    }
  }
}
