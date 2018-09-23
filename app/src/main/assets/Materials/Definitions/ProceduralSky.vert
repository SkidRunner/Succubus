#import "Common/ShaderLib/GLSLCompat.glsllib"

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 direction;

void main() {
	// set w coordinate to 0
	vec4 position = vec4(inPosition, 0.0);
	
	// compute rotation only for view matrix
	position = g_ViewMatrix * position;
	
	// now find projection
	position.w = 1.0;
	gl_Position = g_ProjectionMatrix * position;
	
	vec4 normal = vec4(inNormal * m_NormalScale, 0.0);
	direction = (g_WorldMatrixInverse * normal).xyz;
}
