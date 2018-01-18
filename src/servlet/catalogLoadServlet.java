package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Question;
import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.loader.FilesystemLoader;
import de.fhwgt.quiz.loader.LoaderException;

import org.json.simple.JSONObject;

/**
 * Servlet implementation class catalogLoadServlet
 */
@WebServlet("/catalogLoadServlet")
public class catalogLoadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public catalogLoadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		FilesystemLoader loader = new FilesystemLoader("catalogs");
		Map<String, Catalog> kataloge = new HashMap<String, Catalog>();
	
	
		Quiz quiz = Quiz.getInstance();
		quiz.initCatalogLoader(loader);
		Question q;
		
		//*******XML KOMMUNIKATION FÜR KATALOG ÜBERTRAGUNG***********
		response.setContentType("text/xml");
		PrintWriter writer = response.getWriter();
		//writer.print("<catalogname>test</catalogname>");
		String catName = "<?xml version=\"1.0\"?><catalogs>";
		try {
			kataloge = quiz.getCatalogList();
		} catch (LoaderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(Map.Entry e : kataloge.entrySet()){
			  catName += "<catalogname>" + e.getKey().toString() + "</catalogname>";
			  
			  
			}
		catName += "</catalogs>";
		writer.print(catName);
		
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
