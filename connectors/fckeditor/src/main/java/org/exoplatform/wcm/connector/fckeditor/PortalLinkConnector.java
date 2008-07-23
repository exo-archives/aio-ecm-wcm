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

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wcm.connector.fckeditor.portal.PageURIBuilder;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jul 11, 2008
 */

@URITemplate("/portalLinks/")
public class PortalLinkConnector implements ResourceContainer {
  private RepositoryService                 repositoryService;

  private ThreadLocalSessionProviderService sessionProviderService;

  private UserPortalConfigService           portalConfigService;

  private DataStorage                       portalDataStorage;

  private UserACL                           portalUserACL;

  private ConversationRegistry              conversationRegistry;

  public PortalLinkConnector(InitParams params, RepositoryService repositoryService,
      ThreadLocalSessionProviderService sessionProviderService,
      UserPortalConfigService portalConfigService, DataStorage dataStorage, UserACL userACL,
      ConversationRegistry conversationRegistry) throws Exception {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
    this.portalConfigService = portalConfigService;
    this.portalDataStorage = dataStorage;
    this.portalUserACL = userACL;
    this.conversationRegistry = conversationRegistry;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/pageURI/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getPageURI(@QueryParam("CurrentFolder")
  String currentFolder, @QueryParam("Command")
  String command, @QueryParam("Type")
  String type) throws Exception {
    PageURIBuilder builder = new PageURIBuilder(portalConfigService, portalDataStorage,
        portalUserACL);
    String userId = getCurrentUser();
    return builder.buildReponse(currentFolder, command, userId);
  }

  private String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    } catch (Exception e) {
    }
    return null;
  }
}
