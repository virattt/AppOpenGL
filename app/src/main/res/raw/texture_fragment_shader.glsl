precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main()
{
	vec4 tex = texture2D(u_TextureUnit, v_TextureCoordinates);
	if (tex.a < 0.5) {
	  discard;
	} else {
	  gl_FragColor = tex;
	}
}