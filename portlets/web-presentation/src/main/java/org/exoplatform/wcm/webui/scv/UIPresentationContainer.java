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
package org.exoplatform.wcm.webui.scv;

import javax.portlet.PortletPreferences;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : Do Ngoc Anh *
 * Email: anh.do@exoplatform.com *
 * May 14, 2008
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template="app:/groovy/SingleContentViewer/UIPresentationContainer.gtmpl",
    events = {
      @EventConfig(listeners=UIPresentationContainer.QuickEditActionListener.class),
      @EventConfig(listeners=UIPresentationContainer.QuickPrintActionListener.class)
    }
)


public class UIPresentationContainer extends UIContainer{
  
  /**
   * Instantiates a new uI presentation container.
   * 
   * @throws Exception the exception
   */
  public UIPresentationContainer() throws Exception{                
    addChild(UIPresentation.class,null,null);
  }

  private boolean isQuickPrint = false;
  
  /**
   * Checks if is quick editable.
   * 
   * @return true, if is quick editable
   * 
   * @throws Exception the exception
   */
  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("ShowQuickEdit", null));    
    return Utils.turnOnQuickEditable(portletRequestContext, isQuickEdit);
  }

  /**
   * Gets the portlet id.
   * 
   * @return the portlet id
   */
  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }



  /**
   * The listener interface for receiving quickEditAction events.
   * The class that is interested in processing a quickEditAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addQuickEditActionListener<code> method. When
   * the quickEditAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see QuickEditActionEvent
   */
public static class QuickEditActionListener extends EventListener<UIPresentationContainer>{   
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPresentationContainer> event) throws Exception {
      UIPresentationContainer uicomp = event.getSource();
      UISingleContentViewerPortlet uiportlet = uicomp.getAncestorOfType(UISingleContentViewerPortlet.class);
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();      
      org.exoplatform.webui.core.UIPopupContainer maskPopupContainer = uiportlet.getChild(org.exoplatform.webui.core.UIPopupContainer.class);     
      UIPortletConfig portletConfig = maskPopupContainer.createUIComponent(UIPortletConfig.class,null,null);
      uicomp.addChild(portletConfig);
      portletConfig.init();
      portletConfig.setRendered(true);
      WebUIPropertiesConfigService propertiesConfigService = uicomp.getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_QUICK_EDIT);
      maskPopupContainer.activate(portletConfig,popupProperties.getWidth(),popupProperties.getHeight());            
      context.addUIComponentToUpdateByAjax(maskPopupContainer);

    }
  }

  public static class QuickPrintActionListener extends EventListener<UIPresentationContainer>{   
    public void execute(Event<UIPresentationContainer> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext)event.getRequestContext();
      PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
      String repository = preferences.getValue(UISingleContentViewerPortlet.REPOSITORY, null);    
      String workspace = preferences.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
      String nodeIdentifier = preferences.getValue(UISingleContentViewerPortlet.IDENTIFIER, null) ;
      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      String portalURI = portalRequestContext.getPortalURI();
      String baseURI = portletRequestContext.getRequest().getScheme() + "://" + portletRequestContext.getRequest().getServerName() + ":" + String.format("%s", portletRequestContext.getRequest().getServerPort());
      UIPresentationContainer uicomp = event.getSource();
      WCMConfigurationService wcmConfigurationService = uicomp.getApplicationComponent(WCMConfigurationService.class);
      String parameterizedPageURI = wcmConfigurationService.getParameterizedPageURI();
      String url = baseURI + portalURI + parameterizedPageURI.substring(1, parameterizedPageURI.length()) + "/" + repository + "/" + workspace + nodeIdentifier;
      
      UISingleContentViewerPortlet uiportlet = uicomp.getAncestorOfType(UISingleContentViewerPortlet.class);
      UIPopupContainer maskPopupContainer = uiportlet.getChild(UIPopupContainer.class);     
      UIPrintFrame printFrame = maskPopupContainer.createUIComponent(UIPrintFrame.class,null,null);
      printFrame.setWebContentId(uicomp.getPortletId());
      printFrame.setIframeUrl(url);
      maskPopupContainer.activate(printFrame, 800, 500);            
      portletRequestContext.addUIComponentToUpdateByAjax(maskPopupContainer);
    }
  }

  public boolean isQuickPrint() {return isQuickPrint;}
  public void setQuickPrint(boolean isQuickPrint) {this.isQuickPrint = isQuickPrint;}
}