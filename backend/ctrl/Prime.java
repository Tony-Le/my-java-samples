package ctrl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Engine;

/**
 * Servlet implementation class Prime
 */
@WebServlet("/Prime.do")
public class Prime extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Prime()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String min = request.getParameter("min");
		String max = request.getParameter("max");
		String nextPrime = "";
		Engine engine = Engine.getInstance();
		Writer out = response.getWriter();
		response.setContentType("text/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		try
		{
			nextPrime = engine.doPrime(min, max);
			if (nextPrime.equals("-1"))
			{
				// request.setAttribute("error", "No more primes in range.");
				out.write("{\"status\":0, \"error\":\"No more Primes in range.\"}");
			} else
			{
				// request.setAttribute("result", nextPrime);
				out.write("{\"status\":1, \"result\":" + nextPrime + "}");
			}
		} catch (NumberFormatException e)
		{
			// request.setAttribute("error", "Invalid Entries!");
			out.write("{\"status\":0, \"error\":\"Invalid Entries\"}");
		} catch (ArithmeticException e)
		{
			out.write("{\"status\":0, \"error\":\"Invalid Entries\"}");
		} catch (

		Exception e)
		{
			//request.setAttribute("error", e.getMessage());
			out.write("{\"status\":0, \"error\":" + e.getMessage() + "}");
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
