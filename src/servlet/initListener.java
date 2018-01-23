package servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.loader.*;

@WebListener
public class initListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		FilesystemLoader loader = new FilesystemLoader("catalogs");
		Quiz quiz = Quiz.getInstance();
		quiz.initCatalogLoader(loader);
		System.out.println("# Loader initialised");
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}