/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.wcm.core;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.services.listener.ListenerService;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jan 21, 2010  
 * TODO: This class is temporary, we HAVE TO remove it after update to next version of GateIn
 */
public class DataStorageImpl extends org.exoplatform.portal.config.DataStorageImpl {

  public final static String CREATE_PORTAL_EVENT = "UserPortalConfigService.portal.onCreate".intern();

  public final static String REMOVE_PAGE_EVENT = "UserPortalConfigService.portal.onRemove".intern();
  
  private ListenerService listenerService;
  
  public DataStorageImpl(ModelDataStorage delegate,
                         ListenerService listenerService) {
    super(delegate);
    this.listenerService = listenerService;
  }

  public void create(PortalConfig config) throws Exception {
    super.create(config);
    listenerService.broadcast(CREATE_PORTAL_EVENT, this, config);
  }
  
  public void remove(PortalConfig config) throws Exception {
    super.remove(config);
    listenerService.broadcast(REMOVE_PAGE_EVENT, this, config);
  }
  
}
