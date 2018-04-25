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

/**
 * Represents the constant terminal expression
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 * @param <ValueType> the value type Boolean, Double ...
 */
public abstract class ConstExpr<ValueType> extends TermExpr<ValueType> {

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     * @param term_type the basic constant expression type
     */
    public ConstExpr(final String expr_type, final String term_type) {
        super(expr_type, term_type);
    }

    /**
     * The copy constructor
     *
     * @param other the numeric constant to copy from
     */
    protected ConstExpr(final ConstExpr other) {
        super(other);
    }
}
