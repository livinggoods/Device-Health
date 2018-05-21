package org.goods.living.tech.health.device.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.goods.living.tech.health.device.cdi.Hello;
import org.goods.living.tech.health.device.utility.ApplicationParameters;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet({ "/SyncServlet", "/sync" })
public class SyncServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Inject
	private ApplicationParameters applicationParameters;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SyncServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		//.append("  " + hello.getTest()).append("  " + applicationParameters.getHashKey());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
