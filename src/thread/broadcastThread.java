package thread;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import javax.websocket.Session;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.fhwgt.quiz.application.Player;
import websockets.ConnectionManager;
import websockets.Echo;

public class broadcastThread extends Thread{
	private static final int SEND_PLAYERLIST = 6;
	private static final int SEND_GAMEOVER_TYPE = 11;
	
	public broadcastThread(){
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		System.out.println("~ BroadcastThread ~");
		System.out.println("~ SessionCount: " + ConnectionManager.getSessionCount());
		//SpielerListe erstellen
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
			
			//SpielerListe sortieren nach Rang
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
			
			//Spieler-Daten in das objArray einfügen
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
			System.out.println("PLAYERLISTSIZE: " + (int) playerList.get("count"));
			if(ConnectionManager.getGameOver() == (int) playerList.get("count")) {
				gameOver.put("type", SEND_GAMEOVER_TYPE);
				gameOver.put("name", spieler[ConnectionManager.getSessionCount()-1][0]);
				gameOver.put("score", spieler[ConnectionManager.getSessionCount()-1][1]);
				
				
				Set<Session> sList =ConnectionManager.getSessions();
				for(Iterator<Session> iter = sList.iterator(); iter.hasNext();) {
					Session s = iter.next();
					//Nachricht an alle "echten" Sessions senden
					Echo.sendJSON(s, gameOver);
					
				}
				
			}
			//playerList an alle Clients senden
			Echo.broadcast(playerList);	
				
			
			
		}
		
		
	}
	
}