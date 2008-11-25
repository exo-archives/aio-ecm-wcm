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
package org.exoplatform.services.wcm.skin;

import java.io.Reader;
import java.io.StringReader;

import org.exoplatform.portal.webui.skin.Resource;
import org.exoplatform.portal.webui.skin.ResourceResolver;
import org.exoplatform.portal.webui.skin.SkinConfig;
import org.exoplatform.portal.webui.skin.SkinService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Nov 25, 2008  
 */
public class WCMSkinResourceResolver implements ResourceResolver{  
  private SkinService skinService;

  public WCMSkinResourceResolver(SkinService skinService) {
    this.skinService = skinService;
  }

  public Resource resolve(String path) {
    if(!path.matches(XSkinService.SKIN_PATH_REGEXP)) return null;
    String[] elements = path.split("/");
    String portalName = elements[4];
    String skinName = elements[5];
    String skinModule = portalName;
    String cssPath = null;    
    SkinConfig portalSkinConfig = skinService.getSkin(portalName,skinName);
    if(portalSkinConfig != null) {
      cssPath = portalSkinConfig.getCSSPath();
    }    
    //get css for shared portal if the portalName is shared Portal
    if(cssPath == null) {
      for(SkinConfig skinConfig: skinService.getPortalSkins(skinName)) {
        if(skinConfig.getModule().equals(skinModule)) {
          cssPath = skinConfig.getCSSPath();
          break;        
        }
      }  
    }
    final String cssData = skinService.getCSS(cssPath);
    if(cssData == null) 
      return null;     
    return new Resource(path) {
      public Reader read() {
        return new StringReader(cssData);
      }
    };            
  }
}
