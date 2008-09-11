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
package org.exoplatform.wcm.connector.fckeditor;

import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
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
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS 
 * Author : Anh Do Ngoc 
 * anh.do@exoplatform.com
 * Sep 10, 2008
 */

/**
 * The Class DocumentConnector.
 */
@URITemplate("/wcmDocument/")
public class DocumentConnector extends BaseConnector implements ResourceContainer {

  /**
   * Instantiates a new document connector.
   * 
   * @param container the container
   */
  public DocumentConnector(ExoContainer container) {
    super(container);
  }

  /**
   * Gets the folders and files.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * 
   * @return the folders and files
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("repositoryName")
  String repositoryName, @QueryParam("workspaceName")
  String workspaceName, @QueryParam("jcrPath")
  String jcrPath, @QueryParam("currentFolder")
  String currentFolder, @QueryParam("command")
  String command, @QueryParam("type")
  String type) throws Exception {
    Response response = buildXMLResponseOnExpand(currentFolder, workspaceName, repositoryName,
        jcrPath, command);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }

  /**
   * Creates the folder.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @param currentFolder the current folder
   * @param newFolderName the new folder name
   * @param command the command
   * @param language the language
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/createFolder/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response createFolder(@QueryParam("repositoryName")
  String repositoryName, @QueryParam("workspaceName")
  String workspaceName, @QueryParam("jcrPath")
  String jcrPath, @QueryParam("currentFolder")
  String currentFolder, @QueryParam("newFolderName")
  String newFolderName, @QueryParam("command")
  String command, @QueryParam("language")
  String language) throws Exception {
    Response response = buildXMLDocumentOnCreateFolder(newFolderName, currentFolder, jcrPath,
        repositoryName, workspaceName, command, language);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }

  /**
   * Upload file.
   * 
   * @param inputStream the input stream
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param uploadId the upload id
   * @param language the language
   * @param contentType the content type
   * @param contentLength the content length
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uploadFile/upload/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response uploadFile(InputStream inputStream, @QueryParam("repositoryName")
  String repositoryName, @QueryParam("workspaceName")
  String workspaceName, @QueryParam("currentFolder")
  String currentFolder, @QueryParam("jcrPath")
  String jcrPath, @QueryParam("uploadId")
  String uploadId, @QueryParam("language")
  String language, @HeaderParam("content-type")
  String contentType, @HeaderParam("content-length")
  String contentLength) throws Exception {
    return createUploadFileResponse(inputStream, repositoryName, workspaceName, currentFolder,
        jcrPath, uploadId, language, contentType, contentLength);
  }

  /**
   * Process upload.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param action the action
   * @param language the language
   * @param fileName the file name
   * @param uploadId the upload id
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/uploadFile/control/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response processUpload(@QueryParam("repositoryName")
  String repositoryName, @QueryParam("workspaceName")
  String workspaceName, @QueryParam("currentFolder")
  String currentFolder, @QueryParam("jcrPath")
  String jcrPath, @QueryParam("action")
  String action, @QueryParam("language")
  String language, @QueryParam("fileName")
  String fileName, @QueryParam("uploadId")
  String uploadId) throws Exception {
    return createProcessUploadResponse(repositoryName, workspaceName, currentFolder, jcrPath,
        action, language, fileName, uploadId);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getRootContentStorage(javax.jcr.Node)
   */
  @Override
  protected Node getRootContentStorage(Node parentNode) throws Exception {
    try {
      PortalFolderSchemaHandler folderSchemaHandler = webSchemaConfigService
          .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      return folderSchemaHandler.getDocumentStorage(parentNode);
    } catch (Exception e) {
      WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService
          .getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      return webContentSchemaHandler.getDocumentFolder(parentNode);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getContentStorageType()
   */
  @Override
  protected String getContentStorageType() throws Exception {
    return FCKUtils.DOCUMENT_TYPE;
  }

}
