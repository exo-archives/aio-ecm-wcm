/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.wcm.newsletter.handler;

import java.util.List;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterManageUserHandler {

  private static Log log = ExoLogger.getLogger(NewsletterManageUserHandler.class);
  private RepositoryService repositoryService;
  private ThreadLocalSessionProviderService threadLocalSessionProviderService;
  private String repository;
  private String workspace;
  
  public NewsletterManageUserHandler(String repository, String workspace) {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    threadLocalSessionProviderService = ThreadLocalSessionProviderService.class.cast(ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class));
    this.repository = repository;
    this.workspace = workspace;
  }
  
  public void add(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Create new user node with the node name is the email
      // Set the property "exo:newsletterUserMail" is the email
      // Set the property "exo:newsletterUserBanned" is false
      // Add the uuid of this user's node to the subscription to reference
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
  public void edit(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
  public void ban(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Update the property "exo:newsletterUserBanned" of user's node to true
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
  public void delete(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Remove all reference of this user to subscription
      // Remove user's node
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  public List<String> getUsersBySubscription(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Get all users of the subscription by reference 
    } catch (Exception e) {
      // TODO: handle exception
    }
    return null;
  }
  
  public List<String> getUsersByCategory(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Get all subscriptions of the category
      // call getUsersBySubscription()
    } catch (Exception e) {
      // TODO: handle exception
    }
    return null;
  }
  
  public int countUsersBySubscription(SessionProvider sessionProvider) {
    try {
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      // TODO: Needs to implement
      // Count all users of the subscription
    } catch (Exception e) {
      // TODO: handle exception
    }
    return 0;
  }
  
}
