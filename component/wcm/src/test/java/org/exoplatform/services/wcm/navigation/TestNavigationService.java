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
package org.exoplatform.services.wcm.navigation;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.wcm.BaseWCMTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 22, 2009  
 */
public class TestNavigationService extends BaseWCMTestCase {

  private NavigationService navigationService;
  
  public void setUp() throws Exception {
    super.setUp();
    navigationService = getService(NavigationService.class);
  }
  
  public void testGetNavigationsAsJSON() throws Exception {
    PageNode pageNode = new PageNode();
    pageNode.setPageReference("portal::classic::testpage");
    pageNode.setName("testpage");
    pageNode.setUri("testpage");
    
    PageNavigation pageNavigation = new PageNavigation();
    pageNavigation.setOwnerType(PortalConfig.USER_TYPE);
    pageNavigation.setOwnerId("root");
    
    List<PageNavigation> pageNavigations = new ArrayList<PageNavigation>();
    pageNavigations.add(pageNavigation);
    String jsonResult = navigationService.getNavigationsAsJSON(pageNavigations);
    
    String jsonExpert = "[" +
    		                  "{" +
    		                    "\"modifier\":" + pageNavigation.getModifier() + "," +
    		                    "\"id\":" + pageNavigation.getId() + "," +
    		                    "\"storageName\":" + pageNavigation.getStorageName() + "," +
    		                    "\"ownerId\":\"" + pageNavigation.getOwnerId() + "\"," +
    		                    "\"nodes\":" + pageNavigation.getNodes().toString() + "," +
    		                    "\"description\":" + pageNavigation.getDescription() + "," +
    		                    "\"priority\":" + pageNavigation.getPriority() + "," +
    		                    "\"ownerType\":\"" + pageNavigation.getOwnerType() + "\"," +
    		                    "\"owner\":\"" + pageNavigation.getOwner() + "\"," +
    		                    "\"storageId\":" + pageNavigation.getStorageId() + "," +
    		                  	"\"creator\":" + pageNavigation.getCreator() +
    		                  "}" +
    		                "]";
    assertEquals(jsonExpert, jsonResult);
  }
}
