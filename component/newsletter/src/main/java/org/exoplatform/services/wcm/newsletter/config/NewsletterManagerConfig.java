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
package org.exoplatform.services.wcm.newsletter.config;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 9, 2009  
 */
public class NewsletterManagerConfig {

  private String newsletterName;
  
  private String newsletterTitle;
  
  private Date newsletterSentDate;
  
  private String status;
  
  private String subcriptionName;

  public String getSubcriptionName() {
    return subcriptionName;
  }

  public void setSubcriptionName(String subcriptionName) {
    this.subcriptionName = subcriptionName;
  }

  public String getNewsletterName() {
    return newsletterName;
  }

  public Date getNewsletterSentDate() {
    return newsletterSentDate;
  }

  public void setNewsletterSentDate(Date newsletterSentDate) {
    this.newsletterSentDate = newsletterSentDate;
  }

  public void setNewsletterName(String newsletterName) {
    this.newsletterName = newsletterName;
  }

  public String getNewsletterTitle() {
    return newsletterTitle;
  }

  public void setNewsletterTitle(String newsletterTitle) {
    this.newsletterTitle = newsletterTitle;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
