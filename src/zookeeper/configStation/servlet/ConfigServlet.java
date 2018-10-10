package zookeeper.configStation.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
	
	public static String encodeBase64(String str) {  
        byte[] b = null;  
        String s = null;  
        try {  
            b = str.getBytes("utf-8");  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
        if (b != null) {  
            s = new BASE64Encoder().encode(b);  
        }  
        return s;  
    }  
  
    // 解密  
    public static String decodeBase64(String s) {  
        byte[] b = null;  
        String result = null;  
        if (s != null) {  
            BASE64Decoder decoder = new BASE64Decoder();  
            try {  
                b = decoder.decodeBuffer(s);  
                result = new String(b, "utf-8");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return result;  
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
		String user = decodeBase64(request.getParameter("user"));
    	String pass = decodeBase64(request.getParameter("pass"));
    	
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
