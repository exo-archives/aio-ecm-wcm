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

package org.exoplatform.services.wcm.impl;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.wcm.BaseWebContentHandler;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 10, 2008  
 */
public class HTMLContentHandler extends BaseWebContentHandler {

  private final String NT_FOLDER = "nt:folder".intern();

  public HTMLContentHandler() {  }

  protected String getFileType() { return "nt:file"; }

  protected String getFolderPathExpression() { return null; }

  protected String getFolderType() { return "exo:htmlFolder"; }

  public String handle(Node file) throws Exception {    
    Session session = file.getSession();    
    Node webFolder = file.getParent();    
    Node webContentFolder = createWebContentFolder(webFolder,file.getName(),NT_FOLDER);
    if(!webContentFolder.isNodeType("exo:webContent")) {
      webContentFolder.addMixin("exo:webContent") ;      
    }
    if(!file.isNodeType("exo:htmlFile")) {
      file.addMixin("exo:htmlFile");
      file.setProperty("exo:presentationType","exo:htmlFile");
    }
    String newPath = webContentFolder.getPath() + "/" +file.getName();    
    createWebContentSchema(webContentFolder);
    session.move(file.getPath(),newPath);
    session.save();
    return file.getUUID();
  }


  private Node createWebContentFolder(Node homeNode, String folderName, String primaryType) throws Exception {
    String structure = getDateTimePath();    
    Node dateTimeNode = createDateTimeFolder(homeNode,structure);
    Node storedNode = null;    
    try {
      storedNode = dateTimeNode.getNode(folderName);
    } catch (PathNotFoundException e) {      
      storedNode = dateTimeNode.addNode(folderName,primaryType);      
    }
    return storedNode;
  }

  private Node createDateTimeFolder(Node location,String schemaPath) throws Exception {
    Node node = makeNode(location,schemaPath,"nt:folder");    
    return node;
  }

  private Node makeNode(Node rootNode, String path, String nodetype)
  throws PathNotFoundException, RepositoryException {    
    String[] tokens = path.split("/") ;
    Node node = rootNode;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      if(node.hasNode(token)) {
        node = node.getNode(token) ;
      }else {
        node = node.addNode(token, nodetype);
        if (node.canAddMixin("exo:privilegeable")){
          node.addMixin("exo:privilegeable");
        }        
      }      
    }
    return node;
  }    
  private String getDateTimePath() { 
    //TODO hard coded here. Path like this: /{year}/{month}/{start day of week} {month}-{end day of week}{month}    
    // We should allow user can define the pattern
    //LocaleConfigService configService = getService(LocaleConfigService.class);
    Locale locale = Locale.ENGLISH;
    Calendar calendar = new GregorianCalendar(locale);
    String[] monthNames = new DateFormatSymbols().getMonths();
    String currentYear  = Integer.toString(calendar.get(Calendar.YEAR)) ;    
    String currentMonth = monthNames[calendar.get(Calendar.MONTH)] ;    
    int weekday = calendar.get(Calendar.DAY_OF_WEEK);    
    int diff = 2 - weekday ;        
    calendar.add(Calendar.DATE, diff);    
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
    String startDateOfWeek = dateFormat.format(calendar.getTime());
    String[] arrStartDate = startDateOfWeek.split("/") ;
    String startWeekDay = arrStartDate[0] ;       
    calendar.add(Calendar.DATE, 6);
    String endDateOfWeek = dateFormat.format(calendar.getTime());
    String[] arrEndDate = endDateOfWeek.split("/") ;
    String endWeekDay = arrEndDate[0] ;       
    StringBuilder builder = new StringBuilder();
    //Year folder
    builder.append(currentYear).append("/")
    //Month folder
    .append(currentMonth).append("/")
    //week folder
    .append(startWeekDay).append(" ").append(currentMonth)
    .append("-")
    .append(endWeekDay).append(" ").append(currentMonth).append("/");    
    return builder.toString();
  }

  private void createWebContentSchema(Node webContent) throws Exception {
    //TODO hard coded here. should define schema as init param    
    webContent.addNode("js","exo:jsFolder") ;    
    webContent.addNode("css","exo:cssFolder") ;    
    Node multimedia = webContent.addNode("multimedia",NT_FOLDER);
    multimedia.addMixin("exo:multimediaFolder");
    Node images = multimedia.addNode("images",NT_FOLDER);
    images.addMixin("exo:pictureFolder");
    Node video = multimedia.addNode("videos",NT_FOLDER);
    video.addMixin("exo:videoFolder");
    Node audio = multimedia.addNode("audio",NT_FOLDER);
    audio.addMixin("exo:musicFolder");
    Node document = webContent.addNode("Structured Document",NT_FOLDER) ;
    document.addMixin("exo:documentFolder");    
  }

}
