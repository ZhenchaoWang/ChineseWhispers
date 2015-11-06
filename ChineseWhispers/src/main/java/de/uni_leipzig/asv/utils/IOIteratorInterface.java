/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/IOIteratorInterface.java,v 1.3 2005/06/11 15:06:56 steresniak Exp $
 * 
 * Created on 12.05.2005, 17:13:23 by knorke
 * for project knorke
 */
package de.uni_leipzig.asv.utils;

import java.sql.SQLException;

/**
 * @author knorke
 */
public interface IOIteratorInterface {
    /**
     * returns true if more data available
     * @return
     */
    public boolean hasNext();


    /**
     * returns a String[] for every entry in file/database
     * @return
     * @throws SQLException
    * @throws IOIteratorException
     */
    public Object next() throws IOIteratorException;
}