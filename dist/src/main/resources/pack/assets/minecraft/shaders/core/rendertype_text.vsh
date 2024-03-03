#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform int FogShape;
uniform vec2 ScreenSize;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

#define DEFAULT_HEIGHT 1023
#define SEVEN_BIT 127
#define HEIGHT_BIT 10
#define DEFAULT_OFFSET 64
#define ADD_OFFSET 512
#define CHECK_AMOUNT 1

int more(vec3 i1, vec3 i2) {
    return (i1.x >= i2.x && i1.y >= i2.y && i1.z >= i2.z) ? 1 : 0;
}
int less(vec3 i1, vec3 i2) {
    return (i1.x <= i2.x && i1.y <= i2.y && i1.z <= i2.z) ? 1 : 0;
}

void main() {

    vec3 pos = Position;

    float scale = round((ProjMat[0][0] / 2.0) / (1.0 / ScreenSize.x));
    vec2 ui = ScreenSize / scale;

    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * pos, FogShape);
    texCoord0 = UV0;

    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);

    vec3 color = Color.xyz;

    if (pos.y >= ScreenSize.y && pos.y >= DEFAULT_HEIGHT) {
        int bit = int(pos.y - DEFAULT_HEIGHT) >> HEIGHT_BIT;

        int xBit = (bit >> 7) & SEVEN_BIT;
        int yBit = (bit & SEVEN_BIT);

        if (xBit >= CHECK_AMOUNT && yBit >= CHECK_AMOUNT) {
            pos.y -= (DEFAULT_HEIGHT + DEFAULT_OFFSET + ADD_OFFSET);

            pos.y -= xBit << (HEIGHT_BIT + 7);
            pos.y -= yBit << HEIGHT_BIT;

            pos.x -= int(ui.x * (0.5 - float(xBit - CHECK_AMOUNT) / 100.0));
            pos.y += int(ui.y * float(yBit - CHECK_AMOUNT) / 100.0);

            vertexColor = (pos.z == 0) ? vec4(0) : Color * texelFetch(Sampler2, UV2 / 16, 0);
        } else if (color == vec3(0)) {
            vertexColor = vec4(0);
        }
    } else {
        vec3 exp = vec3(128.0, 255.0, 32.0);

        if (ui.x >= 1 && ui.y >= 0.7 && ((more(color, exp / 257.0) == 1 && less(color , exp / 253.0) == 1) || color == vec3(0, 0, 0))) {
            vertexColor = vec4(0);
        }
    }

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
