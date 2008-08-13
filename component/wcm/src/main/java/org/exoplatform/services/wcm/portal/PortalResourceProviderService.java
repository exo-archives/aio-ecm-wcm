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
package org.exoplatform.services.wcm.portal;

import javax.jcr.Node;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.wcm.skin.XSkinService;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
@URITemplate("/resources/")
public class PortalResourceProviderService implements ResourceContainer {

  private LivePortalManagerService livePortalManagerService;
  private XSkinService skinService;
  private ThreadLocalSessionProviderService sessionProviderService;

  public PortalResourceProviderService(LivePortalManagerService livePortalManagerService, XSkinService skinService, ThreadLocalSessionProviderService sessionProviderService) {
    this.livePortalManagerService = livePortalManagerService;
    this.skinService = skinService;
    this.sessionProviderService = sessionProviderService;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/{portalName}/javascript/")
  public Response getPortalJavaScript() {
    return null;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/{portalName}/css/Stylesheet.css/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getPortalCSS(@URIParam("portalName") String portalName) throws Exception{
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Node portal = livePortalManagerService.getLivePortal(portalName,sessionProvider);
    String css = skinService.getActiveStylesheet(portal);    
    return Response.Builder.ok(css).mediaType("text/css").build();
  }
}
