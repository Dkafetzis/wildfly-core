/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.server.deployment.module;

import java.io.Serializable;
import java.util.Objects;

import org.jboss.modules.filter.PathFilter;

/**
* @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
*/
public final class FilterSpecification implements Serializable {

    private static final long serialVersionUID = -4233637179300139418L;

    private final PathFilter pathFilter;
    private final boolean include;

    public FilterSpecification(final PathFilter pathFilter, final boolean include) {
        this.pathFilter = pathFilter;
        this.include = include;
    }

    public PathFilter getPathFilter() {
        return pathFilter;
    }

    public boolean isInclude() {
        return include;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterSpecification that = (FilterSpecification) o;
        return include == that.include && pathFilter.equals(that.pathFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathFilter, include);
    }
}
