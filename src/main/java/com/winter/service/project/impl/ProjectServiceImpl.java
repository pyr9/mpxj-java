package com.winter.service.project.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.winter.mapper.ProjectMapper;
import com.winter.model.Project;
import com.winter.service.project.ProjectService;
import com.winter.utils.StringUtils;
import net.sf.mpxj.*;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mspdi.MSPDIWriter;
import net.sf.mpxj.reader.UniversalProjectReader;
import net.sf.mpxj.writer.ProjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created By Donghua.Chen on  2018/1/9
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    public static final int ROOT_LEVEL_NUM = 1;
    public static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;


    @Override
    public Integer addProjectInfo(Project project) {
        return projectMapper.addProjectSelective(project);
    }

    @Transactional
    @Override
    public void readMmpFileToDB(File file) {
        try {
            MPPReader mppRead = new MPPReader();
            ProjectFile pf = mppRead.read(file);
            System.out.println(file.getName());
            List<Task> tasks = pf.getChildTasks();
            System.out.println("tasks.size() : " + tasks.size());
            List<Project> proList = new LinkedList<>();
            Project pro = new Project();
            pro.setBatchNum(StringUtils.UUID());//生成批次号UUID

            getChildrenTask(tasks.get(0), pro, proList, 0);
        } catch (MPXJException e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        }
    }


    @Override
    public void getChildrenTask(Task task, Project project, List<Project> list, int levelNum) {
        if (task.getResourceAssignments().size() == 0) {
            levelNum++;//层级号需要增加
            List<Task> tasks = task.getChildTasks();
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getResourceAssignments().size() == 0) {//说明还是在父任务层
                    System.out.println("+++++" + tasks.get(i));
                    Project pro = new Project();
                    if (project.getProjId() != null) {//说明不是第一次读取了
                        pro.setParentId(project.getProjId());//将上一级目录的Id赋值给下一级
                    }
                    pro.setBatchNum(project.getBatchNum());
                    pro.setImportTime(new Date());
                    pro.setLevel(levelNum);
                    pro.setTaskName(tasks.get(i).getName());
                    pro.setDurationDate(tasks.get(i).getDuration().toString());
//                    pro.setStartDate(local2date(tasks.get(i).getStart()));
//                    pro.setEndDate(local2date(tasks.get(i).getFinish()));
                    pro.setStartDate(tasks.get(i).getStart());
                    pro.setEndDate(tasks.get(i).getFinish());
                    pro.setResource(tasks.get(i).getResourceGroup());
                    this.addProjectInfo(pro);
                    pro.setProjId(pro.getProjId());
                    //getResourceAssignment(tasks.get(i),pro,list,levelNum);
                    getChildrenTask(tasks.get(i), pro, list, levelNum);
                } else {
                    getChildrenTask(tasks.get(i), project, list, levelNum);
                }
            }
        } else {
            if (project.getProjId() != null) {

                getResourceAssignment(task, project, list, levelNum);
            }
        }
    }

    public void getResourceAssignment(Task task, Project project, List<Project> proList, int levelNum) {
        List<ResourceAssignment> list = task.getResourceAssignments();
        ResourceAssignment rs = list.get(0);
        System.out.println("task = [" + task.getName());
        Project pro = new Project();
        pro.setTaskName(task.getName());
        pro.setParentId(project.getProjId());
        pro.setLevel(levelNum);
        pro.setImportTime(new Date());
        pro.setBatchNum(project.getBatchNum());
        pro.setDurationDate(task.getDuration().toString());
//        pro.setStartDate(local2date(rs.getStart()));
//        pro.setEndDate(local2date(rs.getFinish()));
        pro.setStartDate(rs.getStart());
        pro.setEndDate(rs.getFinish());
        String resource = "";
        if (list.size() > 1) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getResource() != null) {
                    if (i < list.size() - 1) {
                        resource += list.get(i).getResource().getName() + ",";
                    } else {
                        resource += list.get(i).getResource().getName();
                    }
                }
            }
        } else {

            if (list.size() > 0 && list.get(0).getResource() != null) {
                resource = list.get(0).getResource().getName();
            }
        }
        if (!StringUtils.isEmpty(resource)) {
            pro.setResource(resource);
        }
        this.addProjectInfo(pro);
        pro.setProjId(pro.getProjId());
        proList.add(pro);

    }


    @Override
    public void writeMppFileToDB(String fileLocation, String batchNum, File file) {
        try {
//            ProjectFile pf = mppRead.read(file);
            UniversalProjectReader universalProjectReader = new UniversalProjectReader();
            ProjectFile pf =  universalProjectReader.read(file);

            List<Project> projects = projectMapper.getProjectsByBatchNum(batchNum);
            writeChildrenTaskToObj(projects, ROOT_LEVEL_NUM, pf, pf.addTask(), null);

            //生成临时xml文件
            ProjectWriter writer = new MSPDIWriter();
            long time = System.currentTimeMillis();
            String xmlPath = "D:\\tmp\\test\\开办新公司"+".xml";

            try{
                writer.write(pf, xmlPath);
            }catch(IOException ioe){
                throw ioe;
            }


            // 将xml文件转换成Mpp文件
            String mppPath = "D:\\tmp\\test\\开办新公司" + time + ".mpp";
            exportProjectToMpp(xmlPath, mppPath);

            // todo 删除xml文件

        } catch (MPXJException | IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        }
    }


    @Override
    //	导出mpp文件
    public void exportProjectToMpp(String xmlPath, String mppPath) {
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
        // todo 不默认打开文件
//		activexComponent.invoke("Quit");
    }

    public void writeResourceAssignmentToObj(Project pro,ProjectFile pf, int parentId, Task parentTask){

        Task task = parentTask.addTask();
        // Let's create an alias for TEXT1
        CustomFieldContainer customFields = pf.getCustomFields();
        CustomField customField = customFields.getOrCreate(TaskField.TEXT1);
        customField.setAlias("中文描述");
        task.setText(10, "文本10");
        task.setName(pro.getTaskName());
        task.setText(1, "中文描述-content");

        task.setPercentageComplete(70.0);
        task.setPercentageWorkComplete(90);

        task.setDuration(Duration.getInstance(13, TimeUnit.DAYS));
        // 设置基准线
//        task.setBaselineStart(pro.getStartDate());
//        task.setBaselineFinish(pro.getEndDate());

//        task.setStart(date2local(pro.getStartDate()));
//        task.setFinish(date2local(pro.getEndDate()));
        task.setStart(pro.getStartDate());
        task.setFinish(pro.getEndDate());
        task.setResourceGroup(pro.getResource());
        task.setOutlineLevel(parentTask.getOutlineLevel() + 1);
        task.setUniqueID(parentTask.getUniqueID() + 1);
        task.setID(parentTask.getID() + 1);


    }

    public LocalDateTime date2local(Date date){
         return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    public Date local2date(LocalDateTime localDateTime){
        return  Date.from(
                localDateTime.atZone(ZoneId.systemDefault())
                        .toInstant());
    }


    //
//    public void writeResourceAssignmentToObj(Project pro, int parentId, Task parentTask) {
//
//        Task task = parentTask.addTask();
//        task.setName(pro.getTaskName());
//        // 设置工期
//        task.setDuration(Duration.getInstance(5, TimeUnit.DAYS));
//        task.setStart(pro.getStartDate());
//        task.setFinish(pro.getEndDate());
//        task.setResourceGroup(pro.getResource());
//        task.setOutlineLevel(parentTask.getOutlineLevel() + 1);
//        task.setUniqueID(parentTask.getUniqueID() + 1);
//        task.setID(parentTask.getID() + 1);
////        task.setPercentageComplete(30);
//
//        //设置前置关系
////        Task preTask = projectMapper.findProjectById(pro.getPreTask());
////        Task preTask = new Task();
////        Relation r=task.addPredecessor(parentTask, RelationType.FINISH_START, null);
//
//        // 设置基准线
////        task.setBaselineStart(pro.getStartDate());
////        task.setBaselineFinish(pro.getEndDate());
//    }
    public void writeChildrenTaskToObj(List<Project> projects, int levelNum, ProjectFile pf, Task parentTask, Integer parentId) {
        //首先从第一层开始读取
        List<Project> subProjects = getSubProjects(projects, parentId, levelNum);

        for (Project project : subProjects) {
            int currentLevelNum = levelNum;
            //然后利用parentId进行进一步的读取,并且这个可以进行判断是否是最底层
            List<Project> childrenList = getSubProjects(projects, project.getProjId(), currentLevelNum + 1);
            CustomFieldContainer customFields = pf.getCustomFields();
            customFields.stream().forEach(e -> System.out.println(e.getAlias()));
            CustomField customField = customFields.getOrCreate(TaskField.TEXT1);
            System.out.println("aaaaaaaaaaaaaaaaaa"+customField.getAlias());

            customField.setAlias("中文描述");
            //这个判断很重要，如果size为0，说明当前的层级是最底层
            if (childrenList.size() > 0) {
                //说明是父任务，进行父任务的写入，然后进行下一次递归
                Task task = parentTask.addTask();
                task.setText(10, "文本10");
                task.setName(project.getTaskName());
                System.out.println("-----------------------------");

                task.setText(1, "中文描述-content");
                task.setPercentageComplete(30.0);
                task.setPercentageWorkComplete(70);
                // 任务周期
                task.setDuration(Duration.getInstance(13, TimeUnit.DAYS));
                // 任务开始日期
//                task.setStart(date2local(project.getStartDate()));

                task.setStart((project.getStartDate()));
                // 任务结束日期
//                task.setFinish(date2local(project.getEndDate()));

                task.setFinish((project.getEndDate()));
                if (currentLevelNum == 1) {//如果是读取第一层
                    task.setOutlineLevel(1);
                    task.setUniqueID(1);
                    task.setID(1);
                } else {
                    // 大纲ID
                    task.setOutlineLevel(parentTask.getOutlineLevel() + 1);
                    // 独立任务ID
                    task.setUniqueID(parentTask.getUniqueID() + 1);
                    // 普通任务ID
                    task.setID(parentTask.getID() + 1);
                }
                currentLevelNum++;
                //进行递归写入
                writeChildrenTaskToObj(projects, currentLevelNum, pf, task, project.getProjId());
            } else {//说明当前层级为最底层
                writeResourceAssignmentToObj(project, pf, project.getParentId(), parentTask);
            }

        }

    }


    /**
     * 获取子任务列表
     */
    private List<Project> getSubProjects(List<Project> projects, Integer parentId, Integer levelNum) {

        List<Project> subList = new LinkedList<>();
        List<Project> rsList = new LinkedList<>();
        if (levelNum != null) {
            for (Project pro : projects) {
                if (pro.getLevel().equals(levelNum)) {
                    subList.add(pro);
                }
            }
        } else {
            subList = projects;
        }

        if (parentId != null) {
            for (Project pro : subList) {
                if (pro.getParentId().equals(parentId)) {
                    rsList.add(pro);
                }
            }
        }
        if (parentId != null) {
            return rsList;
        } else {
            return subList;
        }
    }
}
