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
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham
 * hoa.pham@exoplatform.com
 * May 13, 2008
 */
public class WebContributionService {

  /** The user in contributor group can contribute web content 
   * in some special way like quick edit in some web presentation portlet.
   */
  private String contributorGroup;

  /** The conversation registry. */
  private ConversationRegistry conversationRegistry;

  /**
   * Instantiates a new web contribution service.
   * 
   * @param initParams the init params
   * @param conversationRegistry the conversation registry service
   */
  public WebContributionService(ConversationRegistry registry, InitParams initParams) {
    contributorGroup = initParams.getPropertiesParam("service.params")
    .getProperty("web.contributor.group");
    this.conversationRegistry = registry;
  }

  /**
   * Checks for contribution role.
   *
   * @param userId the user id
   *
   * @return true, if userId has contribution role
   */
  public final boolean hasContributionPermission(final String userId) {
    ConversationState conversationState = conversationRegistry.getState(userId);
    Identity identity = conversationState.getIdentity();
    if (identity != null) {
      MembershipEntry entry = MembershipEntry.parse(contributorGroup);
      return identity.isMemberOf(entry);
    }
    return false;
  }
}
