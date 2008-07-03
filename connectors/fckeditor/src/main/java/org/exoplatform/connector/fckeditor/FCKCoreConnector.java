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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.upload.UploadService;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 3, 2008  
 */
@URITemplate("/fckconnector/jcr/")
public class FCKCoreConnector implements ResourceContainer {

  private static final String GET_FILES = "GetFiles".intern();  
  private static final String GET_FOLDERS = "GetFolders".intern();       
  private static final String RESOURCE_AS_FILE = "File".intern();
  private static final String RESOURCE_AS_IMAGES = "Image".intern();

  protected UploadService uploadService;

  public FCKCoreConnector(UploadService uploadService) { 
    this.uploadService = uploadService;
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getResource/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getResource(@QueryParam("repositoryName") String repositoryName, @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Command") String command,
      @QueryParam("Type") String type) throws Exception {
    if (currentFolder == null || currentFolder.length() == 0) currentFolder = "/";       
    repositoryName = "repository";
    workspaceName = "collaboration";
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = null;
    if (type == null || RESOURCE_AS_FILE.equals(type)) {
      connectorXMLOutputBuilder = new FilesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    } else if (RESOURCE_AS_IMAGES.equals(type)) {
      connectorXMLOutputBuilder = new ImagesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    }
    Document document = null;
    if (GET_FOLDERS.equalsIgnoreCase(command)) {
      document = connectorXMLOutputBuilder.buildFoldersXMLOutput(repositoryName, workspaceName, currentFolder);
    } else if (GET_FILES.equals(command)) {
      document = connectorXMLOutputBuilder.buildFilesXMLOutput(repositoryName, workspaceName, currentFolder);
    } else {
      document = connectorXMLOutputBuilder.buildFoldersAndFilesXMLOutput(repositoryName, workspaceName, currentFolder);
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("repositoryName") String repositoryName, @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type) throws Exception {
    if (currentFolder == null || currentFolder.length() == 0) {
      currentFolder = "/";
    }
    repositoryName = "repository";
    workspaceName = "collaboration";
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = null;
    if (type == null || RESOURCE_AS_FILE.equals(type)) {
      connectorXMLOutputBuilder = new FilesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    } else if (RESOURCE_AS_IMAGES.equals(type)) {
      connectorXMLOutputBuilder = new ImagesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    }
    Document document = connectorXMLOutputBuilder.buildFoldersAndFilesXMLOutput(repositoryName, workspaceName, currentFolder);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFiles/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFiles(@QueryParam("repositoryName") String repositoryName, @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type) throws Exception {
    if (currentFolder == null || currentFolder.length() == 0) currentFolder = "/";       
    repositoryName = "repository";
    workspaceName = "collaboration";
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = null;
    if (type == null || RESOURCE_AS_FILE.equals(type)) {
      connectorXMLOutputBuilder = new FilesXMLOutputBuilder(ExoContainerContext.getCurrentContainer()); 
    } else if (RESOURCE_AS_IMAGES.equals(type)) {
      connectorXMLOutputBuilder = new ImagesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    }    
    Document document = connectorXMLOutputBuilder.buildFilesXMLOutput(repositoryName, workspaceName, currentFolder);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFolders/")  
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFolders(@QueryParam("repositoryName") String repositoryName, @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type) throws Exception {
    if (currentFolder == null || currentFolder.length() == 0) currentFolder = "/";    
    repositoryName = "repository";
    workspaceName = "collaboration";
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = null;
    if (type == null || RESOURCE_AS_FILE.equals(type)) {
      connectorXMLOutputBuilder = new FilesXMLOutputBuilder(ExoContainerContext.getCurrentContainer()); 
    } else if (RESOURCE_AS_IMAGES.equals(type)) {
      connectorXMLOutputBuilder = new ImagesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    }    
    Document document = connectorXMLOutputBuilder.buildFoldersXMLOutput(repositoryName, workspaceName, currentFolder);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).mediaType("text/xml").build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/createFolder/")
  @OutputTransformer(XMLOutputTransformer.class) 
  public Response createFolder(
      @QueryParam("repositoryName") String repositoryName, 
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("CurrentFolder") String currentFolder, 
      @QueryParam("Type") String type, 
      @QueryParam("NewFolderName") String newFolderName) throws Exception {
    if (currentFolder == null || currentFolder.length() == 0) currentFolder = "/";    
    repositoryName = "repository";
    workspaceName = "collaboration";
    FCKConnectorXMLOutputBuilder connectorXMLOutputBuilder = null;
    if (type == null || RESOURCE_AS_FILE.equals(type)) {
      connectorXMLOutputBuilder = new FilesXMLOutputBuilder(ExoContainerContext.getCurrentContainer()); 
    } else if (RESOURCE_AS_IMAGES.equals(type)) {
      connectorXMLOutputBuilder = new ImagesXMLOutputBuilder(ExoContainerContext.getCurrentContainer());
    }    
    Document document = connectorXMLOutputBuilder.buildFoldersXMLOutput(repositoryName, workspaceName, currentFolder, newFolderName);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/upload/") 
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)  
  public Response uploadFile(
      InputStream inputStream,
      @QueryParam("action") String action, 
      @QueryParam("uploadId") String uploadId,
      @HeaderParam("content-length") String contentLength, 
      @HeaderParam("content-type") String contentType) throws Exception {        
    Document document = null;            
    FileUploadHandler fileUploadHelper = new FileUploadHandler(uploadService);      
    if (FileUploadHandler.UPLOAD_ACTION.equals(action)) {      
      double length = Double.parseDouble(contentLength);
      document = fileUploadHelper.upload(uploadId, inputStream, "UTF-8", length);      
    } else if (FileUploadHandler.PROGRESS_ACTION.equals(action)) {
      document = fileUploadHelper.refreshProgress(uploadId);
    } else if (FileUploadHandler.ABORT_ACTION.equals(action)) {
      fileUploadHelper.abort(uploadId);
    } else if (FileUploadHandler.DELETE_ACTION.endsWith(action)) {
      fileUploadHelper.delete(uploadId);
    } else if (FileUploadHandler.SAVE_ACTION.equals(action)) {
      //get current node by current folder
      Node currentNode = null;
      fileUploadHelper.saveAsNTFile(uploadId,currentNode);
    }        
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);    
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }
}
