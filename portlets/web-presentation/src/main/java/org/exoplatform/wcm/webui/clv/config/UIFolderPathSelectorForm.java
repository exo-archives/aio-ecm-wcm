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
package org.exoplatform.wcm.webui.clv.config;

import java.io.Writer;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class, 
    events = @EventConfig(listeners = UIFolderPathSelectorForm.CloseActionListener.class)
)
public class UIFolderPathSelectorForm extends UIBaseNodeTreeSelector {
  public UIFolderPathSelectorForm() throws Exception {
    addChild(UIFolderPathTreeBuilder.class, null, UIFolderPathTreeBuilder.class.getSimpleName()
        + hashCode());
    addChild(UISelectFolderPathPanel.class, null, UISelectFolderPathPanel.class.getSimpleName()
        + hashCode());
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {    
    Writer writer = context.getWriter();
    String link = event(getComponentConfig().getEvents().get(0).getName());
    writer.write("<div id=\"UIFolderPathSelectorForm\" style=\"padding: 10px;\">");
    super.renderChildren();    
    writer.write( "<div class=\"UIAction\">");
    writer.write(   "<table class=\"ActionContainer\">");
    writer.write(   "<tr>");
    writer.write(   "<td>");
    writer.write(     "<div onclick=\"" + link + "\" class=\"ActionButton LightBlueStyle\" onmouseover=\"this.style.color = '#058ee6'\" onmouseout=\"this.style.color='black'\">");
    writer.write(       "<div class=\"ButtonLeft\">");
    writer.write(         "<div class=\"ButtonRight\">");
    writer.write(           "<div class=\"ButtonMiddle\">");
    writer.write(             "<a href=\"javascript:void(0);\">Close</a>");
    writer.write(           "</div>");
    writer.write(         "</div>");
    writer.write(       "</div>");
    writer.write(     "</div>");
    writer.write(   "</td>");
    writer.write(   "</tr>");
    writer.write(   "</table>");
    writer.write( "</div>");
    writer.write("</div>");
  }

  public void init() throws Exception {
    UIFolderPathTreeBuilder treeBuilder = getChild(UIFolderPathTreeBuilder.class);
    UISelectFolderPathPanel pathPanel = getChild(UISelectFolderPathPanel.class);
    String[] acceptNodeTypes = new String[] { "nt:folder", "nt:unstructured" };
    pathPanel.setAcceptedNodeTypes(acceptNodeTypes);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    provider.close();
  }

  public void onChange(Node node, Object val) throws Exception {
    UISelectFolderPathPanel selectPathPanel = getChild(UISelectFolderPathPanel.class);
    selectPathPanel.setParentNode(node);
  }

  public static class CloseActionListener extends EventListener<UIFolderPathSelectorForm> {
    public void execute(Event<UIFolderPathSelectorForm> event) throws Exception {
      UIFolderPathSelectorForm uiFolderPathSelectorForm = event.getSource();
      UIPortletConfig uiPortletConfig = uiFolderPathSelectorForm
          .getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.removeChild(UIPopupWindow.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortletConfig);
    }
  }

}
