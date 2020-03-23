import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;


public class ChatApp {
	
	ServerSocket serverSocket = null;
	Socket socket = null;
	Map<String, PrintWriter> clientMap;
	
	//생성자
	public ChatApp() {
		// 클라이언트의 출력스트림을 저장할 해쉬맵 생성
		clientMap = new HashMap<String, PrintWriter>();
		//해쉬맵 동기화 설정
		Collections.synchronizedMap(clientMap); // 지금 느끼는건. 누락없이 순서대로 In 하기위해 하는것 같음.
	}
		
		
	
	// #2
	Connection con;
	PreparedStatement pstmt1;
	PreparedStatement pstmt2;
	PreparedStatement pstmt3;
	String ip;
	
	
	
	// #1
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("연결되었습니다.1");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	
	// #3
	public void connectDatabase() {
		try {
			con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:xe",
				"scott",
				"tiger");
			System.out.println("연결되었습니다.2");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		//서버 객체 생성.
		ChatApp ms = new ChatApp();
		ms.init(); //소캣 자동연결 쓰래드
//		ms.doRun(); //실행. 3/22 버려도될것같음.
	}
	
	public void init() {
		try {
			serverSocket = new ServerSocket(9999); //9999 포트로 서버소켓 객체생성.
			System.out.println("서버가 시작되었습니다.");
			
			while(true) {
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress()+":"+socket.getPort());
				ip = socket.getInetAddress().toString();
				
				Thread mst = new MultiServerT(socket); // 쓰레드 생성.
				mst.start(); // 쓰레드 시동.
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	////////////////////////////////////////////////////////////
	// 내부 클래스
	// 클라이언트로 부터 읽어온 메세지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
	
	class MultiServerT extends Thread{
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		
		// 생성자.
		public MultiServerT(Socket socket) {
			this.socket = socket;
			
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
												this.socket.getInputStream() ));
			} catch (Exception e) {
				System.out.println("예외:"+e);
			}
		}
		
		
		// 쓰레드를 사용하기 위해서 run() 메서드 재정의
		
