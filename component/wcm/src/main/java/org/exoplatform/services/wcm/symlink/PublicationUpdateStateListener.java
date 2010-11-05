/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.wcm.symlink;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 11, 2010  
 */
public class PublicationUpdateStateListener extends Listener<CmsService, Node> {

	private static Log log = ExoLogger.getLogger("wcm:PublicationUpdateStateListener");
  
  private RepositoryService repositoryService;
  
  public PublicationUpdateStateListener() {
    repositoryService = WCMCoreUtils.getService(RepositoryService.class);
  }
  
  public void onEvent(Event<CmsService, Node> event) throws Exception {
	if ("WCMPublicationService.event.updateState".equals(event.getEventName())) {
		Node targetNode = event.getData();
		if (targetNode.isNodeType("exo:taxonomyLink")) return;
		try {
			targetNode.getUUID();
		} catch (UnsupportedRepositoryOperationException e) {
			return;
		}
		String title = null;
		String name = targetNode.getName();
		Node liveNode = null;
		Calendar liveDate = null;
		String titlePublished = null;
		// We missing an action to update the modified date of the node
		Calendar dateModified = new GregorianCalendar();
		targetNode.setProperty("exo:dateModified", dateModified);
		
		if (targetNode.hasProperty("exo:title")) {
			try {
				title = targetNode.getProperty("exo:title").getString();
			} catch (PathNotFoundException e) {
				log.info("No such of property exo:title for this node:");
			}
		}
		
		if (targetNode.hasProperty("publication:liveDate")) {
			try {
				liveDate = targetNode.getProperty("publication:liveDate").getDate();
			} catch (PathNotFoundException e) {
				log.info("No such of property publication:liveDate for this node:");
			}
		}

	    try {
	        String nodeVersionUUID = targetNode.getProperty("publication:liveRevision").getString(); 
	        Node revNode = targetNode.getVersionHistory().getSession().getNodeByUUID(nodeVersionUUID);
	        if (revNode!=null)
	        	liveNode = revNode.getNode("jcr:frozenNode");
	    } catch (Exception e) { }

		if (targetNode.hasProperty("publication:currentState") && liveNode != null) {
		try {
			if (!targetNode.isNodeType("exo:sortable") && targetNode.canAddMixin("exo:sortable")) {
				targetNode.addMixin("exo:sortable");
			}
		} catch (PathNotFoundException e) {}
		}
	    
	    if (liveNode!=null && liveNode.hasProperty("exo:title")) {
			titlePublished = targetNode.hasProperty("exo:titlePublished")?targetNode.getProperty("exo:titlePublished").getString():null;
			String liveTitle = liveNode.getProperty("exo:title").getString();
			if (liveTitle != null && !liveTitle.equals(titlePublished)) {
				targetNode.setProperty("exo:titlePublished", liveTitle);
				titlePublished = liveTitle;
				targetNode.save();
			}
	    }
		
		
//		NodeLocation targetLocation = NodeLocation.make(targetNode);
		String[] wsNames = repositoryService.getCurrentRepository().getWorkspaceNames();
		for (String workspace:wsNames) {
			Session session = repositoryService.getCurrentRepository().getSystemSession(workspace);
						try {
							  QueryManager queryManager = session.getWorkspace().getQueryManager();
							  Query query = queryManager.createQuery("SELECT * FROM exo:taxonomyLink WHERE exo:uuid='" + targetNode.getUUID() + "'", Query.SQL);
							  NodeIterator iterator = query.execute().getNodes();
							  boolean needSessionSave=false;
							  while (iterator.hasNext()) {
							    Node linkNode = iterator.nextNode();
							    if (!linkNode.isNodeType("exo:sortable")) {
							      if (!linkNode.canAddMixin("exo:sortable")) {
							        break;
							      }
							      linkNode.addMixin("exo:sortable");
							    }
							    try {
							      String currentName = linkNode.hasProperty("exo:name")?linkNode.getProperty("exo:name").getString():null;
							      if (name != null && !name.equals(currentName)) {
							        linkNode.setProperty("exo:name", name);
							        needSessionSave = true;
							      }
							    } catch (PathNotFoundException e) {}
							    
							    try {
							      String currentTitle = linkNode.hasProperty("exo:title")?linkNode.getProperty("exo:title").getString():null;
							      if (title != null && !title.equals(currentTitle)) {
							        linkNode.setProperty("exo:title", title);
							        needSessionSave = true;
							      }
							    } catch (PathNotFoundException e) {}
							    
				
//				if (targetNode.hasProperty("publication:currentState") && "published".equals(targetNode.getProperty("publication:currentState").getString())) {
							    			    try {
							    				      String currentTitlePublished = linkNode.hasProperty("exo:titlePublished")?linkNode.getProperty("exo:titlePublished").getString():null;
							    				      if (titlePublished != null && !titlePublished.equals(currentTitlePublished)) {
							    				        linkNode.setProperty("exo:titlePublished", titlePublished);
							    				        needSessionSave = true;
							    				      }
							    				    } catch (PathNotFoundException e) {}				
//				}
							    				    			    
							    				    			    try {
							    				    			      Calendar currentLiveDate = linkNode.hasProperty("publication:liveDate")?linkNode.getProperty("publication:liveDate").getDate():null;
							    				    			      if (liveDate != null && !liveDate.equals(currentLiveDate)) {
							    				    			        linkNode.setProperty("publication:liveDate", liveDate);
							    				    			        needSessionSave = true;
							    				    			      }
							    				    			    } catch (PathNotFoundException e) {}
							    				    			    
							    				    			    try {
							    				    			      Calendar currentDateModified = linkNode.getProperty("exo:dateModified").getDate();
							    				    			      if (dateModified != null && !dateModified.equals(currentDateModified)) {
							    				    			        linkNode.setProperty("exo:dateModified", dateModified);  
							    				    			        needSessionSave = true;
							    				    			      }
							    				    			    } catch (PathNotFoundException e) {}
							    				   			    
							    				    			    if (log.isInfoEnabled()) {
							    				    			      String currentState = targetNode.hasProperty("publication:currentState")?targetNode.getProperty("publication:currentState").getString():"";
							    				    			      String currentName = linkNode.hasProperty("exo:name")?linkNode.getProperty("exo:name").getString():"";
							    				    			      String currentTitle = linkNode.hasProperty("exo:title")?linkNode.getProperty("exo:title").getString():"";
							    				    			      String currentTitlePub = linkNode.hasProperty("exo:titlePublished")?linkNode.getProperty("exo:titlePublished").getString():"";
							    				    			      String currentLiveDate = linkNode.hasProperty("publication:liveDate")?linkNode.getProperty("publication:liveDate").getDate().getTime().toString():"";
							    				    			      String currentDateModified = linkNode.hasProperty("exo:dateModified")?linkNode.getProperty("exo:dateModified").getDate().getTime().toString():"";
							    				    			      
							    				    			      log.info("@@@@ "+needSessionSave+" @state@"+currentState+" @Name@"+currentName+" @Title@"+currentTitle+" @TitlePub@"+currentTitlePub+" @DateLive@"+currentLiveDate+" @DateMod@"+currentDateModified);
							    				    			    }
							    				    			    
				
//      linkNode.setProperty("exo:dateModified", dateModified);
							    				    			    			    
							    				    			    			  }
							    				    			    			  if (needSessionSave) session.save();
							    				    			    			} catch(Exception e) {
							    				    			    			  log.error("Unexpected problem occur. Update state process is not completed", e);
							    				    			    			} finally {
							    				    			    			  if(session != null) session.logout();				
			}
			
			
		}
	}
    return;
  }
  
}
