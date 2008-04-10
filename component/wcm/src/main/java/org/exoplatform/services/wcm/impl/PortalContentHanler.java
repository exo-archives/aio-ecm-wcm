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

package org.exoplatform.services.wcm.impl;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.wcm.BaseWebContentHandler;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 10, 2008  
 */
public class PortalContentHanler extends BaseWebContentHandler {  
  
  protected String getContentType() { return "exo:portalFolder"; }
  protected String getFolderPathExpression() { return null; }
  protected String getFolderType() { return NT_UNSTRUCTURED;  }

  public String handle(Node portalFolder) throws Exception {
    //TODO hard coded here
    Session session = portalFolder.getSession();    
    Node jsFolder = portalFolder.addNode("js","exo:jsFolder") ;    
    addMixin(jsFolder,EXO_OWNABLE) ;
    Node cssFolder = portalFolder.addNode("css","exo:cssFolder") ;    
    addMixin(cssFolder, EXO_OWNABLE);
    Node multimedia = portalFolder.addNode("multimedia",NT_FOLDER);    
    addMixin(multimedia, "exo:multimediaFolder") ;
    addMixin(multimedia, EXO_OWNABLE) ;    
    Node images = multimedia.addNode("images",NT_FOLDER);    
    addMixin(images, "exo:pictureFolder") ;
    addMixin(images, EXO_OWNABLE);
    Node video = multimedia.addNode("videos",NT_FOLDER);
    addMixin(video, "exo:videoFolder") ;    
    addMixin(video, EXO_OWNABLE) ;
    Node audio = multimedia.addNode("audio",NT_FOLDER);    
    addMixin(audio, "exo:musicFolder") ;
    addMixin(audio, EXO_OWNABLE) ;
    Node document = portalFolder.addNode("documents",NT_UNSTRUCTURED) ;
    addMixin(document, "exo:documentFolder") ;
    addMixin(document, EXO_OWNABLE) ;
    Node webFolder = portalFolder.addNode("html","exo:webFolder") ;    
    addMixin(webFolder, EXO_OWNABLE) ;
    session.save();
    return portalFolder.getPath();
  }  

}
