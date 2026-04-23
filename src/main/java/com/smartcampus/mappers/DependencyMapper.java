/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.MediaType;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;

@Provider // This tells JAX-RS to use this class for handling the specific exception 
public class DependencyMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        // Return HTTP 422 Unprocessable Entity for missing references inside valid JSON
        return Response.status(422) 
                .header("Content-Type", MediaType.APPLICATION_JSON) // Ensure the client knows it's JSON
                .entity("{\"error\": \"" + ex.getMessage() + "\"}")
                .build();
    }
}
