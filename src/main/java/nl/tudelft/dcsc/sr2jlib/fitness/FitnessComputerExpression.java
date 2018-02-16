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

    //Stores the fitness computer instance
    private static FitnessComputerExpression m_inst = null;

    /**
     * Allows to set the instance of the fitness computer.
     *
     * @param inst the instance to be set
     */
    public static void set_inst(final FitnessComputerExpression inst) {
        m_inst = inst;
    }

    /**
     * Allows to get the instance of the fitness computer
     *
     * @return the instance of the fitness computer or null if none is set
     */
    public static FitnessComputerExpression inst() {
        return m_inst;
    }

    /**
     * Allows to compute fitness for the given class and manager id
     *
     * @param exp_trees the vector function of the individual given by the
     * expression trees
     * @param mgr_id the population manager id from which the individual is
     * originated
     * @return individual's fitness
     */
    public abstract Fitness compute_fitness(
            final Expression[] exp_trees, final int mgr_id);

}
