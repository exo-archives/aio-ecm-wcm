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
package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 26, 2009  
 */
public class NewsletterTemplateHandler {

  private static Log log = ExoLogger.getLogger(NewsletterTemplateHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  private List<Node> dialogs;
  
  public NewsletterTemplateHandler(String repository, String workspace) {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  public List<Node> getDialogs() {
    log.info("Trying to get dialog's template for exo:webContent");
    try {
      List<Node> dialogs = new ArrayList<Node>();
      NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      String templateHomePath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_TEMPLATES_PATH);
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = threadLocalSessionProviderService.getSessionProvider(null).getSession(workspace, manageableRepository);
      Node templateHomeNode = (Node) session.getItem(templateHomePath);
      Node nodetypeNode = templateHomeNode.getNode("exo:webContent");
      NodeIterator dialogNodeIterator = nodetypeNode.getNode("dialogs").getNodes();
      while (dialogNodeIterator.hasNext()) {
        dialogs.add(dialogNodeIterator.nextNode());
      }
      this.dialogs = dialogs;
      return dialogs;
    } catch (Exception e) {
      log.error("Get dialog's template for exo:webContent failed because of " + e.getMessage());
    }
    return null;
  }
  
  public Node getDialog(String dialogName) {
    log.info("Trying to get dialog " + dialogName);
    try {
      if (dialogs == null) dialogs = getDialogs();
      for (Node dialog : dialogs) {
        if (dialogName.equals(dialog.getName())) {
          return dialog;
        }
      }
    } catch (Exception e) {
      log.error("Get dialog " + dialogName + " failed because of " + e.getMessage());
    }
    return null;
  }
  
  public void convertAsTemplate(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
}
