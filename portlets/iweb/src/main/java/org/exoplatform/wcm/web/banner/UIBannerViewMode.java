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
import org.exoplatform.dms.application.JCRResourceResolver;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;


/**
 * Author : Do Ngoc Anh *      
 * Email: anhdn86@gmail *
 * May 9, 2008  
 */

@ComponentConfig (
    lifecycle = UIFormLifecycle.class        
)  


public class UIBannerViewMode extends UIComponent{
  
  private final String DEFAULT_TEMPLATE = "app:/groovy/banner/webui/UIBannerPortlet.gtmpl".intern() ;
  private ResourceResolver  resourceResolver ;

  public UIBannerViewMode() throws Exception {           
  }

  public String getTemplate() {  
    return DEFAULT_TEMPLATE;    
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context) throws Exception{
    PortletRequestContext pContext = (PortletRequestContext)context;    
    PortletRequest portletRequest = pContext.getRequest();    
    
    String repository = portletRequest.getPreferences().getValue("repository",null) ;
    String workspace = portletRequest.getPreferences().getValue("workspace",null) ;
    String nodeUUID = portletRequest.getPreferences().getValue("nodeUUID",null) ;
    if(repository != null && workspace != null && nodeUUID != null) {
      //load template from jcr      
      
    }
    
    resourceResolver = pContext.getApplication().getResourceResolver();
    
    return resourceResolver ;   
  }
  
  public boolean isQuickEditable() throws Exception{
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance() ;
    String quickEdit = pContext.getRequest().getPreferences().getValue("quickEdit","") ;
    return (Boolean.parseBoolean(quickEdit)) ;    
  }
}
