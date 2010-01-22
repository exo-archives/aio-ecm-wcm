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
package org.exoplatform.services.wcm.extensions.publication;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.wcm.extensions.publication.context.impl.ContextConfig.Context;
import org.exoplatform.services.wcm.extensions.publication.impl.PublicationManagerImpl;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.State;
import org.exoplatform.services.wcm.extensions.utils.ContextComparator;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

public class WCMPublicationServiceImpl extends org.exoplatform.services.wcm.publication.WCMPublicationServiceImpl {

    /** The publication service. */
    private PublicationService publicationService;

    /** The content composer. */
    private WCMComposer wcmComposer;

    /**
     * Instantiates a new WCM publication service. This service delegate to
     * PublicationService to manage the publication
     * 
     * @param publicationService
     *            the publication service
     */
    public WCMPublicationServiceImpl() {
	super();
	this.publicationService = WCMCoreUtils.getService(PublicationService.class);
	this.wcmComposer = WCMCoreUtils.getService(WCMComposer.class);
    }

    /**
     * This default implementation uses "States and versions based publication"
     * as a default lifecycle for all sites and "Simple Publishing" for the root
     * user.
     */
    public void enrollNodeInLifecycle(Node node, String siteName, String remoteUser) {
	try {
	    ExoContainer container = ExoContainerContext.getCurrentContainer();
	    PublicationManagerImpl publicationManagerImpl = (PublicationManagerImpl) container
		    .getComponentInstanceOfType(PublicationManagerImpl.class);
	    ContextComparator comparator = new ContextComparator();
	    TreeSet<Context> treeSetContext = new TreeSet<Context>(comparator);
	    treeSetContext.addAll(publicationManagerImpl.getContexts());
	    for (Context context : treeSetContext) {
		boolean pathVerified = true;
		boolean nodetypeVerified = true;
		boolean siteVerified = true;
		boolean membershipVerified = true;
		String path = context.getPath();
		String nodetype = context.getNodetype();
		String site = context.getSite();
		String membership = context.getMembership();
		if (path != null) {
		    String workspace = node.getSession().getWorkspace().getName();
		    ManageableRepository manaRepository = (ManageableRepository) node.getSession().getRepository();
		    String repository = manaRepository.getConfiguration().getName();
		    String[] pathTab = path.split(":");
		    pathVerified = node.getPath().contains(pathTab[2]) && (repository.equals(pathTab[0])) && (workspace.equals(pathTab[1]));
		}
		if (nodetype != null)
		    nodetypeVerified = nodetype.equals(node.getPrimaryNodeType().getName());
		if (site != null)
		    siteVerified = site.equals(siteName);
		if (membership != null) {
		    String[] membershipTab = membership.split(":");
		    IdentityRegistry identityRegistry = (IdentityRegistry) container.getComponentInstanceOfType(IdentityRegistry.class);
		    Identity identity = identityRegistry.getIdentity(remoteUser);
		    membershipVerified = identity.isMemberOf(membershipTab[1], membershipTab[0]);
		}
		if (pathVerified && nodetypeVerified && siteVerified && membershipVerified) {
		    Lifecycle lifecycle = publicationManagerImpl.getLifecycle(context.getLifecycle());
		    String lifecycleName = this.getWebpagePublicationPlugins().get(lifecycle.getPublicationPlugin()).getLifecycleName();
		    if (node.canAddMixin("publication:authoring")) {
			node.addMixin("publication:authoring");
			node.setProperty("publication:lastUser", remoteUser);
			node.setProperty("publication:lifecycle", lifecycle.getName());
			
            setInitialState(node, lifecycle);
  		  }
		    enrollNodeInLifecycle(node, lifecycleName);
		    break;
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     *  Automatically move to initial state if 'automatic'
     * @param node
     * @param lifecycle
     * @throws Exception
     */
    private void setInitialState(Node node, Lifecycle lifecycle) throws Exception {
      List<State> states = lifecycle.getStates();
      if (states != null && states.size() > 0) {
        State initialState = states.get(0);
        if ("automatic".equals(initialState.getMembership())) {
          node.setProperty("publication:currentState", initialState.getState());
        }
      }
    }

    /**
     * This default implementation checks if the state is valid then delegates
     * the update to the node WebpagePublicationPlugin.
     */
    public void updateLifecyleOnChangeContent(Node node, String siteName, String remoteUser, String newState) throws Exception {
	if (!publicationService.isNodeEnrolledInLifecycle(node)) {
	    enrollNodeInLifecycle(node, siteName, remoteUser);
	}
	String lifecycleName = publicationService.getNodeLifecycleName(node);
	WebpagePublicationPlugin publicationPlugin = this.getWebpagePublicationPlugins().get(lifecycleName);

	publicationPlugin.updateLifecyleOnChangeContent(node, remoteUser, newState);

	wcmComposer.updateContent(node.getSession().getRepository().toString(), node.getSession().getWorkspace().getName(), node.getPath(),
		new HashMap<String, String>());
    }
}
