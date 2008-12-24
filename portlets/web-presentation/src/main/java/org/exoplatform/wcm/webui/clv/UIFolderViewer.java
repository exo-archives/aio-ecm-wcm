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
package org.exoplatform.wcm.webui.clv;

import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.clv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UIFolderViewer.
 */
@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/groovy/ContentListViewer/UIFolderListViewer.gtmpl", events = { @EventConfig(listeners = UIFolderViewer.QuickEditActionListener.class) })
public class UIFolderViewer extends UIContainer implements RefreshDelegateActionListener {

  /** The can view list content. */
  private boolean canViewListContent;

  /** The message key. */
  private String  messageKey;

  /**
   * Instantiates a new uI folder viewer.
   * 
   * @throws Exception the exception
   */
  public UIFolderViewer() throws Exception {
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    NodeIterator nodeIterator = null;
    canViewListContent = true;
    messageKey = null;
    try {
      nodeIterator = getRenderedContentNodes();
    } catch (ItemNotFoundException e) {
      messageKey = "UIMessageBoard.msg.folder-not-found";
      canViewListContent = false;
      return;
    } catch (AccessDeniedException e) {
      messageKey = "UIMessageBoard.msg.no-permission";
      canViewListContent = false;
      return;
    } catch (Exception e) {
      messageKey = "UIMessageBoard.msg.error-nodetype";
      canViewListContent = false;
      return;
    }
    if (nodeIterator.getSize() == 0) {
      messageKey = "UIMessageBoard.msg.folder-empty";      
    }
    PortletPreferences portletPreferences = getPortletPreference();
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE,
                                                                    null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodeIterator,
                                                                            itemsPerPage);
    UIContentListPresentation contentListPresentation = addChild(UIContentListPresentation.class,
                                                                 null,
                                                                 null);
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();
    contentListPresentation.init(templatePath, resourceResolver, paginatedNodeIterator);
    contentListPresentation.setContentColumn(portletPreferences.getValue(UIContentListViewerPortlet.HEADER,
                                                                         null));
    contentListPresentation.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIContentListViewerPortlet.SHOW_HEADER,
                                                                                           null)));
    contentListPresentation.setHeader(portletPreferences.getValue(UIContentListViewerPortlet.HEADER,
                                                                  null));
  }

  /**
   * Gets the message.
   * 
   * @return the message
   * 
   * @throws Exception the exception
   */
  public String getMessage() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();    
    return requestContext.getApplicationResourceBundle().getString(messageKey);
  }

  /**
   * Can view list content.
   * 
   * @return true, if successful
   */
  public boolean canViewListContent() {
    return canViewListContent;
  }

  /**
   * Gets the portlet preference.
   * 
   * @return the portlet preference
   */
  protected PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  /**
   * Gets the repository.
   * 
   * @return the repository
   */
  protected String getRepository() {
    return getPortletPreference().getValue(UIContentListViewerPortlet.REPOSITORY, null);
  }

  /**
   * Gets the form view template path.
   * 
   * @return the form view template path
   */
  protected String getFormViewTemplatePath() {
    return getPortletPreference().getValue(UIContentListViewerPortlet.FORM_VIEW_TEMPLATE_PATH, null);
  }

  /**
   * Gets the rendered content nodes.
   * 
   * @return the rendered content nodes
   * 
   * @throws Exception the exception
   */
  public NodeIterator getRenderedContentNodes() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UIContentListViewerPortlet.REPOSITORY, null);
    String worksapce = preferences.getValue(UIContentListViewerPortlet.WORKSPACE, null);
    String folderPath = preferences.getValue(UIContentListViewerPortlet.FOLDER_PATH, null);
    if (repository == null || worksapce == null || folderPath == null)
      throw new ItemNotFoundException();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    repositoryService.getCurrentRepository();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    SessionProvider sessionProvider = null;
    if (userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    } else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    Session session = sessionProvider.getSession(worksapce, manageableRepository);
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> listDocumentTypes = templateService.getDocumentTemplates(repository);
    StringBuffer documentTypeClause = new StringBuffer();
    for (int i = 0; i < listDocumentTypes.size(); i++) {
      String documentType = listDocumentTypes.get(i);
      documentTypeClause.append("jcr:primaryType = '" + documentType + "'");
      if (i != (listDocumentTypes.size() - 1)) {
        documentTypeClause.append(" OR ");
      }
    }
    QueryManager manager = session.getWorkspace().getQueryManager();
    String statement = "select * from nt:base where jcr:path like '" + folderPath
        + "/%' AND NOT jcr:path like'" + folderPath + "/%/%'" + " AND( "
        + documentTypeClause.toString() + ")";
    Query query = manager.createQuery(statement, Query.SQL);
    return query.execute().getNodes();
  }

  /**
   * Gets the template resource resolver.
   * 
   * @return the template resource resolver
   * 
   * @throws Exception the exception
   */
  private ResourceResolver getTemplateResourceResolver() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = getRepository();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  /**
   * Gets the portlet id.
   * 
   * @return the portlet id
   */
  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }

  /**
   * Checks if is quick editable.
   * 
   * @return true, if is quick editable
   * 
   * @throws Exception the exception
   */
  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue(UIContentListViewerPortlet.SHOW_QUICK_EDIT_BUTTON,
                                                              null));
    UIContentListViewerPortlet uiPresentationPortlet = getAncestorOfType(UIContentListViewerPortlet.class);
    if (isQuickEdit)
      return uiPresentationPortlet.canEditPortlet();
    return false;
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
  public static class QuickEditActionListener extends EventListener<UIFolderViewer> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIFolderViewer> event) throws Exception {
      UIFolderViewer uiFolderViewer = event.getSource();
      UIContentListViewerPortlet uiListViewerPortlet = uiFolderViewer.getAncestorOfType(UIContentListViewerPortlet.class);
      UIPopupContainer uiMaskPopupContainer = uiListViewerPortlet.getChild(UIPopupContainer.class);
      UIPortletConfig uiPortletConfig = uiMaskPopupContainer.createUIComponent(UIPortletConfig.class,
                                                                               null,
                                                                               null);
      uiFolderViewer.addChild(uiPortletConfig);
      uiPortletConfig.setRendered(true);
      uiMaskPopupContainer.activate(uiPortletConfig, 700, -1);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.addUIComponentToUpdateByAjax(uiMaskPopupContainer);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.webui.clv.RefreshDelegateActionListener#onRefresh(org.exoplatform.webui.event.Event)
   */
  public void onRefresh(Event<UIContentListPresentation> event) throws Exception {
    UIContentListPresentation contentListPresentation = event.getSource();
    UIFolderViewer uiFolderViewer = contentListPresentation.getParent();
    uiFolderViewer.getChildren().clear();
    uiFolderViewer.init();
  }

}
