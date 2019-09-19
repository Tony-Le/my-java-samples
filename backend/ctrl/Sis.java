package ctrl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import model.Engine;

/**
 * Servlet implementation class Sis
 */
@WebServlet("/Sis.do")
public class Sis extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Sis() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Writer out = response.getWriter();
		response.setContentType("text/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Engine engine = Engine.getInstance();
		String prefix = request.getParameter("prefix");
		String minGpa = request.getParameter("minGpa");
		String sortBy = request.getParameter("sortBy");
		try {
			// request.setAttribute("result", engine.doSIS(prefix, minGpa, sortBy));
			Gson gson = new Gson();
			out.write(gson.toJson(engine.doSIS(prefix, minGpa, sortBy)));
		} catch (NumberFormatException e) {
			// request.setAttribute("error", "Invalid Entries!");
			out.write("{\"status\":0, \"error\":\"Invalid Entries\"}");
		} catch (Exception e) {
			out.write("{\"status\":0, \"error\":" + e.getMessage() + "}");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
