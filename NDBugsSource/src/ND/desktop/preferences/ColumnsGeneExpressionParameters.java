/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.desktop.preferences;

import ND.data.ExpressionDataColumnName;
import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.MultiChoiceParameter;

/**
 *
 * @author scsandra
 */

public class ColumnsGeneExpressionParameters extends SimpleParameterSet {


public static final MultiChoiceParameter<ExpressionDataColumnName> ExpressionData = new MultiChoiceParameter<ExpressionDataColumnName>(
			"Select columns for GCxGC-MS", "Select columns for GCxGC-MS", ExpressionDataColumnName.values());

        public ColumnsGeneExpressionParameters() {
                super(new Parameter[]{ExpressionData});
        }

}
