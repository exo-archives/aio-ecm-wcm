/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.newsletter.manager;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;

/**
 * The Class NewsLetterUtil.
 */
public class NewsLetterUtil {
	
	/**
	 * Gets the portal name.
	 * 
	 * @return the portal name
	 */
	public static String getPortalName() {
		UIPortal portal = Util.getUIPortal();
		return portal.getName();  
	}
	
	/**
	 * Generate link.
	 * 
	 * @param url the url
	 * 
	 * @return the string
	 */
	public static String generateLink(String url){
    String link = url.replaceFirst("Subcribe", "ConfirmUserCode")
                      .replaceFirst("UINewsletterViewerForm", "UINewsletterViewerPortlet")
                      .replaceAll("&amp;", "&");
    String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
    String portalName = "/" + Util.getUIPortal().getName() ;
    if(link.indexOf(portalName) > 0) {
      if(link.indexOf(portalName + "/" + selectedNode) < 0){
        link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
      }                 
    } 
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    url = portalContext.getRequest().getRequestURL().toString();
    url = url.replaceFirst("http://", "") ;
    url = url.substring(0, url.indexOf("/")) ;
    link = "http://" + url + link;
    return link.replaceFirst("private", "public");
	}
	
	static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
}
