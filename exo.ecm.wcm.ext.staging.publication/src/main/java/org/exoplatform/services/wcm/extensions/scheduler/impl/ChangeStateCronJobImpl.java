package org.exoplatform.services.wcm.extensions.scheduler.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
public class ChangeStateCronJobImpl implements Job {
  private static final Log    log                 = ExoLogger.getLogger(ChangeStateCronJobImpl.class);

  private static final String START_TIME_PROPERTY = "publication:startPublishedDate".intern();

  private static final String END_TIME_PROPERTY   = "publication:endPublishedDate".intern();

  private String              fromState           = null;

  private String              toState             = null;

  private String              predefinedPath      = null;

  private String              workspace           = null;

  private String              repository          = null;

  private String              contentPath         = null;

  public void execute(JobExecutionContext context) throws JobExecutionException {
    Session session = null;
    try {

      log.debug("Start Execute ChangeStateCronJob");
      if (fromState == null) {

        JobDataMap jdatamap = context.getJobDetail().getJobDataMap();

        fromState = jdatamap.getString("fromState");
        toState = jdatamap.getString("toState");
        predefinedPath = jdatamap.getString("predefinedPath");
        String[] pathTab = predefinedPath.split(":");
        repository = pathTab[0];
        workspace = pathTab[1];
        contentPath = pathTab[2];
      }
      log.debug("Start Execute ChangeStateCronJob: change the State from " + fromState + " to "
          + toState);
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RepositoryService repositoryService_ = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
      ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
      if (manageableRepository == null) {
        if (log.isDebugEnabled())
          log.debug("Repository '" + repository + "' not found., ignoring");
        return;
      }
      session = sessionProvider.getSession(workspace, manageableRepository);
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      String property = null;
      if ("staged".equals(fromState) && "published".equals(toState)) {
        property = START_TIME_PROPERTY;
      } else if ("published".equals(fromState) && "unpublished".equals(toState)) {
        property = END_TIME_PROPERTY;

      }
      if (property != null) {

        // appends trailing / if missing
        if (contentPath != null) {
          if (!contentPath.endsWith("/")) {
            contentPath += "/";
          }
        }

        Query query = queryManager.createQuery("select * from nt:base where publication:currentState='"
                                                   + fromState
                                                   + "' and jcr:path like '"
                                                   + contentPath + "%'",
                                               Query.SQL);
        QueryResult queryResult = query.execute();
        long numberOfItemsToChange = queryResult.getNodes().getSize();

        if (numberOfItemsToChange > 0) {

          log.debug(numberOfItemsToChange + " '" + fromState + "' candidates for state '" + toState
              + "' found in " + predefinedPath);
          PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                                  .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
          HashMap<String, String> context_ = new HashMap<String, String>();
          for (NodeIterator iter = queryResult.getNodes(); iter.hasNext();) {
            Node node_ = iter.nextNode();
            if (node_.hasProperty(property)) {

              SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
              Date now = Calendar.getInstance().getTime();
              Date nodeDate = node_.getProperty(property).getDate().getTime();
              if (now.compareTo(nodeDate) >= 0) {
                log.info("'" + toState + "' " + node_.getPath() + " (" + property + "="
                    + format.format(nodeDate) + ")");

                if (PublicationDefaultStates.UNPUBLISHED.equals(toState)) {
                  if (node_.hasProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP)) {
                    String liveRevisionProperty = node_.getProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP)
                                                       .getString();
                    if (!"".equals(liveRevisionProperty)) {
                      Node liveRevision = session.getNodeByUUID(liveRevisionProperty);
                      if (liveRevision != null) {
                        context_.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME,
                                     liveRevision.getName());
                      }
                    }
                  }

                }
                publicationPlugin.changeState(node_, toState, context_);
              }
            } else if (START_TIME_PROPERTY.equals(property)) {
              log.info("'" + toState + "' " + node_.getPath());
              publicationPlugin.changeState(node_, toState, context_);
            }
          }
        } else {
          log.debug("no '" + fromState + "' content found in " + predefinedPath);
        }
      }
      log.debug("End Execute ChangeStateCronJob");
    } catch (RepositoryException ex) {
      log.debug("Repository '" + repository + "' not found., ignoring");
    } catch (Exception ex) {
      log.error("error when changing the state of the content : " + ex.getMessage(), ex);
    } finally {
      if (session != null)
        session.logout();
    }
  }
}
