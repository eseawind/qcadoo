/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 0.4.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.report.internal.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.report.api.ReportException;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.exception.ClassDrivenExceptionResolver;

@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    @Qualifier("exceptionResolver")
    private ClassDrivenExceptionResolver exceptionResolver;

    @PostConstruct
    public void init() {
        exceptionResolver.addExceptionInfoResolver(ReportException.class, new ReportExceptionInfoResolver());
    }

    @RequestMapping(value = "generateReportForEntity/{templatePlugin}/{templateName}", method = RequestMethod.GET)
    public void generateReportForEntity(@PathVariable("templatePlugin") final String templatePlugin,
            @PathVariable("templateName") final String templateName, @RequestParam("id") final List<Long> entityIds,
            @RequestParam("additionalArgs") final String requestAdditionalArgs, final HttpServletRequest request,
            final HttpServletResponse response, final Locale locale) throws ReportException {

        ReportService.ReportType reportType = getReportType(request);
        Map<String, String> additionalArgs = convertJsonStringToMap(requestAdditionalArgs);

        byte[] reportContent = reportService.generateReportForEntity(templatePlugin, templateName, reportType, entityIds,
                additionalArgs, locale);

        try {
            IOUtils.copy(new ByteArrayInputStream(reportContent), response.getOutputStream());
        } catch (IOException e) {
            throw new ReportException(ReportException.Type.ERROR_WHILE_COPYING_REPORT_TO_RESPONSE, e);
        }
        response.setContentLength(reportContent.length);
        response.setContentType(reportType.getMimeType());
        disableCache(response);

    }

    private ReportService.ReportType getReportType(final HttpServletRequest request) throws ReportException {
        String uri = request.getRequestURI();
        String type = uri.substring(uri.lastIndexOf(".") + 1).toUpperCase();
        try {
            return ReportService.ReportType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new ReportException(ReportException.Type.WRONG_REPORT_TYPE, e, type);
        }
    }

    private Map<String, String> convertJsonStringToMap(final String jsonText) throws ReportException {
        Map<String, String> result = new HashMap<String, String>();
        try {
            JSONObject userArgsObject = new JSONObject(jsonText);
            @SuppressWarnings("unchecked")
            Iterator<String> it = userArgsObject.keys();
            while (it.hasNext()) {
                String key = it.next();
                result.put(key, userArgsObject.getString(key));
            }
        } catch (JSONException e) {
            throw new ReportException(ReportException.Type.JSON_EXCEPTION, e);
        }
        return result;
    }

    private void disableCache(final HttpServletResponse response) {
        response.addHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.addHeader("Pragma", "no-cache");
    }

}