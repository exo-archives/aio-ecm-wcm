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
package org.exoplatform.wcm.webui.pcv;

import javax.jcr.Node;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform
 * SAS Author : Phan Le Thanh Chuong
 * chuong.phan@exoplatform.com,
 * phan.le.thanh.chuong@gmail.com Nov 4, 2008
 */

@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/groovy/ParameterizedContentViewer/UIContentViewerContainer.gtmpl", events = { @EventConfig(listeners = UIContentViewerContainer.QuickEditActionListener.class) })
public class UIContentViewerContainer extends UIContainer {

  /** The Constant WEB_CONTENT_sDIALOG. */
  public static final String WEB_CONTENT_sDIALOG = "webContentDialog";

  /**
   * Instantiates a new uI content viewer container.
   * 
   * @throws Exception the exception
   */
  public UIContentViewerContainer() throws Exception {
    addChild(UIContentViewer.class, null, null);
  }

  /**
   * Checks if is quick edit able.
   * 
   * @return true, if is quick edit able
   * 
   * @throws Exception the exception
   */
  public boolean isQuickEditAble() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();    
    String userId = context.getRemoteUser();
    return Utils.canEditCurrentPortal(userId);
  }

  /**
   * The listener interface for receiving quickEditAction events.
   * The class that is interested in processing a quickEditAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addQuickEditActionListener<code> method. When
   * the quickEditAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see QuickEditActionEvent
   */
  public static class QuickEditActionListener extends EventListener<UIContentViewerContainer> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentViewerContainer> event) throws Exception {
      UIContentViewerContainer uiContentViewerContainer = event.getSource();
      UIContentViewer uiContentViewer = uiContentViewerContainer.getChild(UIContentViewer.class);
      Node contentNode = uiContentViewer.getNode();
      ManageableRepository manageableRepository = (ManageableRepository) contentNode.getSession()
                                                                                    .getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      uiContentViewerContainer.removeChild(UIContentViewer.class);
      UIDocumentDialogForm uiDocumentForm = uiContentViewerContainer.createUIComponent(UIDocumentDialogForm.class,
                                                                                       null,
                                                                                       null);
      uiDocumentForm.setRepositoryName(repository);
      uiDocumentForm.setWorkspace(workspace);
      uiDocumentForm.setContentType(contentNode.getPrimaryNodeType().getName());
      uiDocumentForm.setNodePath(contentNode.getPath());
      uiDocumentForm.setStoredPath(contentNode.getPath());
      uiDocumentForm.addNew(false);
      uiContentViewerContainer.addChild(uiDocumentForm);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContentViewerContainer);
    }
  }

}
