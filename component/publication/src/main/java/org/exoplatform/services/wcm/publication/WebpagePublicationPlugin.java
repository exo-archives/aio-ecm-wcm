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

import javax.jcr.Node;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.ecm.publication.PublicationPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Sep 29, 2008
 */

/**
 * Base class of Webpage Publication plugins.
 * Webpage publication plugins implement a publication lifecycle. Each time a new
 * custom lifecycle needs to be defined, a new plugin has to be implemented
 * and registered with the Publication Service.
 */
public abstract class WebpagePublicationPlugin extends PublicationPlugin { 
  
  /**
   * Publish content node to a portal page.
   * 
   * @param content the jcr content node
   * @param page the portal page
   * 
   * @throws Exception the exception
   */
  public abstract void publishContentToPage(Node content, Page page) throws Exception;  
  
  /**
   * Suspend published content from a portal page.
   * 
   * @param content the content
   * @param page the page
   * 
   * @throws Exception the exception
   */
  public abstract void suspendPublishedContentFromPage(Node content, Page page) throws Exception;
}
