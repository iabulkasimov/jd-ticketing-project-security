package com.cybertek.implementation;

import com.cybertek.dto.ProjectDTO;
import com.cybertek.dto.TaskDTO;
import com.cybertek.dto.UserDTO;
import com.cybertek.entity.User;
import com.cybertek.exception.TicketingProjectException;
import com.cybertek.mapper.MapperUtil;
import com.cybertek.mapper.UserMapper;
import com.cybertek.repository.UserRepository;
import com.cybertek.service.ProjectService;
import com.cybertek.service.TaskService;
import com.cybertek.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private ProjectService projectService;
    private TaskService taskService;
//    private UserMapper userMapper;
    private MapperUtil mapperUtil;

    public UserServiceImpl(UserRepository userRepository, @Lazy ProjectService projectService,
                           TaskService taskService, MapperUtil mapperUtil) {
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.taskService = taskService;
        this.mapperUtil = mapperUtil;
    }
//    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
//                           @Lazy ProjectService projectService, TaskService taskService){
//        this.userRepository = userRepository;
//        this.userMapper = userMapper;
//        this.projectService = projectService;
//        this.taskService = taskService;
//    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> list = userRepository.findAll(Sort.by("firstName"));
        //return list.stream().map(obj ->{return userMapper.convertToDto(obj);}).collect(Collectors.toList());

        //return list.stream().map(obj ->{return mapperUtil.convert(obj,new UserDTO());}).collect(Collectors.toList());
        return list.stream().map(obj -> mapperUtil.convert(obj,new UserDTO())).collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {
        User user = userRepository.findByUserName(username);
//        return userMapper.convertToDto(user);
        return mapperUtil.convert(user,new UserDTO());
    }

    @Override
    public void save(UserDTO dto) {
//        User obj = userMapper.convertToEntity(dto);
        User obj = mapperUtil.convert(dto,new User());
        userRepository.save(obj);
    }

    @Override
    public UserDTO update(UserDTO dto) {

        //find current user
        User user = userRepository.findByUserName(dto.getUserName());
        //map update user dto to entity object
//        User convertedToUser = userMapper.convertToEntity(dto);
        User convertedToUser = mapperUtil.convert(dto,new User());
        //set id to the converted object
        convertedToUser.setId(user.getId());
        //save updated user
        userRepository.save(convertedToUser);

        return findByUserName(dto.getUserName());
    }

    @Override
    public void delete(String username) throws TicketingProjectException {
        User user = userRepository.findByUserName(username);

        if (user == null){
            throw new TicketingProjectException("User Does Not Exists");
        }

        if (!checkIfUserCanBeDeleted(user)){
            throw new TicketingProjectException("User can not be deleted. It is linked by a project or task");
        }

        user.setUserName(user.getUserName()+ "-"+user.getId());

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    @Override
    public void deleteByUserName(String username) {
        userRepository.deleteByUserName(username);
    }

    @Override
    public List<UserDTO> listAllByRole(String role) {
        List<User> user = userRepository.findAllByRoleDescriptionIgnoreCase(role);
        //converting user by one to dto and returning list
        //return user.stream().map(obj -> {return mapperUtil.convert(obj,new UserDTO());}).collect(Collectors.toList());
        return user.stream().map(obj -> mapperUtil.convert(obj,new UserDTO())).collect(Collectors.toList());
    }

    @Override
    public Boolean checkIfUserCanBeDeleted(User user) {
        switch (user.getRole().getDescription()){
            case "Manager":
                List<ProjectDTO> projectList = projectService.readAllByAssignedManager(user);
                return projectList.size() == 0;
            case "Employee":
                List<TaskDTO> taskList = taskService.readAllByEmployee(user);
                return taskList.size() == 0;
            default:
                return true;
        }
    }
}
