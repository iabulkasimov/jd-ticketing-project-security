package com.cybertek.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.lang.reflect.Type;

@Component
public class MapperUtil {

    private ModelMapper modelMapper;

    public MapperUtil(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <T> T convert(Object objectToBeConverted, T convertedObject){
        return modelMapper.map(objectToBeConverted, (Type) convertedObject.getClass());
    }
}
