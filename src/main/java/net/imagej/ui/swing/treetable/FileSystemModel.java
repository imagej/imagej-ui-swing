/*
 * %W% %E%
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */
package net.imagej.ui.swing.treetable;

import java.io.File;
import java.util.Date;

/**
 * FileSystemModel is a TreeTableModel representing a hierarchical file 
 * system. Nodes in the FileSystemModel are FileNodes which, when they 
 * are directory nodes, cache their children to avoid repeatedly querying 
 * the real file system. 
 * 
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

public class FileSystemModel extends AbstractTreeTableModel 
                             implements TreeTableModel {

    // Names of the columns.
    static protected String[]  cNames = {"Name", "Size", "Type", "Modified"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, Integer.class, String.class, Date.class};

    // The the returned file length for directories. 
    public static final Integer ZERO = new Integer(0); 

    public FileSystemModel() { 
	super(new FileNode(new File(File.separator))); 
    }

    //
    // Some convenience methods. 
    //

    protected File getFile(Object node) {
	FileNode fileNode = ((FileNode)node); 
	return fileNode.getFile();       
    }

    protected Object[] getChildren(Object node) {
	FileNode fileNode = ((FileNode)node); 
	return fileNode.getChildren(); 
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) { 
	Object[] children = getChildren(node); 
	return (children == null) ? 0 : children.length;
    }

    public Object getChild(Object node, int i) { 
	return getChildren(node)[i]; 
    }

    // The superclass's implementation would work, but this is more efficient. 
    public boolean isLeaf(Object node) { return getFile(node).isFile(); }

    //
    //  The TreeTableNode interface. 
    //

    public int getColumnCount() {
	return cNames.length;
    }

    public String getColumnName(int column) {
	return cNames[column];
    }

    public Class getColumnClass(int column) {
	return cTypes[column];
    }
 
    public Object getValueAt(Object node, int column) {
	File file = getFile(node); 
	try {
	    switch(column) {
	    case 0:
		return file.getName();
	    case 1:
		return file.isFile() ? new Integer((int)file.length()) : ZERO;
	    case 2:
		return file.isFile() ?  "File" : "Directory";
	    case 3:
		return new Date(file.lastModified());
	    }
	}
	catch  (SecurityException se) { }
   
	return null; 
    }
}

/* A FileNode is a derivative of the File class - though we delegate to 
 * the File object rather than subclassing it. It is used to maintain a 
 * cache of a directory's children and therefore avoid repeated access 
 * to the underlying file system during rendering. 
 */
class FileNode { 
    File     file; 
    Object[] children; 

    public FileNode(File file) { 
	this.file = file; 
    }

    // Used to sort the file names.
    static private MergeSort  fileMS = new MergeSort() {
	public int compareElementsAt(int a, int b) {
	    return ((String)toSort[a]).compareTo((String)toSort[b]);
	}
    };

    /**
     * Returns the the string to be used to display this leaf in the JTree.
     */
    public String toString() { 
	return file.getName();
    }

    public File getFile() {
	return file; 
    }

    /**
     * Loads the children, caching the results in the children ivar.
     */
    protected Object[] getChildren() {
	if (children != null) {
	    return children; 
	}
	try {
	    String[] files = file.list();
	    if(files != null) {
		fileMS.sort(files); 
		children = new FileNode[files.length]; 
		String path = file.getPath();
		for(int i = 0; i < files.length; i++) {
		    File childFile = new File(path, files[i]); 
		    children[i] = new FileNode(childFile);
		}
	    }
	} catch (SecurityException se) {}
	return children; 
    }
}

