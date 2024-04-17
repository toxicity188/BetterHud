#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform vec2 ScreenSize;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec4 normal;

#CreateConstant

float getDistance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
        float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
        return max(distXZ, distY);
    }
}

void main() {
    vec3 pos = Position;
//RemapHotBar    float scale = round((ProjMat[0][0] / 2.0) / (1.0 / ScreenSize.x));
//RemapHotBar    vec2 ui = ScreenSize / scale;
//RemapHotBar    vec2 scr = 2 / vec2(ProjMat[0][0], -ProjMat[1][1]);
//RemapHotBar    if (ProjMat[3].x <= -1 && scr.y - pos.y <= 25) {
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

    vertexDistance = getDistance(ModelViewMat, pos, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}

