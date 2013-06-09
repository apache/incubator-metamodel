/**
 * eobjects.org MetaModel
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.metamodel.convert;

/**
 * A {@link TypeConverter} that converts String values (on the physical layer)
 * to interpreted Doubles.
 * 
 * @author Kasper Sørensen
 * @author Ankit Kumar
 */
public class StringToDoubleConverter implements TypeConverter<String, Double> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPhysicalValue(Double virtualValue) {
		if (virtualValue == null) {
			return null;
		}
		return virtualValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double toVirtualValue(String physicalValue) {
		if (physicalValue == null || physicalValue.length() == 0) {
			return null;
		}
		return Double.parseDouble(physicalValue);
	}

}