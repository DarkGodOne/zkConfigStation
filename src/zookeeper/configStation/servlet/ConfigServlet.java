package zookeeper.configStation.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class configServlet
 */
@WebServlet("/configServlet")
public class ConfigServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static boolean isLogin = false;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConfigServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		isLogin = false;
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		isLogin = false;
	} 

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!isLogin)
			request.getRequestDispatcher("/static/login.html").forward(request,response);
		else
		{
			request.getRequestDispatcher("/static/tree.html").forward(request,response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String user = request.getParameter("user");
    	String pass = request.getParameter("pass");
    	
    	if(user.equals("admin") && pass.equals("gxk123"))
    	{
    		isLogin = true;
    		doGet(request,response);
    	}
    	else
    	{
    		PrintWriter out = response.getWriter();
    		response.setStatus(401);
		    out.append("User or PassWord is Error");
		    out.flush();
		    out.close();
    	}
	}
}
