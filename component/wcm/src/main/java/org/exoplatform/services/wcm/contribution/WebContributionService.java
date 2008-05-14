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
package org.exoplatform.services.wcm.contribution;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 13, 2008  
 */
public class WebContributionService {

  private String contributorGroup_ ;
  private AuthenticationService authService_ ;

  public WebContributionService(AuthenticationService authService ,InitParams initParams) {
    contributorGroup_ = initParams.getPropertiesParam("service.params").getProperty("web.contributor.group") ;
    this.authService_ = authService ;
  }

  public boolean hasContributionPermission(String userId) {
    //TODO should use PermissionManagerService form new ecm component when it finish
    Identity identity = authService_.getCurrentIdentity() ;
    if(identity != null && identity.getUsername().equalsIgnoreCase(userId)) 
      return identity.isInGroup(contributorGroup_) ;
    return false ;    
  }
  
}
