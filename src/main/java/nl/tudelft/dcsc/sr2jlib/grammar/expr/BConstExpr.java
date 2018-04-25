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
 * Boolean constant expression
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class BConstExpr extends ConstExpr<Boolean> {

    /**
     * The character representing the boolean constant entry
     */
    public static final String ENTRY_CBOOL_STR = "L";

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     */
    public BConstExpr(final String expr_type) {
        super(expr_type, ENTRY_CBOOL_STR);
    }

    /**
     * Allows to instantiate a materialized boolean constant with the given
     * constant value.
     *
     * @param expr_type the expression type
     * @param value the value to be stored
     * @return the created and materialized boolean constant
     */
    public static BConstExpr make_const(final String expr_type, final boolean value) {
        BConstExpr expr = new BConstExpr(expr_type);
        expr.m_value = value;
        return expr;
    }

    /**
     * The copy constructor
     *
     * @param other the boolean constant to copy from
     */
    protected BConstExpr(final BConstExpr other) {
        super(other);
    }

    @Override
    public void materialize(int max_size) {
        m_value = ThreadLocalRandom.current().nextBoolean();
    }

    @Override
    public synchronized Expression duplicate() {
        return new BConstExpr(this);
    }

    @Override
    public String serialize() {
        return Boolean.toString(m_value);
    }

    @Override
    public String toString() {
        return ENTRY_CBOOL_STR;
    }
}
