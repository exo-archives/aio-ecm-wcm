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
package org.exoplatform.services.wcm.extensions.scheduler;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 5, 2010  
 */
public class ImportUnpublishedContentsCronJob extends CronJob{
  
  private static final Log log = ExoLogger.getLogger(ImportUnpublishedContentsCronJob.class);

  private JobDataMap jobDataMap;

  public ImportUnpublishedContentsCronJob(InitParams params) throws Exception {
    super(params);
    if (log.isInfoEnabled()) {
      log.info("Start Init ImportUnpublishedContentsCronJob");
    }
    ExoProperties props = params.getPropertiesParam("importUnpublishedContentsCronJob.generalParams")
                                .getProperties();
    jobDataMap = new JobDataMap();      
    String contentState = props.getProperty("contentState");
    jobDataMap.put("contentState", contentState);
    String stagingStorage = props.getProperty("stagingStorage");
    jobDataMap.put("stagingStorage", stagingStorage);
    String predefinedPath = props.getProperty("predefinedPath");
    jobDataMap.put("predefinedPath", predefinedPath);
    if (log.isInfoEnabled()) {
      log.info("CronJob Param... unpublishedContents : "+contentState+", predefinedPath : " + predefinedPath);

      log.info("End Init ImportUnpublishedContentsCronJob");
    }
  }

  public JobDataMap getJobDataMap() {
    return jobDataMap;
  }
}
