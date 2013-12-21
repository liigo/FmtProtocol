package com.tianxunnet.fmt;

import java.util.Arrays;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import com.tianxunnet.fmt.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        testFmt();
        testFmtParser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void testFmt() {
    	// Test byte Fmt
    	Fmt byteFmt = Fmt.newByteFmt((byte)127);
    	check(byteFmt.getType() == FmtType.PDT_BYTE);
    	check(byteFmt.getByte() == 127);
    	byteFmt.decRef();
    	
    	// Test short Fmt
    	Fmt shortFmt = Fmt.newShortFmt((short)32767);
    	check(shortFmt.getType() == FmtType.PDT_SHORT);
    	check(shortFmt.getShort() == 32767);
    	byteFmt.decRef();    	
    	
    	// Test int Fmt
    	Fmt intFmt = Fmt.newIntFmt(123);
    	check(intFmt.getType() == FmtType.PDT_INTEGER);
    	check(intFmt.getInt() == 123);
    	intFmt.decRef();
    	
    	// Test long Fmt
    	Fmt longFmt = Fmt.newLongFmt((long)100200);
    	check(longFmt.getType() == FmtType.PDT_LONG);
    	check(longFmt.getLong() == 100200);
    	longFmt.decRef();
    	
    	// Test double Fmt
    	Fmt doubleFmt = Fmt.newDoubleFmt(1.5);
    	check(doubleFmt.getType() == FmtType.PDT_DOUBLE);
    	check(doubleFmt.getDouble() == 1.5);
    	doubleFmt.decRef();
    	
    	// Test string Fmt
    	Fmt strFmt = Fmt.newStringFmt("string汉字");
    	check(strFmt.getType() == FmtType.PDT_STRING);
    	String str = strFmt.getString();
    	check(str.equals("string汉字"));
    	strFmt.decRef();
    	
    	// Test binary Fmt
    	byte[] data = {0x1, 0x2, 0x3, 0x4, 0x5};
    	Fmt binFmt = Fmt.newBinaryFmt(data);
    	check(binFmt.getType() == FmtType.PDT_STRING); // yes, PDT_STRING
    	byte[] tmpdata = binFmt.getBinary();
    	check(binFmt.getBinaryLength() == data.length);
    	check(Arrays.equals(tmpdata, data));
    	binFmt.decRef();
    	
    	// Test object Fmt
    	
    	Fmt fmt1 = Fmt.newIntFmt(1);
    	Fmt fmt2 = Fmt.newIntFmt(2);
    	Fmt fmt3 = Fmt.newIntFmt(3);
    	
    	Fmt objFmt = Fmt.newObjectFmt();
    	check(objFmt.getType() == FmtType.PDT_OBJECT);
    	check(objFmt.getFieldCount() == 0);
    	objFmt.addField("1", fmt1);
    	objFmt.addField("2", fmt2);
    	objFmt.addField("3", fmt3);
    	check(objFmt.getFieldCount() == 3);
    	Fmt tmp = objFmt.getField("2");
    	check(tmp.getType() == FmtType.PDT_INTEGER);
    	check(tmp.getInt() == 2);
    	tmp.decRef();
    	objFmt.decRef();
    	
    	// Test array Fmt
    	Fmt aryFmt = Fmt.newArrayFmt();
    	check(aryFmt.getType() == FmtType.PDT_ARRAY);
    	check(aryFmt.arrayLength() == 0);
    	aryFmt.arrayAppend(fmt1);
    	aryFmt.arrayAppend(fmt3);
    	check(aryFmt.arrayLength() == 2);
    	tmp = aryFmt.arrayAt(0);
    	check(tmp.getType() == FmtType.PDT_INTEGER);
    	check(tmp.getInt() == 1);
    	tmp.decRef();
    	// aryFmt.decRef();
    	
    	// Packet Fmt
    	byte[] packetBytes = fmt1.packet(ProtocolCmd.PT_PING, 0);
    	byte[] expectedPacket1 = {3,1,0,0,6,0,0,0,0,5,4,0,0,0,1};
    	check(Arrays.equals(packetBytes, expectedPacket1));
    	packetBytes = aryFmt.packet(ProtocolCmd.PT_PONG, 0);
    	byte[] expectedPacket2 = {3,1,0,0,7,0,0,0,0,12,11,4,0,0,0,1,4,0,0,0,3,-1};
    	check(Arrays.equals(packetBytes, expectedPacket2));
	
    	fmt1.decRef();
    	fmt2.decRef();
    	fmt3.decRef();
    	aryFmt.decRef();
    }

    private void testFmtParser() {
    	// the data comes from "Packet Fmt" in testFmt()
    	byte[] data = {/*fmt1:*/3,1,0,0,6,0,0,0,0,5,4,0,0,0,1,
    				 /*aryFmt:*/3,1,0,0,7,0,0,0,0,12,11,4,0,0,0,1,4,0,0,0,3,-1};

		final FmtParser parser = FmtParser.newFmtParser(false, false);

		parser.pushData(data, new OnFmtParsed() {
			int index = 0;
			@Override
			public void onFmtParsed(Fmt fmt, int cmd) {
				if(index == 0) {
					// fmt1
					check(fmt.getType() == FmtType.PDT_INTEGER);
					check(fmt.getInt() == 1);
					check(cmd == ProtocolCmd.PT_PING);
				} else if(index == 1) {
					// aryFmt
					check(fmt.getType() == FmtType.PDT_ARRAY);
					check(fmt.arrayLength() == 2);
					Fmt tmp = fmt.arrayAt(1);
					check(tmp.getType() == FmtType.PDT_INTEGER);
					check(tmp.getInt() == 3);
					tmp.decRef();
					check(cmd == ProtocolCmd.PT_PONG);
				}
				index++;
			}
		});
		
		parser.close();
	}
    
    private void check(boolean expr) {
    	if(!expr) {
    		Log.e("fmt-native", "ensure failed!");
    	}
    }
}