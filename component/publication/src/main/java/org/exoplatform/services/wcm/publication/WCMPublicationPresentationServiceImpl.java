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

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.config.model.Page;
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
public class WCMPublicationPresentationServiceImpl implements WCMPublicationPresentationService, Startable {
  
  private static Log log = ExoLogger.getLogger(WCMPublicationPresentationServiceImpl.class);
  private HashMap<String, WebpagePublicationPlugin> publicationPlugins = 
    new HashMap<String, WebpagePublicationPlugin>();  
  
  private PublicationService publicationService;

  /**
   * Instantiates a new WCM publication presentation service.
   * This service delegate to PublicationService to manage the publication
   * 
   * @param publicationService the publication service
   */
  public WCMPublicationPresentationServiceImpl(PublicationService publicationService) {
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

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WCMPublicationPresentationService#getWebpagePublicationPlugins()
   */
  public Map<String, WebpagePublicationPlugin> getWebpagePublicationPlugins() {
    return publicationPlugins;
  }  
  
  public void start() {
    //Need implement startable interface to make sure all WebpagePublicationPlugin are injected to PublicationService
    log.info("Start WCMPublicationPresentationService...");
  }
  
  public void stop() {
    
  }  
}
