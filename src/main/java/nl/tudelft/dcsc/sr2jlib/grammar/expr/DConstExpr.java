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
package nl.tudelft.dcsc.sr2jlib.grammar.expr;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Double constant expression
 *
 * @author Dr. Ivan S. Zapreev
 */
public class DConstExpr extends ConstExpr<Double> {

    /**
     * The character representing the numeric constant entry
     */
    public static final String ENTRY_CDOUBLE_STR = "D";

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     */
    public DConstExpr(final String expr_type) {
        super(expr_type, ENTRY_CDOUBLE_STR);
    }

    /**
     * Allows to instantiate a materialized numerical constant with the given
     * constant value.
     *
     * @param expr_type the expression type
     * @param value the value to be stored
     * @return the created and materialized numerical constant
     */
    public static DConstExpr make_const(final String expr_type, final double value) {
        DConstExpr expr = new DConstExpr(expr_type);
        expr.m_value = value;
        return expr;
    }

    /**
     * The copy constructor
     *
     * @param other the numeric constant to copy from
     */
    protected DConstExpr(final DConstExpr other) {
        super(other);
    }

    @Override
    public void materialize(int max_size) {
        m_value = (ThreadLocalRandom.current().nextBoolean() ? 1.0d : -1.0d)
                * (ThreadLocalRandom.current().nextDouble()
                //Add the minimum value some times in order to get value >=1
                + (ThreadLocalRandom.current().nextBoolean() ? 0.0d : Double.MIN_VALUE));
    }

    @Override
    public synchronized Expression duplicate() {
        return new DConstExpr(this);
    }

    @Override
    public String serialize() {
        final String value = Double.toString(m_value);
        return m_value < 0.0 ? "(" + value + ")" : value;
    }

    @Override
    public String toString() {
        return ENTRY_CDOUBLE_STR;
    }
}
