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
package org.exoplatform.services.wcm.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 12, 2008  
 */
@URITemplate("/wcmNavigation/")
public class RESTNavigationService implements ResourceContainer{

  private UserPortalConfigService portalConfigService;     
  private LocaleConfigService localeConfigService;
  public RESTNavigationService(UserPortalConfigService portalConfigService, LocaleConfigService localeConfigService) throws Exception {
    this.portalConfigService = portalConfigService;        
    this.localeConfigService = localeConfigService;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getPortalNavigations/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getNavigations(@QueryParam("portalName")String portalName, 
      @QueryParam("language") String language) throws Exception {
    String userId = getCurrentUserId();    
    if(language == null) 
      language = Locale.ENGLISH.getLanguage();
    List<PageNavigation> navigations = portalConfigService.getUserPortalConfig(portalName, userId).getNavigations();
    LocaleConfig localeConfig = localeConfigService.getLocaleConfig(language);
    for(PageNavigation nav : navigations) {
      if(nav.getOwnerType().equals(PortalConfig.USER_TYPE)) continue ;
      ResourceBundle res = localeConfig.getNavigationResourceBundle(nav.getOwnerType(), nav.getOwnerId()) ;
      for(PageNode node : nav.getNodes()) {
        resolveLabel(res, node) ;
      }
    }
    PortalNavigation portalNavigation = new PortalNavigation(navigations);    
    return Response.Builder.ok(portalNavigation).mediaType("text/xml").build();
  }     
  
  //TODO need retrieve current user from REST in REST 2.0
  private String getCurrentUserId() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    } catch (Exception e) {
    }
    return null;
  }

  private void resolveLabel(ResourceBundle res, PageNode node) {
    node.setResolvedLabel(res) ;
    if(node.getChildren() == null) return;
    for(PageNode childNode : node.getChildren()) {
      resolveLabel(res, childNode) ;
    }
  }

  public static class PortalNavigation {
    private List<PageNavigation> navigations = new ArrayList<PageNavigation>();
    public PortalNavigation(List<PageNavigation> list) {
      this.navigations = list;
    }    
    public List<PageNavigation> getNavigations() { return this.navigations; }
    public void setNavigations(List<PageNavigation> list) { this.navigations = list; }
  }      
}
