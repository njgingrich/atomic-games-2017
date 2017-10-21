package com.atomicobject.rts.pathfinding;

import com.atomicobject.rts.model.Tile;

public class AGNode extends AbstractNode {

	Tile tile;
	public AGNode(int xPosition, int yPosition) {
		super(xPosition, yPosition);
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}
	@Override
	public void sethCosts(AbstractNode endAbstractNode) {
		 this.sethCosts((Math.abs(this.getxPosition() - endAbstractNode.getxPosition())
                 + Math.abs(this.getyPosition() - endAbstractNode.getyPosition()))
                 * BASICMOVEMENTCOST);

	}

}
