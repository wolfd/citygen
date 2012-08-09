package wolf.city.buildings.shape;

public class WallSection extends Wall{
	public Wall parent;
	
	public WallSection(Wall wall, float sectionLength) {
		super(wall.floor, sectionLength);
		parent = wall;
	}
}
