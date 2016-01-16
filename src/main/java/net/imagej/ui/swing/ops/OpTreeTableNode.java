package net.imagej.ui.swing.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scijava.command.CommandInfo;

public class OpTreeTableNode implements Comparable<OpTreeTableNode> {
	private String simpleName = "";
	private String referenceClass = "";
	private String codeCall = "";
	private CommandInfo info;
	private List<OpTreeTableNode> children = new ArrayList<>();

	/**
	 * <ul>
	 * <li>String 1: display name</li>
	 * <li>String 2: code call</li>
	 * <li>String 3: referenced class</li>
	 * </ul>
	 */
	public OpTreeTableNode(String... fields) {
		if (fields.length > 0)
			simpleName = fields[0];
		if (fields.length > 1)
			codeCall = fields[1];
		if (fields.length > 2)
			referenceClass = fields[2];
	}

	public String getName() {
		return simpleName;
	}

	public void setName(String simpleName) {
		this.simpleName = simpleName;
	}

	public String getReferenceClass() {
		return referenceClass;
	}

	public void setReferenceClass(String referenceClass) {
		this.referenceClass = referenceClass;
	}

	public String getCodeCall() {
		return codeCall;
	}

	public void setCodeCall(String codeCall) {
		this.codeCall = codeCall;
	}

	public List<OpTreeTableNode> getChildren() {
		return children;
	}

	public void add(final OpTreeTableNode child) {
		int index = -(Collections.binarySearch(children, child)+1);
		children.add(index, child);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setCommandInfo(final CommandInfo info) {
		this.info = info;
	}

	public CommandInfo getCommandInfo() {
		return info;
	}

	// -- Comparable api --

	@Override
	public int compareTo(final OpTreeTableNode o) {
		final int v = simpleName.compareTo(o.simpleName);
		// sort by simple name first, then by reference class
		return v != 0 ? v : referenceClass.compareTo(o.referenceClass);
	}
}