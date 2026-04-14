#version 150

vec4 sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texelFetch(lightMap, uv / 16, 0);
}