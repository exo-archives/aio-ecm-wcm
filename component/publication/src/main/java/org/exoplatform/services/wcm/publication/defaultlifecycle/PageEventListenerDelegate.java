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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.model.Page;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 6, 2008  
 */
public class PageEventListenerDelegate {

  private String lifecycleName;
  private ExoContainer container;
  public PageEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
    this.container = container;
  }

  public void updateLifecyleOnCreatePage(Page page) throws Exception {
  }

  public void updateLifecyleOnChangePage(Page page) throws Exception { }

  public void updateLifecycleOnRemovePage(Page page) throws Exception { }

}
