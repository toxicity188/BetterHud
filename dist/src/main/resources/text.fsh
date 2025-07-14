#version 150

#CreateConstant

#moj_import <fog.glsl>

#if SHADER_VERSION >= 3
#moj_import <minecraft:dynamictransforms.glsl>
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#else
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in float vertexDistance;
#endif

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#GenerateOtherDefinedMethod

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    vec4 color = texColor * vertexColor * ColorModulator;

    #GenerateOtherMainMethod

    if (color.a < 0.1) {
        discard;
    }
#if SHADER_VERSION >= 3
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#else
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
#endif
}
