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
package org.exoplatform.wcm.webui.fastcontentcreator;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 25, 2009  
 */
public class UIFastContentCreatorUtils {

  public static PortletPreferences getPortletPreferences() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletRequest request = portletRequestContext.getRequest();
    return request.getPreferences();
  }
  
  public static String getPreferenceRepository() {
    return getPortletPreferences().getValue(UIFastContentCreatorConstant.PREFERENCE_REPOSITORY, "");
  }

  public static String getPreferenceWorkspace() {
    return getPortletPreferences().getValue(UIFastContentCreatorConstant.PREFERENCE_WORKSPACE, "");
  }
  
  public static String getPreferenceType() {
    return getPortletPreferences().getValue(UIFastContentCreatorConstant.PREFERENCE_TYPE, "");
  }
  
  public static String getPreferencePath() {
    return getPortletPreferences().getValue(UIFastContentCreatorConstant.PREFERENCE_PATH, "");
  }
  
}
