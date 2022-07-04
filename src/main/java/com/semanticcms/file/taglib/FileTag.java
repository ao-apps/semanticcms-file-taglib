/*
 * semanticcms-file-taglib - Files nested within SemanticCMS pages and elements in a JSP environment.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-file-taglib.
 *
 * semanticcms-file-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-file-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-file-taglib.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.semanticcms.file.taglib;

import static com.aoapps.taglib.AttributeUtils.resolveValue;

import com.aoapps.encoding.taglib.EncodingBufferedTag;
import com.aoapps.html.servlet.DocumentEE;
import com.aoapps.io.buffer.BufferResult;
import com.aoapps.io.buffer.BufferWriter;
import com.aoapps.lang.Strings;
import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.DomainName;
import com.aoapps.net.Path;
import com.semanticcms.core.controller.ResourceRefResolver;
import com.semanticcms.core.controller.SemanticCMS;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.model.ResourceRef;
import com.semanticcms.core.pages.CaptureLevel;
import com.semanticcms.core.taglib.ElementTag;
import com.semanticcms.file.model.File;
import com.semanticcms.file.renderer.html.FileHtmlRenderer;
import java.io.IOException;
import java.io.Writer;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/**
 * Writes a link to a file.
 */
public class FileTag extends ElementTag<File> {

  private ValueExpression domain;

  public void setDomain(ValueExpression domain) {
    this.domain = domain;
  }

  private ValueExpression book;

  public void setBook(ValueExpression book) {
    this.book = book;
  }

  private ValueExpression path;

  public void setPath(ValueExpression path) {
    this.path = path;
  }

  private boolean hidden;

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  @Override
  protected File createElement() {
    return new File();
  }

  private BufferResult writeMe;

  @Override
  protected void doBody(File file, CaptureLevel captureLevel) throws JspException, IOException {
    try {
      final PageContext pageContext = (PageContext) getJspContext();
      final ELContext elContext = pageContext.getELContext();
      final ServletContext servletContext = pageContext.getServletContext();
      final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
      // Resolve file now to catch problems earlier even in meta mode
      ResourceRef resourceRef = ResourceRefResolver.getResourceRef(
          servletContext,
          request,
          DomainName.valueOf(
              Strings.nullIfEmpty(
                  resolveValue(domain, String.class, elContext)
              )
          ),
          Path.valueOf(
              Strings.nullIfEmpty(
                  resolveValue(book, String.class, elContext)
              )
          ),
          resolveValue(path, String.class, elContext)
      );
      file.setResource(
          SemanticCMS.getInstance(servletContext).getBook(resourceRef.getBookRef()).getResources(),
          resourceRef
      );
      file.setHidden(hidden);
      super.doBody(file, captureLevel);
      BufferWriter capturedOut;
      if (captureLevel == CaptureLevel.BODY) {
        capturedOut = EncodingBufferedTag.newBufferWriter(request);
      } else {
        capturedOut = null;
      }
      try {
        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        FileHtmlRenderer.writeFileImpl(
            servletContext,
            request,
            response,
            (capturedOut == null) ? null : new DocumentEE(
                servletContext,
                request,
                response,
                capturedOut,
                false, // Do not add extra newlines to JSP
                false  // Do not add extra indentation to JSP
            ),
            file
        );
      } finally {
        if (capturedOut != null) {
          capturedOut.close();
        }
      }
      writeMe = capturedOut == null ? null : capturedOut.getResult();
    } catch (ServletException | ValidationException e) {
      throw new JspTagException(e);
    }
  }

  @Override
  public void writeTo(Writer out, ElementContext context) throws IOException {
    if (writeMe != null) {
      writeMe.writeTo(out);
    }
  }
}
