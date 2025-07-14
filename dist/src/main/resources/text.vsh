#version 150

#CreateConstant

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
out float vertexDistance;
uniform vec2 ScreenSize;
uniform float GameTime;
#endif

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec3 ChunkOffset;

out vec4 vertexColor;
out vec2 texCoord0;

out float applyColor;

bool range(float t, float m1, float m2) {
    return t >= m1 && t <= m2;
}

bool range(vec2 t, vec2 m1, vec2 m2) {
    return range(t.x, m1.x, m2.x) && range(t.y, m1.y, m2.y);
}

bool range(vec3 t, vec3 m1, vec3 m2) {
    return range(t.x, m1.x, m2.x) && range(t.y, m1.y, m2.y) && range(t.z, m1.z, m2.z);
}

bool checkElement(float z) {
    if (z == 0) return true; //<=1.20.4 vanilla
    else if (z == 1000) return true; //>=1.20.5 vanilla
    else if (z == -90) return true; //<=1.20.4 forge
    else if (z == 2800) return true; //neoforge
    return false;
}

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
    vec2 ui = ceil(2 / vec2(ProjMat[0][0], -ProjMat[1][1]));
    vec2 uiScreen = ui / ScreenSize;
    vec3 color = Color.xyz;
    applyColor = 0;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    if (pos.y >= ui.y && ProjMat[3].x == -1) {
        int bit = int(pos.y) >> HEIGHT_BIT;

        if (((bit >> MAX_BIT) & 1) == 1) {

            int id = bit - (1 << MAX_BIT);

            pos.x -= 0.5 * ui.x;
            pos.y -= (bit << HEIGHT_BIT) + ADD_OFFSET + DEFAULT_OFFSET;

            float xGui = 0;
            float yGui = 0;
            float layer = 0;
            float opacity = 1;
            bool outline = false;
            int property = 0;

            switch (id) {
                #CreateLayout
            }

#if SHADER_VERSION < 2
            vertexColor = (checkElement(pos.z) && !outline) ? vec4(0) : Color * texelFetch(Sampler2, UV2 / 16, 0) * vec4(1, 1, 1, opacity);
#else
            vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0) * vec4(1, 1, 1, opacity);
#endif

            //Wave
            if ((property & 1) > 0) {
                pos.y += 4 * sin((GameTime * 1200 + pos.x / ui.x) * 3.1415 * 2);
            }
            //Rainbow
            if ((property & 2) > 0) {
                int hash = int(pos.x) * int(pos.y);
                float time = GameTime * 1200;
                hash = 31 * (hash + int(vertexColor.x + time));
                float r = float(hash % 224 + 32) / 255;
                hash = 31 * (hash + int(vertexColor.y + time));
                float g = float(hash % 224 + 32) / 255;
                hash = 31 * (hash + int(vertexColor.z + time));
                float b = float(hash % 224 + 32) / 255;
                float maxValue = max(max(r, g), b);
                vertexColor = vec4(pow(r / maxValue, 3), pow(g / maxValue, 3), pow(b / maxValue, 3), vertexColor.w);
            }
            //Tiny rainbow
            if ((property & 4) > 0) {
                int hash = int(pos.x) * int(pos.y);
                float time = GameTime * 1200;
                hash = 31 * (hash + int(vertexColor.x + time));
                float r = vertexColor.x + float(hash % 64) / 255;
                hash = 31 * (hash + int(vertexColor.y + time));
                float g = vertexColor.y + float(hash % 64) / 255;
                hash = 31 * (hash + int(vertexColor.z + time));
                float b = vertexColor.z + float(hash % 64) / 255;
                vertexColor = vec4(r, g, b, vertexColor.w);
            }

            pos.x += xGui;
            pos.y += yGui;
            pos.z += layer;

        }
    } else {
//HideExp        vec3 exp = vec3(128.0, 255.0, 32.0);
//HideExp        if (ProjMat[3].x == -1 && range(pos.y, ui.y - 60, ui.y - 20) && range(pos.x, ui.x / 2 - 60, ui.x / 2 + 60) && (range(color, exp / 256, exp / 254) || color == vec3(0))) {
//HideExp            vertexColor = vec4(0);
//HideExp        }
//RemapHotBar        if ((int(pos.z) == 200 || int(pos.z) == 600) && ProjMat[3].x == -1 && ui.y - pos.y <= 20) {
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

    #GenerateOtherMainMethod

#if SHADER_VERSION >= 3
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
#else
    vertexDistance = fogDistance(pos, FogShape);
#endif

    texCoord0 = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
