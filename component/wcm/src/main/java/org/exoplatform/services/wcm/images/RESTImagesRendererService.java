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

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 31, 2009  
 */
@URITemplate("/images/{repositoryName}/{workspaceName}/{nodeIdentifier}/")
public class RESTImagesRendererService implements ResourceContainer{

  private ThreadLocalSessionProviderService sessionProviderService;
  private RepositoryService repositoryService;
  static Log log = ExoLogger.getLogger(RESTImagesRendererService.class);
  private String baseRestURI = "/portal/rest/";
  public RESTImagesRendererService(RepositoryService repositoryService, ThreadLocalSessionProviderService sessionProviderService) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
  }

  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response serveImage(@URIParam("repositoryName") String repository, @URIParam("workspaceName") String workspace,
      @URIParam("nodeIdentifier") String nodeIdentifier) { 
    return serveImage(repository,workspace,nodeIdentifier,"jcr:content/jcr:data");
  }
  
  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response serveImage(@URIParam("repositoryName") String repository, @URIParam("workspaceName") String workspace,
      @URIParam("nodeIdentifier") String nodeIdentifier, @QueryParam("propertyName") String propertyName) {
    Node node = null;
    try {            
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      Session session = sessionProvider.getSession(workspace,repositoryService.getRepository(repository));
      if(nodeIdentifier.contains("/"))
        node = (Node)session.getItem(nodeIdentifier);
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
      log.error(e.getMessage(),e);
      return Response.Builder.serverError().build(); 
    }
  }
  
  
  public String generateURI(Node file) throws Exception {
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
    return builder.append(accessURI).append("images/").append(repository).append("/").append(workspaceName).append("/").append(nodeIdentifiler).toString();
  }
  
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
