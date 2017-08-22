/*
 * semanticcms-file-taglib - Files nested within SemanticCMS pages and elements in a JSP environment.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017  AO Industries, Inc.
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
 * along with semanticcms-file-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.file.taglib;

import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.io.buffer.BufferWriter;
import com.aoindustries.net.DomainName;
import com.aoindustries.net.Path;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.aoindustries.taglib.AutoEncodingBufferedTag;
import com.aoindustries.util.StringUtility;
import com.aoindustries.validation.ValidationException;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.model.ResourceRef;
import com.semanticcms.core.pages.CaptureLevel;
import com.semanticcms.core.servlet.ResourceRefResolver;
import com.semanticcms.core.servlet.SemanticCMS;
import com.semanticcms.core.taglib.ElementTag;
import com.semanticcms.file.model.File;
import com.semanticcms.file.servlet.impl.FileImpl;
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
			final PageContext pageContext = (PageContext)getJspContext();
			final ELContext elContext = pageContext.getELContext();
			final ServletContext servletContext = pageContext.getServletContext();
			final HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			// Resolve file now to catch problems earlier even in meta mode
			ResourceRef resourceRef = ResourceRefResolver.getResourceRef(
				servletContext,
				request,
				DomainName.valueOf(
					StringUtility.nullIfEmpty(
						resolveValue(domain, String.class, elContext)
					)
				),
				Path.valueOf(
					StringUtility.nullIfEmpty(
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
			if(captureLevel == CaptureLevel.BODY) {
				capturedOut = AutoEncodingBufferedTag.newBufferWriter(request);
			} else {
				capturedOut = null;
			}
			try {
				FileImpl.writeFileImpl(
					servletContext,
					request,
					(HttpServletResponse)pageContext.getResponse(),
					capturedOut,
					file
				);
			} finally {
				if(capturedOut != null) capturedOut.close();
			}
			writeMe = capturedOut==null ? null : capturedOut.getResult();
		} catch(ServletException e) {
			throw new JspTagException(e);
		} catch(ValidationException e) {
			throw new JspTagException(e);
		}
	}

	@Override
	public void writeTo(Writer out, ElementContext context) throws IOException {
		if(writeMe != null) writeMe.writeTo(out);
	}
}
