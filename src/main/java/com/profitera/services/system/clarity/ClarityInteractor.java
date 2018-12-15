package com.profitera.services.system.clarity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.mtmf.MTMFMemoBusinessBean;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.SystemService;
import com.profitera.util.Bytes;
import com.profitera.util.Strings;

/**
 * Sends a login string
 */
public class ClarityInteractor extends SystemService {

	public final static Log log = LogFactory.getLog(ClarityInteractor.class);

	public static final int SECURITY_HEADER_FIELD_SIZE = 44;

	public static final int REQUEST_LENGTH_FIELD_SIZE = 4;

	public static final int HEADER_FIELD_SIZE = 94;

	public static final int LOGIN_HEADER_FIELD_SIZE = 123;

	public static final int LOGIN_RESPONSE_SIZE = 48;

	public static final int MTI_FIELD_SIZE = 4; //Version + MTI

	public static final char MTMF_CUSTOMER_MEMO = 'C';

	public static final char MTMF_ACCOUNT_MEMO = 'H';

	private static final String CLARITY_USER = "Clarity-User";

	private static final String CLARITY_PASSWORD = "Clarity-Password";

	private static final String LOGIN_RESPONSE_TIMEOUT = "Login-Response-Timeout";

	private static final String RESPONSE_TIMEOUT = "Response-Timeout";
	
	private static final String CLARITY_SUCCESS_CODE_PROPERTY = "Clarity-Success-Code";

	private static final String REASON_L = "L";

	private static final String REASON_S = "S";

	private static final String REASON_Q = "Q";

	public static final String CARMA_SOURCE_ID = "CM  ";

	public static final String ISO_CONSTANT = "ISO";

	private static final byte NULL = (byte) 0;

	private static final byte THREE = (byte) 3;

	private static final byte SIXTEEN = (byte) 16;

	private static final byte SPACE = (byte) 32;

	private static final byte HAT = (byte) 94;

	private static final byte X = (byte) 0x58;

	private static final int RESPONSE_UNUSED_HEAD_LENGTH = SECURITY_HEADER_FIELD_SIZE
			+ REQUEST_LENGTH_FIELD_SIZE + MTI_FIELD_SIZE;

	//Security Header
	//31 Chr(0) + Chr(3) + 3 Chr(0) + Chr(32) + 3 Chr(0) + Chr(88) + 4 Chr(0)
	public static final byte[] SECURITY_HEADER = new byte[SECURITY_HEADER_FIELD_SIZE];

	private Map ISO_STRUCTURE = new HashMap();

	private static String USER_LOGIN = null;

	private static String USER_PASS = null;

	private String RELAY_SERVER_HOST = null;

	private int RELAY_SERVER_PORT;

	private long LOGIN_WAIT = 0;

	private long RESPONSE_WAIT = 0;

	private String CLARITY_SUCCESS_CODE = "900000";
	
	static {
		// This is based on the samples given by HLBB
		Arrays.fill(SECURITY_HEADER, NULL);
		SECURITY_HEADER[3] = SIXTEEN;
		SECURITY_HEADER[39] = HAT;
	}

	private final static byte[] LOGIN_SECURITY_HEADER = new byte[SECURITY_HEADER_FIELD_SIZE];
	static {
		// This is based on the samples given by HLBB
		Arrays.fill(LOGIN_SECURITY_HEADER, NULL);
		LOGIN_SECURITY_HEADER[3] = SIXTEEN;
		LOGIN_SECURITY_HEADER[31] = THREE;
		LOGIN_SECURITY_HEADER[35] = SPACE;
		LOGIN_SECURITY_HEADER[39] = X;
	}

	public ClarityInteractor() throws Exception {

		RELAY_SERVER_HOST = ServiceEngine.getProp(ClarityByteRelay.RELAY_HOST,
				"localhost");
		RELAY_SERVER_PORT = ServiceEngine.getIntProp(ClarityByteRelay.RELAY_PORT,
				10001);

		USER_LOGIN = ServiceEngine.getProp(CLARITY_USER, "HUB");
		USER_PASS = ServiceEngine.getProp(CLARITY_PASSWORD,
				"C1F3C83B7F88E2A16CA13127C325500B");
		LOGIN_WAIT = (long) ServiceEngine.getIntProp(LOGIN_RESPONSE_TIMEOUT, 0) * 1000;
		RESPONSE_WAIT = (long) ServiceEngine.getIntProp(RESPONSE_TIMEOUT, 0) * 1000;
		CLARITY_SUCCESS_CODE = ServiceEngine.getProp(CLARITY_SUCCESS_CODE_PROPERTY, "900000");
		Socket clarityConnection = getClarityConnection(RELAY_SERVER_HOST,
				RELAY_SERVER_PORT);

		try {
			login(clarityConnection);
		} catch (IOException ex2) {
			log.error("Error occured when attempting to login to Clarity Server!");
			throw ex2;
		}
		closeCommunication(clarityConnection);
	}

	private void closeCommunication(Socket clarityConnection) {
		try {
			clarityConnection.shutdownInput();
			clarityConnection.shutdownOutput();
			clarityConnection.close();
		} catch (Exception e) {
			log.error("Exception occurred while trying to close communication link from Clarity Interactor!", e);
		}
	}

	private Socket getClarityConnection(String relayHost, int relayPort)
			throws IOException, Exception {
		Socket clarityConnection;
		try {
			clarityConnection = new Socket(relayHost, relayPort);
		} catch (IOException ex1) {
			log
					.error("Exception occured when attempting to connect to Clarity Server!");
			log.error("Clarity Host: " + relayHost + "; Clarity Port: " + relayPort);
			throw ex1;
		}
		return clarityConnection;
	}

	public ClarityInteractor(String relayHost, int relayPort, File structFile)
			throws IOException, Exception {
		RELAY_SERVER_HOST = relayHost;
		RELAY_SERVER_PORT = relayPort;

		LOGIN_WAIT = (long) 3 * 1000;
		RESPONSE_WAIT = (long) 10 * 1000;
		USER_LOGIN = "HUB";
		USER_PASS = "C1F3C83B7F88E2A16CA13127C325500B";

		Socket clarityConn = getClarityConnection(RELAY_SERVER_HOST,
				RELAY_SERVER_PORT);
		login(clarityConn);
		closeCommunication(clarityConn);

		//ISO_STRUCTURE = ISO8583Message.loadMessageFields(structFile);
	}

