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

import static com.aoapps.servlet.filter.FunctionContext.getRequest;
import static com.aoapps.servlet.filter.FunctionContext.getResponse;
import static com.aoapps.servlet.filter.FunctionContext.getServletContext;
import com.semanticcms.core.model.Page;
import com.semanticcms.file.servlet.FileUtils;
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
}
