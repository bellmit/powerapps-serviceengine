package com.profitera.util.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;

import com.profitera.io.Base64;
import com.profitera.util.io.FileUtil;

public class BouncyCastleLicenseGenerator {
  private static final int EX = 16;
  private static final int ENCRYPTION_KEY_STRENGTH = 8 * EX;
  public static final String EXPIRY_DATE = "AGDDSS";
  public static final String CONCURRENT_USER = "CU1232";
  public static final String NO_OF_ACCOUNTS = "ACC64645";
  public static final String PROCESSOR_COUNT = "P145345";
  public static final String AGENCY_COUNT = "A3243";
  public static final String HIGH_AVAILABILITY = "HA15239";
  public static final String EXPIRY_WARNING_DAY_COUNT = "EXPIRY_WARNING_DAY_COUNT";

  private static final String LICENSE_FILE_NAME = "license.key";
  private static final String KEY_FILE_NAME = "key.key";
  private final long seed = 14762254736437389l;
  public static final String LICENSE_DATE_FORMAT = "yyyyMMdd";

  public enum NamedUserType {
    Internal1(1), External2(2), ExternalManagement3(3), UserType4(4);
    private long typeId;

    private NamedUserType(long typeId) {
      this.typeId = typeId;
    }

    public long getTypeId() {
      return typeId;
    }

    public String getTag() {
      String no = "";
      if (getTypeId() != 1L) {
        no = "_" + getTypeId();
      }
      return "NAMED_USER" + no;
    }
  };

  private Long getTextLong(String name, Map<String, Object> values) {
    if (values.get(name) == null) {
      return null;
    } else if (values.get(name).equals("")) {
      return null;
    } else {
      return new Long((String) values.get(name));
    }
  }

  public void generate(Map<String, Object> values, String oPath)
      throws InvalidCipherTextException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException,
      BadPaddingException, ParseException {
    /*
     * get the output specified and construct the properties into one line
     * string get the random generated key
     */
    if (values == null) {
      throw new RuntimeException(
          "Invalid value for license details, license generator failed.");
    }
    Map<NamedUserType, Long> namedUsers = new HashMap<NamedUserType, Long>();
    SimpleDateFormat df = new SimpleDateFormat(LICENSE_DATE_FORMAT);
    Date dateBased = values.get(EXPIRY_DATE) != null
        && !values.get(EXPIRY_DATE).equals("") ? df.parse((String) values
        .get(EXPIRY_DATE)) : null;
    Long alarmDays = getTextLong(EXPIRY_WARNING_DAY_COUNT, values);
    NamedUserType[] types = NamedUserType.values();
    for (int i = 0; i < types.length; i++) {
      String name = types[i].getTag();
      Long count = getTextLong(name, values);
      add(count, types[i], namedUsers);
    }
    Long cocurrentUser = getTextLong(CONCURRENT_USER, values);
    Long noOfAccounts = getTextLong(NO_OF_ACCOUNTS, values);
    Long proc = getTextLong(PROCESSOR_COUNT, values);
    Boolean ha = values.get(HIGH_AVAILABILITY) != null
        && !values.get(HIGH_AVAILABILITY).equals("") ? new Boolean(values.get(
        HIGH_AVAILABILITY).equals("Yes")) : null;
    Long agency = getTextLong(AGENCY_COUNT, values);
    String licenseText = getLicenseText(dateBased, alarmDays, namedUsers,
        cocurrentUser, noOfAccounts, proc, ha, agency);
    long key = getRandomKey();
    generate(licenseText, key, oPath);
  }

  public String getLicenseText(Date dateBased, Long alarmDays,
      Map<NamedUserType, Long> namedUsers, Long cocurrentUser,
      Long noOfAccounts, Long processors, Boolean ha, Long agencyCount) {
    StringBuffer licenseString = new StringBuffer();
    SimpleDateFormat df2 = new SimpleDateFormat(LICENSE_DATE_FORMAT);
    licenseString.append(dateBased != null ? EXPIRY_DATE + "="
        + df2.format(dateBased) + ";" : "");
    licenseString.append(alarmDays != null ? EXPIRY_WARNING_DAY_COUNT + "="
        + alarmDays + ";" : "");
    for (Entry<NamedUserType, Long> userType : namedUsers.entrySet()) {
      licenseString.append(userType.getKey().getTag() + "="
          + userType.getValue() + ";");
    }
    licenseString.append(cocurrentUser != null ? CONCURRENT_USER + "="
        + cocurrentUser + ";" : "");
    licenseString.append(noOfAccounts != null ? NO_OF_ACCOUNTS + "="
        + noOfAccounts + ";" : "");
    licenseString.append(processors != null ? PROCESSOR_COUNT + "="
        + processors + ";" : "");
    licenseString.append(ha != null ? HIGH_AVAILABILITY + "="
        + (ha.booleanValue() ? "Yes" : "No") + ";" : "");
    licenseString.append(agencyCount != null ? AGENCY_COUNT + "=" + agencyCount
        + ";" : "");
    String licenseText = licenseString.toString();
    return licenseText;
  }

