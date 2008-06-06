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
package org.exoplatform.connector.fckeditor;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.connector.fckeditor.portal.PageURIBuilder;
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
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 22, 2008  
 */
@URITemplate("/fckconnectorext/")
public class RESTResourcesConnector implements ResourceContainer {

  private RepositoryService repositoryService_ ;
  private ThreadLocalSessionProviderService sessionProviderService_ ;  
  private UserPortalConfigService portalConfigService_ ;
  private DataStorage portalDataStorage_ ;
  private UserACL portalUserACL_ ; 
  private ConversationRegistry conversationRegistry_ ; ;

  public RESTResourcesConnector(InitParams params, RepositoryService repositoryService,ThreadLocalSessionProviderService sessionProviderService,
      UserPortalConfigService portalConfigService, DataStorage dataStorage, UserACL userACL, ConversationRegistry conversationRegistry) throws Exception {
    this.repositoryService_ = repositoryService ;
    this.sessionProviderService_ = sessionProviderService;
    this.portalConfigService_ = portalConfigService ;
    this.portalDataStorage_ = dataStorage ;
    this.portalUserACL_ = userACL ;            
    this.conversationRegistry_ = conversationRegistry ;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/images/")
  public void getImages(@URIParam("location")String location,@URIParam("Type") String type, @URIParam("currentFolder") String currentFolder) {
    new Exception().printStackTrace();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/documents/")
  public void getDocuments() {    
    new Exception().printStackTrace() ;
  }  

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/pageURI/")    
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getPageURI(@QueryParam("CurrentFolder") String currentFolder, @QueryParam("Command") String command,@QueryParam("Type") String type) throws Exception {    
    PageURIBuilder builder = new PageURIBuilder(portalConfigService_,portalDataStorage_,portalUserACL_) ;
    String userId = getCurrentUser() ;
    System.out.println("============>"+userId);
    return builder.buildReponse(currentFolder, command, userId) ;
  }   
  
  private String getCurrentUser() {    
    try{
      ConversationState conversationState = ConversationState.getCurrent() ;      
      return conversationState.getIdentity().getUserId() ;
    }catch (Exception e) {
    }
    return null ;
  }
}
