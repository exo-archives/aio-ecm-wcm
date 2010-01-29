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
package org.exoplatform.wcm.connector.collaboration;

import javax.jcr.Node;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.connector.BaseConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS 
 * Author : Benjamin Paillereau
 * benjamin.paillereau@exoplatform.com
 * July 10, 2009
 */
@Path("/contents/vote/")
public class VoteConnector extends BaseConnector implements ResourceContainer {

  /**
   * Instantiates a new vote connector.
   * 
   * @param container the container
   */
  public VoteConnector() {
	  super(ExoContainerContext.getCurrentContainer());
	  
	  
  }

  /**
   * post a Vote for a content
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * 
   * @return http code
   * 
   * @throws Exception the exception
   */
  @GET
  @Path("/postVote/")
//  @InputTransformer(PassthroughInputTransformer.class)
  public Response postVote( 
		  @QueryParam("repositoryName") String repositoryName, 
		  @QueryParam("workspaceName") String workspaceName, 
		  @QueryParam("jcrPath") String jcrPath,
		  @QueryParam("vote") String vote, 
		  @QueryParam("lang") String lang
		  ) throws Exception {
    try {
    	Node content = getContent(repositoryName, workspaceName, jcrPath);
    	if (content.isNodeType("mix:votable")) {
    		String userName = content.getSession().getUserID();
    		votingService.vote(content, Double.parseDouble(vote), userName, lang);
    	}
    } catch (Exception e) {
    	Response.serverError().build();
    }    
    return Response.ok().build();
  }

  /**
   * get a Vote for a content
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * 
   * @return http code
   * 
   * @throws Exception the exception
   */
  @GET
  @Path("/getVote/")
//  @OutputTransformer(XMLOutputTransformer.class)
  public Response getVote( 
		  @QueryParam("repositoryName") String repositoryName, 
		  @QueryParam("workspaceName") String workspaceName, 
		  @QueryParam("jcrPath") String jcrPath) throws Exception {
	  try {
		  Node content = getContent(repositoryName, workspaceName, jcrPath);
		  if (content.isNodeType("mix:votable")) {
			  String votingRate = "";
			  if (content.hasProperty("exo:votingRate")) votingRate = content.getProperty("exo:votingRate").getString();
			  String votingTotal = "";
			  if (content.hasProperty("exo:voteTotalOfLang")) votingTotal = content.getProperty("exo:voteTotalOfLang").getString();
			  
			  Document document =
					DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			  Element element = document.createElement("vote");
			  Element rate = document.createElement("rate");
//			  rate.setTextContent(votingRate);
			  Element total = document.createElement("total");
//			  total.setTextContent(votingTotal);
			  element.appendChild(rate);
			  element.appendChild(total);
			  document.appendChild(element);

			  return Response.ok(document, "text/xml").build();
		  }
	  } catch (Exception e) {
		  Response.serverError().build();
	  }    
	  return Response.ok().build();
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
