/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.util.FileUtils;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Algorithm to prepare MetamorphoSys.
 */
public class PrepareMetamorphoSysAlgorithm extends AbstractAlgorithm {

  /** The email. */
  private String email;
  
  private List<String> fileList = new ArrayList<>();

  /**
   * Instantiates an empty {@link PrepareMetamorphoSysAlgorithm}.
   *
   * @throws Exception the exception
   */
  public PrepareMetamorphoSysAlgorithm() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    // Check the process input path
    final String path =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    final File pathAsFile = new File(path);
    if (!pathAsFile.exists()) {
      throw new LocalException(
          "Input path specified in process does not exist");
    }

    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    
    //
    //Prepare mmsys.zip with updated release version
    //
    final File inputPath = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final File pathMeta = new File(inputPath, "/META");
    final File pathTemp = new File(pathMeta, "/x");
    logInfo("  pathTemp absolute: " + pathTemp.getAbsolutePath());
    logInfo("  pathTemp canonical: " + pathTemp.getCanonicalPath());
    
    // If temp dir "path/x  exists already, remove it"
    if (pathTemp.exists()) {
      logInfo("  Remove directory = " + pathTemp);
      FileUtils.deleteDirectory(pathTemp);
    }
    
    // Make backup of mmsys.zip, if it doesn't already exist e.g. mmsys.202106.zip
	if (!new File(pathMeta.getPath() + "/mmsys." + getProcess().getVersion() + ".zip").exists()) {
		Path copied = Paths.get(pathMeta.getPath() + "/mmsys." + getProcess().getVersion() + ".zip");
		Path originalPath = new File(pathMeta.getPath() + "/mmsys.zip").toPath();
		Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
	}
    
    // Unzip "path/META/mmsys.zip" into "path/x"
    logInfo("  Unzip " + pathMeta.getPath() + "/mmsys.zip");
    commitClearBegin();
    ConfigUtility.unzip(pathMeta.getPath() + "/mmsys.zip",
        pathTemp.getPath());

    //"config" (path/META/x/config)
    final File pathConfig = new File(pathTemp, "/config");
    // get most recent release folder in the config directory
    final File previousReleaseFolder = getLastModified(pathConfig);
    final String previousRelease = previousReleaseFolder.getName();
    final File currentReleaseFolder = new File (pathConfig, "/" + getProcess().getVersion());
    
    //Rename the previous release directory to current release (e.g. % mv 201203 201209)
    previousReleaseFolder.renameTo(currentReleaseFolder);
    
    //Edit mmsys.prop to refer to new current release version (e.g. 201209)
    //Edit contents of current release directory to refer to this as the release version
    //    umls.prop
    //    release.dat
    //    user.*.prop
    //    Don't worry about the other contents, the build process will rewrite with corrected config files, the placeholders just need to exist.
    replaceAllInFile(pathConfig.getAbsolutePath(), "mmsys.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "umls.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "user.a.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "user.b.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "user.c.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "user.d.prop", previousRelease, currentReleaseFolder.getName());
    replaceAllInFile(currentReleaseFolder.getAbsolutePath(), "release.dat", previousRelease, currentReleaseFolder.getName());    
    
    //Delete original /local/content/MEME/MEME5/mr/META/mmsys.zip
    new File(pathMeta.getPath() + "/mmsys.zip").delete();
    
    //Zip the contents of path/x into revised path/META/mmsys.zip 
    //compressDirectory(pathTemp.getAbsolutePath(), pathMeta + "/mmsys.zip");
    //zip(pathTemp.getCanonicalPath(), pathMeta + "/mmsys.zip");

    ZipParameters params = new ZipParameters();
    params.setIncludeRootFolder(false);
    new ZipFile(pathMeta + "/mmsys.zip").addFolder(new File(pathTemp.getAbsolutePath()), params);
    
    logInfo("Finished " + getName());
  }
  
  public void replaceAllInFile(String folder, String file, String previousRelease, String currentRelease) throws Exception {
	  Path path = Paths.get(folder, file);
	    Charset charset = StandardCharsets.UTF_8;

	    String content = new String(Files.readAllBytes(path), charset);
	    content = content.replaceAll(previousRelease, currentRelease);
	    Files.write(path, content.getBytes(charset));
  }

  
  public static File getLastModified(File directoryFile)
  {
      File[] files = directoryFile.listFiles(File::isDirectory);
      long lastModifiedTime = Long.MIN_VALUE;
      File chosenFile = null;

      if (files != null)
      {
          for (File file : files)
          {
              if (file.lastModified() > lastModifiedTime)
              {
                  chosenFile = file;
                  lastModifiedTime = file.lastModified();
              }
          }
      }

      return chosenFile;
  }
  
  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
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
    if (p.getProperty("email") != null) {
      email = p.getProperty("email");
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    final AlgorithmParameter email = new AlgorithmParameterJpa(
        "Notification emails", "email", "Email addresses for notification",
        "e.g. a@b.com", 4000, AlgorithmParameter.Type.TEXT,
        ConfigUtility.getConfigProperties().getProperty("mail.smtp.to"));
    params.add(email);
    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }

  /**
   * Returns the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email.
   *
   * @param email the email
   */
  public void setEmail(String email) {
    this.email = email;
  }
  
  public static void zip(final String sourcNoteseDirPath, final String zipFilePath) throws IOException {
      Path zipFile = Files.createFile(Paths.get(zipFilePath));

      Path sourceDirPath = Paths.get(sourcNoteseDirPath);
      try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
           Stream<Path> paths = Files.walk(sourceDirPath)) {
          paths
                  .filter(path -> !Files.isDirectory(path))
                  .forEach(path -> {
                      ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                      try {
                          zipOutputStream.putNextEntry(zipEntry);
                          Files.copy(path, zipOutputStream);
                          zipOutputStream.closeEntry();
                      } catch (IOException e) {
                          System.err.println(e);
                      }
                  });
      }
      System.out.println("Zip is created at : "+zipFile);
  }

  private void compressDirectory(String dir, String zipFile) {
      File directory = new File(dir);
      getFileList(directory);

      try (FileOutputStream fos = new FileOutputStream(zipFile);
           ZipOutputStream zos = new ZipOutputStream(fos)) {

          for (String filePath : fileList) {
              System.out.println("Compressing: " + filePath);

              // Creates a zip entry.
              String name = filePath.substring(
                  directory.getAbsolutePath().length() + 1,
                  filePath.length());

              ZipEntry zipEntry = new ZipEntry(name);
              zos.putNextEntry(zipEntry);

              // Read file content and write to zip output stream.
              try (FileInputStream fis = new FileInputStream(filePath)) {
                  byte[] buffer = new byte[1024];
                  int length;
                  while ((length = fis.read(buffer)) > 0) {
                      zos.write(buffer, 0, length);
                  }

                  // Close the zip entry.
                  zos.closeEntry();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  /**
   * Get files list from the directory recursive to the sub directory.
   */
  private void getFileList(File directory) {
      File[] files = directory.listFiles();
      if (files != null && files.length > 0) {
          for (File file : files) {
              if (file.isFile()) {
                  fileList.add(file.getAbsolutePath());
              } else {
                  getFileList(file);
              }
          }
      }

  }

}
