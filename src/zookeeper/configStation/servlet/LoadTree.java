package zookeeper.configStation.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.google.gson.Gson;

/**
 * Servlet implementation class LoadTree
 */
@WebServlet("/LoadTree")
public class LoadTree extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// ����log4j����־ʵ��
	private final static String VERSIONNAME = "Manifest-Version";
	private final static String relativeWARPath = "/META-INF/MANIFEST.MF";
	private static Logger LOG = Logger.getLogger(ConfigServlet.class);
	private String logConfig = "./log4j.properties";
	private String webRealPath = ""; 
	private String zkhost = "127.0.0.1:2181";
	private ZooKeeper myzk = null;
	private boolean isConnectZk = false;
	private Gson gson = new Gson();
	
	static class TreeNode{
		String nodeName;
		String nodePath;
		boolean hasChild;
		ArrayList<TreeNode> childNodes;
		TreeNode(){
			nodeName = null;
			nodePath = null;
			hasChild = false;
			childNodes = new ArrayList<TreeNode>();
		}
	}
	
	public class ZkWatcher implements Watcher {
		//private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
		@Override
		public void process(WatchedEvent event) {
			// TODO Auto-generated method stub
			if (KeeperState.SyncConnected == event.getState()) {
				if(isConnectZk == false)
				{
					isConnectZk = true;
					LOG.info("connect zkhost success!");
				}
	        }
			else if(KeeperState.Disconnected == event.getState())
			{
				if(isConnectZk == true)
				{
					isConnectZk = false;
					LOG.info("disconnect from zkhost!");
				}
			}
		}

	}
		
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoadTree() {
        super();
        // TODO Auto-generated constructor stub
    }
    
 
    
    //链接
    private void zookeeperConnect()
    {  	
		try {
			if(myzk != null)
			{
				myzk.close();
				myzk = null;
			}
			zkhost = getServletContext().getInitParameter("zkhost");
			myzk = new ZooKeeper(zkhost, 3000, new ZkWatcher());
			
			int wait = 500;
			while(wait-- > 0 && !isConnectZk)
			{
				Thread.sleep(10);
			}
			LOG.info("connect to zkhost: " + zkhost);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			myzk = null;
			LOG.error("Close connect to zkhost: " + zkhost + " error[" + e.getMessage() + "]");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			myzk = null;
			LOG.error("connect to zkhost: " + zkhost + " error[" + e.getMessage() + "]");
		}
		
    }
    
    private boolean createConfig(String curpath, String filename, StringBuffer result)
    {
    	boolean ret = true;
    	String cPath = "";
    	
    	List<String> cfgInfo = Common.readLines(webRealPath+"/files/import.conf");
    	if(cfgInfo.isEmpty())
    	{
    		result.reverse();
    		result.append("config file is empty");
    		return false;
    	}
		Map<Integer,String> mainPath = new HashMap<Integer,String>();
		for(String line : cfgInfo)
		{
			line = line.trim();
			if(line.isEmpty()) continue;
			if(line.startsWith("#"))
			{
				int num = Common.getFlagNum(line);
				for(int i=num; i<=mainPath.size(); i++)
				{
					mainPath.remove(i);
				}
				
				String tpath = "";
				if(num == 1)
				{
					String rootName = curpath;
					if(curpath.equals("/"))
					{
						rootName = "/";
					}
					else
					{
						int idx = curpath.lastIndexOf("/");
						if(idx >= 0)
						{
							rootName = curpath.substring(idx + 1, curpath.length());
						}
					}
					LOG.info("line: "+line.replace("#", "")+ " rootName: "+rootName);
					if(!line.replace("#", "").equals(rootName))
					{
						result.reverse();
						result.append("root path is not match");
						ret = false;
						break;
					}
					
					tpath = curpath;
				}
				else
				{
					String pPath = mainPath.get(num-1);
					if(pPath == null || pPath.equals(""))
					{
						result.reverse();
						result.append("path node is not inorder");
						ret = false;
						break;
					}
					
					if(pPath.equals("/"))
					{
						tpath = pPath+line.replace("#", "");
					}
					else
					{
						tpath = pPath + "/" + line.replace("#", "");
					}
					
					try {
						if(myzk.exists(tpath, false) == null)
						{
							String data = "1";
							myzk.create(tpath,data.getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
						}
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result.reverse();
						result.append("createConfig: " + tpath + " error: "+e.getMessage());
						LOG.error("createConfig " + tpath + " KeeperException: "+e.getMessage());
						ret = false;
						break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result.reverse();
						result.append("createConfig " + tpath + " error: "+e.getMessage());
						LOG.error("createConfig " + tpath + " InterruptedException: "+e.getMessage());
						ret = false;
						break;
					}
				}
				
				if(!tpath.equals(""))
				{
					mainPath.put(num, tpath);
				}
				cPath = tpath;
			}
			else
			{
				String[] kv = line.split("=", 2);
				if(kv.length != 2) continue;
				String tpath = "";
				if(cPath.equals("/"))
				{
					tpath = cPath + kv[0].trim();
				}
				else
				{
					tpath = cPath + "/" + kv[0].trim();
				}
				
				try {
					if(myzk.exists(tpath, false) == null)
					{
						myzk.create(tpath,kv[1].trim().getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
					}
					else
					{
						myzk.setData(tpath,kv[1].trim().getBytes(),-1);
					}
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result.reverse();
					result.append("createConfig: " + tpath + " error: "+e.getMessage());
					LOG.error("createConfig " + tpath + " KeeperException: "+e.getMessage());
					ret = false;
					break;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result.reverse();
					result.append("createConfig " + tpath + " error: "+e.getMessage());
					LOG.error("createConfig " + tpath + " InterruptedException: "+e.getMessage());
					ret = false;
					break;
				}
			}
		}
		
    	return ret;
    }
    
    private boolean getSubNode(TreeNode node) throws IOException
    {
    	boolean retEnd = false;
    	try {
			Stat stat = new Stat();
			myzk.getData(node.nodePath, new ZkWatcher(), stat);
			if(stat.getNumChildren() > 0)
			{
				node.hasChild = true;
				List<String> result = myzk.getChildren(node.nodePath, false);
				if(result.size() > 0)
				{
					ArrayList<TreeNode> ret = new ArrayList<TreeNode>();
					for(int i=0; i<result.size(); i++)
					{
						TreeNode tmp = new TreeNode();
						tmp.nodeName = result.get(i);
						if(node.nodePath.equals("/"))
						{
							tmp.nodePath = node.nodePath + result.get(i);
						}
						else
						{
							tmp.nodePath = node.nodePath + "/" + result.get(i);
						}
						getSubNode(tmp);
						ret.add(tmp);
						
					}
					node.childNodes = ret;
				}
			}
			else
			{
				node.hasChild = false;
				node.childNodes = new ArrayList<TreeNode>();;
			}
			retEnd = true;
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			throw new IOException(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			throw new IOException(e);
		}
    	
    	return retEnd;
    }
    
    private String getConfig(String curpath, int index) throws IOException
    {
    	String result = "";
    	
    	try {
			Stat stat = new Stat();
			byte[] data = myzk.getData(curpath, new ZkWatcher(), stat);
			if(stat.getNumChildren() == 0)
			{
				String nodename = curpath;
				int idx = nodename.lastIndexOf("/");
				if(idx != 0)
				{
					nodename = nodename.substring(idx + 1, nodename.length());
				}
				result += nodename+" = "+new String(data) + "<br/>";
			}
			else
			{
				String nodename = curpath;
				int idx = nodename.lastIndexOf("/");
				if(!nodename.equals("/"))
				{
					nodename = nodename.substring(idx + 1, nodename.length());
				}
				result += "<br/>";
				for(int i=0; i<=index; i++)
				{
						result += "#";
				}
				result += nodename + "<br/>";
				index++;
				
				List<String> ret = myzk.getChildren(curpath, false);
				for(int i=0; i<ret.size(); i++)
				{
					String tmppath = "";
					if(curpath.equals("/"))
					{
						tmppath = curpath + ret.get(i);
					}
					else
					{
						tmppath = curpath + "/" + ret.get(i);
					}
					result += getConfig(tmppath,index);
				}
			}
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
    		throw new IOException(e);
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
    		throw new IOException(e);
		} catch (IOException e)
    	{
			throw new IOException(e);
    	}
    	return result;
    }
    
    private void delSubNode(String curpath) throws IOException
    {
    	try {
			Stat stat = myzk.exists(curpath,false);
			if(stat != null)
			{
				if(stat.getNumChildren() != 0)
				{
					List<String> ret = myzk.getChildren(curpath, false);
					for(int i=0; i<ret.size(); i++)
					{
						String tmppath = "";
						if(curpath.equals("/"))
						{
							tmppath = curpath + ret.get(i);
						}
						else
						{
							tmppath = curpath + "/" + ret.get(i);
						}
						delSubNode(tmppath);
					}
				}
				myzk.delete(curpath,-1);
			}
			else
			{
				LOG.info("stat is null : "+curpath);
			}
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
    		throw new IOException(e);
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
    		throw new IOException(e);
		} catch (IOException e)
    	{
			throw new IOException(e);
    	}
    }
    
    private static TreeNode getcurNode(TreeNode jsobj,String nodename)
    {
    	TreeNode retobj = null;
    	
    	if(jsobj.nodeName.equals(nodename))
    		retobj = jsobj;
    	else
    	{
    		ArrayList<TreeNode> jsarr = jsobj.childNodes;
    		if(jsarr != null)
    		{
				for(int i=0; i<jsarr.size(); i++)
				{
					TreeNode tobj = jsarr.get(i);
					if(tobj.nodeName.equals(nodename))
					{
						retobj = tobj;
						break;
					}
				}
    		}
    	}
		
		return retobj;
    }
    
    private static TreeNode getTreeNode(TreeNode node,String path,int type)
    {
    	String[] patharray = path.split("/");
		TreeNode tobj = node;
		TreeNode pnode = node;
		for(int i=0; i<patharray.length; i++)
		{
			if(patharray[i].equals(""))
				continue;
			pnode = tobj;
			tobj = getcurNode(pnode,patharray[i]);
			if(tobj == null)
				break;
		}
		if(type == 2)
			tobj = pnode;
		return tobj;
    }
    
    private void loadTree(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String resultstr = "";
		String curpath = request.getParameter("curpath").trim();
		LOG.info("curpath: "+curpath);
		
		try {
			TreeNode root = new TreeNode();
			root.nodeName = curpath;
			root.nodePath = curpath;
			Stat stat = new Stat();
			myzk.getData(curpath, new ZkWatcher(), stat);
			if(stat.getNumChildren() > 0)
			{
				root.hasChild = true;
				List<String> result = myzk.getChildren(curpath, false);
				if(result.size() > 0)
				{
					ArrayList<TreeNode> ret = new ArrayList<TreeNode>();
					for(int i=0; i<result.size(); i++)
					{
						TreeNode tmp = new TreeNode();
						tmp.nodeName = result.get(i);
						if(root.nodePath.equals("/"))
						{
							tmp.nodePath = root.nodePath + result.get(i);
						}
						else
						{
							tmp.nodePath = root.nodePath + "/" + result.get(i);
						}
						getSubNode(tmp);
						ret.add(tmp);
						
					}
					root.childNodes = ret;
				}
			}
			else
			{
				root.hasChild = false;
				root.childNodes = new ArrayList<TreeNode>();
			}
			
			resultstr = gson.toJson(root);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("loadTree KeeperException: "+e.getMessage());
			resultstr = "loadTree KeeperException: "+e.getMessage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("loadTree InterruptedException: "+e.getMessage());
			resultstr = "loadTree InterruptedException: "+e.getMessage();
		} catch (IOException e)
    	{
			e.printStackTrace();
			LOG.error("loadTree IOException: "+e.getMessage());
			resultstr = "loadTree IOException: "+e.getMessage();
    	}
		response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		PrintWriter out = response.getWriter();
	    out.append(URLEncoder.encode(resultstr,"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	private void getData(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String curpath = request.getParameter("curpath").trim();
    	LOG.info("curpath: "+curpath);
    	
    	String result = "";
    	
    	try {
			Stat stat = new Stat();
			byte[] data = myzk.getData(curpath, new ZkWatcher(), stat);
			
			result += "<ul>\n";
			if(data != null)
			{
				result += "<li style='word-wrap:break-word;'>data = "+new String(data)+"</li>";
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			//result += "<li>cZxid = "+stat.getCzxid()+"</li>";
			Date createData = new Date(stat.getCtime());
			result += "<li>ctime = "+sdf.format(createData)+"</li>";
			//result += "<li>mZxid = "+stat.getMzxid()+"</li>";
			Date modData = new Date(stat.getMtime());
			result += "<li>mtime = "+sdf.format(modData)+"</li>";
			//result += "<li>pZxid = "+stat.getPzxid()+"</li>";
			//result += "<li>cversion = "+stat.getCversion()+"</li>";
			result += "<li>dataVersion = "+stat.getVersion()+"</li>";
			//result += "<li>aclVersion = "+stat.getAversion()+"</li>";
			//result += "<li>ephemeralOwner = "+stat.getEphemeralOwner()+"</li>";
			result += "<li>dataLength = "+stat.getDataLength()+"</li>";
			result += "<li>numChildren = "+stat.getNumChildren()+"</li></ul>";
			
			result += "<text style='word-break:keep-all;white-space:nowrap;'><===========SUBNODE CONFIG===========><br/>";
			int index = 0;
			result += getConfig(curpath,index)+"</text>";
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result += "<h1>get "+curpath+" data is error: "+e.getMessage()+"</h1>";
			LOG.error("getData KeeperException: "+e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result += "<h1>get "+curpath+" data is error: "+e.getMessage()+"</h1>";
			LOG.error("getData InterruptedException: "+e.getMessage());
		}
    	catch (IOException e)
    	{
    		e.printStackTrace();
			result += "<h1>get "+curpath+" data is error: "+e.getMessage()+"</h1>";
			LOG.error("getData IOException: "+e.getMessage());
    	}
    	response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	out.append(URLEncoder.encode(result,"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	private void addNode(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String curpath = request.getParameter("curpath").trim();
    	String nodename = request.getParameter("nodename").trim();
    	String nodedata = request.getParameter("nodedata").trim();
    	BufferedReader br = request.getReader();
    	String str, treelist = "";
    	while((str = br.readLine()) != null){
    		treelist += str;
    	}
    	treelist = URLDecoder.decode(treelist,"UTF-8").trim();
    	String resultstr = treelist;
    	LOG.info("curpath: "+curpath);
    	LOG.info("nodename: "+nodename);
    	LOG.info("nodedata: "+nodedata);
    	//LOG.info("treelist: "+treelist);
    	
    	try {
			Stat stat = new Stat();
			myzk.getData(curpath, new ZkWatcher(), stat);
			
			TreeNode tree = gson.fromJson(treelist, TreeNode.class);
			TreeNode parantNode = getTreeNode(tree,curpath,1);
			ArrayList<TreeNode> childs = parantNode.childNodes;
			
			String newpath = "";
			if(curpath.equals("/"))
			{
				newpath = curpath + nodename;
			}
			else
			{
				newpath = curpath + "/" + nodename;
			}
			newpath = myzk.create(newpath,nodedata.getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
			TreeNode newNode = new TreeNode();
			newNode.nodeName = nodename;
			newNode.nodePath = newpath;
			newNode.hasChild = false;
			childs.add(newNode);
			parantNode.hasChild = true;
			
			resultstr = gson.toJson(tree);
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultstr = "addNode error: "+e.getMessage();
			LOG.error("addNode KeeperException: "+e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultstr = "addNode error: "+e.getMessage();
			LOG.error("addNode InterruptedException: "+e.getMessage());
		}
    	response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
	    out.append(resultstr);
	    out.flush();
	    out.close();
    }
    
	private void delNode(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String curpath = request.getParameter("curpath").trim();
    	BufferedReader br = request.getReader();
    	String str, treelist = "";
    	while((str = br.readLine()) != null){
    		treelist += str;
    	}
    	treelist = URLDecoder.decode(treelist,"UTF-8").trim();
    	String resultstr = treelist;
    	LOG.info("curpath: "+curpath);
    	
    	try {
			Stat stat = myzk.exists(curpath, false);
			if(stat != null)
			{
				if(curpath.equals("/") || curpath.equals(""))
				{
					resultstr = "can not del: "+curpath;
				}
				else if(stat.getNumChildren() > 0)
				{
					delSubNode(curpath);
					resultstr = "del "+ curpath +" is over";
				}
				else
				{
					TreeNode tree = gson.fromJson(treelist, TreeNode.class);
					TreeNode parantNode = getTreeNode(tree,curpath,2);
					ArrayList<TreeNode> childs = parantNode.childNodes;
					
					myzk.delete(curpath,-1);
					
					String[] patharray = curpath.split("/");
					for(int i=0; i<childs.size(); i++)
					{
						if(childs.get(i).nodeName.equals(patharray[patharray.length-1]))
						{
							childs.remove(i);
							break;
						}
					}
					if(childs.size() > 0)
					{
						parantNode.hasChild = false;
					}
					
					resultstr = gson.toJson(tree);
				}
				
			}
			else
			{
				resultstr = "path: "+curpath+ " is not exists";
				LOG.error("delNode path: "+curpath+ " is not exists");
			}
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultstr = "delNode error: "+e.getMessage();
			LOG.error("delNode KeeperException: "+e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultstr = "delNode error: "+e.getMessage();
			LOG.error("delNode InterruptedException: "+e.getMessage());
		}
    	response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
	    out.append(URLEncoder.encode(resultstr,"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	private void setNode(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String curpath = request.getParameter("curpath").trim();
    	BufferedReader br = request.getReader();
    	String str, nodedata = "";
    	while((str = br.readLine()) != null){
    		nodedata += str;
    	}
    	nodedata = URLDecoder.decode(nodedata,"UTF-8").trim();
    	LOG.info("curpath: "+curpath);
    	LOG.info("nodedata: "+nodedata);
    	response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	
    	String result = "";
    	try {
			myzk.setData(curpath,nodedata.getBytes(),-1);
			
			Stat stat = new Stat();
			byte[] data = myzk.getData(curpath, new ZkWatcher(), stat);
			result += "<ul>\n";
			if(data != null)
			{
				result += "<li>data = "+new String(data)+"</li>";
			}
			result += "<li>cZxid = "+stat.getCzxid()+"</li>\n";
			result += "<li>ctime = "+stat.getCtime()+"</li>\n";
			result += "<li>mZxid = "+stat.getMzxid()+"</li>\n";
			result += "<li>mtime = "+stat.getMtime()+"</li>\n";
			result += "<li>pZxid = "+stat.getPzxid()+"</li>\n";
			result += "<li>cversion = "+stat.getCversion()+"</li>\n";
			result += "<li>dataVersion = "+stat.getVersion()+"</li>\n";
			result += "<li>aclVersion = "+stat.getAversion()+"</li>\n";
			result += "<li>ephemeralOwner = "+stat.getEphemeralOwner()+"</li>\n";
			result += "<li>dataLength = "+stat.getDataLength()+"</li>\n";
			result += "<li>numChildren = "+stat.getNumChildren()+"</li>\n";
			result += "</ul>\n";
			
    	}catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result += "<h1>get "+curpath+" data is error: "+e.getMessage()+"</h1>";
			LOG.error("setNode KeeperException: "+e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result += "<h1>get "+curpath+" data is error: "+e.getMessage()+"</h1>";
			LOG.error("setNode InterruptedException: "+e.getMessage());
		} 
    	out.append(URLEncoder.encode(result,"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	private void importFile(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	StringBuffer result = new StringBuffer();
    	String curpath = request.getParameter("curpath").trim();
    	LOG.info("import curpath : "+curpath);
    	ServletFileUpload fileUpload = new ServletFileUpload();
    	FileItemIterator iter = null;
    	FileItemStream item = null;
    	InputStream is = null;
		try {
			iter = fileUpload.getItemIterator(request);
	    	while (iter.hasNext()){
				item = iter.next();//获取文件流
				if(!item.isFormField()){
					//这里主要针对图片来写的，因为我用到的是转成图片，获取图片属性。
					is = item.openStream();
					if(is.available()>0){
						BufferedInputStream fileIn = new BufferedInputStream(is);
						byte[] buf = new byte[1024];
						File fOut = new File(webRealPath+"/files/import.conf");
						BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(fOut));
						while (true) { 
							// 读取数据
							int bytesIn = fileIn.read(buf, 0, 1024);
							if (bytesIn == -1) 
							{ 
								break; 
							} 
							else
							{ 
								fileOut.write(buf, 0, bytesIn); 
							} 
						}
						
						fileIn.close();
						fileOut.flush(); 
						fileOut.close();
						LOG.info("upload file: " + fOut.getAbsolutePath() +" Over");
						result.reverse();
						result.append("import file upload OK");
						break;
					}
				}
	    	}
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.reverse();
			result.append("upload file error: "+e.getMessage());
		}
		
		if(!createConfig(curpath, webRealPath+"/files/import.conf", result))
		{
			LOG.error("createConfig error: " + result);
		}
		response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	out.append(URLEncoder.encode(result.toString(),"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	private void exportFile(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
    	String curpath = request.getParameter("curpath").trim();
    	int index = 0;
    	
    	String result = null;
    	try{
    		result = getConfig(curpath,index);
    		
    		if(result == null || result.equals(""))
        	{
        		LOG.warn("export File is null");
        		result = "export File is null";
        	}
        	else
        	{
        		result = result.replace("<br/>","\r\n");
        		File fOut = new File(webRealPath+"/files/export.conf");
        		FileWriter fileWriter = new FileWriter(fOut);
                fileWriter.write(result);  
                fileWriter.close(); // 关闭数据流
                
                if(fOut.exists()){  
                    FileInputStream  fis = new FileInputStream(fOut);  
                    String filename=URLEncoder.encode(fOut.getName(),"utf-8"); //解决中文文件名下载后乱码的问题  
                    byte[] b = new byte[fis.available()];  
                    fis.read(b);  
                    response.setCharacterEncoding("utf-8");  
                    response.setContentLength((int) fOut.length());
                    response.setContentType("application/octet-stream");
                    response.setHeader("Content-Disposition","attachment; filename="+filename+"");
                    //获取响应报文输出流对象  
                    ServletOutputStream  out = response.getOutputStream();  
                    //输出  
                    out.write(b);  
                    out.flush();  
                    out.close();  
                    fis.close();
                	LOG.info("export File is success");
                	return;
                }
                else
                {
                	LOG.warn("export File is not exist");
                	result = "export File is not exist";
                }
        	}
    		
    	} catch (IOException e)
    	{
    		e.printStackTrace();
    		LOG.error("exportFile IOException: "+e.getMessage());
    		result = "exportFile IOException: "+e.getMessage();
    	}
    	response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	out.append(URLEncoder.encode(result,"UTF-8"));
	    out.flush();
	    out.close();
    }
    
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String func = request.getParameter("func").trim();
		LOG.info("get doGet, func = "+ func);
		
		if(func == null)
		{
			response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			PrintWriter out = response.getWriter();
			LOG.error("doGet func is null!");
			out.append(URLEncoder.encode("<h1>404, NOT FOUND!</h1>","UTF-8"));
		    out.flush();
		    out.close();
		    return;
		}
		
		if(!isConnectZk)
		{
			zookeeperConnect();
		}
		
		if(func.equals("loadtree"))
		{
			loadTree(request,response);
		}
		else if(func.equals("getdata"))
		{
			getData(request,response);
		}
		else if(func.equals("addnode"))
		{
			addNode(request,response);
		}
		else if(func.equals("delnode"))
		{
			delNode(request,response);
		}
		else if(func.equals("setnode"))
		{
			setNode(request,response);
		}
		else if(func.equals("uploadfile"))
		{
			importFile(request,response);
		}
		else if(func.equals("downloadfile"))
		{
			exportFile(request,response);
		}
		else
		{
			response.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.append(URLEncoder.encode("<alter>func is not access!</alter>","UTF-8"));
			LOG.error("doGet func is not access!");
		    out.flush();
		    out.close();
		}
		
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOG.info("get loadtree doPost");
		doGet(req, resp);
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		try {
			if(myzk != null)
			{
				myzk.close();
				myzk = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("servlet is stoped!!");
		super.destroy();
	}
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		webRealPath = getServletContext().getRealPath("/");
		logConfig = webRealPath+"/"+getServletContext().getInitParameter("logConfig");
		// log4j配置
		PropertyConfigurator.configure(logConfig);
		try {
			InputStream input = getServletContext().getResourceAsStream(relativeWARPath);
	        Manifest mainfest;
			mainfest = new Manifest(input);
			String version = mainfest.getMainAttributes().getValue(VERSIONNAME);
			LOG.info("configStation Version : V"+version);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("Can't get Version from: "+relativeWARPath);
		}
		LOG.info("logPath: " + logConfig);
		zookeeperConnect();
	}
	
	public static void main(String args[])
	{
		String str = "/";
		int idx = str.lastIndexOf("/");
		str = str.substring(idx + 1, str.length());
        System.out.println(str);
        
        long ti = 1511939860088L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date d = new Date(ti);
        System.out.println("format time : "+sdf.format(d));

		/*Gson gson = new Gson();
		TreeNode treelist = gson.fromJson("{'nodeName':'/','nodePath':'/','childNodes':[{'nodeName':'data','nodePath':'/data','childNodes':[]}]}", TreeNode.class);
		
		String curpath = "/";
		String nodename = "mia";
		TreeNode parantNode = getTreeNode(treelist,curpath,2);
		String newpath = "";
		if(curpath.equals("/"))
		{
			newpath = curpath + nodename;
		}
		else
		{
			newpath = curpath + "/" + nodename;
		}
		//newpath = myzk.create(newpath,nodedata.getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
		TreeNode newNode = new TreeNode();
		newNode.nodeName = nodename;
		newNode.nodePath = newpath;
		newNode.hasChild = false;
		if(parantNode != null)
			parantNode.childNodes.add(newNode);
		
		String json = gson.toJson(treelist);  
		System.out.println("TreeNode: "+json);*/
	}
}
