/*
 * Copyright (C) 2003-2007 eXo Platform SEA.
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
package org.exoplatform.wcm.webui.selector.content;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SEA
 * Author : Ha Mai Van
 * maivanha1610@gmail.com
 * Sep 7, 2009
 */
@URITemplate("/wcmTreeContent/")
public class UpdateTreeConnector implements ResourceContainer {
  
  /** The Constant FILE_TYPE_WEBCONTENT. */
  public static final String FILE_TYPE_WEBCONTENT                  = "Web Contents"; 
  
  /** The Constant FILE_TYPE_DMSDOC. */
  public static final String FILE_TYPE_DMSDOC                      = "DMS Documents"; 
  
  /** The Constant FILE_TYPE_MEDIAS. */
  public static final String FILE_TYPE_MEDIAS                      = "Medias"; 
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(UpdateTreeConnector.class);

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getChildNodes/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getChildNodes(@QueryParam("nodePath") String nodePath,
                              @QueryParam("workspaceName") String workspaceName,
                              @QueryParam("repositoryName") String repositoryName) throws Exception {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();
    try {
      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
      RepositoryService repositoryService = (RepositoryService)ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = sessionProvider.getSession(workspaceName, manageableRepository);
      Node node = (Node)session.getItem(nodePath);
      NodeIterator nodeIterator = node.getNodes();
      int index = 0;
      Element parentNode = document.createElement("childNodes");
      document.appendChild(parentNode);
      StringBuilder buffer;
      Element childNode;
      Element workSpaceEle;
      Element repositoryEle;
      Element nameEle;
      Element nodePathEle;
      while(nodeIterator.hasNext()){
        node = nodeIterator.nextNode();
        if (!node.isNodeType(NodetypeConstant.EXO_WEBCONTENT) && !node.isNodeType(NodetypeConstant.EXO_HIDDENABLE) &&
            (node.isNodeType(NodetypeConstant.EXO_TAXONOMY) || node.isNodeType(NodetypeConstant.NT_UNSTRUCTURED) || node.isNodeType(NodetypeConstant.NT_FOLDER)) ) {
          buffer = new StringBuilder(128);
          buffer.append(node.getName());
          index = node.getIndex();
          if (index > 1) {
            buffer.append('[');
            buffer.append(index);
            buffer.append(']');
          }
          childNode = document.createElement("childNode");
          parentNode.appendChild(childNode);
          workSpaceEle = document.createElement("workspaceName");
          workSpaceEle.setTextContent(workspaceName);
          repositoryEle = document.createElement("repositoryName");
          repositoryEle.setTextContent(repositoryName);
          nameEle = document.createElement("name");
          nameEle.setTextContent(buffer.toString());
          nodePathEle = document.createElement("nodePath");
          nodePathEle.setTextContent(node.getPath());
          
          childNode.appendChild(workSpaceEle);
          childNode.appendChild(repositoryEle);
          childNode.appendChild(nameEle);
          childNode.appendChild(nodePathEle);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }
  
  /**
   * Gets the drivers by user id.
   * 
   * @param repoName the repo name
   * @param userId the user id
   * 
   * @return the drivers by user id
   * 
   * @throws Exception the exception
   */
  private List<DriveData> getDriversByUserId(String repoName, String userId) throws Exception {    
    ManageDriveService driveService = (ManageDriveService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ManageDriveService.class);      
    List<DriveData> driveList = new ArrayList<DriveData>();    
    List<String> userRoles = getMemberships(userId);    
    List<DriveData> allDrives = driveService.getAllDrives(repoName);
    Set<DriveData> temp = new HashSet<DriveData>();
    if (userId != null) {
      // We will improve ManageDrive service to allow getAllDriveByUser
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        boolean flag = false;
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("${userId}")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            flag = true;
            break;
          }
          if (flag)
            continue;
          for (String rolse : userRoles) {
            if (driveData.hasPermission(allPermission, rolse)) {
              temp.add(driveData);
              break;
            }
          }
        }
      }
    } else {
      for (DriveData driveData : allDrives) {
        String[] allPermission = driveData.getAllPermissions();
        for (String permission : allPermission) {
          if (permission.equalsIgnoreCase("*")) {
            temp.add(driveData);
            break;
          }
        }
      }
    }
    
    for(Iterator<DriveData> iterator = temp.iterator();iterator.hasNext();) {
      driveList.add(iterator.next());
    }
    Collections.sort(driveList);
    return driveList; 
  }
  
