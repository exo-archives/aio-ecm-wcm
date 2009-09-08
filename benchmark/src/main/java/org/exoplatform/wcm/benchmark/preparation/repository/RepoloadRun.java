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

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

// TODO: Auto-generated Javadoc
/**
 * The Class RepoloadRun.
 */
public class RepoloadRun {

  /** The nt folder count. */
  private int                 ntFolderCount;

  /** The nt file count. */
  private int                 ntFileCount;

  /** The read property. */
  private boolean             readProperty = true;

  /** The repository service. */
  private RepositoryService   repositoryService;

  /** The repoload. */
  private RepoloadServiceImpl repoload;

  /** The l1. */
  private int                 l1           = 0;

  /** The l2. */
  private int                 l2           = 0;

  /** The l3. */
  private int                 l3           = 0;

  /** The l4. */
  private int                 l4           = 0;

  /** The mime type. */
  private String              mimeType     = "";

  /** The repository. */
  private String              repository   = "";

  /** The workspace. */
  private String              workspace    = "";

  /** The start node. */
  private String              startNode    = "";

  /** The first word. */
  private int                 firstWord    = 1;

  /** The second word. */
  private int                 secondWord   = 2;

  /** The start index. */
  private int                 startIndex   = 0;

  /** The random period. */
  private int                 randomPeriod = 0;

  /** The config. */
  @SuppressWarnings("unused")
  private String              config       = "conf/hsql/test-configuration.xml";

  /** The container. */
  private StandaloneContainer container;

  // argument config
  // 8 150 100 100 "text/html" "repository" "production" "/teststorage/root2" 2
  // 10 8 ora

  /**
   * Instantiates a new repoload run.
   * 
   * @param args the args
   */
  public RepoloadRun(String[] args) {
    l1 = Integer.valueOf(args[0]).intValue();
    l2 = Integer.valueOf(args[1]).intValue();
    l3 = Integer.valueOf(args[2]).intValue();
    l4 = Integer.valueOf(args[3]).intValue();

    mimeType = args[4];
    repository = args[5];
    workspace = args[6];
    startNode = args[7];

    firstWord = Integer.valueOf(args[8]).intValue();
    secondWord = Integer.valueOf(args[9]).intValue();
    startIndex = Integer.valueOf(args[10]).intValue();
    @SuppressWarnings("unused")
    String conf = args[11];
    printConfig();
  }

  /**
   * Prints the config.
   */
  private void printConfig() {
    System.out.println("tree = \t" + l1 + "-" + l2 + "-" + l3 + "-" + l4);
    System.out.println("mime type = \t" + mimeType);
    System.out.println("repository = \t" + repository);
    System.out.println("workspace = \t" + workspace);
    System.out.println("start node = \t" + startNode);
    System.out.println("first word, % = \t" + firstWord);
    System.out.println("second word, % = \t" + secondWord);
    System.out.println("start Index, % = \t" + startIndex);
    System.out.println("randomPeriod, % = \t" + randomPeriod);
    System.out.println("jcr config file, % = \t" + System.getProperty("jcr.configuration.file"));
  }

  /**
   * Sets the up.
   * 
   * @throws Exception the exception
   */
  public void setUp() throws Exception {

    StandaloneContainer.addConfigurationPath(System.getProperty("jcr.configuration.file"));

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "conf/login.conf");

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
  }

  /**
   * Stop.
   */
  public void stop() {
    container.stop();
  }

  /**
   * Run.
   */
  public void run() {
    repoload = new RepoloadServiceImpl(15000,
                                       l1,
                                       l2,
                                       l3,
                                       l4,
                                       mimeType,
                                       repository,
                                       startNode,
                                       workspace,
                                       repositoryService,
                                       firstWord,
                                       secondWord,
                                       startIndex,
                                       randomPeriod);

    repoload.start();

  }

  /**
   * The main method.
   * 
   * @param args the arguments
   */
  public static void main(String[] args) {
    try {
      RepoloadRun repoloadRun = new RepoloadRun(args);
      repoloadRun.setUp();
      repoloadRun.run();
      repoloadRun.stop();
      System.exit(0);
    } catch (Exception re) {
      re.printStackTrace();
    }
  }

  /**
   * Gets the nt folder count.
   * 
   * @return the ntFolderCount
   */
  public int getNtFolderCount() {
    return ntFolderCount;
  }

  /**
   * Sets the nt folder count.
   * 
   * @param ntFolderCount the ntFolderCount to set
   */
  public void setNtFolderCount(int ntFolderCount) {
    this.ntFolderCount = ntFolderCount;
  }

  /**
   * Gets the nt file count.
   * 
   * @return the ntFileCount
   */
  public int getNtFileCount() {
    return ntFileCount;
  }

  /**
   * Sets the nt file count.
   * 
   * @param ntFileCount the ntFileCount to set
   */
  public void setNtFileCount(int ntFileCount) {
    this.ntFileCount = ntFileCount;
  }

  /**
   * Checks if is read property.
   * 
   * @return the readProperty
   */
  public boolean isReadProperty() {
    return readProperty;
  }

  /**
   * Sets the read property.
   * 
   * @param readProperty the readProperty to set
   */
  public void setReadProperty(boolean readProperty) {
    this.readProperty = readProperty;
  }
}
