package thread;

import java.util.TimerTask;

import javax.websocket.Session;

import org.json.simple.JSONObject;

import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.error.QuizError;
import websockets.ConnectionManager;


private static final int QUESTIN_ERROR = 4;

public class TimerThread extends TimerTask {
	
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
			error.put("subtype", arg1)
			
		}
		
	}

}