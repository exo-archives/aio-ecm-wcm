package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/**
 * Created by The eXo Platform MEA Author : 
 * haikel.thamri@exoplatform.com
 */
public class ImportContentsJob implements Job {
	private static final Log log = ExoLogger.getLogger(ImportContentsJob.class);
	private static final String MIX_TARGET_PATH = "mix:targetPath".intern();
	private static final String MIX_TARGET_WORKSPACE = "mix:targetWorkspace"
			.intern();
	private static final String JCR_File_SEPARATOR = "/".intern();
	private static String stagingStorage;
	private static String temporaryStorge;

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			if (log.isInfoEnabled()) {
				log.info("Start Execute ImportXMLJob");
			}
			if (stagingStorage == null) {

				JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
				stagingStorage = jdatamap.getString("stagingStorage");
				temporaryStorge = jdatamap.getString("temporaryStorge");
				log.debug("Init parameters first time :");
			}
			SessionProvider sessionProvider = SessionProvider
					.createSystemProvider();

			ExoContainer container = ExoContainerContext.getCurrentContainer();
			RepositoryService repositoryService_ = (RepositoryService) container
					.getComponentInstanceOfType(RepositoryService.class);
			ManageableRepository manageableRepository = repositoryService_
					.getRepository("repository");
			XMLInputFactory factory = XMLInputFactory.newInstance();

			File stagingFolder = new File(stagingStorage);
			File tempfolder = new File(temporaryStorge);

			File[] files = null;
			File xmlFile = null;
			XMLStreamReader reader = null;
			InputStream xmlInputStream = null;
			int eventType;
			if (stagingFolder.exists()) {
				files = stagingFolder.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						xmlFile = files[i];
						if (xmlFile.isFile()) {
							MimeTypeResolver resolver = new MimeTypeResolver();
							String fileName = xmlFile.getName();
							String hashCode = fileName.split("-")[0];
							String mimeType = resolver.getMimeType(xmlFile
									.getName());
							if ("text/xml".equals(mimeType)) {
								xmlInputStream = new FileInputStream(xmlFile);
								reader = factory
										.createXMLStreamReader(xmlInputStream);

								while (reader.hasNext()) {
									eventType = reader.next();
									if (eventType == XMLEvent.START_ELEMENT
											&& "content".equals(reader
													.getLocalName())) {
										String content = reader
												.getElementText();
										if (!tempfolder.exists())
											tempfolder.mkdirs();
										long time = System.currentTimeMillis();
										File file = new File(temporaryStorge
												+ File.separator + "-"
												+ hashCode + "-" + time
												+ ".xml.tmp");
										InputStream inputStream = new ByteArrayInputStream(
												content.getBytes());
										OutputStream out = new FileOutputStream(
												file);
										byte[] buf = new byte[1024];
										int len;
										while ((len = inputStream.read(buf)) > 0)
											out.write(buf, 0, len);
										out.close();
										inputStream.close();

									}
								}
								reader.close();
								xmlInputStream.close();
								xmlFile.delete();

							}
						}
					}
				}
			}
			files = tempfolder.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					xmlFile = files[i];
					InputStream inputStream = new FileInputStream(xmlFile);
					reader = factory.createXMLStreamReader(inputStream);
					String workspace = null;
					String nodePath = new String();
					while (reader.hasNext()) {
						eventType = reader.next();
						if (eventType == XMLEvent.START_ELEMENT) {
							if (reader.getLocalName().equals("property")) {
								String value = reader.getAttributeValue(0);
								if (MIX_TARGET_PATH.equals(value)) {
									eventType = reader.next();
									if (eventType == XMLEvent.START_ELEMENT) {
										reader.next();
										nodePath = reader.getText();

									}
								} else if (MIX_TARGET_WORKSPACE.equals(value)) {
									eventType = reader.next();
									if (eventType == XMLEvent.START_ELEMENT) {
										reader.next();
										workspace = reader.getText();
									}
								}
							}
						}
					}
					reader.close();
					inputStream.close();
					Session session = sessionProvider.getSession(workspace,
							manageableRepository);
					if (session.itemExists(nodePath))
						session.getItem(nodePath).remove();
					session.save();

					String path = nodePath.substring(0, nodePath
							.lastIndexOf(JCR_File_SEPARATOR));
					if (!session.itemExists(path)) {
						String[] pathTab = path.split(JCR_File_SEPARATOR);
						Node node_ = session.getRootNode();
						StringBuffer path_ = new StringBuffer(
								JCR_File_SEPARATOR);
						for (int j = 1; j < pathTab.length; j++) {
							path_ = path_.append(pathTab[j]
									+ JCR_File_SEPARATOR);
							if (!session.itemExists(path_.toString())) {
								node_.addNode(pathTab[j], "nt:unstructured");
							}
							node_ = (Node) session.getItem(path_.toString());
						}

					}
					session.importXML(path, new FileInputStream(xmlFile), 0);
					session.save();
					xmlFile.delete();
				}
			}
			if (log.isInfoEnabled()) {
				log.info("End Execute ImportXMLJob");
			}
		} catch (Exception ex) {
			log.error("Error when importing Contents");
		}

	}

}
