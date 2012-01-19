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
package org.exoplatform.services.wcm.images;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.version.VersionHistory;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.QueryTemplate;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Mar 31, 2009
 */
@URITemplate("/images/{repositoryName}/{workspaceName}/{nodeIdentifier}/")
public class RESTImagesRendererService implements ResourceContainer{

  /** The Constant LAST_MODIFIED_PROPERTY. */
  private static final String LAST_MODIFIED_PROPERTY = "Last-Modified";

  /** The Constant IF_MODIFIED_SINCE_DATE_FORMAT. */
  private static final String IF_MODIFIED_SINCE_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

  /** The session provider service. */
  private SessionProviderService sessionProviderService;
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The log. */
  static Log log = ExoLogger.getLogger(RESTImagesRendererService.class);
  
  /** The base rest uri. */
  private String baseRestURI = "/portal/rest/";
  /** Default mime type **/
  private static String DEFAULT_MIME_TYPE = "image/jpg";

  /** Mime type property **/
  private static String PROPERTY_MIME_TYPE = "jcr:mimeType";
  
  /**
   * Instantiates a new rEST images renderer service.
   * 
   * @param repositoryService the repository service
   * @param sessionProviderService the session provider service
   */
  public RESTImagesRendererService(RepositoryService repositoryService, SessionProviderService sessionProviderService) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
  }

  /**
   * Serve image.
   * 
   * @param repository the repository
   * @param workspace the workspace
   * @param nodeIdentifier the node identifier
   * 
   * @return the response
   */
  @HTTPMethod("GET")
  @QueryTemplate("type=file")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response serveImage(@URIParam("repositoryName") String repository, @URIParam("workspaceName") String workspace,
      @URIParam("nodeIdentifier") String nodeIdentifier, @HeaderParam("If-Modified-Since") String ifModifiedSince) { 
    Node node = null;
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = null;
    try {            
      session = sessionProvider.getSession(workspace,repositoryService.getRepository(repository));
      if(nodeIdentifier.contains("/"))
        node = session.getRootNode().getNode(nodeIdentifier);
      else
        node = session.getNodeByUUID(nodeIdentifier);
      if(node == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
      }
      Node dataNode = null; 
      InputStream jcrData = null;
      if(node.isNodeType("nt:file")) {
        dataNode = node;
      }else if(node.isNodeType("nt:versionedChild")) {
        VersionHistory versionHistory = (VersionHistory)node.getProperty("jcr:childVersionHistory").getNode();
        String versionableUUID = versionHistory.getVersionableUUID();
        dataNode = session.getNodeByUUID(versionableUUID);
      }else {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
      } 
      
      if (ifModifiedSince != null && isModified(ifModifiedSince, dataNode) == false) {
      	return Response.Builder.notModified().build();
      }
      
      DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
      Node jcrContentNode = dataNode.getNode("jcr:content");
      String mimeType = DEFAULT_MIME_TYPE;
      if (jcrContentNode.hasProperty(PROPERTY_MIME_TYPE))
      {
          mimeType = jcrContentNode.getProperty(PROPERTY_MIME_TYPE).getString();
      }

      jcrData = jcrContentNode.getProperty("jcr:data").getStream();
      return Response.Builder.ok().entity(jcrData, mimeType).build();

    } catch (PathNotFoundException e) {
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
    }catch (ItemNotFoundException e) {
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
    }catch (Exception e) {
      log.error("Error when serveImage: ", e.fillInStackTrace());
      return Response.Builder.serverError().build(); 
    } finally {
      if (session != null) session.logout();
      sessionProvider.close();
    }
  }
  
  /**
   * Serve image.
   * 
   * @param repository the repository
   * @param workspace the workspace
   * @param nodeIdentifier the node identifier
   * @param propertyName the property name
   * 
   * @return the response
   */
  @HTTPMethod("GET")  
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response serveImage(@URIParam("repositoryName") String repository, @URIParam("workspaceName") String workspace,
      @URIParam("nodeIdentifier") String nodeIdentifier, @QueryParam("propertyName") String propertyName, @HeaderParam("If-Modified-Since") String ifModifiedSince) {
    Node node = null;
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = null;
    try {            
      session = sessionProvider.getSession(workspace,repositoryService.getRepository(repository));
      if(nodeIdentifier.contains("/"))
        node = session.getRootNode().getNode(nodeIdentifier);
      else
        node = session.getNodeByUUID(nodeIdentifier);
      if(node == null) {
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
      }           
      
      if (ifModifiedSince != null && isModified(ifModifiedSince, node) == false) {
      	return Response.Builder.notModified().build();
      }
      
      InputStream jcrData = node.getProperty(propertyName).getStream();
      DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
      return Response.Builder.ok().entity(jcrData, DEFAULT_MIME_TYPE).build();
    } catch (PathNotFoundException e) {
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
    }catch (ItemNotFoundException e) {
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND).build();
    }catch (Exception e) {
      log.error("Error when serve image: ", e.fillInStackTrace());
      return Response.Builder.serverError().build(); 
    } finally {
      if (session != null) session.logout();
      sessionProvider.close();
    }
  }
  
  /**
   * Generate uri.
   * 
   * @param file the file
   * 
   * @return the string
   * 
   * @throws Exception the exception
   */
  public String generateURI(Node file) throws Exception {
    if(!file.isNodeType("nt:file")) throw new UnsupportedOperationException("The node isn't nt:file");
    StringBuilder builder = new StringBuilder();    
    String repository = ((ManageableRepository)file.getSession().getRepository()).getConfiguration().getName();
    String workspaceName = file.getSession().getWorkspace().getName();
    String nodeIdentifiler = null;
    InputStream stream = file.getNode("jcr:content").getProperty("jcr:data").getStream();
    if (stream.available() == 0) return null;
    stream.close();
    if (file.isNodeType("mix:referenceable")) {
      nodeIdentifiler = file.getUUID();
    } else {
     nodeIdentifiler = file.getPath().replaceFirst("/","");
    }  
    String accessURI = baseRestURI;
    String userId = file.getSession().getUserID();    
    if (!SystemIdentity.ANONIM.equals(userId) && SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      accessURI = baseRestURI.concat("private/");
    }
    return builder.append(accessURI).append("images/").append(repository).append("/")
                  .append(workspaceName).append("/").append(nodeIdentifiler).append("?type=file").toString();
  }
  
  /**
   * Generate uri.
   * 
   * @param file the file
   * @param propertyName the property name
   * 
   * @return the string
   * 
   * @throws Exception the exception
   */
  public String generateURI(Node file, String propertyName) throws Exception {
    StringBuilder builder = new StringBuilder();    
    String repository = ((ManageableRepository)file.getSession().getRepository()).getConfiguration().getName();
    String workspaceName = file.getSession().getWorkspace().getName();
    String nodeIdentifiler = null;
    if(file.isNodeType("mix:referenceable")) {
      nodeIdentifiler = file.getUUID();
    }else {
     nodeIdentifiler = file.getPath().replaceFirst("/","");
    }       
    String accessURI = baseRestURI;
    String userId = file.getSession().getUserID();    
    if(!SystemIdentity.ANONIM.equals(userId) && SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      accessURI = baseRestURI.concat("private/");
    }
    return builder.append(accessURI).append("images/").append(repository).append("/").append(workspaceName)
      .append("/").append(nodeIdentifiler).append("?propertyName=").append(propertyName).toString();
  }
  
  /**
   * get the last modified date of node
 * @param node
 * @return the last modified date
 * @throws Exception
 */
  private Date getLastModifiedDate(Node node) throws Exception {
	  Date lastModifiedDate = null;
	  if (node.hasNode("jcr:content")) {
		  lastModifiedDate = node.getNode("jcr:content").getProperty("jcr:lastModified").getDate().getTime();
	  } else if (node.hasNode("exo:dateModified")) {
    	  lastModifiedDate = node.getProperty("exo:dateModified").getDate().getTime();
	  } else {
		  lastModifiedDate = node.getProperty("jcr:created").getDate().getTime();
	  }
	  return lastModifiedDate;
  }
  
  /**
   * check resources were modified or not
   * @param ifModifiedSince
   * @param node
   * @return
   * @throws Exception
   */
  private boolean isModified(String ifModifiedSince, Node node) throws Exception {
	  // get last-modified-since from header
	  DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
	  Date ifModifiedSinceDate = dateFormat.parse(ifModifiedSince);

	  // get last modified date of node
	  Date lastModifiedDate = getLastModifiedDate(node);

	  // Check if cached resource has not been modifed, return 304 code
	  if (ifModifiedSinceDate.getTime() >= lastModifiedDate.getTime()) {			
		return false;
	  }		
	  return true;
  }
}
