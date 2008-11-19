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
package org.exoplatform.services.wcm.organization;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Nov 19, 2008
 */
@URITemplate("/organization/")
public class RESTOrganizationService implements ResourceContainer{
  
  /** The organization service. */
  private OrganizationService organizationService;
  
  /**
   * Instantiates a new rEST organization service.
   * 
   * @param organizationService the organization service
   */
  public RESTOrganizationService(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }
  
  /**
   * Gets the current user id.
   * 
   * @return the current user id
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getCurrentUserID/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getCurrentUserId() {
    String userId = "null";
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      userId =  conversationState.getIdentity().getUserId();
    } catch (Exception e) {
    }    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(userId).mediaType("text/plain").cacheControl(cacheControl).build();
  }

  /**
   * Gets the user profile.
   * 
   * @param userId the user id
   * 
   * @return the user profile
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getUserProfile/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUserProfile(@QueryParam("userId")String userId ) throws Exception {       
    UserProfileHandler profileHandler = organizationService.getUserProfileHandler();       
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    try {
      UserProfile userProfile = profileHandler.findUserProfileByName(userId);      
      return Response.Builder.ok(userProfile).mediaType("text/xml").cacheControl(cacheControl).build();
    } catch (Exception e) {
    }            
    return Response.Builder.badRequest().cacheControl(cacheControl).build();
  }
  
  /**
   * Gets the account.
   * 
   * @param userId the user id
   * 
   * @return the account
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getAccount/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getAccount(@QueryParam("userId")String userId ) throws Exception {           
    UserHandler userHandler = organizationService.getUserHandler();    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    try {
      User user = userHandler.findUserByName(userId);      
      return Response.Builder.ok(user).mediaType("text/xml").cacheControl(cacheControl).build();
    } catch (Exception e) {
    }            
    return Response.Builder.badRequest().cacheControl(cacheControl).build();
  }
}
