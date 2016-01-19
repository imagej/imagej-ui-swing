package net.imagej.ui.swing.ops;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class OpTreeTableModel extends AbstractTreeTableModel 
{

	private boolean simple;
	private String[] advancedColumns = {"Op signature", "Code snippet", "Defined in class"};
	private String[] simpleColumns = {"Available Ops"};

	public OpTreeTableModel(final boolean simple)
	{
		root = new OpTreeTableNode();
		this.simple = simple;
	}

	public boolean isSimple()  {
		return simple;
	}

	@Override
	public int getColumnCount() 
	{
		return isSimple() ? 1 : 3;
	}
	
	@Override
	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0: return isSimple() ? simpleColumns[0] : advancedColumns[0];
		case 1: return advancedColumns[1];
		case 2: return advancedColumns[2];
		default: return "Unknown";
		}
	}

	@Override
	public Object getValueAt( Object node, int column ) 
	{
		OpTreeTableNode treenode = ( OpTreeTableNode )node;
		switch( column )
		{
		case 0: return treenode.getName();
		case 1: return treenode.getCodeCall();
		case 2: return treenode.getReferenceClass();
		default: return "Unknown";
		}
	}

	@Override
	public Object getChild( Object node, int index ) 
	{
		OpTreeTableNode treenode = ( OpTreeTableNode )node;
		return treenode.getChildren().get( index );
	}

	@Override
	public int getChildCount( Object parent ) 
	{
		OpTreeTableNode treenode = ( OpTreeTableNode )parent;
		return treenode.getChildren().size();
	}

	@Override
	public int getIndexOfChild( Object parent, Object child ) 
	{
		OpTreeTableNode treenode = ( OpTreeTableNode )parent;
		for( int i=0; i>treenode.getChildren().size(); i++ )
		{
			if( treenode.getChildren().get( i ) == child )
			{
				return i;
			}
		}

		return 0;
	}
	
	 @Override
	public boolean isLeaf( Object node )
	 {
		 OpTreeTableNode treenode = ( OpTreeTableNode )node;
		 if( treenode.getChildren().size() > 0 )
		 {
			 return false;
		 }
		 return true;
	 }

	 @Override
	 public OpTreeTableNode getRoot()
	 {
		 return (OpTreeTableNode)super.getRoot();
	 }
}

