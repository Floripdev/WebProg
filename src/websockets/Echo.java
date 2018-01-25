package websockets;

import java.io.IOException;
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
import thread.broadcastThread;




@ServerEndpoint("/Echo")
public class Echo{
	//DEFINE TYPES
	//DEFINES 
	private static final int RECV_LOGINREQUEST_TYPE = 1;
	private static final int RECV_CATALOGCHANGE_TYPE = 5;
	private static final int RECV_GAMESTARTED_TYPE = 7;
	private static final int RECV_QUESTIONREQUEST_TYPE = 8;
	private static final int RECV_QUESTIONANSWERED_TYPE = 10;
	private static final int RECV_RESTARTGAME_TYPE = 150;
	private static final int SEND_LOGINREQUEST_TYPE = 1;
	private static final int SEND_CATALOGCHANGE_TYPE = 2;
	private static final int SEND_STARTGAME = 3;
	//private static final int SEND_PLAYERLIST = 6;
	private static final int SEND_QUESTIONREQUEST_TYPE = 9;
	//private static final int SEND_GAMEOVER_TYPE = 11;
	private static final int SEND_QUESTIONEMPTY_TYPE = 90;
	private static final int SEND_ISSUPERUSER_TYPE = 20;
	private static final int SEND_QUESTIONANSWERED_TYPE = 12;
	private static final int ERRORMSG_TYPE = 255;
	
	//DEFINES ERROR-SUBTYPES
	private static final int PLAYER_ERROR = 0;
	private static final int EMPTY_PLAYERNAME = 2;
	private static final int GAMESTART_ERROR = 3;
	private static final int CATALOG_ERROR = 4;
	private static final int QUESTIONANSWERED_ERROR = 5;
	private static final int UNKNOWN_TYPE = 255;
	
	
	//Var declarations global in Function
	private static Quiz quiz = Quiz.getInstance();
	private static QuizError error = new QuizError();
	private Thread bcThread = new broadcastThread();
	
	
	@OnError
	public void error(Session session, Throwable t) {
		System.out.println("Error Opening WebSocket: " + t);
		ConnectionManager.SessionRemove(session);
		ConnectionManager.preSessionRemove(session);
		
	}
	
	//Wenn WebSocket geöffnet wird session der PreList hinzufügen
	@OnOpen
	public void open(Session session, EndpointConfig conf){
		ConnectionManager.addPreSession(session);
		System.out.println("Open Session with SessionID=" + session.getId());
		
	}
	
	/*
	 * Wird ausgeführt wenn der Client geschlossen wird
	 * 
	 */
	@OnClose
	public void close(Session session, CloseReason reason) {
		System.out.println("Closing Session with SessionID: " + session.getId());
		ConnectionManager.preSessionRemove(session);
		ConnectionManager.SessionRemove(session);
		
	}
	
