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

import com.aoindustries.io.TempFileList;
import com.aoindustries.io.buffer.AutoTempFileWriter;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.io.buffer.BufferWriter;
import com.aoindustries.servlet.filter.TempFileContext;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.aoindustries.taglib.AutoEncodingBufferedTag;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.PageRefResolver;
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
			file.setPageRef(
				PageRefResolver.getPageRef(
					servletContext,
					request,
					resolveValue(domain, String.class, elContext),
					resolveValue(book, String.class, elContext),
					resolveValue(path, String.class, elContext)
				)
			);
			file.setHidden(hidden);
			super.doBody(file, captureLevel);
			BufferWriter capturedOut;
			if(captureLevel == CaptureLevel.BODY) {
				// Enable temp files if temp file context active
				capturedOut = TempFileContext.wrapTempFileList(
					AutoEncodingBufferedTag.newBufferWriter(),
					request,
					// Java 1.8: AutoTempFileWriter::new
					new TempFileContext.Wrapper<BufferWriter>() {
						@Override
						public BufferWriter call(BufferWriter original, TempFileList tempFileList) {
							return new AutoTempFileWriter(original, tempFileList);
						}
					}
				);
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
		}
	}

	@Override
	public void writeTo(Writer out, ElementContext context) throws IOException {
		if(writeMe != null) writeMe.writeTo(out);
	}
}
