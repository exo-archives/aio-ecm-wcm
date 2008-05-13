/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wcm.web.banner;

import java.io.InputStream;

import javax.portlet.PortletRequest;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.dms.application.StringResourceResolver;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 9, 2008  
 */

@ComponentConfig(
  lifecycle = Lifecycle.class  
)
public class UIBannerViewMode extends UIComponent {

  private final String     DEFAULT_TEMPLATE = "app:/groovy/banner/webui/UIBannerPortlet.gtmpl".intern();

  public UIBannerViewMode() throws Exception { }

  public String getTemplate() { return DEFAULT_TEMPLATE; }

  public String loadTemplateData() throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String templateData = null ;
    PortletRequest portletRequest = pContext.getRequest();
    String repository = portletRequest.getPreferences().getValue("repository", null);
    String workspace = portletRequest.getPreferences().getValue("workspace", null);
    String nodeUUID = portletRequest.getPreferences().getValue("nodeUUID", null);
    if (repository != null && workspace != null && nodeUUID != null) {
      //load template from jcr: templateData=?      
    }
    //#################### need remove this code when can load template from jcr ####################
    if(templateData == null) {
      InputStream iStream = pContext.getApplication().getResourceResolver().getInputStream(DEFAULT_TEMPLATE);
      templateData = IOUtil.getStreamContentAsString(iStream); 
    }    
    return templateData ;    
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      String templateData = loadTemplateData() ;
      if(templateData != null) 
        return new StringResourceResolver(templateData) ; 
    } catch (Exception e) {
    }    
    return super.getTemplateResourceResolver(context, template);
  }
  
}
