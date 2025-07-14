#version 150

#CreateConstant

#moj_import <light.glsl>
#moj_import <fog.glsl>

#if SHADER_VERSION >= 3
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
#else
uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
out float vertexDistance;
uniform vec2 ScreenSize;
#endif

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform vec3 ChunkOffset;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec2 texCoord2;
out vec4 normal;

float fogDistance(vec3 pos, int shape) {
    if (shape == 0) {
        return length(pos);
    } else {
        float distXZ = length(pos.xz);
        float distY = abs(pos.y);
        return max(distXZ, distY);
    }
}

#GenerateOtherDefinedMethod

void main() {
    vec3 pos = Position;
//RemapHotBar    vec2 ui = ceil(2 / vec2(ProjMat[0][0], -ProjMat[1][1]));
//RemapHotBar    if (ProjMat[3].x <= -1 && ui.y - pos.y <= 25) {
//RemapHotBar        float hotbarX = 0;
//RemapHotBar        float hotbarY = 0;
//RemapHotBar
//RemapHotBar        float center = 0.5 * ui.x;
//RemapHotBar
//RemapHotBar        float pixel = pos.x + 0.5;
//RemapHotBar        if (pixel + 100 < center && pixel + 128 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_1_GUI_X) + (HOTBAR_1_PIXEL_X) - center + 110;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_1_GUI_Y) + (HOTBAR_1_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel + 70 < center && pixel + 98 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_2_GUI_X) + (HOTBAR_2_PIXEL_X) - center + 80;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_2_GUI_Y) + (HOTBAR_2_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel + 50 < center && pixel + 68 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_3_GUI_X) + (HOTBAR_3_PIXEL_X) - center + 60;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_3_GUI_Y) + (HOTBAR_3_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel + 30 < center && pixel + 48 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_4_GUI_X) + (HOTBAR_4_PIXEL_X) - center + 40;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_4_GUI_Y) + (HOTBAR_4_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel + 10 < center && pixel + 28 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_5_GUI_X) + (HOTBAR_5_PIXEL_X) - center + 20;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_5_GUI_Y) + (HOTBAR_5_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel - 10 < center && pixel + 8 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_6_GUI_X) + (HOTBAR_6_PIXEL_X) - center;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_6_GUI_Y) + (HOTBAR_6_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel - 30 < center && pixel - 12 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_7_GUI_X) + (HOTBAR_7_PIXEL_X) - center - 20;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_7_GUI_Y) + (HOTBAR_7_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel - 50 < center && pixel - 32 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_8_GUI_X) + (HOTBAR_8_PIXEL_X) - center - 40;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_8_GUI_Y) + (HOTBAR_8_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel - 70 < center && pixel - 52 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_9_GUI_X) + (HOTBAR_9_PIXEL_X) - center - 60;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_9_GUI_Y) + (HOTBAR_9_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        } else if (pixel - 90 < center && pixel - 72 >= center) {
//RemapHotBar
//RemapHotBar            hotbarX += ui.x / 100.0 * (HOTBAR_10_GUI_X) + (HOTBAR_10_PIXEL_X) - center - 80;
//RemapHotBar            hotbarY += ui.y / 100.0 * (HOTBAR_10_GUI_Y) + (HOTBAR_10_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar        }
//RemapHotBar        pos.x += hotbarX;
//RemapHotBar        pos.y += hotbarY;
//RemapHotBar    }

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

#if SHADER_VERSION >= 3
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
#else
    vertexDistance = fogDistance(pos, FogShape);
#endif

    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color) * texelFetch(Sampler2, UV2 / 16, 0);

    texCoord0 = UV0;
    texCoord1 = UV1;
    texCoord2 = UV2;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