	/*
	 * Funktion OnMessage zum Empfangen von JSON-Strings vom Client
	 * 
	 * @param session Web-Socket von dem die Nachricht kommt 
	 * @param msg Nachricht vom Client
	 * @param last
	 * 
	 */
	@SuppressWarnings({ "unchecked" })
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) throws ParseException{
		System.out.println("MSG recieved from Client with SessionID:  " + session.getId() + " Message Data: " + msg);
		JSONObject msgJSON = (JSONObject) new JSONParser().parse(msg);
		int msgType = Integer.parseInt(msgJSON.get("type").toString());
		
		if(session.isOpen()) {
			switch(msgType) {
				//Verarbeitung eines Login Requests
				case RECV_LOGINREQUEST_TYPE: 
					System.out.println("LoginRequest recived from Client with SessionID:" + session.getId());
					String name = msgJSON.get("name").toString();
					
					//Ist userName leer?
					if(name != null && name.length() > 0) {
						Player player = quiz.createPlayer(name, error);
								
						if(player == null) {
							if(error.isSet()){
								sendError(session, PLAYER_ERROR, error.getDescription(), error.getDescription().length());
								break;
									
							}							
						}
						
						//startet den BroadcastThread um alle User über login zu benachrichtigen
						if(!bcThread.isAlive()) {
							bcThread = new broadcastThread();
							bcThread.start();
							
						}
							
							
						
						ConnectionManager.addSession(session, player); //Player in die "Richtige"-Liste eintragen
						ConnectionManager.preSessionRemove(session); // Session aus Temporärer Liste entfernen
						
						//Falls der User superUser ist wird eine SuperUserMessage gesendet
						if(player.isSuperuser()) {
							JSONObject superUser = new JSONObject();
							superUser.put("type", SEND_ISSUPERUSER_TYPE);
							
							sendJSON(session, superUser);
							
						}
						JSONObject logRequest = new JSONObject();
						logRequest.put("type", SEND_LOGINREQUEST_TYPE);
						sendJSON(session, logRequest);
						
					}else {
						sendError(session, EMPTY_PLAYERNAME, error.getDescription(), error.getDescription().length());
						break;
						
					}
					break;
					
				case RECV_CATALOGCHANGE_TYPE:
					System.out.println("CatalogChange recieved from Client with SessionId: " + session.getId());
					//Katalog aus der Nachricht setzen, meldet einen Error wenn der User kein SuperUser ist
					quiz.changeCatalog(ConnectionManager.getPlayer(session), msgJSON.get("catalogName").toString(), error);
					if(error.isSet()) {
						sendError(session, CATALOG_ERROR, error.getDescription(), error.getDescription().length());
						break;
						
					}
					//changeCatalog erfolgreich -> JSONObject mit aktuellem Katalog zusammenbauen und an alle User per broadcast senden 
					JSONObject  katalogChangeJSON = new JSONObject();
					katalogChangeJSON.put("type", SEND_CATALOGCHANGE_TYPE);
					katalogChangeJSON.put("data", quiz.getCurrentCatalog().getName());
					System.out.println("Current Catalog: " + quiz.getCurrentCatalog().getName());
					broadcast(katalogChangeJSON);				
					break;
					
					//Verarbeitung eines GameStarts
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
					
					//Verarbeitung eines Question Requests
				case RECV_QUESTIONREQUEST_TYPE:
					//Timer starten
					TimerTask timeoutTask = new TimerThread(session);
					JSONObject questionJSON =new JSONObject();
					JSONArray answer = new JSONArray();
					
					//Frage für aktuellen Spieler holen
					Question currentQuestion = quiz.requestQuestion(ConnectionManager.getPlayer(session), timeoutTask, error);
					if(error.isSet()) 
					{	
						System.out.println(error.getDescription());
						sendError(session, CATALOG_ERROR, error.getDescription(), error.getDescription().length());
						break;
						
					}
					//Wenn Question vorhanden JSONObject zusammenbauen und versenden
					if(currentQuestion != null) {
						questionJSON.put("type", SEND_QUESTIONREQUEST_TYPE);
						questionJSON.put("question", currentQuestion.getQuestion());
						for(String a : currentQuestion.getAnswerList()) {
							answer.add(a);
							
						}
						
						questionJSON.put("answer", answer);
						questionJSON.put("timeout", currentQuestion.getTimeout());
						
					}
					else //Client auf setDone setzen und über letzte Frage informieren
					{
						System.out.println("Question empty -> Player = setDone");
						ConnectionManager.countGameOver();
						questionJSON.put("type", SEND_QUESTIONEMPTY_TYPE);
						quiz.setDone(ConnectionManager.getPlayer(session));
					
					}
					if(!bcThread.isAlive()) {
						bcThread = new broadcastThread();
						bcThread.start();
						
					}
					sendJSON(session, questionJSON);
					
					break;
					
					//Verarbeitet beantwortete Fragen vom Client
				case RECV_QUESTIONANSWERED_TYPE:
						//Frage mit index der Funktion übergeben
						Long index = Long.parseLong((String) msgJSON.get("selection").toString());
						Long correctAnswer = quiz.answerQuestion(ConnectionManager.getPlayer(session), index, error);
						if(error.isSet()) 
						{	
							System.out.println(error.getDescription());
							sendError(session, QUESTIONANSWERED_ERROR, error.getDescription(), error.getDescription().length());
							break;
							
						}
						//Falls kein Timeout(-1) --> Richtige Antwort an Client schicken
						if(correctAnswer != -1) {
							JSONObject questResult = new JSONObject();
							questResult.put("type", SEND_QUESTIONANSWERED_TYPE);
							questResult.put("correct", correctAnswer.toString());
							sendJSON(session, questResult);
						}
					break;
				
					//Verarbeitet Error_Messages vom Client
				case ERRORMSG_TYPE:
					System.out.println("Error from Client with SessionID: " + session.getId() + " print Message: " + msg);
					break;
					
					//Verarbeitet eine neue Initalisierung des Spieles -- unvolständing
				case RECV_RESTARTGAME_TYPE:
					System.out.println("Restart Button gedrückt");
					ConnectionManager.restartGame();
					removePlayers();
					
					break;
					
					//Default --> Hier landen alle types die nicht zugeordnet werden können
				default: 	
					String errorMsg = new String("UNKNOWN_TYPE: ");
					errorMsg += msgType;
					sendError(session, UNKNOWN_TYPE, errorMsg, errorMsg.length());
					break;
					
				}
			
			}
		
		}
		

	
	/*
	 * Baut die Errors zusammen und sendet sie ab
	 * 
	 * @param session Websocket an welchen gesendet werden soll
	 * @param subtype Sub-Type des Errors
	 * @param message Die Fehler Beschrreibung
	 * @param length länge der Nachricht
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void sendError(Session session, int subtype, String message, int length) {
		System.out.println("Creating ErrorMSG: " + message);
		JSONObject error = new JSONObject();
		error.put("type", ERRORMSG_TYPE);
		error.put("subtype", subtype);
		error.put("length", length);
		error.put("msg", message);
		sendJSON(session, error);
		
	}
	
	/*
	 * Sendet JSONObject an den Client
	 * 
	 * @param session WebSocket an welchen gesendet werden soll
	 * @param obj zu sendender JSON-String
	 * 
	 * @throws IOException wirf einen Fehler falls das versenden schief geht
	 * 
	 */
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
	/*
	 * Funktion sendet einen Broadcast Message an alle User
	 * 
	 * @param objJSON Der zu versendende JSON-String
	 * 
	 */
	public static void broadcast(JSONObject objJSON){
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
	
	/*
	 * Funktion zum löschen der Spieler aus der Spielerliste
	 * 
	 * 
	 * 
	 */
	public static synchronized void removePlayers() {
		Set<Session> sessionMap = ConnectionManager.getSessions();
		for(Iterator<Session> i = sessionMap.iterator(); i.hasNext();) {
			Session s = i.next();
			System.out.println("Player: " + ConnectionManager.getPlayer(s).getName() + " deleted");
			quiz.removePlayer(ConnectionManager.getPlayer(s), error);
			if(error.isSet()) {
				sendError(s, CATALOG_ERROR, error.getDescription(), error.getDescription().length());
				break;
				
			}
			
		}
		
	}
	
}