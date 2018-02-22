/*
 * Copyright (C) 2018 Dr. Ivan S. Zapreev <ivan.zapreev@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.tudelft.dcsc.sr2jlib;

import nl.tudelft.dcsc.sr2jlib.grid.Individual;

/**
 * This interface is to be implemented by the class
 *
 * @author Dr. Ivan S. Zapreev
 */
@FunctionalInterface
public interface IndividualFilter {

    /**
     * Allows to evaluate an individual
     *
     * @param ind the individual to be evaluated
     * @return true if the individual is "bad" and false if it is "good"
     */
    public boolean evaluate(final Individual ind);

}
