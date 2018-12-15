package com.profitera.services.system.clarity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.util.Bytes;
import com.profitera.util.Strings;

public class TcpServer {
  private static Log LOG = LogFactory.getLog(TcpServer.class);
	private int NONE = 0;
	private int LOGGED_IN = 1;
	private ServerSocket serverSocket;
	private int state = NONE;
	public static void main(String args[]) {
		int port = 1500;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			port = 1500;
		}
    LOG.info("TcpServer started on port " + port);
		new TcpServer().start(port);
	}
	public void start(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
      LOG.error(e.getMessage(), e);
		}
		Runnable server = new Runnable() {
			public void run() {
				try {

				LOG.info("Server waiting for client on port "
						+ serverSocket.getLocalPort());
				Socket socket;
				socket = serverSocket.accept();
				InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream();
        LOG.debug("New connection accepted "
						+ socket.getInetAddress() + ":" + socket.getPort());
				while (true) {
					if (input.available() > 0){
						if (state == NONE){
							if (loginReadResponse(input, output))
								state = LOGGED_IN;
						} else{
              LOG.debug("Real message has come in");
                            byte[] requestBytes = gatherFullRequest(input, false);
							output.write(parseRequest(requestBytes));
						}

					}else{
						try {
							Thread.sleep(10);
						} catch (InterruptedException e1) {
							// TODO Jamison, fix brain-dead catch
							e1.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				// TODO Jamison, fix brain-dead catch
				e.printStackTrace();
              }
			}
		};
		new Thread(server).start();
	}
	public void stop() {
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			// TODO Jamison, fix brain-dead catch
			e.printStackTrace();
		}
	}

	private boolean loginReadResponse(InputStream in, OutputStream out) throws IOException{
    LOG.info("## receiving login message ##");
		byte[] security = gatherField(in, "Security-Header", ClarityInteractor.SECURITY_HEADER_FIELD_SIZE);
		byte[] requestLength = gatherField(in, "Request-Length", ClarityInteractor.REQUEST_LENGTH_FIELD_SIZE);
		byte[] header = gatherField(in, "Header", ClarityInteractor.LOGIN_HEADER_FIELD_SIZE);
		int requestLen = Bytes.buildInt(requestLength);
    LOG.info("Login request has ISO package bytes: " + requestLen);
		byte[] request;
		if (requestLen > 0)
			request = gatherField(in, "Request", requestLen);
		else
			request = new byte[requestLen];
    LOG.info(" ## login received ## " + (security.length + requestLength.length + header.length + request.length) + " bytes received.");
        byte[] loginRep = new byte[ClarityInteractor.LOGIN_RESPONSE_SIZE];
        Arrays.fill(loginRep, (byte)0);
		out.write(loginRep);
		return true;
	}

	private byte[] gatherFullRequest(InputStream in, boolean returnHeaders) throws IOException{
		byte[] security = gatherField(in, "Security-Header", ClarityInteractor.SECURITY_HEADER_FIELD_SIZE);
		byte[] requestLength = gatherField(in, "Request-Length", ClarityInteractor.REQUEST_LENGTH_FIELD_SIZE);
		byte[] header = gatherField(in, "Header", ClarityInteractor.HEADER_FIELD_SIZE);
		int requestLen = Bytes.buildInt(requestLength);
    LOG.debug("Request has ISO package bytes: " + requestLen);
		byte[] request;
		if (requestLen > 0)
			request = gatherField(in, "Request", requestLen);
		else
			request = new byte[requestLen];
        if (returnHeaders)
          return Bytes.mergeArrays(new byte[][]{security, requestLength, header, request});
        else
        {
          byte[] reqNoMTI = new byte[request.length - 4];
          System.arraycopy(request, 4, reqNoMTI, 0, reqNoMTI.length);
		  return reqNoMTI;
		}
	}

	private byte[] gatherField(InputStream in, String name, int fieldWidth) throws IOException{
		int readIn;
		int byteCount = 0;
		byte[] bytes = new byte[fieldWidth];
		while (byteCount < fieldWidth) {
			readIn = in.read();
			bytes[byteCount] = (byte)readIn;
			byteCount++;
		}
    LOG.debug("Gathered field: " + name +"["+fieldWidth+"]" + "'"+ new String(bytes) + "'");
		return bytes;
	}

    private byte[] parseRequest(byte[] request)
    {
      Map bitTypes = new HashMap();
      bitTypes.put(new Integer(2), new BitmappedField.LVARBit(2, false));
	  bitTypes.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
      bitTypes.put(new Integer(4), new BitmappedField.FixedWidthBit(12));
	  bitTypes.put(new Integer(12), new BitmappedField.FixedWidthBit(12));
      bitTypes.put(new Integer(44), new BitmappedField.LVARBit(2, false));
      bitTypes.put(new Integer(56), new BitmappedField.LVARBit(2, false));
      bitTypes.put(new Integer(61), new BitmappedField.LVARBit(3, false));
      bitTypes.put(new Integer(62), new BitmappedField.LVARBit(3, false));
      bitTypes.put(new Integer(63), new BitmappedField.LVARBit(3, false));
      bitTypes.put(new Integer(116), new BitmappedField.LVARBit(3, false));
	  bitTypes.put(new Integer(123), new BitmappedField.LVARBit(4, true));
	  Map requestMap = BitmappedField.breakdown(false, 64, 1, 64, request, bitTypes);

      BitmappedField response = null;

      String reqType = Bytes.convertByteArrayToString((byte[])requestMap.get(new Integer(3)));

      if (reqType != null)
		response = getResponseBitmap(reqType, requestMap);
      else
        throw new RuntimeException("Request transaction type is null!");

      String C = new String();
      C += reqType; //this is the transaction code, msg id, length is 6
      C += ClarityInteractor.ISO_CONSTANT; //this is the ISO constant, value is ISO
      C += "123456789012"; //this is front end reference number, can be any
      C += new SimpleDateFormat("yyMMddHHmmss").format(new Date()); //current
      C += ClarityInteractor.CARMA_SOURCE_ID;
      C += Strings.pad("carmapp", 20); //this is the machine name, im
      C += "1"; //Financial flag, defaulted to 1 as specified in the doc
      C += Strings.pad("", 20); //TIN, dunno what shit this is, but the doc
      C += Bytes.convertByteArrayToString((byte[])requestMap.get(new Integer(2))); //This is form account number, which I presume is the
      byte[] msgWithType = ISO8583Message.asMessage(ISO8583Message.V1993,
        ISO8583Message.FINPRESENTMENT_MSG_CLASS,
        ISO8583Message.RESPONSE_FUNCTION, ISO8583Message.FROM_ACQUIRER, response);
      byte[] sent = sendMessage(ClarityInteractor.SECURITY_HEADER, msgWithType);
      return sent;

    }

    private BitmappedField getResponseBitmap(String type, Map requestMap)
    {
      //break sub-bitmap of the request first
      Map subBitmapStruc = new HashMap();
      subBitmapStruc.put(new Integer(2), new BitmappedField.LVARBit(2, false));
      subBitmapStruc.put(new Integer(3), new BitmappedField.FixedWidthBit(6));
      subBitmapStruc.put(new Integer(11), new BitmappedField.FixedWidthBit(12));
      subBitmapStruc.put(new Integer(20), new BitmappedField.LVARBit(1, false));
      subBitmapStruc.put(new Integer(22), new BitmappedField.FixedWidthBit(4));
      subBitmapStruc.put(new Integer(23), new BitmappedField.LVARBit(2, false));
      subBitmapStruc.put(new Integer(25), new BitmappedField.LVARBit(1, false));
      subBitmapStruc.put(new Integer(42), new BitmappedField.LVARBit(3, false));
      subBitmapStruc.put(new Integer(51), new BitmappedField.LVARBit(2, false));
      subBitmapStruc.put(new Integer(89), new BitmappedField.LVARBit(1, false));
      Map subRequestMap = BitmappedField.breakdown(false, 128, (byte[]) requestMap.get(new Integer(123)), subBitmapStruc);

      // create sub-bitmap (bit 123)
      BitmappedField bit123 = new BitmappedField();
	  bit123.setBinaryBitmap(false);
	  bit123.setBitmapLength(128);
	  bit123.addVariableWidthField(2, 2, Bytes.convertByteArrayToString((byte[])subRequestMap.get(new Integer(2))));
      bit123.addFixedWidthField(11, "123456789012");
      bit123.addVariableWidthField(20, 1, "0123");
      bit123.addFixedWidthField(22, ClarityInteractor.CARMA_SOURCE_ID);
      bit123.addVariableWidthField(23, 2, "Some agent");
      bit123.addVariableWidthField(25, 1, "987");
      //if (type.equals("FACMMI"))
        //bit123.addFixedWidthField(39, "900000");
      //else
        bit123.addFixedWidthField(39, "900000");
      bit123.addVariableWidthField(51, 2, "7652");
	  bit123.addVariableWidthField(75, 2, "Rejected for Testing");
      bit123.addVariableWidthField(89, 1, "9999");

      // create main-bitmap
      BitmappedField mainResp = new BitmappedField();
      mainResp.setBinaryBitmap(false);
      mainResp.setBitmapLength(64);
      mainResp.setSecondaryBitmapLength(64);
      mainResp.setSecondaryIndicatorIndex(1);
      mainResp.addFixedWidthField(3, type);
      mainResp.addFixedWidthField(12, new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
      mainResp.addFixedWidthField(17, new SimpleDateFormat("MMdd").format(new Date()));
      mainResp.addFixedWidthField(39, "000");
      mainResp.addVariableWidthField(44, 2, type + " response message");

      if (type.equals("FACMMI"))
      {
		mainResp.addVariableWidthField(60, 3, "This is some stupid information!!");
		mainResp.addVariableWidthField(61, 3, "This is the notes line 1 for testing!!");
		mainResp.addVariableWidthField(62, 3, "This is the notes line 2 for testing!!");
		mainResp.addVariableWidthField(63, 3, "This is the notes line 3 for testing!!");
		mainResp.addVariableWidthField(116, 3, "This is the notes line 4 for testing!!");
	  }

      // add sub-bitmap to main bitmap
      mainResp.addVariableWidthField(123, 4, bit123);
      return mainResp;
    }

	private byte[] sendMessage (byte[] securityHeader, byte[] requesttext)
	{

	  return sendMessage(securityHeader, requesttext, false);
	}

	private byte[] sendMessage (byte[] securityHeader, byte[] requesttext, boolean isLogin)
	{

	  if (securityHeader.length != ClarityInteractor.SECURITY_HEADER_FIELD_SIZE)
		throw new RuntimeException(
		  "Security header (Field A) length mismatch!\nExpected: "
		  + ClarityInteractor.SECURITY_HEADER_FIELD_SIZE + "\nReceived: "
		  + securityHeader.length);

	  String hexText = Long.toHexString(requesttext.length);
	  // 2 hex digits per byte, so I need a String twice as long as the
	  // btye-width
	  hexText = Strings.leftPad(hexText, ClarityInteractor.REQUEST_LENGTH_FIELD_SIZE * 2, '0');
    LOG.debug("Request len: " + requesttext.length + " " + hexText);
	  byte[] requestLength = new byte[ClarityInteractor.REQUEST_LENGTH_FIELD_SIZE];
	  for (int i = 0; i < requestLength.length; i++)
	  {
		requestLength[i] = (byte) Integer.parseInt(hexText.substring(i * 2,
		  i * 2 + 2), 16);
    LOG.debug("Request byte " + i + ": " + requestLength[i]);
	  }

	  return sendMessage(securityHeader, requestLength, requesttext);
	}

	private byte[] sendMessage (byte[] securityHeader, byte[] requestSize, byte[] requestContent)
	{
	  byte[] completeMessage = Bytes.mergeArrays(new byte[][]
		{
		securityHeader, requestSize, requestContent});
	  return completeMessage;
	}

}