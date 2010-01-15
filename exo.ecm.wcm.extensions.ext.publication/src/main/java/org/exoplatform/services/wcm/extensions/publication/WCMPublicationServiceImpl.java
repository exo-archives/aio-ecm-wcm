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

import java.util.TreeSet;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.wcm.extensions.publication.context.impl.ContextConfig.Context;
import org.exoplatform.services.wcm.extensions.publication.impl.PublicationManagerImpl;
import org.exoplatform.services.wcm.extensions.utils.ContextComparator;

public class WCMPublicationServiceImpl extends org.exoplatform.services.wcm.publication.WCMPublicationServiceImpl {

    /**
     * Instantiates a new WCM publication service. This service delegate to
     * PublicationService to manage the publication
     * 
     * @param publicationService
     *            the publication service
     */
    public WCMPublicationServiceImpl() {
	super();
    }

    /**
     * This default implementation uses "States and versions based publication"
     * as a default lifecycle for all sites and "Simple Publishing" for the root
     * user.
     */
    public void enrollNodeInLifecycle(Node node, String siteName, String remoteUser) throws Exception {
	ExoContainer container = ExoContainerContext.getCurrentContainer();
	PublicationManagerImpl publicationManagerImpl = (PublicationManagerImpl) container.getComponentInstanceOfType(PublicationManager.class);
	ContextComparator comparator = new ContextComparator();
	TreeSet<Context> treeSetContext = new TreeSet<Context>(comparator);
	treeSetContext.addAll(publicationManagerImpl.getContexts());
	for (Context context : treeSetContext) {
	    boolean pathVerified = true;
	    boolean nodetypeVerified = true;
	    boolean siteVerified = true;
	    String path = context.getPath();
	    String nodetype = context.getNodetype();
	    String site = context.getSite();
	    String membership = context.getMembership();
	    if (path != null)
		pathVerified = node.getPath().contains(path);
	    if (nodetype != null)
		nodetypeVerified = nodetype.equals(node.getPrimaryNodeType().getName());
	    if (site != null)
		siteVerified = site.equals(siteName);
	    if (pathVerified && nodetypeVerified && siteVerified) {
		// @TODO
	    }
	    // @TODO
	}

    }
}
