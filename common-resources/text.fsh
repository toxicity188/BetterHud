#version 330

#CreateConstant

#moj_import <fog.glsl>

#if SHADER_VERSION >= 2
#moj_import <dynamictransforms.glsl>
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
#ifdef IS_GRAYSCALE
    vec4 texColor = texture(Sampler0, texCoord0).rrrr;
#else
    vec4 texColor = texture(Sampler0, texCoord0);
#endif
#ifdef IS_SEE_THROUGH
    vec4 color = texColor * vertexColor;
#else
    vec4 color = texColor * vertexColor * ColorModulator;
#endif

    #GenerateOtherMainMethod

    if (color.a < 0.1) {
        discard;
    }
#ifdef IS_SEE_THROUGH
    fragColor = color * ColorModulator;
#elif defined(IS_GUI)
    fragColor = color;
#elif SHADER_VERSION >= 2
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#else
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
#endif
}
