#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform vec2 ScreenSize;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

out float applyColor;

#CreateConstant

bool more(vec3 i1, vec3 i2) {
    return (i1.x >= i2.x && i1.y >= i2.y && i1.z >= i2.z);
}
bool less(vec3 i1, vec3 i2) {
    return (i1.x <= i2.x && i1.y <= i2.y && i1.z <= i2.z);
}

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

    float scale = round((ProjMat[0][0] / 2.0) / (1.0 / ScreenSize.x));
    vec2 ui = ScreenSize / scale;

    vec3 color = Color.xyz;

    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);

    applyColor = 0;

    if (pos.y >= ui.y) {
        int bit = int(pos.y) >> HEIGHT_BIT;

        if (((bit >> MAX_BIT) & 1) == 1) {

            int id = bit - (1 << MAX_BIT);

            pos.x -= 0.5 * ui.x;
            pos.y -= (bit << HEIGHT_BIT) + ADD_OFFSET + DEFAULT_OFFSET;

            float xGui = 0;
            float yGui = 0;
            float layer = 0;
            bool outline = false;

            switch (id) {
                #CreateLayout
            }

            vertexColor = ((pos.z == 0 || ceil(pos.z * 100) == 100000) && !outline) ? vec4(0) : Color * texelFetch(Sampler2, UV2 / 16, 0);

            pos.x += xGui;
            pos.y += yGui;
            pos.z += layer;

        }
    } else {
//HideExp        vec3 exp = vec3(128.0, 255.0, 32.0);
//HideExp        if ((int(pos.z) == 0 || int(pos.z) == 600) && ProjMat[3].x == -1 && ((more(color, exp / 256.0) && less(color , exp / 254.0)) || color == vec3(0))) {
//HideExp            vertexColor = vec4(0);
//HideExp        }
//HideItemName        if ((int(pos.z) == 0 || int(pos.z) == 400) && pos.y >= ui.y - 60 && pos.y <= ui.y - 35 && ProjMat[3].x == -1 && pos.x >= 0.5 * ui.x - 100 && pos.x <= 0.5 * ui.x + 100) {
//HideItemName            vertexColor = vec4(0);
//HideItemName        }
//RemapHotBar        vec2 scr = ceil(2 / vec2(ProjMat[0][0], -ProjMat[1][1]));
//RemapHotBar        if ((int(pos.z) == 200 || int(pos.z) == 600) && ProjMat[3].x == -1 && scr.y - pos.y <= 20) {
//RemapHotBar            float hotbarX = 0;
//RemapHotBar            float hotbarY = 0;
//RemapHotBar
//RemapHotBar            float center = 0.5 * ui.x;
//RemapHotBar
//RemapHotBar            if (pos.x + 85 < center && pos.x + 125 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_1_GUI_X) + (HOTBAR_1_PIXEL_X) - center + 110;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_1_GUI_Y) + (HOTBAR_1_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x + 65 < center && pos.x + 85 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_2_GUI_X) + (HOTBAR_2_PIXEL_X) - center + 80;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_2_GUI_Y) + (HOTBAR_2_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x + 45 < center && pos.x + 65 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_3_GUI_X) + (HOTBAR_3_PIXEL_X) - center + 60;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_3_GUI_Y) + (HOTBAR_3_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x + 25 < center && pos.x + 45 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_4_GUI_X) + (HOTBAR_4_PIXEL_X) - center + 40;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_4_GUI_Y) + (HOTBAR_4_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x + 5 < center && pos.x + 25 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_5_GUI_X) + (HOTBAR_5_PIXEL_X) - center + 20;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_5_GUI_Y) + (HOTBAR_5_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x - 15 < center && pos.x + 5 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_6_GUI_X) + (HOTBAR_6_PIXEL_X) - center;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_6_GUI_Y) + (HOTBAR_6_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x - 35 < center && pos.x - 5 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_7_GUI_X) + (HOTBAR_7_PIXEL_X) - center - 20;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_7_GUI_Y) + (HOTBAR_7_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x - 55 < center && pos.x - 25 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_8_GUI_X) + (HOTBAR_8_PIXEL_X) - center - 40;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_8_GUI_Y) + (HOTBAR_8_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x - 75 < center && pos.x - 45 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_9_GUI_X) + (HOTBAR_9_PIXEL_X) - center - 60;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_9_GUI_Y) + (HOTBAR_9_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            } else if (pos.x - 95 < center && pos.x - 65 > center) {
//RemapHotBar
//RemapHotBar                hotbarX += ui.x / 100.0 * (HOTBAR_10_GUI_X) + (HOTBAR_10_PIXEL_X) - center - 80;
//RemapHotBar                hotbarY += ui.y / 100.0 * (HOTBAR_10_GUI_Y) + (HOTBAR_10_PIXEL_Y) - ui.y;
//RemapHotBar
//RemapHotBar            }
//RemapHotBar
//RemapHotBar            pos.x += hotbarX;
//RemapHotBar            pos.y += hotbarY;
//RemapHotBar        }
    }

#CreateOtherShader

    vertexDistance = getDistance(ModelViewMat, pos, FogShape);
    texCoord0 = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
