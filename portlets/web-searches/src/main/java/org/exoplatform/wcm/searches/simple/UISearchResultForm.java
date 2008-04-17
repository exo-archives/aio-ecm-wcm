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
package org.exoplatform.wcm.searches.simple;


import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletRequest;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Philippe Aristote
 *          philippe.aristote@gmail.com
 * May 23, 2007  
 */

@ComponentConfigs ({
  @ComponentConfig (
      lifecycle = UIFormLifecycle.class,
      template = "app:/groovy/simple-search/webui/component/UISearchResultForm.gtmpl"
  ),
  @ComponentConfig (
    type = UIPageIterator.class,
    template = "system:/groovy/webui/core/UIPageIterator.gtmpl",
    events = @EventConfig(listeners = UIPageIterator.ShowPageActionListener.class )
  )
})

public class UISearchResultForm extends UIForm {
  private UIPageIterator pageIterator_ ; 
  private String portalName_ ;
  
  @SuppressWarnings("unchecked")
  public UISearchResultForm() throws Exception {
    pageIterator_ = addChild(UIPageIterator.class,null,null) ;    
  }
  
  public List getCurrentPageData() throws Exception {
    return pageIterator_.getCurrentPageData() ;
  }
  
  public void setResultList(PageList pageList) {
    pageIterator_.setPageList(pageList) ;    
  }   
  
  
  public void setPortalName(String name) { 
    this.portalName_ = name; 
  }  
  
  public String getDateCreated(PageNode pageNode) {        
    return "4/1/2008";
  }
  
  public Node castNode(Object object) {
    if(object instanceof Node) return (Node)object ;
    return null ;
  }
  
  public PageNode castPageNode(Object object) {
    if(object instanceof PageNode) return (PageNode)object ;
    return null ;
  }

  public String createLink(PageNode pageNode) throws Exception {    
    PortletRequestContext context = PortletRequestContext.getCurrentInstance() ;
    String userId = context.getRemoteUser() ;
    String url = null ;
    if(userId == null) {
      url = getBaseUrl() + "portal/public/"+portalName_+"/"+pageNode.getUri() ;      
    }else {            
      url = getBaseUrl() + "portal/private/"+portalName_+"/"+pageNode.getUri() ;
    }    
    return url;    
  }
  
  private String getBaseUrl() {
    PortletRequestContext context = PortletRequestContext.getCurrentInstance() ;
    PortletRequest request = context.getRequest();    
    return request.getScheme() + "://"+request.getServerName()+":"+request.getServerPort()+"/";
  }
  
  public String getSizeInKB(Node node) throws Exception {
    if(node.isNodeType("nt:file")) {
      long size = node.getNode("jcr:content").getProperty("jcr:data").getLength() ;
      return Long.toString(size/1024) ;
    }
    return Integer.toString(node.getPath().length()) ;
  }
  
  public String createDocumentLink(Node node) throws Exception {
    String worksapce = node.getSession().getWorkspace().getName();
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    String url = getBaseUrl() + "portal/rest/jcr/"+repository+"/"+worksapce+node.getPath() ;
    return url ;    
  }
  
  public UIPageIterator getUIPageIterator() { return pageIterator_ ; }
  
}
