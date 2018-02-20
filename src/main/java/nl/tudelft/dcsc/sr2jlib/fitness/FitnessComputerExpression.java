/* 
 * Copyright (C) 2018 Dr. Ivan S. Zapreev <ivan.zapreev@gmail.com>
 *
 *  Visit my Linked-in profile:
 *     https://nl.linkedin.com/in/zapreevis
 *  Visit my GitHub:
 *     https://github.com/ivan-zapreev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tudelft.dcsc.sr2jlib.fitness;

import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;

/**
 *
 * The fitness computer abstract class to compute fitness based on the vector of
 * expression trees
 *
 * @author Dr. Ivan S. Zapreev
 */
public abstract class FitnessComputerExpression {

    /**
     * Allows to compute fitness for the given class and manager id
     *
     * @param mgr_id the population manager id from which the individual is
     * originated
     * @param exp_trees the vector function of the individual given by the
     * expression trees
     * @return individual's fitness
     */
    public abstract Fitness compute_fitness(final int mgr_id,
            final Expression[] exp_trees);

}
