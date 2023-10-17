package com.winter.service.project.impl;

import com.winter.service.project.ProjectService;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectServiceImplTest extends TestCase {
    @Autowired
    private ProjectService projectService;

    // 根据mpp文件模版操作文件后保存为xml文件
    @Test
    public void testWriteMppFileToDB() {
        File file = new File("D:\\tmp\\开办新公司-导出模板.mpp");
        projectService.writeMppFileToDB(null,"100",file);
    }


    // xml文件转Mpp文件
    @Test
    public void testExportProjectToMpp() {
        String xmlPath = "D:\\tmp\\project基础demo.xml";
        long time = System.currentTimeMillis();
        String mppPath = "D:\\tmp\\mppTest"+time+".mpp";
        projectService.exportProjectToMpp(xmlPath,mppPath);
    }
}