package com.profitera.services.system.clarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import com.profitera.util.Bytes;

/**
 *
 *
 * @author jamison
 */
public class ISO8583Message {
	public static final String SECONDARY_BITMAP_BIT_KEY = "SecondaryBitmapBit";
	public static final String PRIMARY_BITMAP_KEY = "PrimaryBitmap";
	public static final String SECONDARY_BITMAP_KEY = "SecondaryBitmap";
	public static final String SUBBITMAP = "SubBitmap";
	public static final char V1987 = '0';
	public static final char V1993 = '1';
	public static final char V2003 = '2';
	//
	public static final char AUTH_MSG_CLASS = '1';
	public static final char FINPRESENTMENT_MSG_CLASS = '2';
	//
	public static final char REQUEST_FUNCTION = '0';
        public static final char RESPONSE_FUNCTION = '1';
	//
	public static final char FROM_ACQUIRER = '0';

	public static byte[] asMessage(char version, char messageClass, char messageFunction, char messageOriginator, BitmappedField messageContent){
		byte[] mti = new byte[]{(byte) version, (byte) messageClass, (byte) messageFunction, (byte) messageOriginator};
		return Bytes.mergeArrays(new byte[][]{mti, messageContent.getContent()});
	}

	public static Map loadMessageFields(String path) throws IOException, Exception{
		Map m = new HashMap();
                File file = new File(path);
                if (!file.exists() || file.isDirectory() || !file.canRead())
                  throw new Exception("File " + path + " is not accessible, please check whether it's a valid file!");
                return loadMessageFields(file);
	}

        public static Map loadMessageFields(File defFile) throws IOException{
                Map m = new HashMap();
                BufferedReader br = new BufferedReader(new FileReader(defFile));
                loadFields(m, br, "");
                return m;
        }

	private static String loadFields(Map m, BufferedReader br, String prefix) throws IOException {
		String line = br.readLine();
		String currentComment = "";
		while(line != null && (line.startsWith(prefix) || line.startsWith("#"))){
			if (line.startsWith("#")){
				currentComment = currentComment+line.substring(1) +"\n";
				line = br.readLine();
				continue;
			}
			String[] linebreakup = line.split("\\s+",3);
			if (linebreakup.length < 2)
				throw new IOException("File format invalid : '"+ line +"'");
			try{
				if (linebreakup[0].substring(prefix.length()).equals("0"))
					m.put(PRIMARY_BITMAP_KEY, new Integer(linebreakup[1]));
				else if (linebreakup.length > 2 && linebreakup[2].equals(SECONDARY_BITMAP_KEY)){
					m.put(SECONDARY_BITMAP_KEY, new Integer(linebreakup[1]));
					m.put(SECONDARY_BITMAP_BIT_KEY, new Integer(linebreakup[0]));
				}else{
					BitmappedField.Field newField;
					if (linebreakup[1].startsWith("L")){
						int i = linebreakup[1].lastIndexOf('L');
						BitmappedField.LVARBit f = new BitmappedField.LVARBit(i+1, false);
						if (linebreakup.length > 2 && linebreakup[2].equals(SUBBITMAP)){
							f.isSubbitmap = true;
							Map submap = new HashMap();
							line = loadFields(submap, br, linebreakup[0]+".");
							f.bitmapDictionary = submap;
							if (linebreakup.length > 2)
								f.shortDescription = linebreakup[2];
							if (!currentComment.equals("")){
								f.longDescription = currentComment;
								currentComment = "";
							}
							m.put(new Integer(linebreakup[0].substring(prefix.length())), f);
							continue;
						}
						m.put(new Integer(linebreakup[0].substring(prefix.length())), f);
						newField = f;
					}else{
						BitmappedField.FixedWidthBit f = new BitmappedField.FixedWidthBit(Integer.parseInt(linebreakup[1]));
						m.put(new Integer(linebreakup[0].substring(prefix.length())), f);
						newField = f;
					}
					if (linebreakup.length > 2)
						newField.shortDescription = linebreakup[2];
					if (!currentComment.equals("")){
						newField.longDescription = currentComment;
						currentComment = "";
					}
				}
				line = br.readLine();
			}catch(NumberFormatException e){
				throw new IOException("File format invalid : '"+ line +"'");
			}
		}
		return line;
	}
}
