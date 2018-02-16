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
package nl.tudelft.dcsc.sr2jlib.grammar;

import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.TermExpr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the default grammar entry
 *
 * @author Dr. Ivan S. Zapreev
 */
class DefGrammarEntry implements GrammarEntry {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(DefGrammarEntry.class.getName());

    private final String m_entry_type;
    private final TermExpr m_expr;
    private final int[] m_min_max_size;

    /**
     * The basic constructor
     * 
     * @param name the grammar entry name
     * @param expr the grammar entry's terminal expresion
     */
    DefGrammarEntry(final String name, final TermExpr expr) {
        this.m_entry_type = name;
        this.m_expr = expr;
        this.m_min_max_size = new int[]{
            m_expr.get_min_size(), m_expr.get_max_size()};
    }

    @Override
    public String get_expr_type() {
        return m_entry_type;
    }

    @Override
    public int compute_max_size() {
        return m_min_max_size[1];
    }

    @Override
    public int[] get_min_max_size() {
        return m_min_max_size;
    }

    @Override
    public boolean compute_min_size() {
        return true;
    }

    @Override
    public void pre_process_arg_sizes(){
        //Noting to be done
    }
    
    @Override
    public int get_min_size() {
        return m_min_max_size[0];
    }

    @Override
    public void prepare_randomizers() {
        //NOTE: Nothing to be done, there is no randomizers in this entry
    }

    @Override
    public Expression choose_expr(int max_size) {
        LOGGER.log(Level.FINE, "Choosing expression {0} for size {1}",
                new Object[]{m_expr, max_size});
        return m_expr.duplicate();
    }

    @Override
    public Expression choose_expr(String signature) {
        LOGGER.log(Level.SEVERE, "Can not get signature expression for {0}", m_entry_type);
        throw new UnsupportedOperationException(
                "Can not get signature expression for " + m_entry_type);
    }

    @Override
    public boolean propagate_placement_nodes(Map<String, GrammarEntry> entries) {
        LOGGER.log(Level.FINE, "Propagated 0 placements for {1}", m_entry_type);
        //Nothign to be doe for the default terminal nodes
        return false;
    }

    @Override
    public List<Expression> get_expressions() {
        List<Expression> list = new ArrayList();
        list.add(m_expr);
        return list;
    }

    @Override
    public List<Double> get_weights() {
        List<Double> list = new ArrayList();
        list.add(1.0);
        return list;
    }
}
