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
package org.exoplatform.wcm.connector.fckeditor;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.WCMConfigurationService;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 26, 2008
 */
public class DocumentLinkHandler extends FCKFileHandler {

  private String baseURI;

  private String currentPortal;

  public DocumentLinkHandler(ExoContainer container) {
    super(container);
  }

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  public String getFileURL(final Node node) throws Exception {
    // baseURI:http://localhost:8080/portal/rest
    String accessMode = "private";
    AccessControlList acl = ((ExtendedNode) node).getACL();
    for (AccessControlEntry entry : acl.getPermissionEntries()) {
      if (entry.getIdentity().equalsIgnoreCase(SystemIdentity.ANY)
          && entry.getPermission().equalsIgnoreCase(PermissionType.READ)) {
        accessMode = "public";
        break;
      }
    }
    WCMConfigurationService configurationService = (WCMConfigurationService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(WCMConfigurationService.class);
    // pageURI: /contentviewer
    String parameterizedPageViewerURI = configurationService.getParameterizedPageURI();
    String repository = ((ManageableRepository) node.getSession().getRepository())
        .getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String nodePath = node.getPath();
    return baseURI.replace("/rest", "") + "/" + accessMode + "/" + currentPortal + parameterizedPageViewerURI + "/"
        + repository + "/" + workspace + nodePath;
  }

  public void setCurrentPortal(String currentPortal) {
    this.currentPortal = currentPortal;
  }

  public String getCurrentPortal() {
    return this.currentPortal;
  }

}
