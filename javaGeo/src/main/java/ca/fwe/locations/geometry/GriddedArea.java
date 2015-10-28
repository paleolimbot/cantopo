package ca.fwe.locations.geometry;


public abstract class GriddedArea implements Rectangle {

	protected Rectangle rectangle ;
	public Object tag ;

	//children will be referred to as [y-1][x-1] here, for ease of writing the id pattern

	private GriddedArea[][] children ;

	private String id ;

	public GriddedArea(String areaId, Rectangle rectangle) {
		this.rectangle = rectangle ;
		this.id = areaId ;
	}

	public String getId() {
		return id ;
	}

	public String getChildId(int x, int y) {
		if(this.getChildIdPattern() != null)
			return getChildIdPattern()[y-1][x-1] ;
		else
			return null ;
	}

	public GriddedArea getChild(String id) {
		if(this.getChildIdPattern() != null && id != null) {
			for(int y=1; y<=this.getChildrenNS(); y++) {
				for(int x=1; x<=this.getChildrenEW(); x++) {
					if(id.equals(getChildId(x,y)))
						return getChild(x, y) ;
				}
			}
		}
		return null ;
	}

	public boolean contains(XY point) {
		return rectangle.contains(point) ;
	}
	
	public boolean hasChildren() {
		return this.getChildIdPattern() != null ;
	}

	public GriddedArea getChild(XY point) {
		if(this.contains(point)) {
			for(int x=1; x<=getChildrenEW(); x++) {
				for(int y=1; y<=getChildrenNS(); y++) {
					GriddedArea child = this.getChild(x, y) ;
					if(child != null) {
						if(child.contains(point))
							return child ;
					}
				}
			}
			return null ;
		} else {
			return null ;
		}
	}

	public GriddedArea getChild(int x, int y) {
		if(this.hasChildren() && children != null)
			return children[y-1][x-1] ;
		else
			return null ;
	}

	public int getChildrenEW() {
		if(this.hasChildren())
			return this.getChildIdPattern()[0].length ;
		else
			return 1 ;
	}

	public int getChildrenNS() {
		if(this.hasChildren())
			return this.getChildIdPattern().length ;
		else
			return 1 ;
	}

	public int getChildrenSize() {
		return getChildrenEW() * getChildrenNS() ;
	}

	public void makeChildren() {
		if(this.getChildrenEW() > 1 || this.getChildrenNS() > 1) {
			children = new GriddedArea[this.getChildrenNS()][this.getChildrenEW()] ;
			Rectangle[][] areas = this.subdivide(getChildrenEW(), getChildrenNS()) ;
			for(int i=0; i<areas.length; i++) {
				for(int j=0; j<areas[i].length; j++) {
					String childId = this.getChildId(i+1, j+1) ;
					children[j][i] = makeChild(childId, areas[i][j]) ;
				}
			}
		} else {
			//do nothing
		}
	}



	@Override
	public Rectangle[][] subdivide(int xRows, int yRows) {
		return rectangle.subdivide(xRows, yRows) ;
	}

	@Override
	public XY upperLeft() {
		return rectangle.upperLeft() ;
	}

	@Override
	public XY upperRight() {
		return rectangle.upperRight() ;
	}

	@Override
	public XY lowerLeft() {
		return rectangle.lowerLeft() ;
	}

	@Override
	public XY lowerRight() {
		return rectangle.lowerRight() ;
	}

	@Override
	public XY centre() {
		return rectangle.centre() ;
	}
	
	protected Object getTag() {
		return tag ;
	}
	
	protected void setTag(Object tag) {
		this.tag = tag ;
	}
	
	protected abstract GriddedArea makeChild(String id, Rectangle rectangle) ;

	protected abstract String[][] getChildIdPattern() ;
	
	public GriddedArea getBaseChild(XY point) {
		this.makeChildren() ;
		GriddedArea child = this.getChild(point) ;
		if(child != null) {
			if(child.hasChildren()) {
				child.makeChildren() ;
				return child.getBaseChild(point) ;
			} else {
				return child ;
			}
		} else {
			return this ;
		}
	}
}
