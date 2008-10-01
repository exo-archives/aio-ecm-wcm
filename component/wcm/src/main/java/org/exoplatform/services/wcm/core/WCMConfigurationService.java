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

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;


/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 20, 2008  
 */
public class WCMConfigurationService {
  
  private static Log log = ExoLogger.getLogger("wcm:WCMConfiguarationService");
  private HashMap<String, NodeLocation> livePortalsLocations = new HashMap<String, NodeLocation>();
  private HashMap<String, String> sharedPortals = new HashMap<String, String>();
  private String parameterizedPageURI;
  private String publishingPortletName;
  private String managedSitesContentDrive;  
  public WCMConfigurationService(InitParams initParams) throws Exception{
    parameterizedPageURI = initParams.getValueParam("parameterizedPageURI").getValue();
    log.info("Page URI is used for view DMS Document as a web page: " + parameterizedPageURI);
    publishingPortletName = initParams.getValueParam("publishingPortletName").getValue();
    log.info("The portlet is used to publish content in a web page: " + publishingPortletName);
    managedSitesContentDrive = initParams.getValueParam("managedSitesContentDrive").getValue();
    log.info("Main drive to manage sites content " + managedSitesContentDrive);
    Iterator<PropertiesParam> iterator = initParams.getPropertiesParamIterator();
    for (; iterator.hasNext(); ) {
      PropertiesParam param = iterator.next();
      if ("share.portal.config".endsWith(param.getName())) {
        String repository = param.getProperty("repository");
        String portalName = param.getProperty("portalName");
        sharedPortals.put(repository, portalName);
        log.info("Name of shared portal to share resources for all portals in repository: "+ repository + " is: "+ portalName);
      }
    }
    Iterator<ObjectParameter> locations = initParams.getObjectParamIterator();
    for (; locations.hasNext(); ) {
      ObjectParameter objectParameter = locations.next();
      if ("live.portals.location.config".equals(objectParameter.getName())) {
        NodeLocation objectParam = (NodeLocation)objectParameter.getObject();
        livePortalsLocations.put(objectParam.getRepository(), objectParam);
        log.info("Location that resources for all live portal is stored in repository:" + objectParam.getRepository() 
            + " is in workspace: "+ objectParam.getWorkspace() + " and with path: "+objectParam.getPath());
         
      }
    }
  }
  
  public String getParameterizedPageURI() { return this.parameterizedPageURI; }
  public String getPublishingPortletName() { return this.publishingPortletName; }
  public String getManagedSitesContentDriveName() {return this.managedSitesContentDrive; }
  public NodeLocation getLivePortalsLocation(final String repository) {
    return livePortalsLocations.get(repository);
  }
//TODO
  public String getSharedPortalName(final String repository) {
    return sharedPortals.get(repository);
  }
  
  public Collection<NodeLocation> getAllLivePortalsLocation() {
    return livePortalsLocations.values();
  }  
}
