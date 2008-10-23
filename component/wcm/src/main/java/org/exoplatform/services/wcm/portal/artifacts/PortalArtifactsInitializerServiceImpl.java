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
package org.exoplatform.services.wcm.portal.artifacts;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Oct 22, 2008  
 */
public class PortalArtifactsInitializerServiceImpl implements PortalArtifactsInitializerService {

  private HashMap<String,BasePortalArtifactsPlugin> artifactPlugins = new HashMap<String,BasePortalArtifactsPlugin>();
  private ArrayList<String> initialPortals = new ArrayList<String>();

  public PortalArtifactsInitializerServiceImpl(InitParams initParams) {     
    ValuesParam valuesParam = initParams.getValuesParam("ignored.portals");
    if(valuesParam != null) {
      initialPortals = valuesParam.getValues();
    }
  }
  public void addPlugin(BasePortalArtifactsPlugin artifactsPlugin) throws Exception {
    artifactPlugins.put(artifactsPlugin.getName(),artifactsPlugin);
  }

  public void deployArtifactsToPortal(String portalName, SessionProvider sessionProvider)
  throws Exception {
    //Do not initalize portal artifact for predefined portal
    if(initialPortals.contains(portalName)) return ;

    for(BasePortalArtifactsPlugin plugin: artifactPlugins.values()) {
      plugin.deployToPortal(portalName,sessionProvider);
    }
  }

}
