package com.winter.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import net.sf.mpxj.ConstraintType;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mspdi.MSPDIWriter;
import org.apache.poi.hpsf.Variant;

import java.io.IOException;
import java.util.Date;

/*Project导出Mpp文件
大致思路为：Project可以打开XML格式的Project文件 
格式在XML里面已经自定义好了
我们只需要将XML 打开之后另存为 MPP文件就行了
需要处理的是将手动需要完成的操作用代码代替（这里用到的是MS的宏）
MPT 也是如此
*/
public class ExpProject {

	public static void main(String[] args) throws Exception {
		//生成Xml文件
//		String xmlPath = "D:\\tmp\\mppTest.xml";
//		createXmlFile(xmlPath);
		//将生成的Xml文件转换成mpp文件
		String xmlPath = "D:\\tmp\\project基础demo.xml";

		String mppPath = "D:\\tmp\\mppTest44.mpp";
		exportProjectToMpp(xmlPath, mppPath);
	}

	//生成xml文件
	public static void createXmlFile(String xmlPath) throws IOException {
		//String xmlPath ="D:/EXPORT/mppTest.xml";
		MSPDIWriter writer = new MSPDIWriter();
		//创建project文件
		ProjectFile pf = new ProjectFile();
		Task task = pf.addTask();
		task.setEffortDriven(true);
		task.setCritical(false);
		task.setPercentageComplete(0);
		task.setPercentageWorkComplete(0);
		task.setConstraintType(ConstraintType.MUST_START_ON);
		task.setConstraintDate(new Date());
		task.setLevelAssignments(true);
		task.setLevelingCanSplit(true);
		//保存project文件
		writer.write(pf, xmlPath);
	}

//	导出mpp文件
	public static void exportProjectToMpp(String xmlPath, String mppPath) {
		//获取MSProject 对象
		ActiveXComponent activexComponent = new ActiveXComponent("MSProject.Application");
		//设置静默打开文件
		activexComponent.setProperty("Visible", true);
		//设置关闭弹窗
		activexComponent.setProperty("DisplayAlerts", false);
		//打开Xml文件
		Dispatch.call(activexComponent, "FileOpen", xmlPath);
//		//加载自定义的宏
//		Dispatch.call(activexComponent, "Run", "PBSScript");		//执行另存为Mpp文件
		Dispatch.call(activexComponent, "FileSaveAs", mppPath);
		//退出Project
//		activexComponent.invoke("Quit", String.valueOf(new Variant[]{}));
	}
}