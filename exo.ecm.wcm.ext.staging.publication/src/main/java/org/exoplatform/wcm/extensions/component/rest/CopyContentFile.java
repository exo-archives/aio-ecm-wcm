package org.exoplatform.wcm.extensions.component.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringInputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.wcm.extensions.security.SHAMessageDigester;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
@URITemplate("/copyfile/")
public class CopyContentFile implements ResourceContainer {

  private static final Log    log         = ExoLogger.getLogger(CopyContentFile.class);

  private static final String OK_RESPONSE = "OK".intern();

  private static final String KO_RESPONSE = "KO".intern();

  private static String       stagingStorage;

  private static String       targetKey;

  public CopyContentFile(InitParams params) {
    stagingStorage = params.getValueParam("stagingStorage").getValue();
    targetKey = params.getValueParam("targetKey").getValue();
  }

  @HTTPMethod("POST")
  @URITemplate("/copy/")
  @InputTransformer(StringInputTransformer.class)
  @OutputTransformer(StringOutputTransformer.class)
  public Response copyFile(String param) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("Start Execute CopyContentFile Web Service");
    }
    try {

      String[] tabParam = param.split("&&");
      String timesTamp = tabParam[0].split("=")[1];
      String clientHash = tabParam[1].split("=")[1];
      String contents = tabParam[2].split("contentsfile=")[1];
      String[] tab = targetKey.split("$TIMESTAMP");
      StringBuffer resultKey = new StringBuffer();
      for (int k = 0; k < tab.length; k++) {
        resultKey.append(tab[k]);
        if (k != (tab.length - 1))
          resultKey.append(timesTamp);
      }
      String serverHash = SHAMessageDigester.getHash(resultKey.toString());
      if (serverHash != null && serverHash.equals(clientHash)) {
        Date date = new Date();
        long time = date.getTime();
        File stagingFolder = new File(stagingStorage);
        if (!stagingFolder.exists())
          stagingFolder.mkdirs();
        File contentsFile = new File(stagingStorage + File.separator + clientHash + "-" + time
            + ".xml");
        OutputStream ops = new FileOutputStream(contentsFile);
        ops.write(contents.getBytes());
        ops.close();
      } else {
        log.warn("Anthentification failed...");
        return Response.Builder.ok(KO_RESPONSE + "...Anthentification failed", "text/plain")
                               .build();
      }
    } catch (Exception ex) {
      log.error("error when copying content file" + ex.getMessage());
      return Response.Builder.ok(KO_RESPONSE + "..." + ex.getMessage(), "text/plain").build();
    }
    if (log.isDebugEnabled()) {
      log.debug("Start Execute CopyContentFile Web Service");
    }
    return Response.Builder.ok(OK_RESPONSE
                                   + "...content has been successfully copied in the production server",
                               "text/plain")
                           .build();

  }

}