		@Override
		public void run() {
					
			//String s = "";
//			String name = ""; // 클라이언트로부터 받은 이름을 저장할 변수.
			connectDatabase();
			
			try {
				doRun(in,out); //회원가입/접속/탈퇴를 관리하는 메소드.
				
				
//				// retrun값이있으면 실행하고 없으면 좋료. if문으로 판단하자.
//				name =in.readLine(); // 클라이언트에서 처음으로 보내는 메세지는
//				 // 클라이언트가 사용할 이름이다.
//
//				sendAllMsg("", name + "님이 입장하셨습니다.");
//
//				//현재 객체가 가지고 있는 소캣을 제외하고 다른 소켓(클라이언트)들에게 접속을 알림.
//
//				clientMap.put(name, out); //해쉬맵에 키를 name 으로 출력스트림 객체를 저장.
//				System.out.println("현재 접속자 수는 " +clientMap.size()+"명 입니다.");
//
//				// 입력스트림이 null이 아니면 반복.
//				String s = "";
//				while (in!=null) {
//					s = in.readLine();
//					System.out.println(s);
//
//					if(s.equals("/list"))
//						list(out);
//					else
//						sendAllMsg(name, s);
//				}
//
//				//System.out.println("Bye...");
				
				
				
			} catch(Exception e) {
				System.out.println("예외:"+e);
			} finally {
				//예외가 발생할때 퇴장. 해수맵에서 해당 데이터 제거.
				//보통 종료하거나 나가면 java.net.SocketException : 예외 발생.
				
//				clientMap.remove(name);
//				sendAllMsg("", name + "님이 퇴장하셨습니다.");
//				System.out.println("현재 접속자 수는 " +clientMap.size()+"명 입니다.");
				
				try {
					in.close();
					out.close();
					
					socket.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// 접속된 모든 클라이언트들에게 메세지를 전달.
		public void sendAllMsg(String user, String msg) {
			
			// 출력 스트림을 순차적으로 얻어와서 해당 메세지를 출력한다.
			Iterator<String> it = clientMap.keySet().iterator();
			
			while (it.hasNext()) {
				try {
					PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
					if (user.equals(""))
						it_out.println(msg);
					else
						it_out.println("["+user+"]"+ msg);
				} catch(Exception e) {
					System.out.println("예외 :"+e);
				}
			}
		}
		
		// 접속자 리스트 보내기
		public void list(PrintWriter out) {
			// 출력스트림을 순차적으로 얻어와서 해당 메세지를 출력한다.
			Iterator<String> it = clientMap.keySet().iterator();
			String msg = "사용자 리스트[";
			while (it.hasNext()) {
				msg += (String)it.next() + ",";
			}
			msg = msg.substring(0,msg.length()-1) +"]";
			try {
				out.println(msg);
			} catch (Exception e) {
			}
		}
		
		public void NewMember(BufferedReader in, PrintWriter out) throws IOException {
		
			String id ="";
			String sql ="";
			
			while(true) {
				out.println("ID 중복가입확인 : ");
				id = in.readLine();
				sql = "select * from clientinfo where id = ?";
				
				try {
					pstmt2 = con.prepareStatement(sql);
					pstmt2.setString(1, id);
					ResultSet rs = pstmt2.executeQuery();
					
					if(rs.next()) {
						out.println(rs.getString(1)+"해당아이디는 이미 가입되어있습니다.");
					} else {
						out.println("축하합니다. 해당아이디로 가입을 진행하겠습니다.");
						break;
					}
					rs.close();
				} catch (Exception e) {
					System.out.println("알 수 없는 에러가 발생했습니다.");
				}
			}
			
			sql = "insert into ClientInfo values(?, ?, ?, ?)";
			out.println("비밀번호  : ");
			String pwd = in.readLine();
			out.println("별명 : ");
			String cha = in.readLine();
			
			
			try {
				pstmt1 = con.prepareStatement(sql);
				pstmt1.setString(1, id);
				pstmt1.setString(2, pwd);
				pstmt1.setString(3, ip);
				pstmt1.setString(4, cha);
				int updateCount = pstmt1.executeUpdate();
				System.out.println("데이터베이스에 추가되었습니다.");
				
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("데이터베이스 입력 에러입니다.");
			}
		}
			

		
		
		
	
		public void Member(BufferedReader in, PrintWriter out) throws IOException {
			
			while(true) {
				out.println("아이디를 입력하세요.");
				String id = in.readLine();
				out.println("비밀번호를 입력하세요.");
				String pwd = in.readLine();
				
				String Did = ""; // 데이터베이스에있는 id
				String Dpw = ""; // 데이터베이스에있는 비밀번호

				
				String sql = "select * from ClientInfo where id = ?";
				try {
					pstmt3 = con.prepareStatement(sql);
					pstmt3.setString(1, id);
					ResultSet rs = pstmt3.executeQuery();
//					Right.equals(map)
					if(rs.next()) {
						Did = rs.getString(1);
						Dpw = rs.getString(2);
						
						if(id.equals(Did) && pwd.equals(Dpw)) {
							out.println("로그인되셨습니다.");
							
							return; // 로그인이되면 return이아닌 채팅방 입장.
						} else {
							out.println("아이디나 비밀번호가 틀립니다.");
						}
					} else {
						out.println("아이디나 비밀번호가 틀립니다.");
					}
					
					
					rs.close();
				} catch (Exception e) {
					System.out.println("알 수 없는 에러가 발생했습니다.");
				}
				
			}
		}
			
			
			
			

//			name =in.readLine(); // 클라이언트에서 처음으로 보내는 메세지는
//			 // 클라이언트가 사용할 이름이다.
//			sendAllMsg("", name + "님이 입장하셨습니다.");
//
//			//현재 객체가 가지고 있는 소캣을 제외하고 다른 소켓(클라이언트)들에게 접속을 알림.
//			clientMap.put(name, out); //해쉬맵에 키를 name 으로 출력스트림 객체를 저장.
//
//			//System.out.println("현재 접속자 수는 " +clientMap.size()+"명 입니다.");
//
//			// 입력스트림이 null이 아니면 반복.
//			String s = "";
//			while (in!=null) {
//				s = in.readLine();
//				System.out.println(s);
//
//				if(s.equals("/list"))
//					list(out);
//				else
//					sendAllMsg(name, s);
//			}			
		
		
		
		//=================
		
		
		
		
		public void DelMember() {
			
		}
		
		public void ShowMenu() {
			
		}
		
		public void doRun(BufferedReader in, PrintWriter out) throws IOException {
			while(true) {
				
				//ShowMenu();// 아래문구 showmenu로 변경예정.
				out.println("안녕하세요~환영합니다.");
				
				String choice = in.readLine();
		
				switch (choice) {
				case "1":
					NewMember(in,out);
					break;
				case "2":
					Member(in,out);
					break;
				case "3":
					DelMember();
					break;
				case "4":
					out.println("프로그램을 종료합니다.");
					return;
				default :
					out.println("잘 못 입력하셨습니다.");
					break;
				}
			}
		}
		
		
		
	}
}

//while (in!=null) {
//s = in.readLine();
//System.out.println(s);
//
//if(s.equals("/list"))
//	list(out);
//else
//	sendAllMsg(cha, s);
//}











