/*
 * semanticcms-file-taglib - Files nested within SemanticCMS pages and elements in a JSP environment.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.validation.ValidationException;
import static com.aoapps.servlet.filter.FunctionContext.getRequest;
import static com.aoapps.servlet.filter.FunctionContext.getResponse;
import static com.aoapps.servlet.filter.FunctionContext.getServletContext;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.resources.Resource;
import com.semanticcms.file.renderer.html.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;

public final class Functions {

  /** Make no instances. */
  private Functions() {
    throw new AssertionError();
  }

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

  public static File getFileInDomain(String domain, String book, String path, boolean require) throws ServletException, IOException, ValidationException {
    Resource resource = com.semanticcms.core.taglib.Functions.getResourceInDomain(domain, book, path, require);
    if (resource == null) {
      assert !require;
      return null;
    }
    File file = resource.getFile();
    if (require && file == null) {
      throw new FileNotFoundException("Resource is not a local file: " + resource);
    }
    return file;
  }

  public static File getFileInBook(String book, String path, boolean require) throws ServletException, IOException, ValidationException {
    return getFileInDomain(null, book, path, require);
  }

  public static File getFile(String path, boolean require) throws ServletException, IOException {
    try {
      return getFileInDomain(null, null, path, require);
    } catch (ValidationException e) {
      throw new ServletException(e);
    }
  }

  public static File getExeFileInDomain(String domain, String book, String path) throws ServletException, IOException, ValidationException {
    File file = getFileInDomain(domain, book, path, true);
    if (
      !file.canExecute()
      && !file.setExecutable(true)
    ) {
      throw new IOException("Unable to set executable flag: " + file.getPath());
    }
    return file;
  }

  public static File getExeFileInBook(String book, String path) throws ServletException, IOException, ValidationException {
    return getExeFileInDomain(null, book, path);
  }

  public static File getExeFile(String path) throws ServletException, IOException {
    try {
      return getExeFileInDomain(null, null, path);
    } catch (ValidationException e) {
      throw new ServletException(e);
    }
  }
}
