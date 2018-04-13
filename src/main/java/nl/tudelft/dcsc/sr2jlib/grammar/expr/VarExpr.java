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
import nl.tudelft.dcsc.sr2jlib.instance.Creator;

/**
 * Variable expression
 *
 * @author Dr. Ivan S. Zapreev
 */
public class VarExpr extends TermExpr<Integer> {

    /**
     * The character representing the variable name prefix
     */
    public static final String VAR_NAME_PREFIX_STR = "x";

    /**
     * The character representing the variable entry
     */
    public static final String ENTRY_VAR_STR = "V";

    /**
     * Stores the minimum variable index
     */
    private static final int MIN_VAR_IDX = 0;

    //Stores the number of variables
    private final int m_num_vars;

    /**
     *
     * The basic constructor
     *
     * @param expr_type the expression type
     * @param num_vars the maximum allowed number of variables - defines the
     * maximum variable index.
     */
    public VarExpr(final String expr_type, final int num_vars) {
        super(expr_type, ENTRY_VAR_STR);
        this.m_num_vars = num_vars;
    }

    /**
     * The copy constructor
     *
     * @param other an expression to copy from
     */
    protected VarExpr(final VarExpr other) {
        super(other);
        this.m_num_vars = other.m_num_vars;
    }

    @Override
    public void materialize(int max_size) {
        m_value = ThreadLocalRandom.current().nextInt(MIN_VAR_IDX, MIN_VAR_IDX + m_num_vars);
    }

    @Override
    public synchronized Expression duplicate() {
        return new VarExpr(this);
    }

    @Override
    public String serialize() {
        return Creator.get_var_name(m_value);
    }

    @Override
    public String to_text() {
        return VAR_NAME_PREFIX_STR + m_value;
    }

    @Override
    public String toString() {
        return ENTRY_VAR_STR;
    }

    @Override
    public boolean is_const() {
        return false;
    }
}
