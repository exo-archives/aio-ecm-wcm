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
import javax.portlet.PortletMode;
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
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(
    lifecycle = Lifecycle.class, 
    template = "app:/groovy/ContentListViewer/UIFolderListViewer.gtmpl",
    events = { @EventConfig(listeners = UIFolderViewer.QuickEditActionListener.class) }
)
public class UIFolderViewer extends UIContainer implements RefreshDelegateActionListener {

  private boolean canViewListContent;

  private String  messageKey;

  public UIFolderViewer() throws Exception {
  }

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
      canViewListContent = false;
      return;
    }
    PortletPreferences portletPreferences = getPortletPreference();
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(
        UIContentListViewerPortlet.ITEMS_PER_PAGE, UIContentListViewerPortlet.ITEMS_PER_PAGE));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodeIterator,
        itemsPerPage);
    UIContentListPresentation contentListPresentation = addChild(UIContentListPresentation.class,
        null, null);
    String templatePath = getTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();
    contentListPresentation.init(templatePath, resourceResolver, paginatedNodeIterator);
  }

  public String getMessage() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    return requestContext.getApplicationResourceBundle().getString(messageKey);
  }

  public boolean canViewListContent() {
    return canViewListContent;
  }

  protected PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  protected String getRepository() {
    return getPortletPreference().getValue(UIContentListViewerPortlet.REPOSITORY,
        UIContentListViewerPortlet.REPOSITORY);
  }

  protected String getTemplatePath() {
    return getPortletPreference().getValue(UIContentListViewerPortlet.TEMPLATE_PATH,
        UIContentListViewerPortlet.TEMPLATE_PATH);
  }

  public NodeIterator getRenderedContentNodes() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UIContentListViewerPortlet.REPOSITORY,
        UIContentListViewerPortlet.REPOSITORY);
    String worksapce = preferences.getValue(UIContentListViewerPortlet.WORKSPACE,
        UIContentListViewerPortlet.WORKSPACE);
    String folderPath = preferences.getValue(UIContentListViewerPortlet.FOLDER_PATH,
        UIContentListViewerPortlet.FOLDER_PATH);
    if (repository == null || worksapce == null || folderPath == null)
      throw new ItemNotFoundException();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
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

  private ResourceResolver getTemplateResourceResolver() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = getRepository();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    return pContext.getWindowId();
  }

  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("showQuickEdit", null));
    UIContentListViewerPortlet uiPresentationPortlet = getAncestorOfType(UIContentListViewerPortlet.class);
    if (isQuickEdit)
      return uiPresentationPortlet.canEditPortlet();
    return false;
  }

  public static class QuickEditActionListener extends EventListener<UIFolderViewer> {
    public void execute(Event<UIFolderViewer> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }

  public void onRefresh(Event<UIContentListPresentation> event) throws Exception {
    UIContentListPresentation contentListPresentation = event.getSource();
    UIFolderViewer uiFolderViewer = contentListPresentation.getParent();
    uiFolderViewer.getChildren().clear();
    uiFolderViewer.init();
  }

}
