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

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.publication.PublicationState;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UIFolderViewer.
 */

@ComponentConfig(      
  lifecycle = Lifecycle.class,                 
   template = "app:/groovy/ContentListViewer/UIContentListViewer.gtmpl",
   events = { 
     @EventConfig(listeners = UIFolderViewer.QuickEditActionListener.class) 
   }
)
public class UIFolderViewer extends UIListViewerBase {

  public void init() throws Exception {
    PortletPreferences portletPreferences = getPortletPreference();    
    NodeIterator nodeIterator = null;
    setViewAbleContent(true);
    messageKey = null;
    try {
      nodeIterator = getRenderedContentNodes();
    } catch (ItemNotFoundException e) {
      messageKey = "UIMessageBoard.msg.folder-not-found";
      setViewAbleContent(false);
      return;
    } catch (AccessDeniedException e) {
      messageKey = "UIMessageBoard.msg.no-permission";
      setViewAbleContent(false);
      return;
    } catch (Exception e) {
      messageKey = "UIMessageBoard.msg.error-nodetype";
      setViewAbleContent(false);
      return;
    }
    if (nodeIterator.getSize() == 0) {
      messageKey = "UIMessageBoard.msg.folder-empty";
      setViewAbleContent(false);
      return;
    }
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodeIterator, itemsPerPage);
    UIContentListPresentation contentListPresentation = addChild(UIContentListPresentation.class, null, null);
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();    
    contentListPresentation.init(templatePath, resourceResolver, paginatedNodeIterator);    
    contentListPresentation.setContentColumn(portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null));
    contentListPresentation.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIContentListViewerPortlet.SHOW_HEADER, null)));
    contentListPresentation.setHeader(portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null));
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {   
    super.processRender(context);
  }
  
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
    String orderQuery = " ORDER BY ";
    String orderBy = preferences.getValue(UIContentListViewerPortlet.ORDER_BY, null);
    String orderType = preferences.getValue(UIContentListViewerPortlet.ORDER_TYPE, null);
    if (orderType == null) orderType = "DESC";
    if (orderBy == null) orderBy = "exo:title";
    orderQuery += orderBy + " " + orderType;
    String statement = "select * from nt:base where jcr:path like '" + folderPath
    + "/%' AND NOT jcr:path like'" + folderPath + "/%/%'" + " AND( "
    + documentTypeClause.toString() + ")" + orderQuery;
    if (Utils.isLiveMode()) {
      statement = "select * from nt:base where jcr:path like '" + folderPath
      + "/%' AND NOT jcr:path like'" + folderPath + "/%/%'" + " AND( "
      + documentTypeClause.toString() + ") AND publication:liveRevision IS NOT NULL AND publication:liveRevision <> '' " + orderQuery;
    }  
    Query query = manager.createQuery(statement, Query.SQL);
    return query.execute().getNodes();
  }
    
}