	public boolean login(Socket clarityConnection) throws IOException {

		boolean loggedIn = false;
		log.info("Attempting to login to Clarity Server with User: " + USER_LOGIN);
		InputStream input = clarityConnection.getInputStream();
		OutputStream out = clarityConnection.getOutputStream();
		String loginString = USER_LOGIN + USER_PASS;
		loginString = Strings.pad(loginString, loginString.length() + 88, (char) 0);
		byte strInfo[] = loginString.getBytes("UTF-8");
		byte[] messageSent = sendMessage(out, LOGIN_SECURITY_HEADER, strInfo,
				new byte[0], true);

		// Now I expect to get back some reply at least to show that i've
		// logged in succesfully
		byte[] reply = null;

		try {
			reply = fetchLoginReply(input);
		} catch (IOException e) {
			log.error("Unable to Login!");
			log.error("Clarity Service will not be available!");
			log.error("Stack Trace:", e);
			return false;
		} catch (Exception e) {
			log.error("Unable to Login!");
			log.error("Clarity Service will not be available!");
			log.error("Stack Trace:", e);
			return false;
		}

		if (reply != null && reply.length != 0) {
			loggedIn = true;
		}

		log.info("User " + USER_LOGIN + " successfully logged in to Clarity.");

		return loggedIn;
	}

	/**
	 * @param input
	 * @return
	 */
	private byte[] fetchReply(InputStream input) throws IOException, Exception {
		byte[] securityHeader = fill(input, new byte[SECURITY_HEADER_FIELD_SIZE],
				"Security-Header", RESPONSE_WAIT);
		byte[] reqLength = fill(input, new byte[REQUEST_LENGTH_FIELD_SIZE],
				"Request-Length", RESPONSE_WAIT);
		int requestTotal = Bytes.buildInt(reqLength);
		byte[] request = fill(input, new byte[requestTotal], "Request",
				RESPONSE_WAIT);
		byte[] reqWithoutMTI = new byte[request.length - 4];
		System.arraycopy(request, 4, reqWithoutMTI, 0, reqWithoutMTI.length);
		return reqWithoutMTI;
	}

	private byte[] fetchLoginReply(InputStream input) throws IOException,
			Exception {
		return fill(input, new byte[LOGIN_RESPONSE_SIZE], "Login-Response",
				LOGIN_WAIT);
	}

	/**
	 * @param input
	 * @param array
	 * @return the same array that was passed in, just for convienience.
	 * @throws IOException
	 */
	private byte[] fill(InputStream input, byte[] array, String name,
			long howLongToWait) throws IOException, Exception {
		int waitx = (int) howLongToWait / 1000;
		int waited = 0;
		try {
			while (true) {

				if (input.available() >= array.length) {
					for (int i = 0; i < array.length; i++) {
						array[i] = (byte) input.read();
					}
					break;
				} else if (waited <= waitx) {
					Thread.sleep(howLongToWait / waitx);
					waited++;
				} else {
					throw new Exception(
							"Input stream do not have enough bytes!\nBytes expected: "
									+ array.length + "\nBytes available: " + input.available());
				}
			}
		} catch (SocketTimeoutException ste) {
			throw new Exception(
					"No response from the Clarity Server after waiting for "
							+ (RESPONSE_WAIT / 1000) + " sec(s)!");
		}
		return array;
	}

	private byte[] sendMessage(OutputStream out, byte[] securityHeader,
			byte[] transHeader, byte[] requesttext) throws IOException {

		return sendMessage(out, securityHeader, transHeader, requesttext, false);
	}

	private byte[] sendMessage(OutputStream out, byte[] securityHeader,
			byte[] transHeader, byte[] requesttext, boolean isLogin)
			throws IOException {

		if (securityHeader.length != SECURITY_HEADER_FIELD_SIZE) {
			throw new RuntimeException(
					"Security header (Field A) length mismatch!\nExpected: "
							+ SECURITY_HEADER_FIELD_SIZE + "\nReceived: "
							+ securityHeader.length);
		} else if (!isLogin && transHeader.length != HEADER_FIELD_SIZE) {
			throw new RuntimeException(
					"Transaction header (Field B) length mismatch!\nExpected: "
							+ HEADER_FIELD_SIZE + "\nReceived: " + transHeader.length);
		}

		String hexText = Long.toHexString(requesttext.length);
		// 2 hex digits per byte, so I need a String twice as long as the
		// btye-width
		hexText = Strings.leftPad(hexText, REQUEST_LENGTH_FIELD_SIZE * 2, '0');
		byte[] requestLength = new byte[REQUEST_LENGTH_FIELD_SIZE];
		for (int i = 0; i < requestLength.length; i++) {
			requestLength[i] = (byte) Integer.parseInt(hexText.substring(i * 2,
					i * 2 + 2), 16);
		}

		return sendMessage(out, securityHeader, requestLength, transHeader,
				requesttext);
	}

	private byte[] sendMessage(OutputStream out, byte[] securityHeader,
			byte[] requestSize, byte[] transHeader, byte[] requestContent)
			throws IOException {
		byte[] completeMessage = Bytes.mergeArrays(new byte[][]{securityHeader,
				requestSize, transHeader, requestContent});
		ByteArrayOutputStream binStream = new ByteArrayOutputStream();
		binStream.write(completeMessage);
		binStream.writeTo(out);
		binStream.flush();
		out.flush();
		return completeMessage;
	}

