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
package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS 
 * Author : Dung Khuong 
 * dung.khuong@exoplatform.com 
 * Feb * 5, 2010
 */
public class ImportUnpublishedContentsCronJobImpl implements Job {

  private static final Log    log               = ExoLogger.getLogger(ImportUnpublishedContentsCronJobImpl.class);
  
  private String              stagingStorage    = null;
  
  private String              repository        = null;
  
  private String              workspace         = null;
  
  private String              contentState      = null;
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {          
      
      JobDataMap jdatamap                       = context.getJobDetail().getJobDataMap();
      
      contentState                              = jdatamap.getString("contentState");
      stagingStorage                            = jdatamap.getString("stagingStorage");      
      
      XMLInputFactory factory                   = XMLInputFactory.newInstance();
      File stagingFolder                        = new File(stagingStorage);
      File[] files                              = null;
      File xmlFile                              = null;
      XMLStreamReader reader                    = null;
      InputStream xmlInputStream                = null;
      int eventType;
      
      SessionProvider sessionProvider           = null;
      ExoContainer container                    = null; 
      RepositoryService repositoryService_      = null;
      ManageableRepository manageableRepository = null;  
      Session session                           = null;
      PublicationService publicationService     = null; 
      
      log.info("Start Executing ImportUnpublishedContentsCronJobImpl: clean all the contents with stage = "+ contentState);      
      if (stagingFolder.exists()) {
        files = stagingFolder.listFiles();
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            xmlFile = files[i];
            if (xmlFile.isFile()) {
              MimeTypeResolver resolver = new MimeTypeResolver();
              String mimeType = resolver.getMimeType(xmlFile.getName());
              if ("text/xml".equals(mimeType)) {
                xmlInputStream = new FileInputStream(xmlFile);
                reader = factory.createXMLStreamReader(xmlInputStream);
                Node currentContent = null;
                container           = ExoContainerContext.getCurrentContainer();
                publicationService  = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
                while (reader.hasNext()) {
                  try {
                    eventType = reader.next();
                    if (eventType == XMLEvent.START_ELEMENT && "content".equals(reader.getLocalName())) {
                                                
                        String contentTargetPath = reader.getAttributeValue(0);                        
                        String[] strContentPath  = contentTargetPath.split(":");                               
                        String contentPath       = "";
                        boolean flag             = true;
                        for(int index = 2; index < strContentPath.length; index++ ){
                          if(flag){
                            contentPath     += strContentPath[index];                      
                            flag=false;
                          }else{                      
                            contentPath     += ":";
                            contentPath     += strContentPath[index];
                          }                   
                        }
                        
                        sessionProvider          = SessionProvider.createSystemProvider();
                                                
                        repositoryService_       = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
                        repository               = strContentPath[0];  
                        manageableRepository     = repositoryService_.getRepository(repository);
                        workspace                = strContentPath[1];                        
                        session                  = sessionProvider.getSession(workspace, manageableRepository);                        
                        currentContent           = (Node) session.getItem(contentPath);
                        
                        HashMap<String, String> variables = new HashMap<String, String>();                                               
                        variables.put("nodePath", contentTargetPath);
                        variables.put("workspaceName", workspace);                            
                                                                        
                        publicationService.changeState(currentContent, contentState, variables);                                                
                        
                    }
                  } catch (Exception e) {
                    log.info("Error while reading xml file " + e.getMessage());
                  }
                }               
                reader.close();
                xmlInputStream.close();                
              }
            }
          }
        }
      }else{
        log.info("Staging Storage does not exists");
      }
      log.info("End Executing ImportUnpublishedContentsCronJobImpl successfully");
    } catch (Exception e) {
      log.info("Error when Importing Unpublished Contents: " + e.getMessage());
    }
  }

}
