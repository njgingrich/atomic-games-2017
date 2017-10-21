package com.atomicobject.rts.pathfinding;

public class AGFactory implements NodeFactory {

	@Override
	public AbstractNode createNode(int x, int y) {
		return new AGNode(x, y);
	}
}
