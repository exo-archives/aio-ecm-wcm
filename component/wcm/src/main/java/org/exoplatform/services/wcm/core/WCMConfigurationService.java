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
package org.exoplatform.services.wcm.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.PropertiesParam;


/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 20, 2008  
 */
public class WCMConfigurationService {

  private HashMap<String, NodeLocation> livePortalsLocations = new HashMap<String, NodeLocation>();
  private HashMap<String, String> sharedPortals = new HashMap<String, String>();

  public WCMConfigurationService(InitParams initParams) {
    Iterator<PropertiesParam> iterator = initParams.getPropertiesParamIterator();
    for (; iterator.hasNext(); ) {
      PropertiesParam param = iterator.next();
      if ("share.portal.config".endsWith(param.getName())) {
        sharedPortals.put(param.getProperty("repository"), param.getProperty("portalName"));
      }
    }
    Iterator<ObjectParameter> locations = initParams.getObjectParamIterator();
    for (; locations.hasNext(); ) {
      ObjectParameter objectParameter = locations.next();
      if ("live.portals.location.config".equals(objectParameter.getName())) {
        NodeLocation objectParam = (NodeLocation)objectParameter.getObject();
        livePortalsLocations.put(objectParam.getRepository(), objectParam);
      }
    }
  }

  public NodeLocation getLivePortalsLocation(final String repository) {
    return livePortalsLocations.get(repository);
  }

  public String getSharedPortalName(final String repository) {
    return sharedPortals.get(repository);
  }
  
  public Collection<NodeLocation> getAllLivePortalsLocation() {
    return livePortalsLocations.values();
  }  
}
