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

import javax.jcr.Node;
import javax.jcr.NodeIterator;

/**
 * This class is responsible of getting contents inside the WCM product.
 * We shouldn't access directly contents from the jcr on front side.
 * 
 * In a general manner, this service stands between publication and cache.
 * 
 * @author Benjamin Paillereau - benjamin.paillereau@exoplatform.com
 * @version 1.0
 *
 */
public interface WCMComposer {
	
	/**
	 * Filter parameter to filter results by state.
	 * ex : draft, staged, published
	 */
	public final static String FILTER_STATE = "filter-state";
	/**
	 * Filter parameter to filter results by primary type.
	 * ex: exo:article, exo:webContent
	 */
	public final static String FILTER_PRIMARY_TYPE = "filter-primary-type";
	/**
	 * Filter parameter to order results.
	 * ex: exo:title, dc:title
	 */
	public final static String FILTER_ORDER_BY = "filter-order-by";
	/**
	 * Filter parameter to order results in ascendant order or descendant order.
	 * values : ASC, DESC
	 */
	public final static String FILTER_ORDER_TYPE = "filter-order-type";
	/**
	 * Filter parameter to filter results by target mode.
	 * ex: editing, approving, live
	 */
	public final static String FILTER_MODE = "filter-mode";
	public final static String MODE_EDIT = "Edit";
	public final static String MODE_LIVE = "Live";
	/**
	 * Filter parameter to filter results by site.
	 * ex: classic
	 */
	public final static String FILTER_SITE_NAME = "filter-site";
	/**
	 * Filter parameter to filter results by user. We will return only contents authored by this user.
	 * ex: 
	 */
	public final static String FILTER_REMOTE_USER = "filter-remote-user";
	
	/**
	 * returns content at the specified path based on filters
	 * 
	 * @param repository
	 * @param workspace
	 * @param path
	 * @param filters
	 * @return a jcr node
	 */
	public Node getContent(String repository, String workspace, String path, HashMap<String, String> filters) ;

	/**
	 * returns contents at the specified path based on filters
	 * 
	 * @param repository
	 * @param workspace
	 * @param path
	 * @param filters
	 * @return a jcr node
	 */
	public NodeIterator getContents(String repository, String workspace, String path, HashMap<String, String> filters) throws Exception ;

	public boolean updateContent(String repository, String workspace, String path, HashMap<String, String> filters);
	
	public boolean updateContents(String repository, String workspace, String path, HashMap<String, String> filters);
}
