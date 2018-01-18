package websockets;

import java.io.IOException;
import java.util.Collection;

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




@ServerEndpoint("/Echo")
public class Echo{
	//DEFINE TYPES
	//DEFINES 
	private static final int RECV_LOGINREQUEST_TYPE = 1;
	private static final int RECV_CATALOGCHANGE_TYPE = 5;
	private static final int RECV_GAMESTARTED_TYPE = 7;
	private static final int RECV_QUESTIONREQUEST_TYPE = 8;
	private static final int RECV_QUESTIONANSWERED_TYPE = 10;
	private static final int ERRORMSG_TYPE = 255;
	
	//DEFINES ERROR-SUBTYPES
	private static final int MAX_PLAYER_ERROR = 0;
	private static final int PLAYERNAME_ALREADY_EXISTS = 1;
	private static final int EMPTY_PLAYERNAME = 2;
	private static final int UNKNOWN_TYPE = 255;
	
	
	//Var declarations global in Function
	private Quiz quiz = Quiz.getInstance();
	private QuizError reason = new QuizError();
	
	
	@OnError
	public void error(Session session, Throwable t) {
		System.out.println("Fehler beim öffnen des WebSockets: " + t);
		
	}
	
	@OnOpen
	public void open(Session session, EndpointConfig conf){
		ConnectionManager.addSession(session);
		System.out.println("Open Socket with SessionID=" + session.getId());
		
	}
	
	@OnClose
	public void close(Session session, CloseReason reason) {
		System.out.println("Session mit der SessionID: " + session.getId() + " gelöscht");
		ConnectionManager.SessionRemove(session);
		
	}
	
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) throws ParseException{
		System.out.println("MSG vom Client mit der SessionID. " + session.getId() + " erhalten mit dem Inhalt: " + msg);
		JSONObject msgJSON = (JSONObject) new JSONParser().parse(msg);
		int msgType = Integer.parseInt(msgJSON.get("type").toString());
		
		if(session.isOpen()) {
			switch(msgType) {
				case RECV_LOGINREQUEST_TYPE:
					System.out.println("LoginRequest erhalten msgType: " + msgType);
					String name = msgJSON.get("name").toString();
						
					if(name != null && name.length() > 0) {
						Player player = quiz.createPlayer(name, reason);
								
						if(player == null) {
							if(reason.getType() == QuizErrorType.TOO_MANY_PLAYERS){
								String errorMsg = new String("Es sind bereits 4 Spieler angemeldet!");
								sendError(session, MAX_PLAYER_ERROR, errorMsg, errorMsg.length());
									
							}
							if(reason.getType() == QuizErrorType.USERNAME_TAKEN) {
								String errorMsg = new String("Spielername bereits vergeben!");
								sendError(session, PLAYERNAME_ALREADY_EXISTS, errorMsg, errorMsg.length());
									
							}
									
						}
						
					}else {
						String errorMsg = new String("Keinen Spielername angegeben");
						sendError(session, EMPTY_PLAYERNAME, errorMsg, errorMsg.length());
						
					}
					break;
					
				case RECV_CATALOGCHANGE_TYPE:
					
					break;
					
				case  RECV_GAMESTARTED_TYPE:
					
					break;
					
				case RECV_QUESTIONREQUEST_TYPE:
					
					break;
					
				case RECV_QUESTIONANSWERED_TYPE:
					
					break;
					
				case ERRORMSG_TYPE:
					
					break;
					
				default: 
					String errorMsg = new String("UNKNOWN_TYPE: ");
					errorMsg += msgType;
					sendError(session, UNKNOWN_TYPE, errorMsg, errorMsg.length());
					break;
					
				}
			
			}
		
		}
		

	
	//Baut die Errors zusammen
	public void sendError(Session session, int subtype, String message, int length) {
		System.out.println("Error wird erstellt und an Client vesendet...");
		JSONObject error = new JSONObject();
		error.put("type", ERRORMSG_TYPE);
		error.put("subtype", subtype);
		error.put("length", length);
		error.put("msg", message);
		sendJSON(session, error);
		
	}
	
	//Sendet JSONObject an den Client
	
	public void sendJSON(Session session, JSONObject obj) {
		System.out.println("Sending JSONObject to Client...");
		try {
			//Daten an Server schicken
			session.getBasicRemote().sendText(obj.toJSONString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

	
	
	
}