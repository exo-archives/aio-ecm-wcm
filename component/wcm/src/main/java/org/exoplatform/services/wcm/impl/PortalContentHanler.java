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

  private final String NT_FOLDER = "nt:folder".intern();

  protected String getFileType() { return "exo:portalFolder"; }

  protected String getFolderPathExpression() { return null; }

  protected String getFolderType() { return "nt:unstructured"; }

  public String handle(Node portalFolder) throws Exception {
    //TODO hard coded here
    Session session = portalFolder.getSession();
    portalFolder.addNode("html","exo:htmlFolder") ;    
    portalFolder.addNode("js","exo:jsFolder") ;    
    portalFolder.addNode("css","exo:cssFolder") ;    
    Node multimedia = portalFolder.addNode("multimedia",NT_FOLDER);
    multimedia.addMixin("exo:multimediaFolder");
    Node images = multimedia.addNode("images",NT_FOLDER);
    images.addMixin("exo:pictureFolder");
    Node video = multimedia.addNode("videos",NT_FOLDER);
    video.addMixin("exo:videoFolder");
    Node audio = multimedia.addNode("audio",NT_FOLDER);
    audio.addMixin("exo:musicFolder");    
    Node document = portalFolder.addNode("Structured Document",NT_FOLDER) ;
    document.addMixin("exo:documentFolder");    
    session.save();
    return portalFolder.getPath();
  }  

}
