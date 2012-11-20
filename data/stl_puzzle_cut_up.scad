module city(){
	import("city-repaired2.stl");
}

length = ((4*1024)/16);
size = (4*1024);
intersection(){
	city();
	for(nx = [0 : 15])
	{
		for(ny = [0 : 15])
		{	
			translate([(length*nx)-(size/2),(length*ny)-(size/2),-64])
			{
				cube(size = [length,length,length], center = false);
			}
		}
	}
}