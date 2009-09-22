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
import javax.jcr.NodeIterator;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.connector.BaseConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 10, 2008
 */
@URITemplate("/wcmDocument/")
public class DocumentConnector extends BaseConnector implements ResourceContainer {
  
  /** The document link handler. */
  protected DocumentLinkHandler documentLinkHandler;
  
  /** The limit. */
  private int limit;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(DocumentConnector.class);

  /**
   * Instantiates a new document connector.
   * 
   * @param container the container
   * @param param the param
   */
  public DocumentConnector(ExoContainer container, InitParams param) {
    super(container);
    documentLinkHandler = new DocumentLinkHandler(container);
    limit = Integer.parseInt(param.getValueParam("upload.limit.size").getValue());
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
   * @param currentPortal the current portal
   * @param baseURI the base uri
   * 
   * @return the folders and files
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("repositoryName") String repositoryName,
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("jcrPath") String jcrPath,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("currentPortal") String currentPortal,
      @QueryParam("command") String command,
      @QueryParam("type") String type,
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_BASE_URI) String baseURI) throws Exception {
    SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();
    try {
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider, repositoryName);
      Node activePortal = getCurrentPortalNode(repositoryName, jcrPath, currentPortal, null);
      if (sharedPortal.getPath().equals(activePortal.getPath())) {
        documentLinkHandler.setCurrentPortal(currentPortal);
      } else {
        documentLinkHandler.setCurrentPortal(activePortal.getName());
      }
      documentLinkHandler.setBaseURI(baseURI);
      Response response = buildXMLResponseOnExpand(currentFolder,
          currentPortal,
          workspaceName,
          repositoryName,
          jcrPath,
          command);
      if(response != null)
        return response;
    } catch (Exception e) {
      log.error("Error when perform getFoldersAndFiles: ", e.fillInStackTrace());
    } finally {
      sessionProvider.close();
    }    
    return Response.Builder.ok().build();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.BaseConnector#buildXMLResponseForContentStorage(javax.jcr.Node, java.lang.String)
   */
  protected Response buildXMLResponseForContentStorage(Node node, String command) throws Exception {
    Element rootElement = FCKUtils.createRootElement(command,
        node,
        folderHandler.getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Foders");
    Element files = document.createElement("Files");
    for (NodeIterator iterator = node.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      if (child.isNodeType(FCKUtils.EXO_HIDDENABLE))
        continue;
      String folderType = folderHandler.getFolderType(child);
      if (folderType != null) {
        Element folder = folderHandler.createFolderElement(document, child, folderType);
        folders.appendChild(folder);
      }
      String sourceType = getContentStorageType();
      String fileType = documentLinkHandler.getFileType(child, sourceType);
      if (fileType != null) {
        Element file = documentLinkHandler.createFileElement(document, child, fileType);
        files.appendChild(file);
      }
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
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
   * @param currentPortal the current portal
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/createFolder/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response createFolder(@QueryParam("repositoryName") String repositoryName,
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("jcrPath") String jcrPath,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("currentPortal") String currentPortal,
      @QueryParam("newFolderName") String newFolderName,
      @QueryParam("command") String command,
      @QueryParam("language") String language) throws Exception {
    try {
      Response response = buildXMLDocumentOnCreateFolder(newFolderName,
          currentFolder,
          currentPortal,
          jcrPath,
          repositoryName,
          workspaceName,
          command,
          language);
      if (response != null)        
        return response; 
    } catch (Exception e) {
      log.error("Error when perform createFolder: ", e.fillInStackTrace());
    }    
    return Response.Builder.ok().build();
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
   * @param currentPortal the current portal
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uploadFile/upload/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response uploadFile(InputStream inputStream,
      @QueryParam("repositoryName") String repositoryName,
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("currentPortal") String currentPortal,
      @QueryParam("jcrPath") String jcrPath,
      @QueryParam("uploadId") String uploadId,
      @QueryParam("language") String language,
      @HeaderParam("content-type") String contentType,
      @HeaderParam("content-length") String contentLength) throws Exception {
    try {
      return createUploadFileResponse(inputStream,
          repositoryName,
          workspaceName,
          currentFolder,
          currentPortal,
          jcrPath,
          uploadId,
          language,
          contentType,
          contentLength,
          limit); 
    } catch (Exception e) {
      log.error("Error when perform uploadFile: ", e.fillInStackTrace());
    }    
    return Response.Builder.serverError().build();
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
   * @param currentPortal the current portal
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/uploadFile/control/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response processUpload(@QueryParam("repositoryName") String repositoryName,
      @QueryParam("workspaceName") String workspaceName,
      @QueryParam("currentFolder") String currentFolder,
      @QueryParam("currentPortal") String currentPortal,
      @QueryParam("jcrPath") String jcrPath,
      @QueryParam("action") String action,
      @QueryParam("language") String language,
      @QueryParam("fileName") String fileName,
      @QueryParam("uploadId") String uploadId) throws Exception {
    try {
      return createProcessUploadResponse(repositoryName, workspaceName,currentFolder, currentPortal,
          jcrPath, action,language,fileName,uploadId);  
    } catch (Exception e) {
      log.error("Error when perform processUpload: ", e.fillInStackTrace());
    }
    return Response.Builder.serverError().build();
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.wcm.connector.fckeditor.BaseConnector#getRootContentStorage
   * (javax.jcr.Node)
   */
  @Override
  protected Node getRootContentStorage(Node parentNode) throws Exception {
    try {
      PortalFolderSchemaHandler folderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      return folderSchemaHandler.getDocumentStorage(parentNode);
    } catch (Exception e) {
      WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      return webContentSchemaHandler.getDocumentFolder(parentNode);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.wcm.connector.fckeditor.BaseConnector#getContentStorageType
   * ()
   */
  @Override
  protected String getContentStorageType() throws Exception {
    return FCKUtils.DOCUMENT_TYPE;
  }
}
