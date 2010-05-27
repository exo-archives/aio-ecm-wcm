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
package org.exoplatform.services.wcm.core.impl;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Benjamin Paillereau
 * benjamin.paillereau@exoplatform.com
 * Apr 30, 2009
 */
public class WCMServiceImpl implements WCMService {

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.WCMService#getReferencedContent(java.lang.String, java.lang.String, java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public Node getReferencedContent(SessionProvider sessionProvider, String repository, String workspace, String nodeIdentifier) throws Exception {
		if(repository == null || workspace == null || nodeIdentifier == null) throw new ItemNotFoundException();
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		RepositoryService repositoryService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		Session session = sessionProvider.getSession(workspace, manageableRepository);
		LinkManager linkManager = (LinkManager)container.getComponentInstanceOfType(LinkManager.class);
		Node content = null;
		try {
			content = session.getNodeByUUID(nodeIdentifier);
		} catch (ItemNotFoundException itemNotFoundException) {
		  try {
		    content = (Node) session.getItem(nodeIdentifier);
		  } catch(Exception exception) {
		    content = null;
		  }
		}
		if (content != null && linkManager.isLink(content)) {
		  content = linkManager.getTarget(content, SystemIdentity.SYSTEM.equals(session.getUserID()));
		}
		return content;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.core.WCMService#isSharedPortal(java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
	 */
	public boolean isSharedPortal(SessionProvider sessionProvider, String portalName) throws Exception {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		LivePortalManagerService livePortalManagerService = (LivePortalManagerService)container.getComponentInstanceOfType(LivePortalManagerService.class);
		boolean isShared = false;
  	Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
  	isShared = sharedPortal.getName().equals(portalName);
		return isShared; 
	}
	

}
