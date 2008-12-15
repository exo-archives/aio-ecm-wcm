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
package org.exoplatform.wcm.webui.selector.document;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Sep 3, 2008  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIDocumentPathSelector extends UIBaseNodeTreeSelector implements UIPopupComponent{

  public UIDocumentPathSelector() throws Exception {
    addChild(UIDocumentTreeBuilder.class, null, UIDocumentTreeBuilder.class.getSimpleName() + hashCode());
    addChild(UISelectPathPanel.class, null, UISelectPathPanel.class.getSimpleName() + hashCode());
  }

  public void init() throws Exception {
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, sessionProvider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
    String repositoryName = ((ManageableRepository)(currentPortal.getSession().getRepository())).getConfiguration().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> acceptedNodeTypes = templateService.getDocumentTemplates(repositoryName);
    UISelectPathPanel uiSelectPathPanel = getChild(UISelectPathPanel.class);
    String [] arrAcceptedNodeTypes = new String[acceptedNodeTypes.size()];
    acceptedNodeTypes.toArray(arrAcceptedNodeTypes) ;
    uiSelectPathPanel.setAcceptedNodeTypes(arrAcceptedNodeTypes);
    UIDocumentTreeBuilder treeBuilder = getChild(UIDocumentTreeBuilder.class);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    sessionProvider.close();
  }

  @Override
  public void onChange(Node node, Object context) throws Exception {
    UISelectPathPanel uiSelectPathPanel = getChild(UISelectPathPanel.class);
    uiSelectPathPanel.setParentNode(node);
    uiSelectPathPanel.updateGrid();
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
}
