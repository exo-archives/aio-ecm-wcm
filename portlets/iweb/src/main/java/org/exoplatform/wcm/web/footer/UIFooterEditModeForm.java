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
package org.exoplatform.wcm.web.footer;

import java.io.InputStream;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Author : Do Ngoc Anh *      
 * Email: anhdn86@gmail *
 * May 9, 2008  
 */

@ComponentConfig(
    template = "app:groovy/footer/webui/UIFooterEditModeForm.gtmpl",
    lifecycle = UIFormLifecycle.class,
    events =  {
      @EventConfig(listeners = UIFooterEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIFooterEditModeForm.CancelActionListener.class)
    } 
)

public class UIFooterEditModeForm extends UIForm{
  
  private final String DEFAULT_TEMPLATE = "app:/groovy/footer/webui/UIFooterPortlet.gtmpl".intern(); 
  
  public UIFooterEditModeForm() throws Exception{
    addUIComponentInput(new UIFormTextAreaInput("template", "template", loadTemplateData()));
    addUIComponentInput(new UIFormCheckBoxInput("quickEdit", "quickEdit", null));
  }
  
  public String loadTemplateData() throws Exception{
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletRequest pRequest = pContext.getRequest();
    
    String repository = pRequest.getPreferences().getValue("repository", null);
    String workspace = pRequest.getPreferences().getValue("workspace", null);
    String nodeUUID = pRequest.getPreferences().getValue("nodeUUID", null);
    
    if(repository != null && workspace != null && nodeUUID != null){
      //load template from jcr
    }
    
    InputStream iStream = pContext.getApplication().getResourceResolver().getInputStream(DEFAULT_TEMPLATE);
    return IOUtil.getStreamContentAsString(iStream);
    
  }
  
  public static class SaveActionListener extends EventListener<UIFooterEditModeForm>{
    
    public void execute(Event<UIFooterEditModeForm> event) throws Exception {
      UIFooterEditModeForm editForm = event.getSource();
      PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
      PortletRequest pRequest = pContext.getRequest();
      PortletPreferences portletPreferences = pRequest.getPreferences(); 
      
      //store in jcr
     
//      String repository = null ;
//      String workspace = null ;
//      String nodeUUID = null ;
      
      //save portlet preference
      boolean quickEdit = editForm.getUIFormCheckBoxInput("quickEdit").isChecked();
      portletPreferences.setValue("quickEdit", Boolean.toString(quickEdit));
      
      
//    portletPreferences.setValue("repository",repository) ;
//    portletPreferences.setValue("workspace",workspace) ;
//    portletPreferences.setValue("nodeUUID",nodeUUID) ;
      
      portletPreferences.store();      
      pContext.setApplicationMode(PortletMode.VIEW);
    }
    
  }
  
  public static class CancelActionListener extends EventListener<UIFooterEditModeForm>{
    
    public void execute(Event<UIFooterEditModeForm> event) throws Exception {
      PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
      pContext.setApplicationMode(PortletMode.VIEW);
    }
  }
}
