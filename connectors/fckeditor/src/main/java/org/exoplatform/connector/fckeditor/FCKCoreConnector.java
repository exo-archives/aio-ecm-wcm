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
package org.exoplatform.connector.fckeditor;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 3, 2008  
 */
@URITemplate("/fckconnector/")
public class FCKCoreConnector implements ResourceContainer {
  
  private final String GET_FILES = "GetFiles".intern();  
  private final String GET_FOLDERS = "FetFolders".intern();       
  
  public FCKCoreConnector() { }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getResource/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getResource( @QueryParam("repositoryName") String repositoryName, @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Command") String command,
      @QueryParam("Type") String type) throws Exception {
    if(currentFolder == null || currentFolder.length() == 0) {
      currentFolder = "/" ;
    }
    repositoryName = "repository" ;
    workspaceName = "collaboration" ;    
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = new FCKConnectorXMLOutputBuilder(ExoContainerContext.getCurrentContainer()) ;
    Document document = null ;
    if(GET_FOLDERS.equalsIgnoreCase(command)) {
      document = connectorXMLOutputBuilder.buildFoldersXMLOutput(repositoryName, workspaceName, currentFolder) ;
    }else if(GET_FILES.equals(command)) {
      document = connectorXMLOutputBuilder.buildFilesXMLOutput(repositoryName, workspaceName, currentFolder) ;
    }else {
      document = connectorXMLOutputBuilder.buildFoldersAndFilesXMLOutput(repositoryName, workspaceName, currentFolder) ;
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true) ;
    return Response.Builder.ok(document).cacheControl(cacheControl).build() ;
  }  

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/createFolder/")
  public void createFolder() {        
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uploadFile/")
  public void uploadFile() {    
  }
  
}
