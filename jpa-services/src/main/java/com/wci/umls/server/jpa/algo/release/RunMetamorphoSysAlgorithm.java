/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.codehaus.plexus.util.FileUtils;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;

/**
 * Algorithm to write the RRF index files.
 */
public class RunMetamorphoSysAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** Log bridge for collecting output */
  private PrintWriter logBridge = new PrintWriter(new StringWriter()) {

    /* see superclass */
    @Override
    public void println(String line) {
      try {
        logInfo("    " + line);
        commitClearBegin();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * Instantiates an empty {@link RunMetamorphoSysAlgorithm}.
   *
   * @throws Exception the exception
   */
  public RunMetamorphoSysAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RUNMMSYS");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();

    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final File pathMeta = new File(path, "/META");
    final File pathRelease = new File(path, getProcess().getVersion());

    // Expect that "path/META/mmsys.zip exists"
    if (!new File(pathMeta, "mmsys.zip").exists()) {
      throw new Exception(
          "Unexpected missing file = " + pathMeta + "/mmsys.zip");
    }

    // Expect that "path/$release/META exists"
    if (!new File(pathRelease, "META").exists()) {
      throw new Exception("Unexpected missing file = " + pathRelease + "/META");
    }

    // Expect that "path/$release/METASUBSET exists"
    if (!new File(pathRelease, "METASUBSET").exists()) {
      throw new Exception(
          "Unexpected missing file = " + pathRelease + "/METASUBSET");
    }

    // Expect that "path/$release/MMSYS does NOT exists"
    if (new File(pathRelease, "MMSYS").exists()) {
      throw new Exception(
          "Unexpected directory exists = " + pathRelease + "/MMSYS");
    }

    return result;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    setSteps(4);

    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final File pathMeta = new File(path, "/META");
    final File pathRelease = new File(path, getProcess().getVersion());

    // Unzip "path/META/mmsys.zip" into "path/$release/MMSYS"
    logInfo("  Unzip " + pathMeta.getPath() + "/mmsys.zip");
    commitClearBegin();
    new File(pathRelease, "MMSYS").mkdirs();
    ConfigUtility.unzip(pathMeta.getPath() + "/mmsys.zip",
        pathRelease.getPath() + "/MMSYS");
    updateProgress();

    // Write release.dat
    logInfo("  Write release.dat file(s)");
    commitClearBegin();
    final File mmsysReleaseDat = new File(config.getProperty("source.data.dir")
        + "/" + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/MMSYS/release.dat");
    final File mmsysReleaseConfigDat =
        new File(config.getProperty("source.data.dir") + "/"
            + getProcess().getInputPath() + "/" + getProcess().getVersion()
            + "/MMSYS/config/" + getProcess().getVersion() + "/release.dat");
    final File metaReleaseDat = new File(config.getProperty("source.data.dir")
        + "/" + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/META/release.dat");
    final File releaseDat = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/release.dat");

    final StringBuilder data = new StringBuilder();
    data.append("umls.release.name=" + getProcess().getVersion()).append("\n");
    data.append("umls.release.description=Base Release for "
        + getProcess().getVersion()).append("\n");
    /*
     * SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd"); data.append(
     * "umls.release.date=" + df.format(getProcess().getVersion() + "01"))
     * .append("\n");
     */
    data.append("umls.release.date=").append(getProcess().getVersion() + "01")
        .append("\n");
    data.append("nlm.build.date=")
        .append(new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()))
        .append("\n");

    // Write release.dat files (top-level and in META)
    FileUtils.fileWrite(releaseDat.getPath(), data.toString());
    FileUtils.fileWrite(metaReleaseDat.getPath(), data.toString());
    FileUtils.fileWrite(mmsysReleaseDat.getPath(), data.toString());
    FileUtils.fileWrite(mmsysReleaseConfigDat.getPath(), data.toString());
    updateProgress();

    // Run "make_config.csh"
    logInfo("  Build MMSYS config files from data");
    commitClearBegin();
    final String binDir = ConfigUtility.getHomeDirs().get("bin");
    // Assumes "lvg" dir exists at same level as "config"
    final String lvgDir = ConfigUtility.getHomeDirs().get("lvg");
    String[] env = new String[] {};
    if (new File(lvgDir).exists()) {
      env = new String[] {
          "LVG_HOME=" + lvgDir
      };
    }
    final String cmd = binDir + "/make_config.csh";
    final String meta = pathRelease.getPath() + "/META";
    final String net = path.getPath() + "/NET";
    final String mmsys = pathRelease.getPath() + "/MMSYS";
    ConfigUtility.exec(new String[] {
        cmd, meta, net, mmsys
    }, env, false, binDir, logBridge, true);

    updateProgress();

    // Run metamorphoSys
    logInfo("  Run MetamorphoSys");
    commitClearBegin();

    // Override user configuration settings
    Properties subsetConfig = new Properties();
    subsetConfig.load(new FileInputStream(
        new File(new File(new File(new File(pathRelease, "MMSYS"), "config"),
            getProcess().getVersion()), "user.a.prop")));
    subsetConfig.setProperty("mmsys_output_stream",
        "gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream");
    subsetConfig.setProperty("mmsys_input_stream",
        "gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysInputStream");
    // keep all sources, assume default config for all other filters
    subsetConfig.setProperty(
        "gov.nih.nlm.umls.mmsys.filter.SourceListFilter.remove_selected_sources",
        "true");
    subsetConfig.setProperty(
        "gov.nih.nlm.umls.mmsys.filter.SourceListFilter.selected_sources", "");
    // re-write config file
    subsetConfig.store(
        new FileOutputStream(
            new File(new File(pathRelease, "log"), "mmsys.prop")),
        "MRD configuration");

    // To run MetamorphoSys in batch subsetting mode,
    // it is now way easier to just invoke Java directly.
    // MRD does not use plugin framework so configuring it would be difficult

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      // Try as solaris
      ConfigUtility.exec(new String[] {
          pathRelease.getPath() + "/MMSYS/jre/windows64/bin/java",
          "-Djava.awt.headless=true",
          "-Djpf.boot.config=" + pathRelease.getPath()
              + "\\MMSYS\\etc\\subset.boot.properties",
          "-Dlog4j.configuration=etc\\subset.log4j.properties",
          "-Dscript_type=.sh", "-Dfile.encoding=UTF-8", "-Xms600M", "-Xmx1400M",
          "-Dinput.uri=" + pathRelease.getPath() + "\\META",
          "-Doutput.uri=" + pathRelease.getPath() + "\\METASUBSET",
          "-Dmmsys.config.uri=" + pathRelease.getPath() + "\\log\\mmsys.prop",
          "org.java.plugin.boot.Boot"
      }, new String[] {
          "CLASSPATH=" + pathRelease.getPath() + "\\MMSYS;"
              + pathRelease.getPath() + "\\MMSYS\\lib\\jpf-boot.jar"
      }, false, new File(pathRelease.getPath(), "\\MMSYS").getPath(), logBridge,
          false);
    } else {
      // If fails as solaris, try as linux
      ConfigUtility.exec(new String[] {
          pathRelease.getPath() + "/MMSYS/jre/linux/bin/java",
          "-Djava.awt.headless=true",
          "-Djpf.boot.config=" + pathRelease.getPath()
              + "/MMSYS/etc/subset.boot.properties",
          "-Dlog4j.configuration=etc/subset.log4j.properties",
          "-Dscript_type=.sh", "-Dfile.encoding=UTF-8", "-Xms600M", "-Xmx1400M",
          "-Dinput.uri=" + pathRelease.getPath() + "/META",
          "-Doutput.uri=" + pathRelease.getPath() + "/METASUBSET",
          "-Dmmsys.config.uri=" + pathRelease.getPath() + "/log/mmsys.prop",
          "org.java.plugin.boot.Boot"
      }, new String[] {
          "CLASSPATH=" + pathRelease.getPath() + "/MMSYS:"
              + pathRelease.getPath() + "/MMSYS/lib/jpf-boot.jar"
      }, false, new File(pathRelease.getPath(), "/MMSYS").getPath(), logBridge,
          false);
    }

    updateProgress();

    // Copy the files MRDOC, MRCOLS, MRFILES, release.dat
    for (final String file : new String[] {
        "MRDOC.RRF", "MRFILES.RRF", "MRCOLS.RRF", "release.dat"
    }) {
      FileUtils.copyFile(new File(pathRelease.getPath() + "/META", file),
          new File(pathRelease.getPath(), "METASUBSET"));
    }

    logInfo("Finishing " + getName());
    commitClearBegin();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());

    // Remove the MMSYS directory
    final File pathRelease = new File(config.getProperty("source.data.dir")
        + "/" + getProcess().getInputPath() + "/" + getProcess().getVersion());
    logInfo("  Remove directory = " + pathRelease + "/MMSYS");
    FileUtils.deleteDirectory(new File(pathRelease, "MMSYS"));
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
