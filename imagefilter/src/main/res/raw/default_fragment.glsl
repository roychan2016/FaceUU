#extension GL_OES_EGL_image_external : require

precision mediump float;
varying mediump vec2 textureCoordinate;

uniform samplerExternalOES inputImageTexture;

//大眼
uniform highp float scaleRatio;// 缩放系数，0无缩放，大于0则放大
uniform highp float radius;// 缩放算法的作用域半径
uniform highp vec2 leftEyeCenterPosition; // 左眼控制点，越远变形越小
uniform highp vec2 rightEyeCenterPosition; // 右眼控制点
uniform float aspectRatio; // 所处理图像的宽高比

//瘦脸
uniform float leftContourPoints[20];
uniform float rightContourPoints[20];
uniform float deltaArray[10];
uniform int arraySize;

//美颜
uniform vec2 singleStepOffset;
uniform float params;
const highp vec3 W = vec3(0.299,0.587,0.114);
vec2 blurCoordinates[20];

highp vec2 warpEysPositionToUse(vec2 centerPostion, vec2 currentPosition, float radius, float scaleRatio, float aspectRatio)
{
    vec2 positionToUse = currentPosition;

    vec2 currentPositionToUse = vec2(currentPosition.x, currentPosition.y * aspectRatio + 0.5 - 0.5 * aspectRatio);
    vec2 centerPostionToUse = vec2(centerPostion.x, centerPostion.y * aspectRatio + 0.5 - 0.5 * aspectRatio);

    float r = distance(currentPositionToUse, centerPostionToUse);

    if(r < radius)
    {
        float alpha = 1.0 - scaleRatio * pow(r / radius - 1.0, 2.0);
        positionToUse = centerPostion + alpha * (currentPosition - centerPostion);
    }

    return positionToUse;
}

highp vec2 warpFacePositionToUse(vec2 currentPoint, vec2 contourPointA,  vec2 contourPointB, float radius, float delta, float aspectRatio)
 {
     vec2 positionToUse = currentPoint;

     vec2 currentPointToUse = vec2(currentPoint.x, currentPoint.y * aspectRatio + 0.5 - 0.5 * aspectRatio);
     vec2 contourPointAToUse = vec2(contourPointA.x, contourPointA.y * aspectRatio + 0.5 - 0.5 * aspectRatio);

     float r = distance(currentPointToUse, contourPointAToUse);
     if(r < radius)
     {
         vec2 dir = normalize(contourPointB - contourPointA);
         float dist = radius * radius - r * r;
         float alpha = dist / (dist + (r-delta) * (r-delta));
         alpha = alpha * alpha;

         positionToUse = positionToUse - alpha * delta * dir;

     }

     return positionToUse;
 }

float hardLight(float color)
{
	if(color <= 0.5)
		color = color * color * 2.0;
	else
		color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	return color;
}

void main()
{
    vec2 positionToUse = textureCoordinate;

    if(scaleRatio > 0.0)  //大眼
    {
        positionToUse = warpEysPositionToUse(leftEyeCenterPosition, textureCoordinate, radius, scaleRatio, aspectRatio);
        positionToUse = warpEysPositionToUse(rightEyeCenterPosition, positionToUse, radius, scaleRatio, aspectRatio);
        gl_FragColor = texture2D(inputImageTexture, positionToUse);
        if(arraySize <= 0 && params <= 0.0) return;
    }

    for(int i = 0; i < arraySize; i++) //瘦脸
    {
        positionToUse = warpFacePositionToUse(positionToUse, vec2(leftContourPoints[i * 2], leftContourPoints[i * 2 + 1]), vec2(rightContourPoints[i * 2], rightContourPoints[i * 2 + 1]), radius, deltaArray[i], aspectRatio);
        positionToUse = warpFacePositionToUse(positionToUse, vec2(rightContourPoints[i * 2], rightContourPoints[i * 2 + 1]), vec2(leftContourPoints[i * 2], leftContourPoints[i * 2 + 1]), radius, deltaArray[i], aspectRatio);
        gl_FragColor = texture2D(inputImageTexture, positionToUse);
        if(params <= 0.0) return;
    }

    if(params > 0.0) //美颜
    {
        vec3 centralColor = texture2D(inputImageTexture, positionToUse).rgb;

        blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);
        blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);
        blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);
        blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);
        blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);
        blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);
        blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);
        blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);
        blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);
        blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);
        blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);
        blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);
        blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);
        blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);
        blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);
        blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);
        blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);
        blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);
        blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);
        blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);

        float sampleColor = centralColor.g * 20.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;
        sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;
        sampleColor = sampleColor / 48.0;

        float highPass = centralColor.g - sampleColor + 0.5;
        for(int i = 0; i < 5;i++)
        {
            highPass = hardLight(highPass);
        }
        float luminance = dot(centralColor, W);
        float alpha = pow(luminance, params);
        vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;

        gl_FragColor = vec4(mix(smoothColor.rgb, max(smoothColor, centralColor), alpha), 1.0);

        return;
    }

    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}