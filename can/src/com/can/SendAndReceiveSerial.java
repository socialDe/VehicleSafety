package com.can;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


// 받는 시리얼포트 canpro로 띄우면 작동 안한다!
public class SendAndReceiveSerial implements SerialPortEventListener {
   private BufferedInputStream bin;
   private InputStream in;
   private OutputStream out;
   private SerialPort serialPort;
   private CommPortIdentifier portIdentifier;
   private CommPort commPort;
   private String result;
   private String rawCanID, rawTotal;
   // private boolean start = false;
   
   String sen;

   public String getSen() {
      return sen;
   }

   public void setSen(String sen) {
      this.sen = sen;
   }

   public SendAndReceiveSerial(String portName, boolean mode) {

      try {
         if (mode == true) {
            // 시리얼 포트가 사용가능한지 확인 
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            System.out.printf("Port Connect : %s\n", portName);
            connectSerial();
            // Serial Initialization ....
            (new Thread(new SerialWriter())).start(); //can 네트워크에 들어간다
         }

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void connectSerial() throws Exception {

      if (portIdentifier.isCurrentlyOwned()) {
         System.out.println("Error: Port is currently in use");
      } else {
         commPort = portIdentifier.open(this.getClass().getName(), 5000);
         if (commPort instanceof SerialPort) {
            serialPort = (SerialPort) commPort;
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(921600, // 통신속도 
                  SerialPort.DATABITS_8, // 데이터 비트 
                  SerialPort.STOPBITS_1, // stop 비트 
                  SerialPort.PARITY_NONE); // 패리티 
            in = serialPort.getInputStream();
            bin = new BufferedInputStream(in);
            
            out = serialPort.getOutputStream();
         } else {
            System.out.println("Error: Only serial ports are handled by this example.");
         }
      }
   }

   public void sendSerial(String rawTotal, String rawCanID) {
      this.rawTotal = rawTotal;
      this.rawCanID = rawCanID;
      // System.out.println("send: " + rawTotal);
      try {
         // Thread.sleep(50);
         Thread.sleep(30);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      Thread sendTread = 
            new Thread(new SerialWriter(rawTotal));
      sendTread.start();
   }

   private class SerialWriter implements Runnable {
      String data;

      public SerialWriter() {
         this.data = ":G11A9\r"; // can통신에 참여한다는 신호 can 프로토콜에서 정해놓은 것 :로 시작 \r로 끝난다. 
      }

      public SerialWriter(String serialData) {
         // CheckSum Data 생성 
         this.data = sendDataFormat(serialData);
         // check sum
         // : seriavData chceksum \r
      }

      public String sendDataFormat(String serialData) {
         serialData = serialData.toUpperCase();
         char c[] = serialData.toCharArray();
         int cdata = 0;
         for (char cc : c) {
            cdata += cc;
         }
         
         //check sum
         cdata = (cdata & 0xFF);
         String returnData = ":";
         returnData += serialData + Integer.toHexString(cdata).toUpperCase();
         returnData += "\r";
         return returnData;
      }

      public void run() {
         try {

            byte[] inputData = data.getBytes();

            out.write(inputData); 
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

   }

   

   
   // Asynchronized Receive Data
   // --------------------------------------------------------
   // 데이터를 받는다.
   public void serialEvent(SerialPortEvent event) {
      switch (event.getEventType()) {
      case SerialPortEvent.BI:
      case SerialPortEvent.OE:
      case SerialPortEvent.FE:
      case SerialPortEvent.PE:
      case SerialPortEvent.CD:
      case SerialPortEvent.CTS:
      case SerialPortEvent.DSR:
      case SerialPortEvent.RI:
      case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
         break;
      case SerialPortEvent.DATA_AVAILABLE:
         byte[] readBuffer = new byte[128];

         try {

            while (bin.available() > 0) {
               int numBytes = bin.read(readBuffer);
            }

            String ss = new String(readBuffer);
            System.out.println("Receive Low Data:" + ss + "||");

            // SR: can data만 저장
            sen = ss.substring(1, (27));
            
         } catch (Exception e) {
            e.printStackTrace();
         }
         break;
      }
   }

   

   

   public void close() throws IOException {
      try {
         Thread.sleep(100);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      if (in != null) {
         in.close();
      }
      if (out != null) {
         out.close();
      }
      if (commPort != null) {
         commPort.close();
      }

   }

   

   public static void main(String args[]) throws IOException {

      SendAndReceiveSerial ss = new SendAndReceiveSerial("COM4", true);
      while(true) {
    	  try {
			Thread.sleep(5000);
			ss.sendSerial("W2810003B010000000000005011", "10003B01");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
      }
      
      //                ID,    DATA
      //ss.close();
   }

}





