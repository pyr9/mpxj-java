package com.winter.utils;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.writer.ProjectWriter;
import net.sf.mpxj.writer.ProjectWriterUtility;

import java.io.IOException;

public class MPPExportExample {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
        ProjectFile project = new ProjectFile();

        // 创建任务并设置属性
        Task task1 = project.addTask();
        task1.setName("Task 1");

        Task task2 = project.addTask();
        task2.setName("Task 2");

        // 导出MPP文件
        ProjectWriter writer = ProjectWriterUtility.getProjectWriter("path/to/output.mpp");
        writer.write(project, "path/to/output.mpp");

        System.out.println("MPP文件导出成功！");
    }
}