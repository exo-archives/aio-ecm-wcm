package org.exoplatform.wcm.benchmark.preparation.repository;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

public class RepoloadRun {

  // protected Log log = ExoLogger.getLogger("repload.RepoloadRun");

  private int                 ntFolderCount;

  private int                 ntFileCount;

  private boolean             readProperty = true;

  private RepositoryService   repositoryService;

  private RepoloadServiceImpl repoload;

  private SessionImpl         session;

  private NodeImpl            root;

  private int                 l1           = 0;

  private int                 l2           = 0;

  private int                 l3           = 0;

  private int                 l4           = 0;

  private String              mimeType     = "";

  private String              repository   = "";

  private String              workspace    = "";

  private String              startNode    = "";

  private int                 firstWord    = 1;

  private int                 secondWord   = 2;

  private int                 startIndex   = 0;

  private int                 randomPeriod = 0;

  private String              config       = "conf/hsql/test-configuration.xml";

  private StandaloneContainer container;

  // argument config
  // 8 150 100 100 "text/html" "repository" "production" "/teststorage/root2" 2
  // 10 8 ora

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
    String conf = args[11];
    printConfig();
  }

  private void printConfig() {
    // log.info("tree = \t" + l1 + "-" + l2 + "-" + l3 + "-" + l4);
    // log.info("mime type = \t"+ mimeType);
    // log.info("repository = \t"+ repository);
    // log.info("workspace = \t"+ workspace);
    // log.info("start node = \t"+ startNode);
    // log.info("first word, % = \t"+ firstWord);
    // log.info("second word, % = \t"+ secondWord);
    // log.info("start Index, % = \t"+ startIndex);
    // log.info("database type, % = \t"+ config);
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

  public void setUp() throws Exception {

    StandaloneContainer.addConfigurationPath(System.getProperty("jcr.configuration.file"));

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "conf/login.conf");

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

    // Credentials credentials = new CredentialsImpl("admin",
    // "admin".toCharArray());
    // RepositoryImpl repository = (RepositoryImpl)
    // repositoryService.getDefaultRepository();

    // session = (SessionImpl) repository.login(credentials, "production");
    // root = (NodeImpl) session.getRootNode();
  }

  public void stop() {
    container.stop();
  }

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

  public static void main(String[] args) {
    try {
      RepoloadRun repoloadRun = new RepoloadRun(args);
      repoloadRun.setUp();
      repoloadRun.run();
      repoloadRun.stop();
      System.exit(0);
    } catch (Exception re) {
      // TODO: handle exception
      re.printStackTrace();
    }
  }

}
