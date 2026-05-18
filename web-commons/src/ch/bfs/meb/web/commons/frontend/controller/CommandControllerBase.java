/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: CommandController.java 438 2010-01-14 15:34:11Z jfu $

 */
package ch.bfs.meb.web.commons.frontend.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import ch.bfs.meb.web.commons.dhtmlx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/** Dhtmlx command controller. Maps all commands to the dhtmlx controller and returns the response of the controller. */
@Slf4j
@EnableWebMvc
public abstract class CommandControllerBase {

    @Autowired
    private WebApplicationContext appContext;

    @Autowired
    private CommandDispatcher commandDispatcher;

    @RequestMapping("/controller.do")
    public void runCommand(@RequestParam("control") String control, @RequestParam("command") String command, HttpServletRequest request,
                           HttpServletResponse response) {

        try {
            Object bean = appContext.getBean(control);

            if (bean != null && bean instanceof IDhtmlxManager) {
                IHttpResult result = commandDispatcher.run((IDhtmlxManager) bean, request);

                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Max-Age", "0");

                response.setContentType(result.getContentType());
                response.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8)); // S'assurer de l'encodage UTF-8

                if (result.getContentDisposition() != null) {
                    response.setHeader("Content-Disposition", result.getContentDisposition());
                }


                if (result instanceof BinaryHttpResultBase) {
                    ServletOutputStream out = response.getOutputStream();

                    // Ajout du BOM uniquement si c'est un fichier CSV
                    boolean isCsv = false;

                    if ("text/csv".equalsIgnoreCase(result.getContentType())) {
                        isCsv = true;
                    } else if (result.getContentDisposition() != null && result.getContentDisposition().toLowerCase().contains(".csv")) {
                        isCsv = true;
                    }

                    if (isCsv) {
                        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                        out.write(bom);
                    }

                    ((BinaryHttpResultBase) result).writeTo(out);
                    out.flush();
                    out.close();
                } else if (result instanceof TextHttpResult) {
                    ServletOutputStream out = response.getOutputStream();
                    out.print(((TextHttpResult) result).getText());
                    out.flush();
                    out.close();
                }else if (result instanceof BinaryFileResultBase) {
                    response.setContentLength(((BinaryFileResultBase) result).getContent().length);
                    ServletOutputStream out = response.getOutputStream();
                    ((BinaryFileResultBase) result).writeTo(out);
                    out.flush();
                    out.close();
                } else {
                    PrintWriter out = response.getWriter();
                    out.print(result.getDocument());
                    out.flush();
                    out.close();
                }
            } else {
                log.error("Illegal bean access, bean (" + bean.getClass().getName() + ") is not a Dhtmlxmanager");
            }
        } catch (BeansException e) {
            log.error("Illegal bean access", e);
        } catch (DhtmlxException e) {
            log.error("Illegal manager access", e);
        } catch (IOException e) {
            log.error("Cannot write response", e);
        }

    }

    @RequestMapping("/upload.do")
    public void upload(@RequestParam("goal") String goal, @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            Object bean = appContext.getBean("uploadManager");

            if (bean != null && bean instanceof IDhtmlxControl) {
                IHttpResult result = commandDispatcher.upload((IDhtmlxControl) bean, goal, file);

                response.setContentType(result.getContentType());

                if (result.getContentDisposition() != null) {
                    response.setHeader("Content-Disposition", result.getContentDisposition());
                }

                if (result instanceof TextHttpResult) {
                    ServletOutputStream out = response.getOutputStream();
                    out.print(((TextHttpResult) result).getText());
                    out.flush();
                    out.close();
                }else{
                PrintWriter out = response.getWriter();
                out.print(result.getDocument());
                out.flush();
                out.close();}
            } else {
                log.error("Illegal bean access, bean (uploadManager) is not a Dhtmlxcontrol");
            }
        } catch (BeansException e) {
            log.error("Illegal bean access", e);
        } catch (DhtmlxException e) {
            log.error("Illegal upload manager access", e);
        } catch (IOException e) {
            log.error("Cannot write response", e);
        }
    }


    @RequestMapping("/uploadfile.do")
    public void uploadFile(@RequestParam("goal") String goal, @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            Object bean = appContext.getBean("uploadManager");

            if (bean instanceof IDhtmlxControl) {

                IHttpResult result = commandDispatcher.uploadFile((IDhtmlxControl) bean, goal, file,request);

                response.setContentType(result.getContentType());

                if (result.getContentDisposition() != null) {
                    response.setHeader("Content-Disposition", result.getContentDisposition());
                }
                if (result instanceof TextHttpResult) {
                    ServletOutputStream out = response.getOutputStream();
                    out.print(((TextHttpResult) result).getText());
                    out.flush();
                    out.close();
                }else{
                    PrintWriter out = response.getWriter();
                    out.print(result.getDocument());
                    out.flush();
                    out.close();}
            } else {
                log.error("Illegal bean access, bean (uploadManager) is not a Dhtmlxcontrol");
            }
        } catch (BeansException e) {
            log.error("Illegal bean access", e);
        } catch (DhtmlxException e) {
            log.error("Illegal upload manager access", e);
        }  catch (IOException e) {
            log.error("Cannot write response", e);
        }
    }

    @RequestMapping("/refresh.do")
    public void refresh(@RequestParam("goal") String goal, HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            out.print("ok");
            out.flush();
            out.close();
        } catch (IOException e) {
            log.debug("Refresh failed.", e);
        }
    }
}
