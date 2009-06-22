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
package org.exoplatform.wcm.webui.category;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 19, 2009  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UICategoryNavigationPortlet extends UIPortletApplication {

  public UICategoryNavigationPortlet() throws Exception {
    addChild(UICategoryNavigationContainer.class, null, null); 
    
//    UIOneTaxonomySelector uiOneTaxonomySelector = createUIComponent(UIOneTaxonomySelector.class, null, null);
//    String workspaceName = "dms-system";
//    String repositoryName = "repository";
//    uiOneTaxonomySelector.setIsDisable(workspaceName, false);
//    NodeHierarchyCreator nodeHierarchyCreator = getApplicationComponent(NodeHierarchyCreator.class);
//    String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
//    
//    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;
//    ManageableRepository repository = repositoryService.getRepository(repositoryName);
//    ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
//    SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
//    Session session = sessionProvider.getSession(workspaceName, repository);
//    Node rootTree = (Node) session.getItem(rootTreePath);      
//    NodeIterator childrenIterator = rootTree.getNodes();
//    while (childrenIterator.hasNext()) {
//      Node childNode = childrenIterator.nextNode();
//      rootTreePath = childNode.getPath();
//      break;
//    }
//    uiOneTaxonomySelector.setRootNodeLocation(repositoryName, workspaceName, rootTreePath);
//    uiOneTaxonomySelector.init(sessionProvider);
//    addChild(uiOneTaxonomySelector);
  }
  
}
