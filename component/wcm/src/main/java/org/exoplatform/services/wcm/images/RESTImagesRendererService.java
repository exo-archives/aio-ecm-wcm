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

  /** The session provider service. */
  private SessionProviderService sessionProviderService;
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The log. */
  static Log log = ExoLogger.getLogger(RESTImagesRendererService.class);
  
  /** The base rest uri. */
  private String baseRestURI = "/portal/rest/";
  
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
      @URIParam("nodeIdentifier") String nodeIdentifier) { 
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
      jcrData = dataNode.getNode("jcr:content").getProperty("jcr:data").getStream();
      return Response.Builder.ok().entity(jcrData, "image").build();
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
      @URIParam("nodeIdentifier") String nodeIdentifier, @QueryParam("propertyName") String propertyName) {
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
      InputStream jcrData = node.getProperty(propertyName).getStream();
      return Response.Builder.ok().entity(jcrData, "image").build();
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
}
