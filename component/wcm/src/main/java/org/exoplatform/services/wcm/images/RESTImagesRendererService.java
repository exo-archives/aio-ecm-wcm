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
import javax.jcr.version.VersionHistory;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Mar 31, 2009
 */
@Path("/images/")
public class RESTImagesRendererService implements ResourceContainer{

  /** The session provider service. */
  private SessionProviderService sessionProviderService;
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The log. */
  static Log log = ExoLogger.getLogger(RESTImagesRendererService.class);
  
  /** The base rest uri. */
//  private String baseRestURI = "/portal/rest/";
  
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
  @GET
  @Path("/{repositoryName}/{workspaceName}/{nodeIdentifier}")
  public Response serveImageWithType(@PathParam("repositoryName") String repository, 
                                     @PathParam("workspaceName") String workspace,
                                     @PathParam("nodeIdentifier") String nodeIdentifier,
                                     @QueryParam("param") @DefaultValue("file") String param) { 
    try {
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      WCMService wcmService = WCMCoreUtils.getService(WCMService.class);
      Node node = wcmService.getReferencedContent(sessionProvider, repository, workspace, nodeIdentifier);
      if (node == null) return Response.status(HTTPStatus.NOT_FOUND).build();
      
      
      if ("file".equals(param)) {
        Node dataNode = null; 
        if(node.isNodeType("nt:file")) {
          dataNode = node;
        }else if(node.isNodeType("nt:versionedChild")) {
          VersionHistory versionHistory = (VersionHistory)node.getProperty("jcr:childVersionHistory").getNode();
          String versionableUUID = versionHistory.getVersionableUUID();
          dataNode = sessionProvider.getSession(workspace,repositoryService.getRepository(repository)).getNodeByUUID(versionableUUID);
        }else {
          return Response.status(HTTPStatus.NOT_FOUND).build();
        }     
        InputStream jcrData = dataNode.getNode("jcr:content").getProperty("jcr:data").getStream();
        return Response.ok(jcrData, "image").build();  
      } else {
        InputStream jcrData = node.getProperty(param).getStream();
        return Response.ok(jcrData, "image").build();        
      }
      
    } catch (PathNotFoundException e) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    }catch (ItemNotFoundException e) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    }catch (Exception e) {
      log.error("Error when serveImage: ", e.fillInStackTrace());
      return Response.serverError().build(); 
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
    String baseRestURI = getBaseRestURI();
    String accessURI = baseRestURI;
    String userId = file.getSession().getUserID();    
    if (!SystemIdentity.ANONIM.equals(userId) && SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      accessURI = baseRestURI.concat("private/");
    }
    return builder.append(accessURI).append("images/").append(repository).append("/")
                  .append(workspaceName).append("/").append(nodeIdentifiler).append("?param=file").toString();
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
    String baseRestURI = getBaseRestURI();
    String accessURI = baseRestURI;
    String userId = file.getSession().getUserID();    
    if(!SystemIdentity.ANONIM.equals(userId) && SystemIdentity.SYSTEM.equalsIgnoreCase(userId)) {
      accessURI = baseRestURI.concat("private/");
    }
    return builder.append(accessURI).append("images/").append(repository).append("/").append(workspaceName)
      .append("/").append(nodeIdentifiler).append("?param=").append(propertyName).toString();
  }

  /**
   * Generate the current rest base uri.
   * 
   * @return the current base uri
   */
  private String getBaseRestURI() {
	  StringBuffer s = new StringBuffer("/");
	  s.append(PortalContainer.getCurrentPortalContainerName());
	  s.append("/");
	  s.append(PortalContainer.getCurrentRestContextName());
	  s.append("/");
	  return s.toString();
  }
  
}
