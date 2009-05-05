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
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/**
 * Created by The eXo Platform SAS
 * @author Benjamin Paillereau
 * benjamin.paillereau@exoplatform.com
 * Apr 30, 2009
 */
public class WCMServiceImpl implements WCMService {

	/**
	 * Returns a jcr Node
	 * 
	 * @see org.exoplatform.services.wcm.core.WCMService#getReferencedContent(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Node getReferencedContent(String repository, String workspace, String nodeIdentifier) throws Exception {
		if(repository == null || workspace == null || nodeIdentifier == null) 
			throw new ItemNotFoundException();
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		RepositoryService repositoryService = 
			(RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		String userId = Util.getPortalRequestContext().getRemoteUser();
		SessionProvider sessionProvider = null;
		if(userId == null) {
			sessionProvider = SessionProviderFactory.createAnonimProvider();
		}else {
			sessionProvider = SessionProviderFactory.createSessionProvider();
		}
		Session session = sessionProvider.getSession(workspace, manageableRepository);
		Node content = null;
		try {
			content = session.getNodeByUUID(nodeIdentifier);
		} catch (Exception e) {
			content = (Node) session.getItem(nodeIdentifier);
		} finally {
			sessionProvider.close();
		}
		return content;
	}

	/**
	 * Returns a jcr Node
	 * 
	 * @see org.exoplatform.services.wcm.core.WCMService#getRootNode(java.lang.String, java.lang.String)
	 */
	public Node getRootNode(String repository, String workspace) throws Exception {
		if(repository == null || workspace == null) 
			throw new ItemNotFoundException();
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		RepositoryService repositoryService = 
			(RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
		ManageableRepository manageableRepository = repositoryService.getRepository(repository);
		String userId = Util.getPortalRequestContext().getRemoteUser();
		SessionProvider sessionProvider = null;
		if(userId == null) {
			sessionProvider = SessionProviderFactory.createAnonimProvider();
		}else {
			sessionProvider = SessionProviderFactory.createSessionProvider();
		}
		Session session = sessionProvider.getSession(workspace, manageableRepository);
		Node content = null;
		try {
			content = session.getRootNode();
		} finally {
			sessionProvider.close();
		}
		return content;
	}

	public boolean isSharedPortal(String portalName) throws Exception {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		LivePortalManagerService livePortalManagerService = (LivePortalManagerService)container.getComponentInstanceOfType(LivePortalManagerService.class);
		boolean isShared = false;
	    ThreadLocalSessionProviderService providerService = (ThreadLocalSessionProviderService)container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
	    SessionProvider sessionProvider = providerService.getSessionProvider(null);
	    try {
	    	Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
	    	isShared = sharedPortal.getName().equals(portalName);
	    } finally {
	    	sessionProvider.close();
	    }
		return isShared; 
	}
	

}
