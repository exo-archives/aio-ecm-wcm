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
package org.exoplatform.services.wcm.core;

import java.util.Collection;

import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.wcm.BaseWCMTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 20, 2009  
 */
public class TestWCMConfigurationService extends BaseWCMTestCase {

  private WCMConfigurationService configurationService;
  
  public void setUp() throws Exception {
    super.setUp();
    configurationService = getService(WCMConfigurationService.class);
  }
  
  public void testGetSiteDriveConfig() {
    DriveData driveData = configurationService.getSiteDriveConfig();
    assertEquals("{siteName}", driveData.getName());
    assertEquals("{repository}", driveData.getRepository());
    assertEquals("{workspace}", driveData.getWorkspace());
    assertEquals("{accessPermission}", driveData.getPermissions());
    assertEquals("{sitePath}/categories/{siteName}", driveData.getHomePath());
    assertEquals("", driveData.getIcon());
    assertEquals("wcm-category-view", driveData.getViews());
    assertFalse(driveData.getViewPreferences());
    assertTrue(driveData.getViewNonDocument());
    assertTrue(driveData.getViewSideBar());
    assertFalse(driveData.getShowHiddenNode());
    assertEquals("Both", driveData.getAllowCreateFolder());
  }
  
  public void testGetLivePortalsLocation() {
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation("repository");
    assertEquals("repository", nodeLocation.getRepository());
    assertEquals("collaboration", nodeLocation.getWorkspace());
    assertEquals("/sites content/live", nodeLocation.getPath());
  }
  
  public void testGetRuntimeContextParam() {
    assertEquals("redactor", configurationService.getRuntimeContextParam(WCMConfigurationService.REDACTOR_MEMBERSHIP_TYPE));
    assertEquals("/parameterizedviewer", configurationService.getRuntimeContextParam(WCMConfigurationService.PARAMETERIZED_PAGE_URI));
    assertEquals("/printviewer", configurationService.getRuntimeContextParam(WCMConfigurationService.PRINT_PAGE_URI));
    assertEquals("printviewer", configurationService.getRuntimeContextParam(WCMConfigurationService.PRINT_VIEWER_PAGE));
    assertEquals("wikipresentation", configurationService.getRuntimeContextParam(WCMConfigurationService.CREATE_WIKI_PAGE_URI));
    assertEquals("/presentation/ContentListViewerPortlet", configurationService.getRuntimeContextParam(WCMConfigurationService.CLV_PORTLET));
    assertEquals("/presentation/SingleContentViewer", configurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET));
    assertEquals("/exo:ecm/views/templates/Content List Viewer/list-by-folder/UIContentListPresentationDefault.gtmpl", configurationService.getRuntimeContextParam(WCMConfigurationService.FORM_VIEW_TEMPLATE_PATH));
    assertEquals("/exo:ecm/views/templates/Content List Viewer/paginators/UIPaginatorDefault.gtmpl", configurationService.getRuntimeContextParam(WCMConfigurationService.PAGINATOR_TEMPLAET_PATH));
  }
  
  public void testGetRuntimeContextParams() {
    Collection<String> runtimeContextParams = configurationService.getRuntimeContextParams();
    assertTrue(runtimeContextParams.contains("redactor"));
    assertTrue(runtimeContextParams.contains("/parameterizedviewer"));
    assertTrue(runtimeContextParams.contains("/printviewer"));
    assertTrue(runtimeContextParams.contains("printviewer"));
    assertTrue(runtimeContextParams.contains("wikipresentation"));
    assertTrue(runtimeContextParams.contains("/presentation/ContentListViewerPortlet"));
    assertTrue(runtimeContextParams.contains("/presentation/SingleContentViewer"));
    assertTrue(runtimeContextParams.contains("/exo:ecm/views/templates/Content List Viewer/list-by-folder/UIContentListPresentationDefault.gtmpl"));
    assertTrue(runtimeContextParams.contains("/exo:ecm/views/templates/Content List Viewer/paginators/UIPaginatorDefault.gtmpl"));
    assertEquals(9, runtimeContextParams.size());
  }
  
  public void testGetSharedPortalName() {
    assertEquals("shared", configurationService.getSharedPortalName("repository"));
  }
  
  public void testGetAllLivePortalsLocation() {
    Collection<NodeLocation> nodeLocations = configurationService.getAllLivePortalsLocation();
    assertEquals(1, nodeLocations.size());
  }
  
}
