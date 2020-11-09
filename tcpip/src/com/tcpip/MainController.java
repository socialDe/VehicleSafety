package com.tcpip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.chat.Client;

@Controller
public class MainController {
	
	Client client;
	public MainController() {
		client = new Client("192.168.0.38",5555,"[WEB]");
		try {
			client.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/main.mc")
	public ModelAndView main() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("main");
		return mv;
	}
	@RequestMapping("/iot.mc")
	public void iot() {
		System.out.println("IoT Send Start...");
		client.sendTarget("/192.168.0.38", "100");
	}
	@RequestMapping("/phone.mc")
	public void phone() {
		System.out.println("phone Send Start...");
		
		URL url = null;
		try {
			url = new URL("https://fcm.googleapis.com/fcm/send");
		} catch (MalformedURLException e) {
			System.out.println("Error while creating Firebase URL | MalformedURLException");
			e.printStackTrace();
		}
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			System.out.println("Error while createing connection with Firebase URL | IOException");
			e.printStackTrace();
		}
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");

		// set my firebase server key
		conn.setRequestProperty("Authorization", "key="
				+ "AAAArEyZKFc:APA91bFbensIi5nImV_Hsz4JRvHGwDCHPLjEgWc6th59MxVvgG--1OrFQLzaYcJnRZJK5Z2qp0vKuE-CsXR7rDHRtrTEyLvqdwAEzN8WmGOukUf1dsobAcQmBT2Sx_MigKhIRXBwC2b7");

		// create notification message into JSON format
		JSONObject message = new JSONObject();
		message.put("to", "/topics/tcpip");
		message.put("priority", "high");
		
		JSONObject notification = new JSONObject();
		notification.put("title", "title1");
		notification.put("body", "body1");
		message.put("notification", notification);
		
		JSONObject data = new JSONObject();
		data.put("control", "control1");
		data.put("data", 100);
		message.put("data", data);


		try {
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			System.out.println("FCM 전송:"+message.toString());
			out.write(message.toString());
			out.flush();
			conn.getInputStream();
			System.out.println("OK...............");

		} catch (IOException e) {
			System.out.println("Error while writing outputstream to firebase sending to ManageApp | IOException");
			e.printStackTrace();
		}	
		
		System.out.println("phone Send End...");
	}
	@RequestMapping("/sendmtoiot.mc") // 메시지 전송 to IoT
	public ModelAndView sendMtoIoT(ModelAndView mv, String iot_id, String iot_contents) {
		System.out.println("Send Message to IoT Start...");
		System.out.println(iot_id+"에게 "+iot_contents+"전송.");
		client.sendTarget("/192.168.0.8", iot_id+"는"+iot_contents+"해라.");
		mv.setViewName("main");
		return mv;
	}
	@RequestMapping("/car.mc")
	   public void car(HttpServletRequest request) {
	      String ip = request.getParameter("ip");
	      String sensor = request.getParameter("sensor");
	      String msg = ip+" "+sensor;
	      client.sendTarget("/192.168.0.38",msg);
	      
	      System.out.println(msg);
	      
	      // 명령 코드 확인
	      // 새로운 수신 메시지일 경우에만 FCM 전송
	      String code = request.getParameter("code");
	      if(code.equals("U")) {
			// FCM setting
			URL url = null;
			try {
				url = new URL("https://fcm.googleapis.com/fcm/send");
			} catch (MalformedURLException e) {
				System.out.println("Error while creating Firebase URL | MalformedURLException");
				e.printStackTrace();
			}
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				System.out.println("Error while createing connection with Firebase URL | IOException");
				e.printStackTrace();
			}
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");

			// set my firebase server key
			conn.setRequestProperty("Authorization", "key="
					+ "AAAArEyZKFc:APA91bFbensIi5nImV_Hsz4JRvHGwDCHPLjEgWc6th59MxVvgG--1OrFQLzaYcJnRZJK5Z2qp0vKuE-CsXR7rDHRtrTEyLvqdwAEzN8WmGOukUf1dsobAcQmBT2Sx_MigKhIRXBwC2b7");
			

			// create notification message into JSON format
			JSONObject message = new JSONObject();
			message.put("to", "/topics/message");
			message.put("priority", "high");
			
			JSONObject notification = new JSONObject();
			notification.put("title", ip);
			notification.put("body", sensor);
			message.put("notification", notification);
			
			JSONObject data = new JSONObject();
			data.put("control", "control1");
			data.put("data", 100);
			message.put("data", data);

			try {
				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
				out.write(message.toString());
				out.flush();
				conn.getInputStream();
				System.out.println("OK...............");

			} catch (IOException e) {
				System.out.println("Error while writing outputstream to firebase sending to ManageApp | IOException");
				e.printStackTrace();
			}
	      }
	   }
}
