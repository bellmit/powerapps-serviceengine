package com.profitera.services.system.clarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.util.Strings;
import com.profitera.util.Twiddler;
import com.profitera.util.Bytes;

/**
 * NOTE: For now, you have to add the fields in the order that
 * they will be in the bitmap.
 *
 * @author jamison
 */
public class BitmappedField {
	private int secondaryIndicatorIndex = -1;
	private int bitmapLength;
	private int secondaryBitmapLength;
	private List availableFields = new ArrayList();
	private List fieldData = new ArrayList();
	// For L*VAR fields the item will hold an Integer Object
	private List fieldTypes = new ArrayList();
	private static final String FIXED_WIDTH = "FIXED_WIDTH";
	private boolean binaryBitmap = true;
	/**
	 * @param i
	 * @param string
	 */
	public void addFixedWidthField(int bit, String asciiContent) {
		availableFields.add(new Integer(bit));
		fieldData.add(asciiContent.getBytes());
		fieldTypes.add(FIXED_WIDTH);
	}

	public int getBitmapLength() {
		return bitmapLength;
	}
	public void setBitmapLength(int bitmapLength) {
		this.bitmapLength = bitmapLength;
	}

	/**
	 * This method will generate the content based on the fields
	 * specifed.
	 * @return
	 */
	public byte[] getContent() {
		int[] fields = getAvailableFields();
		byte[] initialMap;
		if (secondaryIndicatorIndex < 1)
			initialMap = Twiddler.asBitmap(fields, getBitmapLength());
		else{
			int[] tempFields = new int[fields.length+1];
			// I can tack it on the end b/c asBitmap will sort the array upon arrival
			System.arraycopy(fields, 0, tempFields, 0, fields.length);
			tempFields[fields.length] = secondaryIndicatorIndex;
			initialMap = Twiddler.asBitmap(tempFields, getBitmapLength() + getSecondaryBitmapLength());
		}
		if (!binaryBitmap){
			initialMap = Twiddler.asHexString(initialMap).getBytes();
		}
		// The rest will be gathered based on the spec passed in and it will all be merged at the end
		List byteArrays = new ArrayList();
		byteArrays.add(initialMap);
		for (int i = 0; i < fields.length; i++) {
			if (fieldTypes.get(i) instanceof Integer){ // variable width field
				int widthWidth = ((Number)fieldTypes.get(i)).intValue();
				byte[] fieldContent ;
				if (fieldData.get(i) instanceof byte[]) // If I'm a byte array, that's the data.
					fieldContent = (byte[]) fieldData.get(i);
				else // otherwise I must be an embedded bitmapped field
					fieldContent = ((BitmappedField) fieldData.get(i)).getContent();
				if ((fieldContent.length + "").length() > widthWidth)
					throw new RuntimeException("Variable length field at bit " + fields[i] + " is longer than permitted by width specifier.");
				byte[] widthtext = Strings.leftPad(fieldContent.length+"", widthWidth, '0').getBytes();
				byteArrays.add(widthtext);
				byteArrays.add(fieldContent);
			}else if (fieldTypes.get(i).equals(FIXED_WIDTH)){
				byteArrays.add(fieldData.get(i));
			}
		}
		byte[][] realContent = Bytes.toByteArrays(byteArrays);
		return Bytes.mergeArrays(realContent);
	}

	public int getSecondaryBitmapLength() {
		return secondaryBitmapLength;
	}
	public void setSecondaryBitmapLength(int secondaryBitmapLength) {
		this.secondaryBitmapLength = secondaryBitmapLength;
	}
	public int getSecondaryIndicatorIndex() {
		return secondaryIndicatorIndex;
	}
	public void setSecondaryIndicatorIndex(int secondaryIndicatorIndex) {
		this.secondaryIndicatorIndex = secondaryIndicatorIndex;
	}
	public int[] getAvailableFields() {
		int[] results = new int[availableFields.size()];
		for (int i = 0; i < results.length; i++) {
			results[i] = ((Number)availableFields.get(i)).intValue();
		}
		return results;
	}

	/**
	 * @param bit - the bit number in the main map
	 * @param widthSpecifierWidth - this is length of the 'LL' of the LLVAR
	 * @param asciiContent - The actual text to send over as the content, the
	 * content of the 'LL' will be determined by the byte length of this.
	 */
	public void addVariableWidthField(int bit, int widthSpecifierWidth, String asciiContent) {
		availableFields.add(new Integer(bit));
		fieldData.add(asciiContent.getBytes());
		fieldTypes.add(new Integer(widthSpecifierWidth));
	}