	/**
	 * Append drivers.
	 * 
	 * @param document the document
	 * @param driversList the drivers list
	 * @param groupName the group name
	 * 
	 * @return the element
	 */
	private Element appendDrivers(Document document, List<DriveData> driversList, String groupName) {
	  Element folders = document.createElement("Folders");
	  folders.setAttribute("name", groupName);
    for (DriveData driver : driversList) {
      Element folder = document.createElement("Folder");
      folder.setAttribute("name", driver.getName());
      folder.setAttribute("driverPath", driver.getHomePath());
      folders.appendChild(folder);  
    }
	  return folders;
  }
  
  /**
   * Personal drivers.
   * 
   * @param driveList the drive list
   * 
   * @return the list< drive data>
   */
  private List<DriveData> personalDrivers(List<DriveData> driveList) {
    List<DriveData> personalDrivers = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    for(DriveData drive : driveList) {
      if(drive.getHomePath().startsWith(userPath + "/${userId}/")) {
        personalDrivers.add(drive);
      }
    }
    Collections.sort(personalDrivers);
    return personalDrivers;
  }
  
  /**
   * Group drivers.
   * 
   * @param driverList the driver list
   * @param userId the user id
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> groupDrivers(List<DriveData> driverList, String userId) throws Exception {
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    List<DriveData> groupDrivers = new ArrayList<DriveData>();
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    List<String> groups = getGroups(userId);
    for(DriveData drive : driverList) {
      if(drive.getHomePath().startsWith(groupPath)) {
        for(String group : groups) {
          if(drive.getHomePath().equals(groupPath + group)) {
            groupDrivers.add(drive);
            break;
          }
        }
      } 
    }
    Collections.sort(groupDrivers);
    return groupDrivers;
  }
  
  /**
   * General drivers.
   * 
   * @param driverList the driver list
   * 
   * @return the list< drive data>
   * 
   * @throws Exception the exception
   */
  private List<DriveData> generalDrivers(List<DriveData> driverList) throws Exception {
    List<DriveData> generalDrivers = new ArrayList<DriveData>();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    String userPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_USERS_PATH);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    for(DriveData drive : driverList) {
      if((!drive.getHomePath().startsWith(userPath) && !drive.getHomePath().startsWith(groupPath)) 
          || drive.getHomePath().equals(userPath)) {
        generalDrivers.add(drive);
      }
    }
    return generalDrivers;
  }

  /**
   * Gets the memberships.
   * 
   * @param userId the user id
   * 
   * @return the memberships
   * 
   * @throws Exception the exception
   */
  private static List<String> getMemberships(String userId) throws Exception {
  	OrganizationService oservice = (OrganizationService)
  		ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    List<String> userMemberships = new ArrayList<String> ();
    userMemberships.add(userId);
    Collection<?> memberships = oservice.getMembershipHandler().findMembershipsByUser(userId);
    if(memberships == null || memberships.size() < 0) return userMemberships;
    Object[] objects = memberships.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Membership membership = (Membership)objects[i];
      String role = membership.getMembershipType() + ":" + membership.getGroupId();
      userMemberships.add(role);     
    }
    return userMemberships;
  }

  /**
   * Gets the groups.
   * 
   * @param userId the user id
   * 
   * @return the groups
   * 
   * @throws Exception the exception
   */
  private static List<String> getGroups(String userId) throws Exception {
    OrganizationService oservice = (OrganizationService) 
    	ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    List<String> groupList = new ArrayList<String> ();
    Collection<?> groups = oservice.getGroupHandler().findGroupsOfUser(userId);
    Object[] objects = groups.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Group group = (Group)objects[i];
      String groupPath = null;
      if(group.getParentId() == null || group.getParentId().length() == 0) groupPath = "/" + group.getGroupName(); 
      else groupPath = group.getParentId() + "/" + group.getGroupName(); 
      groupList.add(groupPath);
    }
    return groupList;
  }
  
  /**
   * Checks if is dMS document.
   * 
   * @param node the node
   * @param repositoryName the repository name
   * 
   * @return true, if is dMS document
   * 
   * @throws Exception the exception
   */
  private boolean isDMSDocument(Node node, String repositoryName) throws Exception {
  	TemplateService templateService = (TemplateService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(TemplateService.class);
  	List<String> dmsDocumentList = templateService.getDocumentTemplates(repositoryName);
  	dmsDocumentList.remove(NodetypeConstant.EXO_WEBCONTENT);
  	for (String documentType : dmsDocumentList) {
	    if (node.getPrimaryNodeType().isNodeType(documentType)) {
	    	return true;
	    }
    }
  	return false;
  }
}
