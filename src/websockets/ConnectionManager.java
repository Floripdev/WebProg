package websockets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;
import de.fhwgt.quiz.application.Player;

 
// Verwaltet eine threadsichere Liste von Socket-Verbindungen
public class ConnectionManager 
{   
	private static Map <Session, Player> mapSessionPlayer = Collections.synchronizedMap(new ConcurrentHashMap<Session, Player>());
	private static List <Session> mapPreSession = Collections.synchronizedList(new ArrayList<Session>());
 
    // Synchronisierte Zugriffe auf die Liste
    public  static synchronized String outputAllSessions(){ return mapSessionPlayer.toString(); }  
    
    // Verbindung an der Position i holen
    public  static synchronized Set<Session> getSessions(){ return mapSessionPlayer.keySet();}
   
    // Anzahl der Verbindungen besorgen
    public  static synchronized int SessionCount() { return mapSessionPlayer.size();}
    
    // Verbindung hinzufügen
    public  static synchronized void addSession(Session session, Player player) { mapSessionPlayer.put(session, player);    }
    
    // Verbindung entfernen
    public  static synchronized void SessionRemove(Session session) { mapSessionPlayer.remove(session);}
    
    //Spieler aus der Logik holen
    public static synchronized Player getPlayer(Session session) {
    	Player player = null;
    	for(Entry<Session, Player> entry : mapSessionPlayer.entrySet()){
    		if(entry.getKey().equals(session)) {
    			player = entry.getValue();
    			
    		}
    		
    	}	
    	return player;
    	
    }
    
    //Returns Players as Collection
    public static synchronized Collection<Player> getPlayers() {
    	return mapSessionPlayer.values();
    	
    }
    
    //Pre Session to not fill in the "Real" Session
    public static synchronized void addPreSession(Session session) {
    	mapPreSession.add(session);
    	
    }
    
    public static synchronized boolean preSessionRemove(Session session){
    	if(mapPreSession.remove(session)){
    		return true;
    		
    	}else{
    		return false;
    		
    	}
    	
    }
    
    public static synchronized List<Session> getPreSessions(){
    	return mapPreSession;
    	
    }
    
    public static synchronized int getSessionCount() { 
        return mapSessionPlayer.size();
   
    }
    
    public static synchronized int 
    
 
}