	/**
	 * @param account_number
	 * @param customer_number
	 */
	public String blockCard(String accNo, String customerId, String userId,
			String blockCode) throws IOException {
		Socket clarityConn = null;
		try {
			clarityConn = getClarityConnection(RELAY_SERVER_HOST, RELAY_SERVER_PORT);
		} catch (Exception ex) {
			log
					.error(
							"Unable to connect to Clarity Server, Clarity interactions will not be available!",
							ex);
			return "Unable to connect to Clarity Server, Clarity interactions will not be available!";
		}

		if (!login(clarityConn)) {
			return "Unable to login to Clarity Server, Clarity interactions will not be available!";
		}

		//anything which has HF refers to common header field, as defined in
		// page 4 or Clarity Tech Spec
		//lets begin by constructing the sub-bitmap data field, field number
		// 123
		BitmappedField bit123 = new BitmappedField();
		bit123.setBinaryBitmap(false);
		bit123.setBitmapLength(128);
		//LLVAR, Customer Id
		bit123.addVariableWidthField(2, 2, customerId); //HF
		//this is sys trace number, must be 12 in length and all numbers,
		// actually it is front end sequence number
		String ranNum = generateRandom();
		bit123.addFixedWidthField(11, ranNum); //HF
		//LLVAR, Host seq number, we set it to 4 spaces, the bloody host
		// system supposed to fill it up
		bit123.addVariableWidthField(20, 1, "    "); //HF
		bit123.addFixedWidthField(22, CARMA_SOURCE_ID); //HF
		//LLVAR, User id
		bit123.addVariableWidthField(23, 2, userId); //HF
		//LVAR, Teller id, we just set it to space
		bit123.addVariableWidthField(25, 1, " "); //HF

		//Construction of field123.42, I hate this field
		//its an LLLVAR
		String field42 = new String();
		field42 += Strings.pad("", 4); //Request type, not used
		field42 += Strings.leftPad("", 2, '0'); // No of Transaction, not used
		field42 += Strings.pad("", 3); //Sequence, not used
		field42 += Strings.pad("", 30); //Name, not used
		field42 += Strings.leftPad("", 8, '0'); //Date of Birth, not used
		field42 += Strings.pad("", 15); //ID No., not used
		field42 += Strings.pad("", 18); //Office phone, not used
		field42 += Strings.pad("", 18); //Home phone, not used
		field42 += Strings.pad("", 30); //Mother maiden name, not used
		field42 += Strings.pad("", 1); //Reason for reporting, either L - Lost/S -
		// Stolen/Q - Suspect Fraud
		field42 += Strings.pad(blockCode, 1); //New Block code
		field42 += Strings.pad("", 849); //Filler

		bit123.addVariableWidthField(42, 3, field42);
		//this is the terminal id, i fixed it here to carmapp, coz all our
		// request goes through app server (carmapp)
		bit123.addVariableWidthField(51, 2, "carmapp"); //HF
		//well, this is the LVAR which defined the transaction code, CMMA
		bit123.addVariableWidthField(89, 1, "CCCN"); //HF

		//now, we've got field 123, lets construct the main part of the
		// messsge
		BitmappedField msg = new BitmappedField();
		msg.setBinaryBitmap(false);
		msg.setBitmapLength(64); //setting the primary bitmap length
		msg.setSecondaryBitmapLength(64); //setting the secondary bitmap
		// length
		msg.setSecondaryIndicatorIndex(1); //telling the guy where the
		// secondary bitmap starts

		//now lets add the fields
		//this is the primary account number (PAN), LLVAR
		msg.addVariableWidthField(2, 2, accNo);
		//this is fixed transaction code of 6 bytes length, for memo add, its
		// FACMMA
		//its actually based on the code CCSA, and I suppose these Finnese
		// Alliance guys added FA in front it, coz its their name
		msg.addFixedWidthField(3, "FACCCN"); //HF
		//this the current date, in the format as displayed below.
		msg.addFixedWidthField(12, new SimpleDateFormat("yyMMddHHmmss")
				.format(new Date())); //HF

		//now that we have the main bitmap well done, we gonna add the
		// sub-bitmap into it
		msg.addVariableWidthField(123, 4, bit123);

		//Now, lets construct the C header
		String C = new String();
		C += "FACCCN"; //this is the transaction code, msg id, length is 6
		C += ISO_CONSTANT; //this is the ISO constant, value is ISO
		C += ranNum; //this is front end reference number, can be any
		// numberlah
		C += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //current
		// date
		// lah
		//the one below is supposed to beservice number, but i think its
		// actually source id.
		//the sample given by chooi ling is CC, which is referring to call
		// center (Siebel)
		C += CARMA_SOURCE_ID;
		C += Strings.pad("carmapp", 20); //this is the machine name, im
		// setting it to our app server name
		C += "1"; //Financial flag, defaulted to 1 as specified in the doc
		C += Strings.pad("", 20); //TIN, dunno what shit this is, but the doc
		// says 20 empty space, so 20 empty space i
		// put
		C += accNo; //This is form account number, which I presume is the
		// account number/card number lah

		//Now, we will construct the full message with includes
		//ISO Message Type, which is Version + MTI
		//ISO Message, the message itself
		byte[] msgWithType = ISO8583Message.asMessage(ISO8583Message.V1993,
				ISO8583Message.FINPRESENTMENT_MSG_CLASS,
				ISO8583Message.REQUEST_FUNCTION, ISO8583Message.FROM_ACQUIRER, msg);
		//now that we've got the message constructed nicely, lets send it
		//Message is constructed by A + B + C + Message Type (MTI) + D
		//A is the security header
		//B is the length of C header, which is dynamically calculated and
		// place by the sendMessage method
		//C is the C header, as specified by Clarity tech doc
		//D is the ISO Message Type + ISO data
		byte[] sent = sendMessage(clarityConn.getOutputStream(), SECURITY_HEADER, C
				.getBytes(), msgWithType);

		log.info("Trace Info - Trace Number: " + ranNum + "; Tran Code: FACCCN; Cust Id: " + customerId + "; Acc Num: " + accNo);
		byte[] respInfo = null;

		try {
			respInfo = fetchReply(clarityConn.getInputStream());
		} catch (IOException e) {
			log.error("Unable to fetch reply, error encountered!", e);
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		} catch (Exception e) {
			log.error("Unable to fetch reply, error encountered! " + e.getMessage());
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		}

		Map bitTypes = new HashMap();
		bitTypes.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
		bitTypes.put(new Integer(12), new BitmappedField.FixedWidthBit(12));
		bitTypes.put(new Integer(17), new BitmappedField.FixedWidthBit(4));
		bitTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(3)); //ISO
		// defines
		// this
		// as 4
		bitTypes.put(new Integer(44), new BitmappedField.LVARBit(2, false)); //ISO
		// defines
		// this
		// LLLLVAR
		bitTypes.put(new Integer(123), new BitmappedField.LVARBit(4, true));
		Map result = BitmappedField.breakdown(false, 64, 1, 64, respInfo, bitTypes);
		writeBreakdown("FACCCN", result);

		Map subTypes = new HashMap();
		subTypes.put(new Integer(2), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(11), new BitmappedField.FixedWidthBit(12));
		subTypes.put(new Integer(20), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(22), new BitmappedField.FixedWidthBit(4));
		subTypes.put(new Integer(23), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(25), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(6));
		subTypes.put(new Integer(51), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(75), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(89), new BitmappedField.LVARBit(1, false));
		Map subResult = BitmappedField.breakdown(false, 128, (byte[]) result
				.get(new Integer(123)), subTypes);
		writeBreakdown("FACCCN - 123", subResult);

		closeCommunication(clarityConn);

