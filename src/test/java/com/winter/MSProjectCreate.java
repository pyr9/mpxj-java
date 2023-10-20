package com.winter;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class MSProjectCreate {
    public static void main(String[] args) {
        ActiveXComponent activexComponent = new ActiveXComponent("MSProject.Application");
        //关闭静默打开
        activexComponent.setProperty("Visible", false);
        //设置关闭弹窗
        activexComponent.setProperty("DisplayAlerts", false);

        Dispatch projects = activexComponent.getProperty("Projects").toDispatch();
        String xmlPath = "D:\\tmp\\项目计划-导出模板.xml";
        //打开Xml文件
//        Dispatch newProject = Dispatch.call(activexComponent, "FileOpen", xmlPath).toDispatch();
        Dispatch newProject = Dispatch.call(projects, "Add").toDispatch();
        try {
            // 获取任务表格
            Dispatch taskTable = Dispatch.get(newProject, "TaskTables").toDispatch();
            Dispatch table = Dispatch.call(taskTable, "Item", 1).toDispatch();

            // 获取列集合
            Dispatch columns = Dispatch.get(table, "TableFields").toDispatch();

            // 添加自定义字段作为新列
            Dispatch.call(columns, "Add", 205520997);
            Dispatch.call(columns, "Add", 188743731, 2, 10 ,"中文描述");

//            Dispatch.call(newProject, "FileOpenEx", xmlPath, false, 0, "", "", "");

            // 保存 MPP 文件
            String projectFilePath = "D:\\tmp\\test\\project00466.mpp";
            Dispatch.call(newProject, "SaveAs", projectFilePath);

            System.out.println("MPP file created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            activexComponent.invoke("Quit");
        }
    }
}