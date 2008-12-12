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
package org.exoplatform.services.wcm.metadata;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 3, 2008  
 */
public class PageMetadataServiceImpl implements PageMetadataService {
  private static Log log = ExoLogger.getLogger(PageMetadataServiceImpl.class);
  
  private LivePortalManagerService livePortalManagerService; 
  private CategoriesService categoriesService;
  private FolksonomyService folksonomyService;
  public PageMetadataServiceImpl(LivePortalManagerService livePortalManagerService, CategoriesService categoriesService, FolksonomyService folksonomyService) throws Exception {        
    this.livePortalManagerService = livePortalManagerService;    
    this.categoriesService = categoriesService;
    this.folksonomyService = folksonomyService;
  }      
  
  public HashMap<String, String> getPortalMetadata(String uri, SessionProvider sessionProvider)
  throws Exception {
    String portalName = uri.split("/")[1];      
    try {
      Node portal = livePortalManagerService.getLivePortal(portalName,sessionProvider);
      return extractPortalMetadata(portal);      
    } catch (Exception e) {
    }       
    return null;
  }

  private HashMap<String,String> extractPortalMetadata(Node portalNode) throws Exception {
    HashMap<String,String> metadata = new HashMap<String,String>();
    NodeType siteMedata = portalNode.getSession().getWorkspace().getNodeTypeManager().getNodeType("metadata:siteMetadata");
    for(PropertyDefinition pdef: siteMedata.getDeclaredPropertyDefinitions()) {
      String metadataName = pdef.getName();
      String metadataValue = getProperty(portalNode,metadataName);
      if(metadataValue != null) 
        metadata.put(metadataName,metadataValue);              
    }
    return metadata;
  }  
  
  public HashMap<String, String> extractMetadata(Node node) throws Exception {
    HashMap<String, String> medatata = new HashMap<String,String>();
    Node portalNode = findPortal(node);    
    String siteTitle = null;
    String portalKeywords = null;
    if(portalNode != null) {
      siteTitle = getProperty(portalNode,SITE_TITLE);
      if(siteTitle == null) siteTitle = portalNode.getName();
      medatata = extractPortalMetadata(portalNode);
      portalKeywords = medatata.get(KEYWORDS);
    }
    String pageTitle = getProperty(node,"exo:title");
    if(pageTitle == null)
      pageTitle = node.getName();    
    if(siteTitle != null) {
      pageTitle = pageTitle + "-" + siteTitle;
    }    
    String description = getProperty(node,"exo:summary");    
    medatata.put(PAGE_TITLE,pageTitle);
    if(description != null) {
      medatata.put(DESCRIPTION,description); 
    }
    String keywords = computeContentKeywords(node,pageTitle);
    if(portalKeywords != null) {
      keywords = keywords.concat(",").concat(portalKeywords);
    }
    medatata.put(KEYWORDS,keywords);    
    return medatata;
  }

  private String computeContentKeywords(Node node, String title) throws Exception {
    StringBuilder builder = new StringBuilder();    
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();    
    for(Node category: categoriesService.getCategories(node,repository)) {
      builder.append(category.getName()).append(",");
    }    
    for(Node tag: folksonomyService.getLinkedTagsOfDocument(node,repository)) {
      builder.append(tag.getName()).append(",");
    }
    builder.append(title.replaceAll(" ",","));
    return builder.toString();
  }

  private String getProperty(Node node, String propertyName) throws Exception {
    return node.hasProperty(propertyName)? node.getProperty(propertyName).getString():null;
  }

  protected Node findPortal(Node child) throws Exception{                       
    try {
      return livePortalManagerService.getLivePortalByChild(child);
    } catch (Exception e) {
    }
    return null;
  }               
}
