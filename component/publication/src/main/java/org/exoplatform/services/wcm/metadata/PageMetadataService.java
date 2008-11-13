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
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 4, 2008  
 */
public interface PageMetadataService {  
  public final static String PAGE_TITLE = "pageTitle";
  public void addMetadata(String pageURI, HashMap<String,String> metadata) throws Exception;  
  public void removeMetadata(String pageURI) throws Exception; 
  public HashMap<String, String> extractMetadata(Node node) throws Exception;
  public Map<String,String> getMetadata(String pageURI, SessionProvider sessionProvider) throws Exception ;
  public HashMap<String,String> getPortalMetadata(String uri, SessionProvider sessionProvider) throws Exception;
  
}
