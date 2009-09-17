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
package org.exoplatform.wcm.benchmark.preparation.repository;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Repository Loader for CTI This loader builds a tree of 4 levels. All trees starting in the first
 * level are created using Threads.
 * 
 * @author Brice.Revenant@exoplatform.com
 */
public class RepoloadServiceImpl implements Startable {

  /** The log. */
  protected Log               log                = ExoLogger.getLogger("repload.RepoloadService");

  /** The repository service. */
  protected RepositoryService repositoryService  = null;

  /** The root node. */
  protected Node              rootNode;

  /** The file size. */
  protected int               fileSize           = 0;

  /** The l1 count. */
  protected int               l1Count            = 0;

  /** The l2 count. */
  protected int               l2Count            = 0;

  /** The l3 count. */
  protected int               l3Count            = 0;

  /** The l4 count. */
  protected int               l4Count            = 0;

  /** The mime type. */
  protected String            mimeType           = null;

  /** The repository. */
  protected String            repository         = null;

  /** The root. */
  protected String            root               = null;

  /** The workspace. */
  protected String            workspace          = null;

  /** The first word. */
  protected int               firstWord          = 10;

  /** The second word. */
  protected int               secondWord         = 10;

  /** The start index. */
  protected int               startIndex         = 10;

  /** The random. */
  private final Random        random;

  /** The spetial titles count. */
  private int                 spetialTitlesCount = 7;

  /**
   * Constructor Initializes the service.
   * 
   * @param params the params
   * @param repositoryService the repository service
   */
  public RepoloadServiceImpl(InitParams params, RepositoryService repositoryService) {

    // Cache the Repository Service
    this.repositoryService = repositoryService;

    // Retrieve configuration information
    PropertiesParam propertiesParam = params.getPropertiesParam("repoload.configuration");
    this.fileSize = Integer.parseInt(propertiesParam.getProperty("fileSize"));
    this.l1Count = Integer.parseInt(propertiesParam.getProperty("l1Count"));
    this.l2Count = Integer.parseInt(propertiesParam.getProperty("l2Count"));
    this.l3Count = Integer.parseInt(propertiesParam.getProperty("l3Count"));
    this.l4Count = Integer.parseInt(propertiesParam.getProperty("l4Count"));
    this.mimeType = propertiesParam.getProperty("mimeType");
    this.repository = propertiesParam.getProperty("repository");
    this.root = propertiesParam.getProperty("root");
    this.workspace = propertiesParam.getProperty("workspace");
    this.random = new Random();
  }

  /**
   * Constructor Initializes the service.
   * 
   * @param fSize the f size
   * @param l1Count the l1 count
   * @param l2Count the l2 count
   * @param l3Count the l3 count
   * @param l4Count the l4 count
   * @param mimeType the mime type
   * @param repository the repository
   * @param root the root
   * @param workspace the workspace
   * @param repositoryService the repository service
   * @param firstWord the first word
   * @param secondWord the second word
   * @param startIndex the start index
   * @param spetialTitlesCount the spetial titles count
   */
  public RepoloadServiceImpl(int fSize,
                             int l1Count,
                             int l2Count,
                             int l3Count,
                             int l4Count,
                             String mimeType,
                             String repository,
                             String root,
                             String workspace,
                             RepositoryService repositoryService,
                             int firstWord,
                             int secondWord,
                             int startIndex,
                             int spetialTitlesCount) {

    // Cache the Repository Service
    this.repositoryService = repositoryService;

    // Retrieve configuration information
    this.fileSize = fSize;
    this.l1Count = l1Count;
    this.l2Count = l2Count;
    this.l3Count = l3Count;
    this.l4Count = l4Count;
    this.mimeType = mimeType;
    this.repository = repository;
    this.root = root;
    this.workspace = workspace;
    this.firstWord = firstWord;
    this.secondWord = secondWord;
    this.startIndex = startIndex;
    this.spetialTitlesCount = spetialTitlesCount;
    this.random = new Random();
  }

  /**
   * Create the root node of the tree.
   */
  public void createRootTree() {
    Session session = null;
    try {
      session = this.repositoryService.getRepository(this.repository)
                                      .getSystemSession(this.workspace);
      if (!session.itemExists(this.root)) {
        this.log.info("Creating the root tree");
        String[] paths = this.root.split("/");
        rootNode = session.getRootNode();
        for (int i = 1; i < paths.length; i++) {
          if (rootNode.hasNode(paths[i])) {
            rootNode = rootNode.getNode(paths[i]);
          } else {
            rootNode = rootNode.addNode(paths[i]);
          }
        }
      } else {
        rootNode = (Node) session.getItem(this.root);
      }
    } catch (Exception e) {
      this.log.error("Exception while creating the tree node", e);
    } finally {
      try {
        session.save();
        session.logout();
      } catch (Exception ignore) {
      }
    }
  }

  /**
   * Triggers the load.
   */
  public void start() {
    try {
      // Stores the current time
      long before = System.currentTimeMillis();
      // Create the root of the tree
      createRootTree();
      // Start as many threads as there are items in the first level
      Thread[] workers = new Thread[l1Count];
      CountDownLatch loch = new CountDownLatch(l1Count);
      boolean[] map = parseBitMap(l1Count * l2Count * l3Count * l4Count, spetialTitlesCount);
      for (int l1 = 0; l1 < l1Count; l1++) {
        workers[l1] = new RepoloadWorker(this, l1, firstWord, secondWord, startIndex, map, loch);
        workers[l1].start();
      }
      // wait workers
      loch.await();
      int totalDocumentLoaded = 0;
      int totlaSpetialTitle = 0;
      for (int i = 0; i < workers.length; i++) {
        totalDocumentLoaded += ((RepoloadWorker) workers[i]).getTotalFile();
        totlaSpetialTitle += ((RepoloadWorker) workers[i]).getSpetialTitleCount();
      }
      // Display the time needed to load all documents
      log.info(totalDocumentLoaded + " documents loaded in "
          + ((System.currentTimeMillis() - before) / 60000) + " minutes. Spetial titles"
          + totlaSpetialTitle);
    } catch (Exception e) {
      log.error("Exception while starting the Repoload service", e);
    }
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
  }

  /**
   * Parses the bit map.
   * 
   * @param documents the documents
   * @param titles the titles
   * 
   * @return the boolean[]
   */
  public boolean[] parseBitMap(int documents, int titles) {
    boolean[] map = new boolean[documents];
    for (int i = 0; i < titles; i++) {
      int position = random.nextInt(documents);
      if (map[position]) {
        i--;
        continue;
      }
      map[position] = true;
    }
    return map;
  }

  /**
   * Gets the root node.
   * 
   * @return the root node
   */
  public Node getRootNode() {
    return rootNode;
  }
}
