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
package org.exoplatform.services.wcm.newsletter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 22, 2009  
 */
public class NewsletterCategoryConfig {
  private String name;
  private String description;
  private String moderator;
  private List<NewsletterSubscriptionConfig> subscriptions = new ArrayList<NewsletterSubscriptionConfig>();
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  
  public String getModerator() { return moderator; }
  public void setModerator(String moderator) { this.moderator = moderator; }
  
  public List<NewsletterSubscriptionConfig> getSubscriptions() { return subscriptions; }
  public void setSubscriptions(List<NewsletterSubscriptionConfig> subscriptions) { this.subscriptions = subscriptions; }    

  public static class NewsletterSubscriptionConfig {
    private String name;
    private String description;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }

}