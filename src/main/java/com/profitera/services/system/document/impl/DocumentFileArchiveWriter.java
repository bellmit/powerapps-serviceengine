package com.profitera.services.system.document.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.profitera.math.MathUtil;
import com.profitera.util.Copy;
import com.profitera.util.Strings;

public class DocumentFileArchiveWriter {
  private enum State {NONE, STARTED, FINISHED};
  private final File finalPath;
  private final File swapFile;
  private State state = State.NONE;
  private FileChannel swapFileChannel;
  private ZipOutputStream swapZipOut;
  private final MessageDigest digest;
  private int fragmentSetCount;

  public DocumentFileArchiveWriter(File swapFile, File finalPath, MessageDigest d) {
    this.swapFile = swapFile;
    this.finalPath = finalPath;
    this.digest = d;
  }
  
  public void start() throws IOException {
    if (state != State.NONE) {
      throw new IllegalStateException("Can not start archive to '" + swapFile.getAbsolutePath() + "' because archive is already initialized");
    }
    // To start we grab the swap file lock, creating the file if it is not there
    // I don't care about the return value, if it is there already or not I just want to
    // make sure it is there now, we just let the IOException bubble up for a failure
    swapFile.createNewFile();
    try {
      swapFileChannel = new RandomAccessFile(swapFile, "rw").getChannel();
      FileLock swapFileLock = swapFileChannel.tryLock();
      if (swapFileLock == null) {
        throw new IOException("Failed to acquire file lock on '" + swapFile.getAbsolutePath() + "'");
      }
      swapZipOut = new ZipOutputStream(Channels.newOutputStream(swapFileChannel));
    } catch (IOException e) {
      swapFileChannel.close();
      throw e;
    }
    digest.reset();
    state = State.STARTED;
    fragmentSetCount = 0;
  }
  
  public void archiveFragments(List<String> fragments) throws UnsupportedEncodingException, IOException {
    if (state != State.STARTED) {
      throw new IllegalStateException("Can not archive to '" + swapFile.getAbsolutePath() + "' because archive is not properly initialized");
    }
    try {
      for (String string : fragments) {
        fragmentSetCount++;
        swapZipOut.putNextEntry(new ZipEntry(fragmentSetCount + ""));
      	byte[] bytes = string.getBytes("UTF8");
        swapZipOut.write(bytes);
        digest.update(bytes);
      }
    } catch (IOException e) {
      state = State.NONE;
      swapZipOut.close();
      // Closing the channel releases the lock
      swapFileChannel.close();
      throw e;
    }
  }
  
  public void end() throws IOException {
    if (state != State.STARTED) {
      throw new IllegalStateException("Can not complete archive to '" + finalPath.getAbsolutePath() + "' because archive is not properly initialized");
    }
    try {
      String paddedHash =getPaddedHash(digest);
      byte[] bytes = paddedHash.getBytes("UTF8");
      swapZipOut.putNextEntry(new ZipEntry("hash"));
      swapZipOut.write(bytes);
      swapFileChannel.force(true);
      swapZipOut.close();
      swapFileChannel.close();
    } catch (IOException e) {
      state = State.NONE;
      swapFileChannel.close();
      throw e;
    }
    if (!finalPath.getParentFile().exists()) {
      finalPath.getParentFile().mkdirs();
    }
    if (!swapFile.renameTo(finalPath)) {
      try {
        Copy.toFile(swapFile, finalPath);
      } catch (IOException e) {
        state = State.NONE;
        throw e;
      }
    }
    state = State.FINISHED;
  }

  static String getPaddedHash(MessageDigest digest) {
    return MathUtil.getPaddedBase16(digest.digest(), 32);
  }

}
