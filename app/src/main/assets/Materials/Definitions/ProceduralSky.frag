#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform vec4 m_SkyColor;
uniform vec4 m_HorizonColor;
uniform vec4 m_GroundColor;

varying vec3 direction;

void main() {
	vec3 normal = normalize(direction);
	if(normal.y < 0.0) {
		gl_FragColor = m_SkyColor;
    } else {
    	gl_FragColor = m_GroundColor;
    }
    //gl_FragColor = vec4((normal * vec3(0.5)) + vec3(0.5), 1.0);
}
