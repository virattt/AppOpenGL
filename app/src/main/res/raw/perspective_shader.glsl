precision mediump float;

uniform sampler2D u_TextureUnit;
uniform float u_rangeStart;
uniform float u_rangeEnd;
varying vec2 v_TextureCoordinates;

// You must set these to your camera values or things won't be correct
float znear = 1.0; //camera clipping start
float zfar = 40.0; //camera clipping end

float linearize(float depth)
{
    return -zfar * znear / (depth * (zfar - znear) - zfar);
}

void main()
{
	vec4 tex = texture2D(u_TextureUnit, v_TextureCoordinates);
    float z = linearize(gl_FragCoord.z);
	if (tex.a < 0.5 || (z > u_rangeStart && z < u_rangeEnd)) {
	  discard;
	} else {
	  gl_FragColor = tex;
	}
}