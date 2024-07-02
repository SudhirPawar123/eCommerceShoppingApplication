package com.jsp.onlineshoppingapplication.securityfilters;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsp.onlineshoppingapplication.util.ErrorStructure;

import jakarta.servlet.http.HttpServletResponse;

public class FilterException {
	
public static void handleJwtExpire(HttpServletResponse response, int status, String message, String rootCause) throws IOException  {
    response.setStatus(status);
    ErrorStructure<String> errorStructure = new ErrorStructure<String>()
            .setStatus(status)
            .setMessage(message)
            .setRootCause(rootCause);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(response.getOutputStream(), errorStructure);
}
}
