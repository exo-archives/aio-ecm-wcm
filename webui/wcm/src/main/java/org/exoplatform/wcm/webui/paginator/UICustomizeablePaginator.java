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
package org.exoplatform.wcm.webui.paginator;

import javax.faces.lifecycle.Lifecycle;

import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Oct 23, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class,
    events = @EventConfig(listeners = UICustomizeablePaginator.ShowPageActionListener.class )    
)
public class UICustomizeablePaginator extends UIPageIterator {

  private String templatePath;
  private ResourceResolver resourceResolver;

  public UICustomizeablePaginator() {    
  }

  public int getTotalPages() { return getPageList().getAvailablePage(); }
  public int getTotalItems() { return getPageList().getAvailable(); }
  public int getItemPerPage() { return getPageList().getPageSize(); }
  
  public void init(ResourceResolver resourceResolver, String templatePath) {
    this.resourceResolver = resourceResolver;
    this.templatePath = templatePath;
  }
  public void setTemplatePath(String path) { this.templatePath = path; }
  public void setResourceResolver(ResourceResolver resolver) { this.resourceResolver = resolver; }

  public String getTemplate() {
    if(templatePath != null) 
      return templatePath;    
    return super.getTemplate();
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context,String template) {
    if(resourceResolver != null)
      return resourceResolver;
    return super.getTemplateResourceResolver(context,template);
  }

  static  public class ShowPageActionListener extends EventListener<UICustomizeablePaginator> {
    public void execute(Event<UICustomizeablePaginator> event) throws Exception {
      UICustomizeablePaginator uiPaginator = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiPaginator.setCurrentPage(page) ;
      UIComponent parent = uiPaginator.getParent();
      if(parent == null) return ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(parent);           
      parent.broadcast(event,event.getExecutionPhase());
    }
  }
}
