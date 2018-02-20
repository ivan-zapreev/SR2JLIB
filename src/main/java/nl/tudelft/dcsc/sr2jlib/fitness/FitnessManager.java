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
package nl.tudelft.dcsc.sr2jlib.fitness;

/**
 * 
 * The singleton class for storing the fitness computer instance
 *
 * @author Dr. Ivan S. Zapreev
 */
public class FitnessManager {
    
    /**
     * The basic constructor
     */
    private FitnessManager(){}

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
    
}
