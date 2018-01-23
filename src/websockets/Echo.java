package websockets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.fhwgt.quiz.application.*;
import de.fhwgt.quiz.error.*;
import thread.TimerThread;




@ServerEndpoint("/Echo")
public class Echo{
	//DEFINE TYPES
	//DEFINES 
	private static final int RECV_LOGINREQUEST_TYPE = 1;
	private static final int RECV_CATALOGCHANGE_TYPE = 5;
	private static final int RECV_GAMESTARTED_TYPE = 7;
	private static final int RECV_QUESTIONREQUEST_TYPE = 8;
	private static final int RECV_QUESTIONANSWERED_TYPE = 10;
	private static final int SEND_LOGINREQUEST_TYPE = 1;
	private static final int SEND_CATALOGCHANGE_TYPE = 2;
	private static final int SEND_STARTGAME = 3;
	private static final int SEND_PLAYERLIST = 6;
	private static final int SEND_QUESTIONREQUEST_TYPE = 9;
	private static final int SEND_GAMEOVER_TYPE = 11;
	private static final int SEND_QUESTIONEMPTY_TYPE = 90;
	private static final int SEND_ISSUPERUSER_TYPE = 20;
	private static final int SEND_QUESTIONANSWERED_TYPE = 12;
	private static final int ERRORMSG_TYPE = 255;
	
	//DEFINES ERROR-SUBTYPES
	private static final int MAX_PLAYER_ERROR = 0;
	private static final int PLAYERNAME_ALREADY_EXISTS = 1;
	private static final int EMPTY_PLAYERNAME = 2;
	private static final int GAMESTART_ERROR = 3;
	private static final int CATALOG_ERROR = 4;
	private static final int UNKNOWN_TYPE = 255;
	
	
	//Var declarations global in Function
	private Quiz quiz = Quiz.getInstance();
	private QuizError error = new QuizError();
	private Thread bcThread = new broadcastThread();
	
	
	@OnError
	public void error(Session session, Throwable t) {
		System.out.println("Error Opening WebSocket: " + t);
		
	}
	
	@OnOpen
	public void open(Session session, EndpointConfig conf){
		ConnectionManager.addPreSession(session);
		System.out.println("Open Session with SessionID=" + session.getId());
		
	}
	
	@OnClose
	public void close(Session session, CloseReason reason) {
		System.out.println("Closing Session with SessionID: " + session.getId());
		ConnectionManager.preSessionRemove(session);
		ConnectionManager.SessionRemove(session);
		
	}
	
