package com.cybertek.implementation;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.Project;
import com.cybertek.entity.User;
import com.cybertek.enums.Status;
import com.cybertek.mapper.ProjectMapper;
import com.cybertek.mapper.UserMapper;
import com.cybertek.repository.ProjectRepository;
import com.cybertek.service.ProjectService;
import com.cybertek.service.TaskService;
import com.cybertek.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private ProjectMapper projectMapper;
    private ProjectRepository projectRepository;
    private UserMapper userMapper;
    private TaskService taskService;
    private UserService userService;

    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectRepository projectRepository,
                              UserMapper userMapper, TaskService taskService, UserService userService) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userMapper = userMapper;
        this.taskService = taskService;
        this.userService = userService;
    }
//    public void setUserService(UserService userService) {
//        this.userService = userService;
//    }
//
//    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectRepository projectRepository,
//                              UserMapper userMapper, TaskService taskService) {
//        this.projectMapper = projectMapper;
//        this.projectRepository = projectRepository;
//        this.userMapper = userMapper;
//        this.taskService = taskService;
//    }

    @Override
    public ProjectDTO getByProjectCode(String code) {
        Project project = projectRepository.findByProjectCode(code);
        return projectMapper.convertToDto(project);
    }

    @Override
    public List<ProjectDTO> listAllProjects() {
        List<Project> list = projectRepository.findAll(Sort.by("projectCode"));
        return list.stream().map(obj -> projectMapper.convertToDto(obj)).collect(Collectors.toList());
    }

    @Override
    public void save(ProjectDTO dto) {
        dto.setProjectStatus(Status.OPEN);
        Project obj = projectMapper.convertToEntity(dto);
//        obj.setAssignedManager(userMapper.convertToEntity(dto.getAssignedManager()));
        projectRepository.save(obj);
    }

    @Override
    public void update(ProjectDTO dto) {

        Project project = projectRepository.findByProjectCode(dto.getProjectCode());
        Project convertedProject = projectMapper.convertToEntity(dto);
        convertedProject.setProjectStatus(project.getProjectStatus());
        convertedProject.setId(project.getId());
        projectRepository.save(convertedProject);
    }

    @Override
    public void delete(String code) {
        Project project = projectRepository.findByProjectCode(code);
        project.setIsDeleted(true);

        project.setProjectCode(project.getProjectCode() + "-" + project.getId());
        projectRepository.save(project);

        taskService.deleteByProject(projectMapper.convertToDto(project));
    }

    @Override
    public void complete(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode);
        project.setProjectStatus(Status.COMPLETE);
        projectRepository.save(project);
    }

    @Override
    public List<ProjectDTO> ListAllProjectDetails() {
        UserDTO currentUserDTO = userService.findByUserName("abc@abc");
        User user = userMapper.convertToEntity(currentUserDTO);
        List<Project> list = projectRepository.findAllByAssignedManager(user);

        return list.stream().map(project -> {
            ProjectDTO obj = projectMapper.convertToDto(project);
            obj.setUnfinishedTaskCounts(taskService.totalNonCompletedTasks(project.getProjectCode()));
            obj.setCompleteTaskCounts(taskService.totalCompletedTasks(project.getProjectCode()));
            return obj;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> readAllByAssignedManager(User user) {
        List<Project> list = projectRepository.findAllByAssignedManager(user);
        return list.stream().map(obj -> projectMapper.convertToDto(obj)).collect(Collectors.toList());
    }

}

