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
package org.exoplatform.wcm.webui.clv.config;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelectPanel;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationState;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : anh.do anh.do@exoplatform.com,
 * anhdn86@gmail.com Feb 16, 2009
 */

@ComponentConfig(
  template = "app:/groovy/ContentListViewer/config/UIContentSelectionPanel.gtmpl", 
  events = { 
    @EventConfig(listeners = UIMultiSelectionPanel.SelectActionListener.class)
  }
)
public class UIMultiSelectionPanel extends UICategoriesSelectPanel {

  public UIMultiSelectionPanel() throws Exception {
    super();
  }

  public List<Node> getListSelectableNodes() throws Exception {
    List<Node> list = new ArrayList<Node>();
    Node parentNode = getParentNode();
    if (parentNode == null)
      return list;
    for (NodeIterator iterator = parentNode.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      if (child.isNodeType("exo:hiddenable"))
        continue;
      if (StageAndVersionPublicationState.OBSOLETE.equals(StageAndVersionPublicationState.getRevisionState(child))) continue;
      if (isDocType(child)) {
        list.add(child);
      }
    }
    return list;
  }

  public boolean isDocType(Node node) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = repositoryService.getCurrentRepository().getConfiguration().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> listDocumentTypes = templateService.getDocumentTemplates(repository);
    if (listDocumentTypes.contains(node.getPrimaryNodeType().getName()))
      return true;
    return false;
  }

  public static class SelectActionListener extends EventListener<UIMultiSelectionPanel> {
    public void execute(Event<UIMultiSelectionPanel> event) throws Exception {
      UIMultiSelectionPanel uiDefault = event.getSource();
      UICorrectContentSelectorForm uiOneContentSelectorForm = uiDefault.getParent();
      UISelectedContentGrid uiSelectedContentGrid = uiOneContentSelectorForm.getChild(UISelectedContentGrid.class);
      String value = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!uiSelectedContentGrid.getSelectedCategories().contains(value)) {
        uiSelectedContentGrid.addCategory(value);
      }
      uiSelectedContentGrid.updateGrid(uiSelectedContentGrid.getUIPageIterator().getCurrentPage());
      uiSelectedContentGrid.setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiOneContentSelectorForm);
    }
  }
}
