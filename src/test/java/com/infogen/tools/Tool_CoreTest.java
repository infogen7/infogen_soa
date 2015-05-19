package com.infogen.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.larrylgq.aop.tools.Tool_Core;

public class Tool_CoreTest {

	@Test(groups = { "tools","all" })
	public void stacktrace() {
		List<String> list = new ArrayList<String>();
		try{
			list.get(1);
		}catch(IndexOutOfBoundsException e){
			String str = Tool_Core.stacktrace(e);
			Assert.assertNotNull(str);
		}
	}

	@Test(groups = { "tools","all" })
	public void prepare_files(){
		// TODO  好像不能创建缺失的文件夹		
		Path path = Paths.get("a.b.c.t");
		Path path2 = Paths.get("a.m.n.t");
		Tool_Core.prepare_files(path,path2);
		boolean path_exists = path.toFile().exists();
		boolean path2_exists = path2.toFile().exists();
		Assert.assertEquals(path_exists, true); 
		Assert.assertEquals(path2_exists, true); 
		path.toFile().deleteOnExit();
		path2.toFile().deleteOnExit();
	}
	
	@Test(groups = { "tools","all" })
	public void load_file() throws IOException{
		Path path = Paths.get("a.b.c.t");
		Tool_Core.prepare_files(path);
		List<String> lines = new ArrayList<String>();
		lines.add("line1:something write.");
		lines.add("line2:something write else.");
		Files.write(path, lines, StandardOpenOption.APPEND);		
		String s = Tool_Core.load_file(path);
		Assert.assertEquals(s, "line1:something write.line2:something write else.");
		path.toFile().deleteOnExit();
	}
	
	@Test(groups = { "tools","all" })
	public void MD5() throws NoSuchAlgorithmException {
		String password = "a";
		String HexString = "1";
		String result = Tool_Core.MD5(password, HexString);
		Assert.assertNotNull(result);
		Assert.assertNotEquals(result, password);
	}

	@Test(groups = { "tools","all" })
	public void getHostName() {
		String host = Tool_Core.getHostName();
		Assert.assertNotNull(host);
	}

	@Test(groups = { "tools","all" })
	public void getLocalIP() {
		String localIP = Tool_Core.getLocalIP();
		Assert.assertNotNull(localIP);
	}

	@Test(groups = { "tools","all" })
	@Deprecated
	public void ip_to_long() {
		Tool_Core tool = new Tool_Core();
		Long ipLong = tool.ip_to_long("192.168.100.146");
		Assert.assertEquals(ipLong, new Long("3232261266"));
	}

	@Test(groups = { "tools","all" })
	public void trim() {
		String str = "  a  b  ";
		String resultStr = Tool_Core.trim(str);
		Assert.assertEquals(resultStr, "a b");

	}
}