		return validateResponse(result, subResult, "FACCCN");
	}

	public String addNotesToMTMF(String accNo, String custId, String userId,
			char type, String category, String desc, String notes) throws IOException {
		Socket clarityConn = null;
		try {
			clarityConn = getClarityConnection(RELAY_SERVER_HOST, RELAY_SERVER_PORT);
		} catch (Exception ex) {
			log
					.error(
							"Unable to connect to Clarity Server, Clarity interactions will not be available!",
							ex);
			return "Unable to connect to Clarity Server, Clarity interactions will not be available!";
		}

		if (!login(clarityConn)) {
			return "Unable to login to Clarity Server, Clarity interactions will not be available!";
		}

		//lets begin by constructing the sub-bitmap data field, field number
		// 123
		BitmappedField bit123 = new BitmappedField();
		bit123.setBinaryBitmap(false);
		bit123.setBitmapLength(128);
		//LLVAR, Customer Id
		bit123.addVariableWidthField(2, 2, custId); //HF
		//this is sys trace number, must be 12 in length and all numbers,
		// actually it is front end sequence number
		String ranNum = generateRandom();
		bit123.addFixedWidthField(11, ranNum); //HF
		//LLVAR, Host seq number, we set it to 4 spaces, the bloody host
		// system supposed to fill it up
		bit123.addVariableWidthField(20, 1, "    "); //HF - LVAR
		bit123.addFixedWidthField(22, CARMA_SOURCE_ID); //HF
		//LLVAR, User id
		bit123.addVariableWidthField(23, 2, userId); //HF
		//LVAR, Teller id, we just set it to space
		bit123.addVariableWidthField(25, 1, " "); //HF

		//Construction of field123.42, I hate this field
		//its an LLLVAR
		String field42 = new String();
		field42 += Strings.pad("", 4); //Request type, not used
		field42 += Strings.leftPad("", 2, ' '); // No of Transaction, not used
		field42 += Strings.pad("", 3); //Sequence, not used
		field42 += Strings.pad("", 30); //Name, not used
		field42 += Strings.leftPad("", 8, ' '); //Date of Birth, not used
		field42 += Strings.pad("", 15); //ID No., not used
		field42 += Strings.pad("", 18); //Office phone, not used
		field42 += Strings.pad("", 18); //Home phone, not used
		field42 += Strings.pad("", 30); //Mother maiden name, not used
		field42 += Strings.pad("", 1); //Reason for reporting, not used
		field42 += (type != MTMF_CUSTOMER_MEMO && type != MTMF_ACCOUNT_MEMO
				? MTMF_ACCOUNT_MEMO
				: type); //Memo key type, 1 character, H for account/card holder, C for
		// customer
		// holder, C for customer, we use H
		field42 += Strings.leftPad("99991231", 8, '0'); //Memo start date, not
		// used
		field42 += Strings.leftPad("", 6, ' '); //Memo time, not used
		field42 += Strings.pad(category, 3); //Memo category, not used
		field42 += Strings.pad((desc == null ? "" : desc.toUpperCase()), 25); //Description,
		// not
		// used
		field42 += Strings.pad((userId != null && userId.length() >= 3) ? userId
				.substring(0, 3).toUpperCase() : "NON", 3); //Operator, not use
		field42 += "00000000                     "; // To make it match with
		// HLBB sample

		bit123.addVariableWidthField(42, 3, field42);
		//bit123.addVariableWidthField(42, 3, " H99991231 FLS ABC00000000 ");
		//this is the terminal id, i fixed it here to carmapp, coz all our
		// request goes through app server (carmapp)
		bit123.addVariableWidthField(51, 2, "carmapp"); //HF
		//well, this is the LVAR which defined the transaction code, CMMA
		bit123.addVariableWidthField(89, 1, "CMMA"); //HF

		//now, we've got field 123, lets construct the main part of the
		// messsge
		BitmappedField msg = new BitmappedField();
		msg.setBinaryBitmap(false);
		msg.setBitmapLength(64); //setting the primary bitmap length
		msg.setSecondaryBitmapLength(64); //setting the secondary bitmap
		// length
		msg.setSecondaryIndicatorIndex(1); //telling the guy where the
		// secondary bitmap starts

		//now lets add the fields
		//this is the primary account number (PAN), LLVAR
		msg.addVariableWidthField(2, 2, accNo);
		//this is fixed transaction code of 6 bytes length, for memo add, its
		// FACMMA
		//its actually based on the code CMMA, and I suppose these Finnese
		// Alliance guys added FA in front it, coz its their name
		msg.addFixedWidthField(3, "FACMMA");
		//this the current date, in the format as displayed below.
		msg.addFixedWidthField(12, new SimpleDateFormat("yyMMddHHmmss")
				.format(new Date()));

		//this is the real notes entry that we want to add
		//clarity defines bit 61, 62, 63 and 116 for it, each bit could have
		// max of 990 bytes of notes entry
		//our system defines note entry to be max of 500 char, which is 500
		// bytes, so we dont even have to bother
		//about bits other than 61
		msg.addVariableWidthField(61, 3, (notes != null
				? notes.toUpperCase()
				: "EMPTY NOTES"));

		//now that we have the main bitmap well done, we gonna add the
		// sub-bitmap into it
		msg.addVariableWidthField(123, 4, bit123);

		//Now, lets construct the C header
		String C = new String();
		C += "FACMMA"; //this is the transaction code, msg id, length is 6
		C += ISO_CONSTANT; //this is the ISO constant, value is ISO
		C += ranNum; //this is front end reference number, can be any
		// numberlah
		C += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //current
		// date
		// lah
		//the one below is supposed to beservice number, but i think its
		// actually source id.
		//the sample given by chooi ling is CC, which is referring to call
		// center (Siebel)
		C += CARMA_SOURCE_ID;
		C += Strings.pad("carmapp", 20); //this is the machine name, im
		// setting it to our app server name
		C += "1"; //Financial flag, defaulted to 1 as specified in the doc
		C += Strings.pad("", 20); //TIN, dunno what shit this is, but the doc
		// says 20 empty space, so 20 empty space i
		// put
		C += accNo; //This is form account number, which I presume is the
		// account number/card number lah

		//Now, we will construct the full message with includes
		//ISO Message Type, which is Version + MTI
		//ISO Message, the message itself
		byte[] msgWithType = ISO8583Message.asMessage(ISO8583Message.V1993,
				ISO8583Message.FINPRESENTMENT_MSG_CLASS,
				ISO8583Message.REQUEST_FUNCTION, ISO8583Message.FROM_ACQUIRER, msg);
		//now that we've got the message constructed nicely, lets send it
		//Message is constructed by A + B + C + Message Type (MTI) + D
		//A is the security header
		//B is the length of C header, which is dynamically calculated and
		// place by the sendMessage method
		//C is the C header, as specified by Clarity tech doc
		//D is the ISO Message Type + ISO data
		byte[] sent = sendMessage(clarityConn.getOutputStream(), SECURITY_HEADER, C
				.getBytes(), msgWithType);

		log.info("Trace Info - Trace Number: " + ranNum + "; Tran Code: FACMMA; Cust Id: " + custId + "; Acc Num: " + accNo);
		
		byte[] respInfo = null;

		try {
			respInfo = fetchReply(clarityConn.getInputStream());
		} catch (IOException e) {
			log.error("Unable to fetch reply, error encountered!", e);
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		} catch (Exception e) {
			log.error("Unable to fetch reply, error encountered! " + e.getMessage());
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		}

		//Map bitTypes = ISO_STRUCTURE;

		Map bitTypes = new HashMap();
		bitTypes.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
		bitTypes.put(new Integer(12), new BitmappedField.FixedWidthBit(12));
		bitTypes.put(new Integer(17), new BitmappedField.FixedWidthBit(4));
		bitTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(3)); //ISO
		// defines
		// this
		// as 4
		bitTypes.put(new Integer(44), new BitmappedField.LVARBit(2, false)); //ISO
		// defines
		// this
		// LLLLVAR
		bitTypes.put(new Integer(123), new BitmappedField.LVARBit(4, true));
		Map result = BitmappedField.breakdown(false, 64, 1, 64, respInfo, bitTypes);
		writeBreakdown("FACMMA", result);

		Map subTypes = new HashMap();
		subTypes.put(new Integer(2), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(11), new BitmappedField.FixedWidthBit(12));
		subTypes.put(new Integer(20), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(22), new BitmappedField.FixedWidthBit(4));
		subTypes.put(new Integer(23), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(25), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(6));
		subTypes.put(new Integer(51), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(75), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(89), new BitmappedField.LVARBit(1, false));
		Map subResult = BitmappedField.breakdown(false, 128, (byte[]) result
				.get(new Integer(123)), subTypes);
		writeBreakdown("FACMMA - 123", subResult);
		
		closeCommunication(clarityConn);

		return validateResponse(result, subResult, "FACMMA");
	}

	public MTMFMemoBusinessBean fetchNotesFromMTMF(String accNo, String custId,
			char type, boolean nextRecord, MTMFMemoBusinessBean memo)
			throws IOException {

		Socket clarityConn = null;
		try {
			clarityConn = getClarityConnection(RELAY_SERVER_HOST, RELAY_SERVER_PORT);
		} catch (Exception ex) {
			log
					.error(
							"Unable to connect to Clarity Server, Clarity interactions will not be available!",
							ex);
			return new MTMFMemoBusinessBean(
					"Unable to connect to Clarity Server, Clarity interactions will not be available!");
		}

		if (!login(clarityConn)) {
			return new MTMFMemoBusinessBean(
					"Unable to login to Clarity Server, Clarity interactions will not be available!");
		}

		//if memo type requested is not customer or account, then we default it to
		// account
		if (type != MTMF_CUSTOMER_MEMO && type != MTMF_ACCOUNT_MEMO) {
			type = MTMF_ACCOUNT_MEMO;

			//lets begin by constructing the sub-bitmap data field, field number
			// 123
		}
		BitmappedField bit123 = new BitmappedField();
		bit123.setBinaryBitmap(false);
		bit123.setBitmapLength(128);
		//LLVAR, Customer Id
		bit123.addVariableWidthField(2, 2, custId); //HF
		//this is sys trace number, must be 12 in length and all numbers,
		// actually it is front end sequence number
		String ranNum = generateRandom();
		bit123.addFixedWidthField(11, ranNum); //HF
		//LLVAR, Host seq number, we set it to 4 spaces, the bloody host
		// system supposed to fill it up
		bit123.addVariableWidthField(20, 1, "    "); //HF
		bit123.addFixedWidthField(22, CARMA_SOURCE_ID); //HF
		//LLVAR, User id
		bit123.addVariableWidthField(23, 2, memo.getCreator()); //HF
		//LVAR, Teller id, we just set it to space
		bit123.addVariableWidthField(25, 1, " "); //HF

		//Construction of field123.42, I hate this field
		//its an LLLVAR
		String field42 = new String();
		field42 += (nextRecord ? "NEXT" : Strings.pad(" ", 4)); //Request type, not
		// used
		field42 += Strings.leftPad("", 2, '0'); // No of Transaction, not used
		field42 += Strings.pad("", 3); //Sequence, not used
		field42 += Strings.pad("", 30); //Name, not used
		field42 += Strings.leftPad("", 8, '0'); //Date of Birth, not used
		field42 += Strings.pad("", 15); //ID No., not used
		field42 += Strings.pad("", 18); //Office phone, not used
		field42 += Strings.pad("", 18); //Home phone, not used
		field42 += Strings.pad("", 30); //Mother maiden name, not used
		field42 += Strings.pad("", 1); //Reason for reporting, not used
		field42 += (type != MTMF_CUSTOMER_MEMO && type != MTMF_ACCOUNT_MEMO
				? MTMF_CUSTOMER_MEMO
				: type); //Memo key type, 1 character, H for account/card holder, C for
		// customer
		field42 += Strings.leftPad("", 8, '0'); //Memo start date, not used
		field42 += Strings.leftPad("", 6, '0'); //Memo time, not used
		field42 += Strings.pad("", 3); //Memo category, not used
		//field42 += Strings.pad("", 25); //Description, not used
		//field42 += Strings.pad("", 3); //Operator, not used

		bit123.addVariableWidthField(42, 3, field42);
		//this is the terminal id, i fixed it here to carmapp, coz all our
		// request goes through app server (carmapp)
		bit123.addVariableWidthField(51, 2, "carmapp"); //HF
		//well, this is the LVAR which defined the transaction code, CMMA
		bit123.addVariableWidthField(89, 1, "CMMI"); //HF

		//now, we've got field 123, lets construct the main part of the
		// messsge
		BitmappedField msg = new BitmappedField();
		msg.setBinaryBitmap(false);
		msg.setBitmapLength(64); //setting the primary bitmap length
		msg.setSecondaryBitmapLength(64); //setting the secondary bitmap
		// length
		msg.setSecondaryIndicatorIndex(1); //telling the guy where the
		// secondary bitmap starts

		//now lets add the fields
		//this is the primary account number (PAN), LLVAR
		msg.addVariableWidthField(2, 2, accNo);
		//this is fixed transaction code of 6 bytes length, for memo add, its
		// FACMMA
		//its actually based on the code CMMA, and I suppose these Finnese
		// Alliance guys added FA in front it, coz its their name
		msg.addFixedWidthField(3, "FACMMI");
		//this the current date, in the format as displayed below.
		msg.addFixedWidthField(12, new SimpleDateFormat("yyMMddHHmmss")
				.format(new Date()));

		//We need to construct field44, again, a bunch of shit only, I hate
		// this field also
		String field44 = new String();
		field44 += (nextRecord ? memo.getEndOfRecInd() : ' '); //end of data
		// indicator
		field44 += (nextRecord ? memo.getNextRecordKey() : Strings.pad("", 30)); //next
		// record
		// key
		field44 += (nextRecord ? memo.getDataElement() : Strings
				.leftPad("", 3, '0')); //data element
		field44 += (nextRecord ? memo.getEchoBackErrorMessage() : Strings.pad("",
				30)); //error message

		//now we add the field44 to the main msg
		msg.addVariableWidthField(44, 2, field44);

		//now that we have the main bitmap well done, we gonna add the
		// sub-bitmap into it
		msg.addVariableWidthField(123, 4, bit123);

		//Now, lets construct the C header
		String C = new String();
		C += "FACMMI"; //this is the transaction code, msg id, length is 6
		C += ISO_CONSTANT; //this is the ISO constant, value is ISO
		C += ranNum; //this is front end reference number, can be any
		// numberlah
		C += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //current
		// date
		// lah
		//the one below is supposed to beservice number, but i think its
		// actually source id.
		//the sample given by chooi ling is CC, which is referring to call
		// center (Siebel)
		C += CARMA_SOURCE_ID;
		C += Strings.pad("carmapp", 20); //this is the machine name, im
		// setting it to our app server name
		C += "1"; //Financial flag, defaulted to 1 as specified in the doc
		C += Strings.pad("", 20); //TIN, dunno what shit this is, but the doc
		// says 20 empty space, so 20 empty space i
		// put
		C += accNo; //This is form account number, which I presume is the
		// account number/card number lah

		//Now, we will construct the full message with includes
		//ISO Message Type, which is Version + MTI
		//ISO Message, the message itself
		byte[] msgWithType = ISO8583Message.asMessage(ISO8583Message.V1993,
				ISO8583Message.FINPRESENTMENT_MSG_CLASS,
				ISO8583Message.REQUEST_FUNCTION, ISO8583Message.FROM_ACQUIRER, msg);
		//now that we've got the message constructed nicely, lets send it
		//Message is constructed by A + B + C + Message Type (MTI) + D
		//A is the security header
		//B is the length of C header, which is dynamically calculated and
		// place by the sendMessage method
		//C is the C header, as specified by Clarity tech doc
		//D is the ISO Message Type + ISO data
		byte[] sent = sendMessage(clarityConn.getOutputStream(), SECURITY_HEADER, C
				.getBytes(), msgWithType);

		log.info("Trace Info - Trace Number: " + ranNum + "; Tran Code: FACMMI; Cust Id: " + custId + "; Acc Num: " + accNo);

		byte[] respInfo = null;

		try {
			respInfo = fetchReply(clarityConn.getInputStream());
		} catch (IOException e) {
			log.error("Unable to fetch reply, error encountered!", e);
			return new MTMFMemoBusinessBean(
					"Unable to fetch reply, error encountered!\n" + e.getMessage());
		} catch (Exception e) {
			log.error("Unable to fetch reply, error encountered! " + e.getMessage());
			return new MTMFMemoBusinessBean(
					"Unable to fetch reply, error encountered!\n" + e.getMessage());
		}

		Map bitTypes = new HashMap();
		bitTypes.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
		bitTypes.put(new Integer(12), new BitmappedField.FixedWidthBit(12));
		bitTypes.put(new Integer(17), new BitmappedField.FixedWidthBit(4));
		bitTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(3)); //ISO
		// defines
		// this
		// as 4
		bitTypes.put(new Integer(44), new BitmappedField.LVARBit(2, false)); //ISO
		// defines
		// this
		// LLLLVAR
		bitTypes.put(new Integer(60), new BitmappedField.LVARBit(3, false));
		bitTypes.put(new Integer(61), new BitmappedField.LVARBit(3, false));
		bitTypes.put(new Integer(62), new BitmappedField.LVARBit(3, false));
		bitTypes.put(new Integer(63), new BitmappedField.LVARBit(3, false));
		bitTypes.put(new Integer(116), new BitmappedField.LVARBit(3, false));
		bitTypes.put(new Integer(123), new BitmappedField.LVARBit(4, true));
		Map result = BitmappedField.breakdown(false, 64, 1, 64, respInfo, bitTypes);
		writeBreakdown("FACMMI", result);

		Map subTypes = new HashMap();
		subTypes.put(new Integer(2), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(11), new BitmappedField.FixedWidthBit(12));
		subTypes.put(new Integer(20), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(22), new BitmappedField.FixedWidthBit(4));
		subTypes.put(new Integer(23), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(25), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(6));
		subTypes.put(new Integer(51), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(75), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(89), new BitmappedField.LVARBit(1, false));
		Map subResult = BitmappedField.breakdown(false, 128, (byte[]) result
				.get(new Integer(123)), subTypes);
		writeBreakdown("FACMMI - 123", subResult);

		String respString = validateResponse(result, subResult, "FACMMI");

		closeCommunication(clarityConn);

		if (isSuccessful(respString)) {
			return fetchMemo(result, new MTMFMemoBusinessBean(), respString);
		} else {
			return new MTMFMemoBusinessBean(respString);
		}
	}

	public String waiverOfCharges(String accNo, String custId, String userId,
			Double amount, String waiveCode) throws IOException {

		Socket clarityConn = null;
		try {
			clarityConn = getClarityConnection(RELAY_SERVER_HOST, RELAY_SERVER_PORT);
		} catch (Exception ex) {
			log
					.error(
							"Unable to connect to Clarity Server, Clarity interactions will not be available!",
							ex);
			return "Unable to connect to Clarity Server, Clarity interactions will not be available!";
		}

		if (!login(clarityConn)) {
			return "Unable to login to Clarity Server, Clarity interactions will not be available!";
		}

		//lets begin by constructing the sub-bitmap data field, field number
		// 123
		BitmappedField bit123 = new BitmappedField();
		bit123.setBinaryBitmap(false);
		bit123.setBitmapLength(128);
		//LLVAR, Customer Id
		bit123.addVariableWidthField(2, 2, custId); //HF
		//Processing Code, 023000 refers to waiver of charges
		bit123.addFixedWidthField(3, "023000");
		//this is sys trace number, must be 12 in length and all numbers,
		// actually it is front end sequence number
		String ranNum = generateRandom();
		bit123.addFixedWidthField(11, ranNum); //HF

		//LLVAR, Host seq number, we set it to 4 spaces, the bloody host
		// system supposed to fill it up
		bit123.addVariableWidthField(20, 1, "    "); //HF
		bit123.addFixedWidthField(22, CARMA_SOURCE_ID); //HF
		//LLVAR, User id
		bit123.addVariableWidthField(23, 2, userId); //HF
		//LVAR, Teller id, we just set it to space
		bit123.addVariableWidthField(25, 1, " "); //HF

		//Construction of field123.42, I hate this field
		//its an LLLVAR
		String[] waive = waiveCode.split("-"); //index 0 is Type and index 1 is for code
	
		String field42 = new String();
		field42 += Strings.pad("", 4); //Request type, not used
		field42 += Strings.pad(waive[1], 2); // No of Transaction
		field42 += Strings.pad("", 3); //Sequence, not used
		field42 += Strings.pad("", 30); //Name, not used
		field42 += Strings.leftPad("", 8, '0'); //Date of Birth, not used
		field42 += Strings.pad("", 15); //ID No., not used
		field42 += Strings.pad("", 18); //Office phone, not used
		field42 += Strings.pad("", 18); //Home phone, not used
		field42 += Strings.pad("", 30); //Mother maiden name, not used
		field42 += Strings.pad("", 1); //Reason for reporting, not used
		field42 += Strings.pad(waive[0], 1); //Charges Type

		bit123.addVariableWidthField(42, 3, field42);
		//this is the terminal id, i fixed it here to carmapp, coz all our
		// request goes through app server (carmapp)
		bit123.addVariableWidthField(51, 2, "carmapp"); //HF
		//well, this is the LVAR which defined the transaction code, CCWV
		//bit123.addVariableWidthField(89, 1, "CCWV"); //HF
		bit123.addVariableWidthField(89, 1, "    "); //HF

		//now, we've got field 123, lets construct the main part of the
		// messsge
		BitmappedField msg = new BitmappedField();
		msg.setBinaryBitmap(false);
		msg.setBitmapLength(64); //setting the primary bitmap length
		msg.setSecondaryBitmapLength(64); //setting the secondary bitmap
		// length
		msg.setSecondaryIndicatorIndex(1); //telling the guy where the
		// secondary bitmap starts

		//now lets add the fields
		//this is the primary account number (PAN), LLVAR
		msg.addVariableWidthField(2, 2, accNo);
		//this is fixed transaction code of 6 bytes length, for waiver, its
		// FACCWV
		//its actually based on the code CMMA, and I suppose these Finnese
		// Alliance guys added FA in front it, coz its their name
		msg.addFixedWidthField(3, "FACCWV");

		//This is the waiver amount field
		//probably there could be a better way of doing this, but this is just
		// a quick way
		//we can do better later
		//here I'm checking for null and 0, and moving it to primitive double
		double amt = ((amount == null || amount.doubleValue() == 0) ? 0 : amount
				.doubleValue());
		//the primitive double, here I multiply with 100, to make the decimal
		// point move 2 spaces right
		//and then im moving it to long, just to throw away the decimal point
		long amtWithoutDec = (long) (amt * 100);
		//well, here im adding it to the bitmap, before that, im just padding
		// it with leading zeroes
		msg.addFixedWidthField(4, Strings.leftPad("" + amtWithoutDec, 12, '0'));

		//this the current date, in the format as displayed below.
		msg.addFixedWidthField(12, new SimpleDateFormat("yyMMddHHmmss")
				.format(new Date()));

		//Construction of field56, I hate this field
		//its an LLVAR
		String field56 = new String();
		//The first field is the msg type which consist of Version + MTI
		field56 += ISO8583Message.V1993 + ""
				+ ISO8583Message.FINPRESENTMENT_MSG_CLASS + ""
				+ ISO8583Message.REQUEST_FUNCTION + "" + ISO8583Message.FROM_ACQUIRER;
		field56 += "000000"; //original sys trace number, i just use the same
		// for the main bit
		field56 += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //Original
		// date/time,
		// I
		// use
		// current
		// date/time
		field56 += Strings.leftPad("", 11, '0'); //original acquirer
		// institution id, dunno what
		// shit this is, I just put 13
		// zeroes
		//now lets add it to the main msg
		msg.addVariableWidthField(56, 2, field56);

		//now that we have the main bitmap well done, we gonna add the
		// sub-bitmap into it
		msg.addVariableWidthField(123, 4, bit123);

		//Now, lets construct the C header
		String C = new String();
		C += "FACCWV"; //this is the transaction code, msg id, length is 6
		C += ISO_CONSTANT; //this is the ISO constant, value is ISO
		C += ranNum; //this is front end reference number, can be any
		// numberlah
		C += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //current
		// date
		// lah
		//the one below is supposed to beservice number, but i think its
		// actually source id.
		//the sample given by chooi ling is CC, which is referring to call
		// center (Siebel)
		C += CARMA_SOURCE_ID;
		C += Strings.pad("carmapp", 20); //this is the machine name, im
		// setting it to our app server name
		C += "1"; //Financial flag, defaulted to 1 as specified in the doc
		C += Strings.pad("", 20); //TIN, dunno what shit this is, but the doc
		// says 20 empty space, so 20 empty space i
		// put
		C += accNo; //This is form account number, which I presume is the
		// account number/card number lah

		//Now, we will construct the full message with includes
		//ISO Message Type, which is Version + MTI
		//ISO Message, the message itself
		byte[] msgWithType = ISO8583Message.asMessage(ISO8583Message.V1993,
				ISO8583Message.FINPRESENTMENT_MSG_CLASS,
				ISO8583Message.REQUEST_FUNCTION, ISO8583Message.FROM_ACQUIRER, msg);

		//now that we've got the message constructed nicely, lets send it
		//Message is constructed by A + B + C + Message Type (MTI) + D
		//A is the security header
		//B is the length of C header, which is dynamically calculated and
		// place by the sendMessage method
		//C is the C header, as specified by Clarity tech doc
		//D is the ISO Message Type + ISO data
		byte[] sent = sendMessage(clarityConn.getOutputStream(), SECURITY_HEADER, C
				.getBytes(), msgWithType);
		log.info("Trace Info - Trace Number: " + ranNum + "; Tran Code: FACCWV; Cust Id: " + custId + "; Acc Num: " + accNo);
		byte[] respInfo = null;

		try {
			respInfo = fetchReply(clarityConn.getInputStream());
		} catch (IOException e) {
			log.error("Unable to fetch reply, error encountered!", e);
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		} catch (Exception e) {
			log.error("Unable to fetch reply, error encountered! " + e.getMessage());
			return "Unable to fetch reply, error encountered!\n" + e.getMessage();
		}

		Map bitTypes = new HashMap();
		bitTypes.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
		bitTypes.put(new Integer(12), new BitmappedField.FixedWidthBit(12));
		bitTypes.put(new Integer(17), new BitmappedField.FixedWidthBit(4));
		bitTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(3)); //ISO
		// defines
		// this
		// as 4
		bitTypes.put(new Integer(44), new BitmappedField.LVARBit(2, false)); //ISO
		// defines
		// this
		// LLLLVAR
		bitTypes.put(new Integer(123), new BitmappedField.LVARBit(4, true));
		Map result = BitmappedField.breakdown(false, 64, 1, 64, respInfo, bitTypes);
		writeBreakdown("FACCWV", result);

		Map subTypes = new HashMap();
		subTypes.put(new Integer(2), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(11), new BitmappedField.FixedWidthBit(12));
		subTypes.put(new Integer(20), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(22), new BitmappedField.FixedWidthBit(4));
		subTypes.put(new Integer(23), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(25), new BitmappedField.LVARBit(1, false));
		subTypes.put(new Integer(39), new BitmappedField.FixedWidthBit(6));
		subTypes.put(new Integer(51), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(75), new BitmappedField.LVARBit(2, false));
		subTypes.put(new Integer(89), new BitmappedField.LVARBit(1, false));
		Map subResult = BitmappedField.breakdown(false, 128, (byte[]) result
				.get(new Integer(123)), subTypes);
		writeBreakdown("FACCWV - 123", subResult);

		closeCommunication(clarityConn);

		return validateResponse(result, subResult, "FACCWV");
	}
	
	public boolean isSuccessful(String errorCode) {
		return (CLARITY_SUCCESS_CODE.equalsIgnoreCase(errorCode));
	}

	private String validateResponse(Map result, Map subResult, String tranCode) {
		//from main result
		String actionCode = Bytes.convertByteArrayToString((byte[]) result.get(new Integer(39)));
		String errorMsg = Bytes.convertByteArrayToString((byte[]) result.get(new Integer(44)));

		//from field 123 result
		String responseCode = Bytes.convertByteArrayToString((byte[]) subResult.get(new Integer(39)));
		String reason = Bytes.convertByteArrayToString((byte[]) subResult.get(new Integer(75)));

		if (!CLARITY_SUCCESS_CODE.equalsIgnoreCase(responseCode)) {
			String logString = new String();
			logString += "Clarity transaction " + tranCode + " was not successful!";
			logString += "Clarity Action Code: " + actionCode + "\n";
			logString += "Clarity Error Message: " + errorMsg + "\n";
			logString += "Clarity Response Code: " + responseCode + "\n";
			logString += "Clarity Reason: " + reason + "\n";
			log.error(logString);
		}
		return responseCode;
	}

	private MTMFMemoBusinessBean fetchMemo(Map result, MTMFMemoBusinessBean memo, String respCode) {
		Integer[] memoPos = {new Integer(61), new Integer(62), new Integer(63),
				new Integer(116)};
		String field44 = Bytes.convertByteArrayToString((byte[]) result
				.get(new Integer(44)));
		String field60 = Bytes.convertByteArrayToString((byte[]) result
				.get(new Integer(60)));
		Vector tmpLines = new Vector();

		try {
			memo.setEndOfRecInd(field44.charAt(0));
		} catch (Exception e) {
			memo.setEndOfRecInd(' ');
		}

		try {
			memo.setNextRecordKey(field44.substring(1, 31));
		} catch (Exception e) {
			memo.setNextRecordKey(Strings.pad("", 30));
		}

		try {
			memo.setDataElement(field44.substring(31, 34));
		} catch (Exception e) {
			memo.setDataElement(Strings.pad("", 3));
		}

		try {
			memo.setEchoBackErrorMessage(field44.substring(34, 64));
		} catch (Exception ex) {
			memo.setEchoBackErrorMessage(Strings.pad("", 30));
		}

		//4 here means by default i consider all the memo fields can be obtained
		int numOfLines = 4;
		try {
			SimpleDateFormat memoDtFmt = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				memo.setDateTime(memoDtFmt.parse(field60.substring(0, 14)));
			} catch (ParseException ex) {
				//wrong date, so i just set the date time to null
				memo.setDateTime(null);
			}
			memo.setCreator(field60.substring(14, 17));
			memo.setFunction(field60.substring(37, 38));
			memo.setCategoryCode(field60.substring(38, 41));
			memo.setDescription(field60.substring(41, 66).trim());
			try {
				numOfLines = Integer.parseInt(field60.substring(66, 69));
			} catch (NumberFormatException ex1) {
				//set it back to 4 lah, unnecessary, but i simply do it
				numOfLines = 0;
			}
		} catch (IndexOutOfBoundsException iobe) {
			//not gonna do anything, it just means the line has ended, no other additional data
		}

		for (int i = 0; (i < numOfLines && i < memoPos.length); i++) {
			String tmpLine = Bytes.convertByteArrayToString((byte[]) result
					.get(memoPos[i]));
			if (tmpLine != null && tmpLine.trim().length() != 0) {
				tmpLines.add(tmpLine.trim());
			}
		}
		memo.setContent((String[]) tmpLines.toArray(new String[0]));
		memo.setErrorCode(respCode);
		return memo;
	}

	private void writeBreakdown(String tranCode, Map broken) {
		FileWriter brWriter;
		try {
			brWriter = new FileWriter(new SimpleDateFormat("yyyyMMdd.hhmmss")
					.format(new Date())
					+ "-" + tranCode);
		} catch (IOException ex) {
			log.warn("Unable to create temporary log file!");
			return;
		}

		Iterator keys = broken.keySet().iterator();
		while (keys.hasNext()) {
			Integer bit = (Integer) keys.next();
			byte[] val = (byte[]) broken.get(bit);
			String byteString = new String();
			String charString = new String();
			for (int i = 0; i < val.length; i++) {
				int convVal = (int) val[i];
				byteString += convVal;
				charString += (convVal == 32 ? "<32>" : "" + (char) convVal);
			}

			try {
				brWriter.write("--------------------- Bit " + bit
						+ " ---------------------\n");
				brWriter.write("Bytes - \"" + byteString + "\"\n");
				brWriter.write("Chars - \"" + charString + "\"\n");
				brWriter.write("------------------- End Bit " + bit
						+ " -------------------\n");
			} catch (IOException ex1) {
				log.warn("Unable to write to file!");
			}
		}

		try {
			brWriter.flush();
		} catch (IOException ex2) {
		}
		try {
			brWriter.close();
		} catch (IOException ex3) {
		}
	}
	
	private String generateRandom() {
		Random random = new Random();
		DecimalFormat fmt = new DecimalFormat("000000000000");
		return fmt.format(random.nextFloat() * 100000000000f);
	}
}