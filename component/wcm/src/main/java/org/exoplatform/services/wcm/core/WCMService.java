/**
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
package org.exoplatform.services.wcm.core;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Benjamin Paillereau
 * benjamin.paillereau@exoplatform.com
 * Apr 30, 2009
 */

public interface WCMService {
  
	/**
	 * This method returns a jcr Node based on the given identifier.
	 * 
	 * @param repository the repository name
	 * @param workspace the workspace name
	 * @param nodeIdentifier the node identifier
	 * @param sessionProvider the session provider
	 * 
	 * @return a jcr Node
	 * 
	 * @throws Exception the exception
	 * 
	 * @see javax.jcr.Node
	 */
	public Node getReferencedContent(SessionProvider sessionProvider, String repository, String workspace, String nodeIdentifier) throws Exception ;

	/**
	 * This method checks if the given portal is the default shared portal.
	 * 
	 * @param portalName the portal name
	 * @param sessionProvider the session provider
	 * 
	 * @return true if portal is shared portal
	 * 
	 * @throws Exception the exception
	 */
	public boolean isSharedPortal(SessionProvider sessionProvider, String portalName) throws Exception ;
	
}