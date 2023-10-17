package com.winter.service.project.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class MPPExportExample {
    public static void main(String[] args) {
        // 创建COM组件
        ActiveXComponent app = new ActiveXComponent("MSProject.Application");

        try {
            // 启动Microsoft Project应用程序
            app.setProperty("Visible", true);

            // 创建一个新的项目
            Dispatch project = app.invokeGetComponent("Projects");
            Dispatch.call(project, "Add");

            // 获取活动项目
            Dispatch activeProject = Dispatch.call(app, "ActiveProject").toDispatch();

            // 创建任务并设置属性
            Dispatch tasks = Dispatch.get(activeProject, "Tasks").toDispatch();
            Dispatch task1 = Dispatch.call(tasks, "Add", "任务1").toDispatch();
            Dispatch task2 = Dispatch.call(tasks, "Add", "任务2").toDispatch();

            // 导出MPP文件
            Dispatch.call(activeProject, "SaveAs", "D:\\tmp\\output.mpp");

            System.out.println("MPP文件导出成功！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭Microsoft Project应用程序
//            app.invoke("Quit", 0);
        }
    }
}