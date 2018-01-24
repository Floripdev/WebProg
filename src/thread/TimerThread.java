package thread;

import java.io.IOException;
import java.util.TimerTask;

import javax.websocket.Session;

import org.json.simple.JSONObject;

import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.error.QuizError;
import websockets.ConnectionManager;



public class TimerThread extends TimerTask {
	private static final int SEND_TIMEOUT_TYPE = 27;
	private static final int QUESTION_ERROR = 4;
	
	Session session;
	
	public TimerThread(Session session) {
		this.session = session;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		System.out.println("- TimerTask started -");
		Quiz quiz = Quiz.getInstance();
		QuizError quizError = new QuizError();
		if(quiz.answerQuestion(ConnectionManager.getPlayer(session), 4, quizError) == -1) {
			JSONObject error = new JSONObject();
			error.put("type", 255);
			error.put("subtype", QUESTION_ERROR);
			error.put("data", quizError.getDescription());
			
			try {
				session.getBasicRemote().sendText(error.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			JSONObject timeout = new JSONObject();
			timeout.put("type", SEND_TIMEOUT_TYPE);
			timeout.put("timeout", "1");
			timeout.put("correct", "-1");
			
			try {
				session.getBasicRemote().sendText(timeout.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	
	}

}