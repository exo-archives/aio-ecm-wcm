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

import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Sep 29, 2008
 */
public interface WCMPublicationPresentationService {   
  
 /**
  * Add a Web Publication Plugin to the service.
  * The method caches all added plugins.
  * 
  * @param p the plugin to add
  */
 public void addPublicationPlugin(WebpagePublicationPlugin p); 
 
 
 /**
  * Publish content to a portal page when the node is in a publication lifecyle
  * 
  * @param content the content
  * @param page the page
  * 
  * @throws NotInPublicationLifecycleException the not in publication lifecycle exception
  * @throws Exception the exception
  */
 public void publishContentToPage(Node content, Page page) throws NotInPublicationLifecycleException, Exception;
 
 /**
  * Publish a content node to a portal page when the node is not in any lifecyle
  * 
  * @param content the content
  * @param page the page
  * @param lifecyleName the lifecyle name
  * 
  * @throws Exception the exception
  */
 public void publishContentToPage(Node content, Page page, String lifecyleName) throws Exception;
 
 /**
  * Suspend a published content from a portal page.
  * 
  * @param content the jcr content node
  * @param page the portal page
  * 
  * @throws NotInPublicationLifecycleException the not in publication lifecycle exception
  * @throws Exception the exception
  */
 public void suspendPublishedContentFromPage(Node content, Page page) throws NotInPublicationLifecycleException, Exception;
 
 /**
  * Retrieves all added web page publication plugins.
  * This method is notably used to enumerate possible lifecycles.
  * 
  * @return the map of web page publication plugin
  */
 public Map<String, WebpagePublicationPlugin> getWebpagePublicationPlugins();
}
