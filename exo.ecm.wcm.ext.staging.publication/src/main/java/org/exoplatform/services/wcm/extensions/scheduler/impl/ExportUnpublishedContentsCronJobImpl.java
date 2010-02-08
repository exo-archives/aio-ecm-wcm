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
import java.io.FileOutputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS Author : Dung Khuong
 * dung.khuong@exoplatform.com Feb 4, 2010
 */
public class ExportUnpublishedContentsCronJobImpl implements Job {

  private static final Log    log            = ExoLogger.getLogger(ExportUnpublishedContentsCronJobImpl.class);

  private static final String URL            = "http://www.w3.org/2001/XMLSchema";

  private String              contentState   = null;

  private String              predefinedPath = null;

  private String              workspace      = null;

  private String              repository     = null;

  private String              stagingStorage = null;

  public void execute(JobExecutionContext context) throws JobExecutionException {
    Session session = null;
    try {

      /**
       * Back up code
       */
      /*
       * log.info("Start Execute CleanUnpublishedContentsJob"); JobDataMap
       * jdatamap = context.getJobDetail().getJobDataMap(); contentState =
       * jdatamap.getString("contentState"); predefinedPath =
       * jdatamap.getString("predefinedPath"); stagingStorage =
       * jdatamap.getString("stagingStore"); String[] pathTab =
       * predefinedPath.split(":"); repository = pathTab[0]; workspace =
       * pathTab[1]; contentPath = pathTab[2];log.info(
       * "Start Execute CleanUnpublishedContentsJob: clean all the contents with stage = 'unpublished'"
       * ); XMLInputFactory factory = XMLInputFactory.newInstance(); File
       * stagingFolder = new File(stagingStorage); File[] files = null; File
       * xmlFile = null; XMLStreamReader reader = null; InputStream
       * xmlInputStream = null; int eventType; List<String> listContents = null;
       * List<String> listUnpublishedContents = null; if
       * (stagingFolder.exists()) { files = stagingFolder.listFiles(); if (files
       * != null) { for (int i = 0; i < files.length; i++) { xmlFile = files[i];
       * if (xmlFile.isFile()) { MimeTypeResolver resolver = new
       * MimeTypeResolver(); String mimeType =
       * resolver.getMimeType(xmlFile.getName()); if
       * ("text/xml".equals(mimeType)) { xmlInputStream = new
       * FileInputStream(xmlFile); reader =
       * factory.createXMLStreamReader(xmlInputStream); listContents = new
       * ArrayList<String>(); while (reader.hasNext()) { try { eventType =
       * reader.next(); if (eventType == XMLEvent.START_ELEMENT &&
       * "content".equals(reader.getLocalName())) {
       * listContents.add(reader.getAttributeValue(0)); } } catch(Exception ie)
       * {
       * log.info("Error while getting contents targetPath: "+ie.getMessage());
       * } } listUnpublishedContents = new ArrayList<String>(); for(String s :
       * listContents){ //--Check if a content's stage is unpublished add that
       * content to listUnpublishedContents if(true){
       * listUnpublishedContents.add(s); } } reader.close();
       * xmlInputStream.close(); xmlFile.delete(); } } } } }
       */

      log.info("Start Execute ExportUnpublishedContentsCronJobImpl");

      JobDataMap jdatamap = context.getJobDetail().getJobDataMap();

      contentState = jdatamap.getString("contentState");
      stagingStorage = jdatamap.getString("stagingStorage");
      predefinedPath = jdatamap.getString("predefinedPath");
      String[] pathTab = predefinedPath.split(":");
      repository = pathTab[0];
      workspace = pathTab[1];      

      SessionProvider sessionProvider = SessionProvider.createSystemProvider();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RepositoryService repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
      session = sessionProvider.getSession(workspace, manageableRepository);
      QueryManager queryManager = session.getWorkspace().getQueryManager();      
      Query query = queryManager.createQuery("select * from nt:base where publication:currentState='"
                                                 + contentState + "'",
                                             Query.SQL);
      QueryResult queryResult = query.execute();

      if (queryResult.getNodes().getSize() > 0) {
        
        log.info("Start exporting unpublished content(s)");
        
        File file = new File(stagingStorage + File.separatorChar + "contents.xml"); 
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        FileOutputStream output = new FileOutputStream(file);
        XMLStreamWriter xmlsw = outputFactory.createXMLStreamWriter(output, "UTF-8");
        xmlsw.writeStartDocument("UTF-8", "1.0");
        xmlsw.writeStartElement("xs", "contents", URL);
        xmlsw.writeNamespace("xs", URL);
        for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
          Node node_ = iter.nextNode();

          if (node_.isNodeType("nt:frozenNode"))
            continue;
          NodeLocation nodeLocation = NodeLocation.make(node_);
          StringBuffer contenTargetPath = new StringBuffer();
          contenTargetPath.append(nodeLocation.getRepository());
          contenTargetPath.append(":");
          contenTargetPath.append(nodeLocation.getWorkspace());
          contenTargetPath.append(":");
          contenTargetPath.append(nodeLocation.getPath());

          xmlsw.writeStartElement("xs", "content", URL);
          xmlsw.writeAttribute("targetPath", contenTargetPath.toString());
          xmlsw.writeEndElement();
        }        
        xmlsw.writeEndElement();
        xmlsw.writeEndDocument();
        xmlsw.flush();
        output.close();
        xmlsw.close();

        log.info("Unpublished content(s) is exported to: " + stagingStorage + File.separatorChar + "contents.xml");
      } else {
        log.info("End Execute ExportUnpublishedContentsCronJobImpl");
      }
    } catch (Exception ex) {
      log.error("Error when exporting unpublished content(s) : " + ex.getMessage(), ex);
    } finally {
      session.logout();
    }
  }

}
