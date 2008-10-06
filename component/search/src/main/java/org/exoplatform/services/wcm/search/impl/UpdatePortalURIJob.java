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

package org.exoplatform.services.wcm.search.impl;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.wcm.search.WcmSearchService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 19, 2008  
 */
public class UpdatePortalURIJob extends BaseJob {
  
  private static Log log_ = ExoLogger.getLogger("job.UpdatePortalURIJob");
  
	public void execute(JobExecutionContext context) throws JobExecutionException {
	  log_.info("running portal page indexing ");
		try{
			ExoContainer container = ExoContainerContext.getCurrentContainer();			
			WcmSearchService searchService = (WcmSearchService)container.getComponentInstanceOfType(WcmSearchService.class) ;
			searchService.updatePagesCache();
		}catch (Exception e) {				 
		}        
	}
}
