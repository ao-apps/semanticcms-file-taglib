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

import static com.aoindustries.servlet.filter.FunctionContext.getRequest;
import static com.aoindustries.servlet.filter.FunctionContext.getResponse;
import static com.aoindustries.servlet.filter.FunctionContext.getServletContext;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.model.Resource;
import com.semanticcms.file.servlet.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;

final public class Functions {

	public static boolean hasFile(Page page, boolean recursive) throws ServletException, IOException {
		return FileUtils.hasFile(
			getServletContext(),
			getRequest(),
			getResponse(),
			page,
			recursive
		);
	}

	public static boolean isOpenFileAllowed() throws ServletException {
		return FileUtils.isOpenFileAllowed(
			getServletContext(),
			getRequest()
		);
	}

	public static File getFileInDomain(String domain, String book, String path, boolean require) throws ServletException, IOException {
		Resource resource = com.semanticcms.core.taglib.Functions.getResourceInDomain(domain, book, path, require);
		if(resource == null) {
			assert !require;
			return null;
		}
		File file = resource.getFile();
		if(require && file == null) {
			throw new FileNotFoundException("File is not local: " + resource.getResourceRef());
		}
		return file;
	}

	public static File getFileInBook(String book, String path, boolean require) throws ServletException, IOException {
		return getFileInDomain(null, book, path, require);
	}

	public static File getFile(String path, boolean require) throws ServletException, IOException {
		return getFileInDomain(null, null, path, require);
	}

	public static File getExeFileInDomain(String domain, String book, String path) throws ServletException, IOException {
		File file = getFileInDomain(domain, book, path, true);
		if(
			!file.canExecute()
			&& !file.setExecutable(true)
		) {
			throw new IOException("Unable to set executable flag: " + file.getPath());
		}
		return file;
	}

	public static File getExeFileInBook(String book, String path) throws ServletException, IOException {
		return getExeFileInDomain(null, book, path);
	}

	public static File getExeFile(String path) throws ServletException, IOException {
		return getExeFileInDomain(null, null, path);
	}

	/**
	 * Make no instances.
	 */
	private Functions() {
	}
}
