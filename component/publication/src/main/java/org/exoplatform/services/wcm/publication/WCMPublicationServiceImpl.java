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
package org.exoplatform.services.wcm.publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Sep 29, 2008
 */
public class WCMPublicationServiceImpl implements WCMPublicationService, Startable {
  
  private static Log log = ExoLogger.getLogger(WCMPublicationServiceImpl.class);
  
  private HashMap<String, WebpagePublicationPlugin> publicationPlugins = 
    new HashMap<String, WebpagePublicationPlugin>();  
  
  private PublicationService publicationService;

  /**
   * Instantiates a new WCM publication service.
   * This service delegate to PublicationService to manage the publication
   * 
   * @param publicationService the publication service
   */
  public WCMPublicationServiceImpl(PublicationService publicationService) {
    this.publicationService = publicationService;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#addPublicationPlugin(org.exoplatform.services.wcm.publication.WebpagePublicationPlugin)
   */
  public void addPublicationPlugin(WebpagePublicationPlugin p) {
    publicationPlugins.put(p.getLifecycleName(),p);
    publicationService.addPublicationPlugin(PublicationPlugin.class.cast(p));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#suspendPublishedContentFromPage(javax.jcr.Node, org.exoplatform.portal.config.model.Page)
   */
  public void suspendPublishedContentFromPage(Node content, Page page) throws NotInPublicationLifecycleException, Exception {
    if (!publicationService.isNodeEnrolledInLifecycle(content)) {
      throw new NotInPublicationLifecycleException();
    }
    String lifecycleName= publicationService.getNodeLifecycleName(content);
    WebpagePublicationPlugin publicationPlugin = publicationPlugins.get(lifecycleName);
    publicationPlugin.suspendPublishedContentFromPage(content,page);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#publishContentToPage(javax.jcr.Node, org.exoplatform.portal.config.model.Page)
   */
  public void publishContentToPage(Node content, Page page)
  throws NotInPublicationLifecycleException, Exception {    
    if(!publicationService.isNodeEnrolledInLifecycle(content))
      throw new NotInPublicationLifecycleException("The node " +content.getPath() + " is not enrolled to any publication lifecyle");
    String lifecycleName = publicationService.getNodeLifecycleName(content);
    WebpagePublicationPlugin publicationPlugin = publicationPlugins.get(lifecycleName);
    publicationPlugin.publishContentToPage(content,page);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#publishContentToPage(javax.jcr.Node, org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  public void publishContentToPage(Node content, Page page, String lifecyleName) throws Exception {          
    publicationService.enrollNodeInLifecycle(content,lifecyleName);  
    String lifecycleName = publicationService.getNodeLifecycleName(content);
    WebpagePublicationPlugin publicationPlugin = publicationPlugins.get(lifecycleName);
    publicationPlugin.publishContentToPage(content,page);
  }    
  
  public void enrollNodeInLifecycle(Node node, String lifecycleName) throws Exception {
    WebpagePublicationPlugin publicationPlugin = publicationPlugins.get(lifecycleName);
    publicationPlugin.addMixin(node);    
    node.setProperty("publication:lifecycleName", lifecycleName);
    node.setProperty("publication:history", new ArrayList<Value>().toArray(new Value[0]));
    publicationPlugin.changeState(node, "enrolled", null);    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#unsubcribeLifecycle(javax.jcr.Node)
   */
  public void unsubcribeLifecycle(Node node) throws NotInPublicationLifecycleException, Exception {
    publicationService.unsubcribeLifecycle(node);    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#getWebpagePublicationPlugins()
   */
  public Map<String, WebpagePublicationPlugin> getWebpagePublicationPlugins() {
    return publicationPlugins;
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecycleOnChangeNavigation(org.exoplatform.portal.config.model.PageNavigation)
   */
  public void updateLifecycleOnChangeNavigation(PageNavigation navigation) throws Exception {
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecycleOnChangeNavigation(navigation);
    }    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecycleOnRemovePage(org.exoplatform.portal.config.model.Page)
   */
  public void updateLifecycleOnRemovePage(Page page) throws Exception {
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecycleOnRemovePage(page);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecyleOnChangePage(org.exoplatform.portal.config.model.Page)
   */
  public void updateLifecyleOnChangePage(Page page) throws Exception {   
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecyleOnChangePage(page);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecyleOnCreateNavigation(org.exoplatform.portal.config.model.PageNavigation)
   */
  public void updateLifecyleOnCreateNavigation(PageNavigation navigation) throws Exception {
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecyleOnCreateNavigation(navigation);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecyleOnCreatePage(org.exoplatform.portal.config.model.Page)
   */
  public void updateLifecyleOnCreatePage(Page page) throws Exception {
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecyleOnCreatePage(page);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#updateLifecyleOnRemoveNavigation(org.exoplatform.portal.config.model.PageNavigation)
   */
  public void updateLifecyleOnRemoveNavigation(PageNavigation navigation) throws Exception {
    for(WebpagePublicationPlugin publicationPlugin: publicationPlugins.values()) {
      publicationPlugin.updateLifecyleOnRemoveNavigation(navigation);
    }
  }
  
  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start()   {   
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {   
  }

  public boolean isEnrolledInWCMLifecycle(Node node) throws NotInPublicationLifecycleException, Exception {
    if(!publicationService.isNodeEnrolledInLifecycle(node))
      return false;
    String lifecyleName = publicationService.getNodeLifecycleName(node);
    if(publicationPlugins.containsKey(lifecyleName))
      return true;
    throw new NotInWCMPublicationException();
  }   
}
