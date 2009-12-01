/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.wcm.javascript;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * benjamin.paillereau@exoplatform.com
 * Nov 28, 2009
 */
public class JavascriptJob extends BaseJob {

  /** The log. */
  private static Log log = ExoLogger.getLogger(JavascriptJob.class);

  /* (non-Javadoc)
   * @see org.exoplatform.services.scheduler.BaseJob#execute(org.exoplatform.services.scheduler.JobContext)
   */
  public void execute(JobContext arg0) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    XJavascriptService jsService = 
      (XJavascriptService)container.getComponentInstanceOfType(XJavascriptService.class);
    if(jsService == null) return;
    try {
      jsService.start();
    } catch (Exception e) {
      log.error("Error when execute Javascript service updater", e);
    }
  }
}
