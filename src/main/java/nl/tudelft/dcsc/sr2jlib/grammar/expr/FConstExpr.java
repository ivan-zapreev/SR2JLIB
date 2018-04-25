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
 * Float constant expression
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class FConstExpr extends ConstExpr<Float> {

    /**
     * The character representing the numeric constant entry
     */
    public static final String ENTRY_CFLOAT_STR = "F";

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     */
    public FConstExpr(final String expr_type) {
        super(expr_type, ENTRY_CFLOAT_STR);
    }

    /**
     * Allows to instantiate a materialized numerical constant with the given
     * constant value.
     *
     * @param expr_type the expression type
     * @param value the value to be stored
     * @return the created and materialized numerical constant
     */
    public static FConstExpr make_const(final String expr_type, final float value) {
        FConstExpr expr = new FConstExpr(expr_type);
        expr.m_value = value;
        return expr;
    }

    /**
     * The copy constructor
     *
     * @param other the numeric constant to copy from
     */
    protected FConstExpr(final FConstExpr other) {
        super(other);
    }

    @Override
    public void materialize(int max_size) {
        m_value = (ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f)
                * (ThreadLocalRandom.current().nextFloat()
                //Add the minimum value some times in order to get value >=1
                + (ThreadLocalRandom.current().nextBoolean() ? 0.0f : Float.MIN_VALUE));
    }

    @Override
    public synchronized Expression duplicate() {
        return new FConstExpr(this);
    }

    @Override
    protected boolean is_bsafe_expr() {
        return (m_value >= 0.0);
    }

    @Override
    public String serialize() {
        //Prevent -0.0 from happening
        return Float.toString((m_value == 0.0f) ? 0.0f : m_value);
    }

    @Override
    public String toString() {
        return ENTRY_CFLOAT_STR;
    }
}
