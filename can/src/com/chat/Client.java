package com.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import com.can.SendAndReceiveSerial;
import com.msg.Msg;

public class Client {

   int port;
   String address;
   String id;
   Socket socket;
   Sender sender;
   static SendAndReceiveSerial ss;
   
   static String ip;
   
   public Client() {}
   public Client(String address, int port, String id) {
      this.address = address;
      this.port = port;
      this.id = id;
   }
   
   public void connect() throws IOException {
      // 소켓이 만들어지는 구간 
      try {
         socket = new Socket(address,port);
      } catch (Exception e) {
         while(true) {
            try {
               Thread.sleep(2000);
               socket = new Socket(address,port);

               break;
            } catch (Exception e1) {
               System.out.println("Retry...");
            }
         }
      }
      
      ip = (socket.getInetAddress().toString());
      
      System.out.println("Connected Server:"+address);
      
      sender = new Sender(socket);
      new Receiver(socket).start();
      
      //sendMsg();
   
   }
   
   public void sendMsg() throws IOException {
      Scanner sc = new Scanner(System.in);
      while(true) {
         System.out.println("Input msg");
         String ms = sc.nextLine();
         Msg msg = null;
         // 1을 보내면 서버에서는 사용자 리스트를 보낸다. 
         if(ms.equals("1")){
            msg = new Msg(id,ms);
         }else {
            ArrayList<String> ips = new ArrayList<>();
            ips.add("/192.168.0.61");
            ips.add("/192.168.0.9");
            ips.add("/192.168.0.72");
//그룹 보내기      msg = new Msg(ips,id,ms);   
            msg = new Msg(null,id,ms);  // 전체 보내기 
         }

         
         sender.setMsg(msg);
         new Thread(sender).start();
         
         if(ms.equals("q")){
            break;
         }
      }
      //sc.close();
      if(socket != null) {
         try {
            socket.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      System.out.println("Bye....");
   }
   
   class Sender implements Runnable{
      Socket socket;
      ObjectOutputStream oo;
      Msg msg;
      
      public Sender(Socket socket) throws IOException {
         this.socket = socket;
         oo = new ObjectOutputStream(socket.getOutputStream());
      }
      
      public void setMsg(Msg msg) {
         this.msg = msg;
      }

      @Override
      public void run() {
         if(oo != null) {
            try {
               oo.writeObject(msg);
            } catch (IOException e) {
               //e.printStackTrace();
               try {
                  if(socket != null) {
                     socket.close();   
                  }
               }catch(Exception e1) {
                  e1.printStackTrace();

               }
               // 서버가 끊기면 connect!
               try {
                  Thread.sleep(2000);
                  connect();
                  //sendMsg();
                  System.out.println("test1");
               } catch (Exception e1) {
                  e1.printStackTrace();
               }
               
            }
         }
      }
      
   }
   
   class Receiver extends Thread{
      ObjectInputStream oi;
      public Receiver(Socket socket) throws IOException {
         oi = new ObjectInputStream(socket.getInputStream());
      }
      @Override
      public void run() {
         while(oi != null) {
            Msg msg = null;
            try {
               msg = (Msg) oi.readObject();
               if(msg.getMaps() != null) {
                  HashMap<String,Msg> hm = msg.getMaps();
                  Set<String> keys = hm.keySet();
                  for(String k : keys) {
                     System.out.println(k);
                  }
                  continue;
               }
               System.out.println(msg.getId()+msg.getMsg());
               ss.sendSerial("W2810003B010000050000005011", "10003B01");
               
            } catch (Exception e) {
               e.printStackTrace();
               break;
            }

         } // end while
         try {
            if(oi != null) {
               oi.close();
            }
            if(socket != null) {
               socket.close();
            }
         }catch(Exception e){
         
         }
         // 서버가 끊기면 connect!
         try {
            Thread.sleep(2000);
            System.out.println("test2");
            connect();
            sendMsg();
         } catch (Exception e1) {
            e1.printStackTrace();
         }
      
      }
      
   }
   
   
   
   public static void main(String[] args) throws InterruptedException {
      Client client = new Client("192.168.0.61",5555,"[재욱]");
      
      try {
         client.connect();
         //client.sendMsg();
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      ss = new SendAndReceiveSerial("COM4", true);
      
      
      // 여기서부터 HttpConnection
      String urlstr = "http://192.168.0.61/tcpip/car.mc";
      URL url = null;
      // HttpURLConnection 사용!
      HttpURLConnection con = null;
      
      //System.out.println("-----test-----");
      
      // 5초에 한번씩 랜덤좌표를 전달하는 작업 
      while(true) {
         //이 안에 전체가 있어야 한다! 
         try {
            Random rd = new Random();
            //double sensor = rd.nextDouble()*100;
            String sensor = ss.getSen();
            url = new URL(urlstr+"?ip="+ip+"&sensor="+sensor);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000); // 10초동안 응답이 없으면 타임아웃 
            con.setRequestMethod("POST"); // 어떤 방식으로 보낼지 
            con.getInputStream();
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            con.disconnect();
         }
         Thread.sleep(5000);

      }


      
   }
      

}