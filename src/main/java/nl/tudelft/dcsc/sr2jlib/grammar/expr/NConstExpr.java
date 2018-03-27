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
 * Numeric constant expression
 *
 * @author Dr. Ivan S. Zapreev
 */
public class NConstExpr extends TermExpr<Double> {

    /**
     * The character representing the numeric constant entry
     */
    public static final String ENTRY_CNUM_STR = "D";

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     */
    public NConstExpr(final String expr_type) {
        super(expr_type);
    }

    /**
     * The copy constructor
     *
     * @param other the numeric constant to copy from
     */
    protected NConstExpr(final NConstExpr other) {
        super(other);
    }

    @Override
    public void materialize(int max_size) {
        m_value = (double) ThreadLocalRandom.current().nextFloat();
    }

    @Override
    public synchronized Expression duplicate() {
        return new NConstExpr(this);
    }

    @Override
    public String serialize() {
        return Double.toString(m_value);
    }

    @Override
    public String toString() {
        return ENTRY_CNUM_STR;
    }

    @Override
    public boolean is_equal_funct(Expression expr) {
        return (expr instanceof NConstExpr);
    }

    @Override
    public String to_text() {
        return Double.toString(m_value);
    }
}
