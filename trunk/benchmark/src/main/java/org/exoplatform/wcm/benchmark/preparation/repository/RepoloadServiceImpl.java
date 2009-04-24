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

  // Logging
  protected Log               log                = ExoLogger.getLogger("repload.RepoloadService");

  // Reference to the Repository Service
  protected RepositoryService repositoryService  = null;

  // Root node
  protected Node              rootNode;

  // Configuration variables
  protected int               fileSize           = 0;

  protected int               l1Count            = 0;

  protected int               l2Count            = 0;

  protected int               l3Count            = 0;

  protected int               l4Count            = 0;

  protected String            mimeType           = null;

  protected String            repository         = null;

  protected String            root               = null;

  protected String            workspace          = null;

  protected int               firstWord          = 10;

  protected int               secondWord         = 10;

  protected int               startIndex         = 10;

  private final Random        random;

  private int                 spetialTitlesCount = 7;

  /**
   * Constructor Initializes the service
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
   * Constructor Initializes the service
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
   * Create the root node of the tree
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
   * Triggers the load
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

  public void stop() {
    // Do nothing
  }

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

  public Node getRootNode() {
    return rootNode;
  }
}
