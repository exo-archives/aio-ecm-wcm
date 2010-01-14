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
package org.exoplatform.wcm.benchmark.preparation.organization;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Dec 2, 2008
 */
public class OrganizationCreationService implements Startable{

  /** The user name prefix. */
  private String userNamePrefix;
  
  /** The common password. */
  private String commonPassword;
  
  /** The number of users. */
  private int numberOfUsers;
  
  /** The organization service. */
  private OrganizationService organizationService;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger("lab:organization");

  /**
   * Instantiates a new organization creation service.
   * 
   * @param initParams the init params
   * @param organizationService the organization service
   */
  public OrganizationCreationService(InitParams initParams, OrganizationService organizationService) {
    userNamePrefix = initParams.getValueParam("userNamePrefix").getValue();
    numberOfUsers = Integer.parseInt(initParams.getValueParam("numberOfUsers").getValue());
    commonPassword = initParams.getValueParam("commonPassword").getValue();
    this.organizationService = organizationService;
  }

  /**
   * Creates the users.
   */
  private void createUsers() {
    log.info("Start create users for organization with: "+ numberOfUsers + " users, users name prefix: " + userNamePrefix + " and common password is: "+ commonPassword);
    long start = System.currentTimeMillis();
    UserHandler handler = organizationService.getUserHandler();    
    for(int i = 1; i<numberOfUsers ; i++ ) {
      String userName = userNamePrefix + Integer.toString(i);
      String email = userName.concat("@exoplatform.com");
      try {        
        org.exoplatform.services.organization.User user = handler.createUserInstance(userName);
        user.setEmail(email);
        user.setPassword(commonPassword);
        user.setFullName(userName);
        user.setFirstName(userName);
        user.setLastName("exo");
        handler.createUser(user,true);
      } catch (Exception e) {
        log.error("Error when create user" + userName, e.fillInStackTrace());
      }           
    }
    long creationTime = System.currentTimeMillis() - start;
    log.info("Create " + numberOfUsers + " users in " + creationTime/1000 + "s");
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {    
    createUsers();
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {    
  }
}
