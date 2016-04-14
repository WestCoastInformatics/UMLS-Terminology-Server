/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.rest.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * API origin filter (CORS).
 */
public class ApiOriginFilter implements Filter {

  /* see superclass */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {
    HttpServletResponse res = (HttpServletResponse) response;
    res.addHeader("Access-Control-Allow-Methods",
        "GET, POST, DELETE, PUT, PATCH, OPTIONS");
    res.addHeader("Access-Control-Allow-Headers",
        "Content-Type, api_key, Authorization");
    chain.doFilter(request, response);
  }

  /* see superclass */
  @Override
  public void destroy() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // do nothing
  }

}