/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.filters;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override // Logs incoming request [cite: 167]
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("HTTP Method: " + requestContext.getMethod() + 
                 ", URI: " + requestContext.getUriInfo().getAbsolutePath());
    }

    @Override // Logs outgoing response [cite: 167]
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        LOG.info("Response Status Code: " + response.getStatus());
    }
}
