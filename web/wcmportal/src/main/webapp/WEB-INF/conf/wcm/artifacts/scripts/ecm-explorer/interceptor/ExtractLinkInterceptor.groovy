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

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Jul 28, 2008  
 */
 
import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.wcm.webcontent.LinkExtractorService;
import org.exoplatform.services.jcr.RepositoryService;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.NodeIterator;

public class SystemOutInterceptor implements CmsScript {

	private LinkExtractorService linkExtractor_;
	private RepositoryService repositoryService_;
	
	public SystemOutInterceptor(LinkExtractorService linkExtractor, RepositoryService repositoryService) {
		linkExtractor_ = linkExtractor;
		repositoryService_ = repositoryService;
	}
	
	public void execute(Object context) {
		String sContext = context.toString();
    int iWorkspace = sContext.indexOf("&workspaceName=");
    int iRepository = sContext.indexOf("&repository=");
		String sNodePath = sContext.substring(1, iWorkspace);
    String sWorkspace = sContext.substring(iWorkspace + 15, iRepository);
    String sRepository = sContext.substring(iRepository + 12);
    
    Session session = repositoryService_.getRepository(sRepository).getSystemSession(sWorkspace) ;
    Node root = session.getRootNode();
    Node webContent = root.getNode(sNodePath);

		linkExtractor_.createLinkNode(webContent);
	}
	
	public void setParams(String[] params) {}
}