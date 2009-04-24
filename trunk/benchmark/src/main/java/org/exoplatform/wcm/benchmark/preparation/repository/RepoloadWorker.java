package org.exoplatform.wcm.benchmark.preparation.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Asynchronous creation of the trees starting from level 2
 */
public class RepoloadWorker extends Thread {

  // Reference to the Repository Loader service
  RepoloadServiceImpl          repoload          = null;

  private final static String  SPETIAL_TITLE     = "eXoPlatformTitle";

  // Index of the tree to create
  int                          l1;

  // Percent for first word
  int                          firstWord;

  // Percent for second word
  int                          secontWord;

  int                          startIndex;

  int                          delitelFirstWord;

  int                          delitelSecondWord;

  int                          totalFile;

  // Logging
  protected Log                log               = ExoLogger.getLogger("repload.RepoloadWorker");

  // JCR Session used
  Session                      session           = null;

  private String[]             fileNames;

  private List<byte[]>         fileBuffer;

  private final Random         random;

  private long                 lastStop          = 0;

  private final CountDownLatch loch;

  private int                  spetialTitleCount = 0;

  private final boolean[]      titlesMap;

  /**
   * Constructor Initializes variables
   * 
   * @param randomPeriod
   * @param loch
   * @throws IOException
   */
  public RepoloadWorker(RepoloadServiceImpl repoload,
                        int l1,
                        int firstWord,
                        int secondWord,
                        int startIndex,
                        boolean[] randomPeriod,
                        CountDownLatch loch) throws IOException {
    this.repoload = repoload;
    this.l1 = l1;
    this.firstWord = firstWord;
    this.secontWord = secondWord;
    this.startIndex = startIndex;
    this.titlesMap = randomPeriod;
    this.loch = loch;
    this.random = new Random();

    delitelFirstWord = (int) (100 / firstWord);
    delitelSecondWord = (int) (100 / secondWord);

    totalFile = 0;

    this.setName("Repoload worker " + l1);
    this.fileNames = new String[] { "/conf/datafile.txt", "/conf/ferst_word.txt", "/conf/second_word.txt" };
    this.fileBuffer = new ArrayList<byte[]>(fileNames.length);
    loadFiles();
  }

  /**
   * Creates the tree from leve 2 and the files in the leaves
   */
  public void run() {

    Session session = null;
    FastAPI fastAPI = null;

    try {
      // Obtain a JCR Session
      session = repoload.repositoryService.getRepository(repoload.repository)
                                          .getSystemSession(repoload.workspace);

      // Instantiate a Fast API object
      fastAPI = new FastAPI((SessionImpl) session);

      // Create the level 1 node
      TransientNodeData nodeL1 = fastAPI.addNode("node" + (l1 + startIndex),
                                                 this.l1,
                                                 (NodeImpl) repoload.rootNode);

      for (int l2 = 0; l2 < repoload.l2Count; l2++) {
        // Create the level 2 node
        TransientNodeData nodeL2 = fastAPI.addNode("node" + l2, l2, nodeL1);

        for (int l3 = 0; l3 < repoload.l3Count; l3++) {
          // Create the level 3 node
          TransientNodeData nodeL3 = fastAPI.addNode("node" + l3, l3, nodeL2);

          // Stores the current time
          long before = System.currentTimeMillis();

          String prefix = l1 + "-" + l2 + "-" + l3;

          for (int l4 = 0; l4 < repoload.l4Count; l4++) {

            // Create the file
            String fileName = prefix + "-" + l4;

            String title = fileName;
            if (titlesMap[totalFile]) {
              title = SPETIAL_TITLE;
              spetialTitleCount++;
            }

            fastAPI.addNode_file(fileName + ".txt",
                                 l4,
                                 prefix,
                                 nodeL3,
                                 getFileValueData(),
                                 repoload.mimeType,
                                 title);
          }
          fastAPI.commit();

          // Display the time needed to create the two underlying levels
          log.info(prefix + " loaded in " + (System.currentTimeMillis() - before) + "\t"
              + (totalFile - 1) + " document loaded.");

        }
        log.info(memoryInfo() + "\n");
      }
    } catch (Exception e) {
      log.error("Exception in worker " + this.l1, e);
    } finally {
      try {
        fastAPI.release();
        loch.countDown();
      } catch (Exception ignore) {
      }
    }
  }

  public int getSpetialTitleCount() {
    return spetialTitleCount;
  }

  public TransientValueData getFileValueData() {

    int fileNumber = 0;
    if (totalFile % delitelFirstWord == 0)
      fileNumber = 1;

    if (totalFile % delitelSecondWord == 1)
      fileNumber = 2;

    totalFile++;

    return new TransientValueData(new ByteArrayInputStream(fileBuffer.get(fileNumber)));
  }

  /**
   * Read file content into memory buffer
   * 
   * @throws IOException
   */
  private void loadFiles() throws IOException {        
    for (int i = 0; i < fileNames.length; i++) {
      // File file = new File(fileNames[i]);
      System.out.println("========>Load file name:" + fileNames[i]);
      InputStream is = this.getClass().getResourceAsStream(fileNames[i]);// /new      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      byte[] buf = new byte[1024];
      int len;
      while ((len = is.read(buf)) > 0) {
        // instead of writing to a ByteArrayOutputStream you can
        // write to the FileOutputStream here (see the comments later
        // after creating the byte[] variable called data
        bos.write(buf, 0, len);
      }
      byte[] data = bos.toByteArray();
      fileBuffer.add(data);
    }
  }

  /**
   * Return runtime memory information
   * 
   * @return
   */
  private String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of "
        + mb(Runtime.getRuntime().totalMemory()) + "M (max: "
        + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }

  private String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d / (1024d * 1024d)) / 100d);
  }

  /**
   * Return total number of loaded files
   * 
   * @return
   */
  public int getTotalFile() {
    return totalFile;
  }
}
