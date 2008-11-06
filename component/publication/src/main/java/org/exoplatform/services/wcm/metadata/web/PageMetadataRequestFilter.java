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
package org.exoplatform.services.wcm.metadata.web;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.metadata.PageMetadataService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 3, 2008  
 */
public class PageMetadataRequestFilter implements Filter {  
  public final static String PCV_PARAMETER_REGX           = "(.*)/(.*)/(.*)";

  public void init(FilterConfig config) throws ServletException {    
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
  throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletRequest;
    try {
      if(!checkAndSetMetadataIfRequestToSCVPortlet(req)) {      
        checkAndSetMetadataIfRequestToPCVPortlet(req);       
      }      
    } catch (Exception e) { } 
    chain.doFilter(servletRequest,servletResponse);
  }  

  private boolean checkAndSetMetadataIfRequestToSCVPortlet(HttpServletRequest req) throws Exception {
    String pathInfo = req.getPathInfo();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PageMetadataService metadataRegistry = 
      (PageMetadataService)container.getComponentInstanceOfType(PageMetadataService.class);
    ThreadLocalSessionProviderService localSessionProviderService = getService(ThreadLocalSessionProviderService.class);
    SessionProvider sessionProvider = localSessionProviderService.getSessionProvider(null);
    Map<String,String> pageMetadata = metadataRegistry.getMetadata(pathInfo,sessionProvider);
    if(pageMetadata == null)
      return false;    
    String pageTitle = pageMetadata.get(PageMetadataService.PAGE_TITLE);
    if(pageTitle != null) {
      req.setAttribute(PortalRequestContext.REQUEST_TITLE,pageTitle);
      pageMetadata.remove(PageMetadataService.PAGE_TITLE);
    }
    req.setAttribute(PortalRequestContext.REQUEST_METADATA,pageMetadata);          
    return true;
  }  

  private boolean checkAndSetMetadataIfRequestToPCVPortlet(HttpServletRequest req) throws Exception {    
    String pathInfo = req.getPathInfo();
    if(pathInfo == null) return false;    
    WCMConfigurationService configurationService = getService(WCMConfigurationService.class);
    String parameterizedPageURI = configurationService.getParameterizedPageURI();
    int index = pathInfo.indexOf(parameterizedPageURI);
    if(index<1) return false;
    String parameter = pathInfo.substring(index);
    if(!parameter.matches(PCV_PARAMETER_REGX)) return false;
    String[] parameters = parameter.split("/");
    String repository = parameters[0];
    String workspace = parameters[1];
    String nodeIdentifier = parameters[2];
    RepositoryService repositoryService = getService(RepositoryService.class);
    ThreadLocalSessionProviderService localSessionProviderService = getService(ThreadLocalSessionProviderService.class);
    SessionProvider sessionProvider = localSessionProviderService.getSessionProvider(null);
    Node node = null;
    Session session = null;
    try {
      session = sessionProvider.getSession(workspace,repositoryService.getRepository(repository));
      node = session.getNodeByUUID(nodeIdentifier);      
    } catch (ItemNotFoundException e) {
      node = (Node)session.getItem(nodeIdentifier);
    }catch (PathNotFoundException e) {
      req.setAttribute("ParameterizedContentViewerPortlet.data.object",new ItemNotFoundException());      
    }catch (AccessControlException e) {
      req.setAttribute("ParameterizedContentViewerPortlet.data.object",e);     
    }catch (Exception e) {
      req.setAttribute("ParameterizedContentViewerPortlet.data.object",new ItemNotFoundException());
    }
    if(node != null) {
      req.setAttribute("ParameterizedContentViewerPortlet.data.object",node);
      PageMetadataService pageMetadataService = getService(PageMetadataService.class);      
      Map<String,String> pageMetadata = pageMetadataService.extractMetadata(node);
      String title = pageMetadata.get(PageMetadataService.PAGE_TITLE);
      if(title != null) {
        req.setAttribute(PortalRequestContext.REQUEST_TITLE,title);
        pageMetadata.remove(PageMetadataService.PAGE_TITLE);
      }
      req.setAttribute(PortalRequestContext.REQUEST_METADATA,pageMetadata);          
      return true;
    }      
    return false;
  }   

  private <T> T getService(Class<T> clazz) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  public void destroy() {    
  }


}