	@SuppressWarnings({ "unchecked", "null" })
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) throws ParseException{
		System.out.println("MSG recieved from Client with SessionID:  " + session.getId() + " Message Data: " + msg);
		JSONObject msgJSON = (JSONObject) new JSONParser().parse(msg);
		int msgType = Integer.parseInt(msgJSON.get("type").toString());
		
		if(session.isOpen()) {
			switch(msgType) {
				case RECV_LOGINREQUEST_TYPE:
					System.out.println("LoginRequest recived from Client with SessionID:" + session.getId());
					String name = msgJSON.get("name").toString();
						
					if(name != null && name.length() > 0) {
						Player player = quiz.createPlayer(name, error);
								
						if(player == null) {
							if(error.getType() == QuizErrorType.TOO_MANY_PLAYERS){
								sendError(session, MAX_PLAYER_ERROR, error.getDescription(), error.getDescription().length());
								break;
									
							}
							if(error.getType() == QuizErrorType.USERNAME_TAKEN) {
								sendError(session, PLAYERNAME_ALREADY_EXISTS, error.getDescription(), error.getDescription().length());
								break;
								
							}								
						}
						if(!bcThread.isAlive()) {
							bcThread = new broadcastThread();
							bcThread.start();
							
						}
						ConnectionManager.addSession(session, player);
						ConnectionManager.preSessionRemove(session);
						if(player.isSuperuser()) {
							JSONObject superUser = new JSONObject();
							superUser.put("type", SEND_ISSUPERUSER_TYPE);
							
							sendJSON(session, superUser);
							
						}
						JSONObject logRequest = new JSONObject();
						logRequest.put("type", SEND_LOGINREQUEST_TYPE);
						sendJSON(session, logRequest);
						/*
						JSONObject playerList = new JSONObject();
						playerList.put("type", SEND_PLAYERLIST);*/
						
					}else {
						sendError(session, EMPTY_PLAYERNAME, error.getDescription(), error.getDescription().length());
						break;
						
					}
					break;
					
				case RECV_CATALOGCHANGE_TYPE:
					//Überprüfen ob angemeldet mit javascript nur admin kann diese funktion aufrufen und diesen Reqzest senden
					System.out.println("CatalogChange recieved from Client with SessionId: " + session.getId());
					quiz.changeCatalog(ConnectionManager.getPlayer(session), msgJSON.get("catalogName").toString(), error);
					if(error.isSet()) {
						sendError(session, CATALOG_ERROR, error.getDescription(), error.getDescription().length());
						break;
						
					}
					JSONObject  katalogChangeJSON = new JSONObject();
					katalogChangeJSON.put("type", SEND_CATALOGCHANGE_TYPE);
					katalogChangeJSON.put("data", quiz.getCurrentCatalog().getName());
					System.out.println("Current Catalog: " + quiz.getCurrentCatalog().getName());
					broadcast(katalogChangeJSON);				
					break;
					
				case  RECV_GAMESTARTED_TYPE:
					System.out.println("StartGame Recived from Client with SessionID: " + session.getId());
					if(quiz.startGame(ConnectionManager.getPlayer(session), error)) {
						System.out.println("Game started!");
						JSONObject gameStartJSON = new JSONObject();
						gameStartJSON.put("type", SEND_STARTGAME);
						gameStartJSON.put("data", "GAME STARTED");
						broadcast(gameStartJSON);
						
					}else {
						sendError(session, GAMESTART_ERROR, error.getDescription(), error.getDescription().length());
						
					}
					break;
					
				case RECV_QUESTIONREQUEST_TYPE:
					TimerTask timeoutTask = new TimerThread(session);
					JSONObject questionJSON =new JSONObject();
					JSONArray answer = new JSONArray();
					System.out.println("case8"+session);
					System.out.println("Case 8:"+ConnectionManager.getPlayer(session));
					Question currentQuestion = quiz.requestQuestion(ConnectionManager.getPlayer(session), timeoutTask, error);
					System.out.println("Question " + currentQuestion);
					if(error.isSet()) 
					{	
						System.out.println(error.getDescription());
						sendError(session, CATALOG_ERROR, error.getDescription(), error.getDescription().length());
						break;
						
					}
					System.out.println(currentQuestion);
					if(currentQuestion != null) {
						questionJSON.put("type", SEND_QUESTIONREQUEST_TYPE);
						questionJSON.put("question", currentQuestion.getQuestion());
						for(String a : currentQuestion.getAnswerList()) {
							answer.add(a);
							
						}
						
						questionJSON.put("answer", answer);
						questionJSON.put("timeout", currentQuestion.getTimeout());
						
					}
					else 
					{
						System.out.println("Question empty -> Player = setDone");
						ConnectionManager.countGameOver();
						questionJSON.put("type", SEND_QUESTIONEMPTY_TYPE);
						quiz.setDone(ConnectionManager.getPlayer(session));
						
					}
					//System.out.println(questionJSON);
					broadcast(questionJSON);
					
					break;
					
			
					 case RECV_QUESTIONANSWERED_TYPE:
						Long index = Long.parseLong((String) msgJSON.get("selection").toString());
						Long correctAnswer = quiz.answerQuestion(ConnectionManager.getPlayer(session), index, error);
						if(correctAnswer != -1) {
							JSONObject questResult = new JSONObject();
							questResult.put("type", SEND_QUESTIONANSWERED_TYPE);
							questResult.put("correct", correctAnswer.toString());
							sendJSON(session, questResult);
						}
						
						
					
					break;
					
				case ERRORMSG_TYPE:
					System.out.println("Error from Client with SessionID: " + session.getId() + " print Message: " + msg);
					break;
					
				default: 
					String errorMsg = new String("UNKNOWN_TYPE: ");
					errorMsg += msgType;
					sendError(session, UNKNOWN_TYPE, errorMsg, errorMsg.length());
					break;
					
				}
			
			}
		
		}
		

	
	//Baut die Errors zusammen und sendet sie ab
	@SuppressWarnings("unchecked")
	public void sendError(Session session, int subtype, String message, int length) {
		System.out.println("Creating ErrorMSG: " + message);
		JSONObject error = new JSONObject();
		error.put("type", ERRORMSG_TYPE);
		error.put("subtype", subtype);
		error.put("length", length);
		error.put("msg", message);
		sendJSON(session, error);
		
	}
	
	//Sendet JSONObject an den Client
	public static synchronized void sendJSON(Session session, JSONObject obj) {
		System.out.println("Sending JSONObject to Client...");
		try {
			//Daten an Server schicken
			session.getBasicRemote().sendText(obj.toJSONString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void broadcast(JSONObject objJSON){
		Set<Session> sessionMap = ConnectionManager.getSessions();
		for(Iterator<Session> iter = sessionMap.iterator(); iter.hasNext();){
			Session ses = iter.next();
			sendJSON(ses, objJSON);
			
		}
		
		List<Session> sessionList = ConnectionManager.getPreSessions();
		if(sessionList.size() > 0){
			for(Session s : sessionList){
				sendJSON(s, objJSON);
				
			}
			
		}
	}
	
	class broadcastThread extends Thread{
		private Echo playerEndpoint;
		
		broadcastThread(){}
		
		@SuppressWarnings("unchecked")
		public void run(){
			System.out.println("~ BroadcastThread ~");
			System.out.println("~ SessionCount: " + ConnectionManager.getSessionCount());
			if(ConnectionManager.getSessionCount() > 0){
				JSONObject playerList = new JSONObject();
				playerList.put("type", SEND_PLAYERLIST);
				playerList.put("count", ConnectionManager.getSessionCount());
				Collection<Player> playerCollection = ConnectionManager.getPlayers();
				JSONArray players = new JSONArray();
				String spieler[][] = new String[playerCollection.size()][3];
				int countPlayer = 0;
				for(Player p : playerCollection){
					spieler[countPlayer][0] = p.getName();
					spieler[countPlayer][1] = "" + p.getScore();
					spieler[countPlayer][2] = "" +p.getId();
					countPlayer++;
					
				}
				
				Arrays.sort(spieler, new Comparator<String[]>(){
					@Override
					public int compare(final String[] entry1, final String[] entry2){
						final String time1 = entry1[1];
						int t1 = Integer.parseInt(time1);
						final String time2 = entry2[1];
						int t2 = Integer.parseInt(time2);
						
						return Integer.compare(t1, t2);
						
					}			
					
				});
				
				for(int i = 0; i < spieler.length; i++){
					JSONObject obj = new JSONObject();
					obj.put("name", spieler[i][0]);
					obj.put("score", spieler[i][1]);
					obj.put("id", spieler[i][2]);
					players.add(obj);
					
				}
				
				playerList.put("players", players);
				
				//GameOver Nachricht an alle Clients
				JSONObject gameOver = new JSONObject();
				if(ConnectionManager.getGameOver() == playerList.size()) {
					gameOver.put("type", SEND_GAMEOVER_TYPE);
					
					Set<Session> sList =ConnectionManager.getSessions();
					for(Iterator<Session> iter = sList.iterator(); iter.hasNext();) {
						Session s = iter.next();
						//Nachricht an alle "echten" Sessions senden
						Echo.sendJSON(s, gameOver);
						
					}
					
				}
				broadcast(playerList);	
					
				
				
			}
			
			
		}
		
	}
	

	
	
	
}