	/**
	 * @param i
	 * @param j
	 * @param field
	 */
	public void addVariableWidthField(int bit, int widthSpecifierWidth, BitmappedField field) {
		availableFields.add(new Integer(bit));
		fieldData.add(field);
		fieldTypes.add(new Integer(widthSpecifierWidth));

	}

	public static class Field{
		public String shortDescription;
		public String longDescription;
	}

	public static class LVARBit extends Field{
		public int fieldWidthBytes;
		public boolean isSubbitmap = false;
		public Map bitmapDictionary;
		public LVARBit(int fieldWidthBytes, boolean isSubbitmap) {
			this.fieldWidthBytes = fieldWidthBytes;
			this.isSubbitmap = isSubbitmap;
		}
	}

	public static class FixedWidthBit extends Field{
		public int fieldBytes;
		public FixedWidthBit(int fieldBytes) {
			super();
			this.fieldBytes = fieldBytes;
		}
	}

	public static Map breakdown(boolean isBinaryBitmap, int bitmapSize, byte[] bytes, Map bitTypes){
		return breakdown(isBinaryBitmap, bitmapSize, -1, 0, bytes, bitTypes);
	}

	public static Map breakdown(boolean isBinaryBitmap, int bitmapSize, int secondaryLocation, int secondarySize, byte[] bytes, Map bitTypes){
		Map results = new HashMap();
		int nextStart = 0;
		int[] usedFields = new int[0];
		if (isBinaryBitmap){
			byte[] bitMapBytes = new byte[bitmapSize/8];
			// Secondary has to be a member of the primary bitmap, so
			// parse tyhe prime first, and if the secondary is 1 then
			// reparse with the secondary bitmap bytes as part of the deal.
			System.arraycopy(bytes, 0, bitMapBytes, 0, bitMapBytes.length);
			usedFields = Twiddler.getBitmapFields(bitMapBytes);
			if (in(usedFields,secondaryLocation) > -1){
				bitmapSize = bitmapSize + secondarySize;
				bitMapBytes = new byte[bitmapSize/8];
				System.arraycopy(bytes, 0, bitMapBytes, 0, bitMapBytes.length);
				usedFields = Twiddler.getBitmapFields(bitMapBytes);
			}
			nextStart = bitMapBytes.length;
		}else{
			byte[] bitMapHexBytes = new byte[bitmapSize/4];
			// Secondary has to be a member of the primary bitmap, so
			// parse tyhe prime first, and if the secondary is 1 then
			// reparse with the secondary bitmap bytes as part of the deal.
			System.arraycopy(bytes, 0, bitMapHexBytes, 0, bitMapHexBytes.length);
			usedFields = Twiddler.getBitmapFields(Twiddler.asBitVector(new String(bitMapHexBytes)));
			if (in(usedFields,secondaryLocation) > -1){
				bitmapSize = bitmapSize + secondarySize;
				bitMapHexBytes = new byte[bitmapSize/4];
				System.arraycopy(bytes, 0, bitMapHexBytes, 0, bitMapHexBytes.length);
				usedFields = Twiddler.getBitmapFields(Twiddler.asBitVector(new String(bitMapHexBytes)));
			}
			nextStart = bitMapHexBytes.length;
		}
		for (int i = 0; i < usedFields.length; i++) {
			if (usedFields[i] == secondaryLocation) continue;
			Object fieldType = bitTypes.get(new Integer(usedFields[i]));
			if (fieldType instanceof FixedWidthBit){
				FixedWidthBit f = (FixedWidthBit) fieldType;
				byte[] value = new byte[f.fieldBytes];
				System.arraycopy(bytes, nextStart, value, 0, f.fieldBytes);
				nextStart = nextStart + f.fieldBytes;
				results.put(new Integer(usedFields[i]), value);
			}
			if (fieldType instanceof LVARBit){
				LVARBit lvar = (LVARBit) fieldType;
				byte[] lLength = new byte[lvar.fieldWidthBytes];
				System.arraycopy(bytes, nextStart, lLength, 0, lvar.fieldWidthBytes);
				nextStart = nextStart + lvar.fieldWidthBytes;
				// parse the length as a numeric string
				int byteLength = Integer.parseInt(new String(lLength));
				byte[] value = new byte[byteLength];
				System.arraycopy(bytes, nextStart, value, 0, byteLength);
				nextStart = nextStart + byteLength;
				results.put(new Integer(usedFields[i]), value);
			}
		}
		return results;
	}

	private static int in(int[] ints, int val) {
		for (int i = 0; i < ints.length; i++) {
			if (ints[i] == val) return i;
		}
		return -1;
	}

	/**
	 * @param b
	 */
	public void setBinaryBitmap(boolean b) {
		this.binaryBitmap = b;
	}
}