  private void generate(String licenseString, long key, String oPath)
      throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidCipherTextException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    /*
     * get the encrypted key and license and write them into output file
     */
    String encrypted = encrypt(key, licenseString);
    String encryptedKey = encryptKey(key);
    writeFiles(encrypted, encryptedKey, oPath);
  }

  public String encrypt(long key, String licenseString)
      throws InvalidCipherTextException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException,
      BadPaddingException {
    /*
     * get FixedSecureRandom and set key as seed
     */
    SecureRandom sr = getSecureRandom();
    sr.setSeed(key);
    String encrypted = encrypt(sr, licenseString);
    return encrypted;
  }

  public Map<String, String> degenerate(String location)
      throws InvalidCipherTextException, FileNotFoundException, IOException,
      InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException {
    /*
     * get the encrypted key and license file from given location
     */
    if (location == null) {
      throw new RuntimeException(
          "Invalid license/key file location, license validation failed.");
    }
    String licenseFile = location + File.separator + LICENSE_FILE_NAME;
    String keyFile = location + File.separator + KEY_FILE_NAME;
    return degenerate(licenseFile, keyFile);
  }

  public Map<String, String> degenerate(String licenseFile, String keyFile)
      throws InvalidCipherTextException, FileNotFoundException, IOException,
      InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException {
    /*
     * read the data and convert to byte[]
     */
    if (licenseFile == null || keyFile == null) {
      throw new RuntimeException(
          "Invalid license/key file, license degeneration failed.");
    }
    byte[] license = Base64.decodeFromFile(licenseFile);
    byte[] key = Base64.decodeFromFile(keyFile);
    if (license == null) {
      throw new FileNotFoundException(licenseFile);
    }
    if (key == null) {
      throw new FileNotFoundException(keyFile);
    }
    return degenerate(license, key);
  }

  public Map<String, String> degenerate(byte[] license, byte[] key)
      throws InvalidCipherTextException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException,
      BadPaddingException {
    /*
     * decrypted the key and license
     */
    byte[] decryptedKey = decryptKey(key);
    SecureRandom sr = getSecureRandom();
    sr.setSeed(decryptedKey);
    byte[] decrypted = decrypt(sr, license);
    String decryptedString = new String(decrypted);
    String[] licenseValuesArray = decryptedString.split(";");
    Map<String, String> licenseValues = new HashMap<String, String>();
    for (int i = 0; i < licenseValuesArray.length; i++) {
      String[] singleValue = licenseValuesArray[i].split("=");
      if (singleValue.length >= 2) {
        licenseValues.put(singleValue[0], singleValue[1]);
      }
    }
    return licenseValues;
  }

  private SecureRandom getSecureRandom() throws NoSuchAlgorithmException {
    return new SecureRandom();
  }

  public long getRandomKey() throws NoSuchAlgorithmException {
    return SecureRandom.getInstance("SHA1PRNG").nextLong();
  }

  private String encrypt(SecureRandom sr, String text)
      throws InvalidCipherTextException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
      BadPaddingException {
    RSAKeyPairGenerator g = new RSAKeyPairGenerator();
    g.init(new RSAKeyGenerationParameters(BigInteger.valueOf(7), sr,
        ENCRYPTION_KEY_STRENGTH, 8));
    AsymmetricCipherKeyPair kp = g.generateKeyPair();
    CipherParameters publicParam = kp.getPublic();
    byte[] in = text.getBytes();
    AsymmetricBlockCipher e = new RSAEngine();
    e.init(true, publicParam);
    byte[] encrypted = new byte[in.length * EX];
    int x = 0;
    for (int i = 0; i < in.length; i++) {
      byte[] b = e.processBlock(in, i, 1);
      System.arraycopy(b, 0, encrypted, x, EX);
      x += EX;
    }
    return Base64.encodeBytes(encrypted, Base64.DONT_BREAK_LINES);
  }

  private byte[] decrypt(SecureRandom sr, byte[] encrypted)
      throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
      InvalidCipherTextException {
    RSAKeyPairGenerator g = new RSAKeyPairGenerator();
    g.init(new RSAKeyGenerationParameters(BigInteger.valueOf(7), sr,
        ENCRYPTION_KEY_STRENGTH, 8));
    AsymmetricCipherKeyPair kp = g.generateKeyPair();
    CipherParameters privateParam = kp.getPrivate();
    AsymmetricBlockCipher e = new RSAEngine();
    e.init(false, privateParam);
    byte[] decrypted = new byte[encrypted.length];
    int y = 0;
    for (int i = 0; i < encrypted.length; i++) {
      byte[] b = e.processBlock(encrypted, i, EX);
      System.arraycopy(b, 0, decrypted, y, 1);
      i = i + (EX - 1);
      y++;
    }
    return decrypted;
  }

  public String encryptKey(long key) throws InvalidCipherTextException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException {
    SecureRandom sr = getSecureRandom();
    sr.setSeed(seed);
    return encrypt(sr, String.valueOf(key));
  }

  private byte[] decryptKey(byte[] encryptedKey)
      throws InvalidCipherTextException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException,
      BadPaddingException {
    SecureRandom sr = getSecureRandom();
    sr.setSeed(seed);
    return decrypt(sr, encryptedKey);
  }

  private void writeFiles(String license, String key, String oPath) {
    try {
      FileUtil.writeFile(new File(oPath + File.separator + LICENSE_FILE_NAME),
          license, "UTF8");
      FileUtil.writeFile(new File(oPath + File.separator + KEY_FILE_NAME), key,
          "UTF8");
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static void add(Long count, NamedUserType t,
      Map<NamedUserType, Long> namedUsers) {
    if (count != null) {
      namedUsers.put(t, count);
    }
  }
}
