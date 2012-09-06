package wolf.util.tess;

import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;
import static org.lwjgl.opengl.GL11.*;

public class TessCallback extends GLUtessellatorCallbackAdapter {

	public void begin(int type) {
		glBegin(type);
	}

	public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
		for (int i=0;i<outData.length;i++) {
			double[] combined = new double[6];
			combined[0] = coords[0];
			combined[1] = coords[1];
			combined[2] = coords[2];
			combined[3] = 1;
			combined[4] = 1;
			combined[5] = 1;
			System.out.println(combined);
			outData[i] = new VertexData(combined);
		}
//		vertex[0] = coords[0];
//		vertex[1] = coords[1];
//		vertex[2] = coords[2];
//
//		for (int i = 3; i < 6; i++)
//		{
//		vertex[i] = weight[0] * vertex_data[0][i] +
//		indent indweight[1] * vertex_data[1][i] +
//		indent indweight[2] * vertex_data[2][i] +
//		indent indweight[3] * vertex_data[3][i];
//		}
//
//		*dataOut = vertex;
	}

	public void end() {
		glEnd();
	}

	public void vertex(Object vertexData) {
		VertexData vertex = (VertexData) vertexData;
		
		glVertex3d(vertex.data[0], vertex.data[1], vertex.data[2]);
		//glColor3d(vertex.data[3], vertex.data[4], vertex.data[5]);
	}
}
