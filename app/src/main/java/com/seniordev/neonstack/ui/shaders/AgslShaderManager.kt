package com.seniordev.neonstack.ui.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.os.SystemClock
import android.view.View
import kotlin.math.PI
import kotlin.math.sin

/**
 * Manages Next-Gen AGSL (Android Graphics Shading Language) Runtime Shaders.
 * Compatible with Android 13+ (API 33+) with graceful no-op on earlier versions.
 */
class AgslShaderManager(private val targetView: View) {

    companion object {
        // AGSL Shockwave & Chromatic Refraction Shader
        private const val SHOCKWAVE_SHADER_SRC = """
            uniform shader compositedImage;
            uniform float2 uResolution;
            uniform float2 uCenter;
            uniform float uProgress;    // 0.0 to 1.0
            uniform float uIntensity;   // Strength of shockwave

            half4 main(float2 fragCoord) {
                if (uProgress <= 0.0 || uProgress >= 1.0) {
                    return compositedImage.eval(fragCoord);
                }

                float2 dir = fragCoord - uCenter;
                float dist = length(dir);
                float2 normDir = dir / max(dist, 0.001);

                // Shockwave ring radius expands with progress
                float maxRadius = length(uResolution) * 0.75;
                float waveRadius = uProgress * maxRadius;
                float waveWidth = 60.0;
                float diff = dist - waveRadius;

                float displacement = 0.0;
                if (abs(diff) < waveWidth) {
                    float factor = 1.0 - abs(diff) / waveWidth;
                    // Sinusoidal refraction ripple
                    displacement = sin(factor * 3.14159265) * (1.0 - uProgress) * uIntensity;
                }

                // Chromatic Aberration: displace RGB channels independently
                float chromaOffset = 6.0 * (1.0 - uProgress);
                float2 coordR = fragCoord - normDir * (displacement + chromaOffset);
                float2 coordG = fragCoord - normDir * displacement;
                float2 coordB = fragCoord - normDir * (displacement - chromaOffset);

                half4 colorR = compositedImage.eval(coordR);
                half4 colorG = compositedImage.eval(coordG);
                half4 colorB = compositedImage.eval(coordB);

                return half4(colorR.r, colorG.g, colorB.b, colorG.a);
            }
        """

        // AGSL Edge Chromatic Aberration Shader for Hard Drops
        private const val ABERRATION_SHADER_SRC = """
            uniform shader compositedImage;
            uniform float2 uResolution;
            uniform float uIntensity; // Decays from 1.0 to 0.0

            half4 main(float2 fragCoord) {
                if (uIntensity <= 0.0) {
                    return compositedImage.eval(fragCoord);
                }

                float2 center = uResolution * 0.5;
                float2 dir = (fragCoord - center) / length(center);
                float distFactor = length(dir);

                // Displace along radial vector from screen center
                float offset = uIntensity * distFactor * 8.0;
                half4 colorR = compositedImage.eval(fragCoord + dir * offset);
                half4 colorG = compositedImage.eval(fragCoord);
                half4 colorB = compositedImage.eval(fragCoord - dir * offset);

                return half4(colorR.r, colorG.g, colorB.b, colorG.a);
            }
        """
        // AGSL Retro Arcade CRT Scanline & Phosphor Vignette Shader
        private const val CRT_SHADER_SRC = """
            uniform shader compositedImage;
            uniform float2 uResolution;
            uniform float uScanlineIntensity;

            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / uResolution;

                // Subtle scanline pattern (simulating 240p arcade CRT monitor)
                float scanline = sin(fragCoord.y * 1.5707963) * 0.5 + 0.5;
                float scanFactor = 1.0 - (scanline * uScanlineIntensity);

                // Subtle vignette dropoff at corners
                float2 d = abs(uv - 0.5) * 2.0;
                float vignette = 1.0 - dot(d, d) * 0.16;

                half4 color = compositedImage.eval(fragCoord);
                // Subtle warm phosphor lift
                half3 phosphor = color.rgb * half3(1.0, 1.02, 1.01);
                return half4(phosphor * scanFactor * vignette, color.a);
            }
        """
    }

    private var shockwaveShader: RuntimeShader? = null
    private var aberrationShader: RuntimeShader? = null
    private var crtShader: RuntimeShader? = null

    var isCrtEnabled: Boolean = false
        set(value) {
            field = value
            targetView.invalidate()
        }

    // Shockwave animation state
    private var shockwaveStartTime: Long = 0L
    private var shockwaveDurationMs: Long = 450L
    private var isShockwaveActive: Boolean = false

    // Chromatic aberration animation state
    private var aberrationStartTime: Long = 0L
    private var aberrationDurationMs: Long = 180L
    private var isAberrationActive: Boolean = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                shockwaveShader = RuntimeShader(SHOCKWAVE_SHADER_SRC)
                aberrationShader = RuntimeShader(ABERRATION_SHADER_SRC)
                crtShader = RuntimeShader(CRT_SHADER_SRC)
            } catch (_: Exception) {
                // If device graphics driver fails compilation, fallback gracefully
                shockwaveShader = null
                aberrationShader = null
                crtShader = null
            }
        }
    }

    fun triggerShockwave(centerX: Float, centerY: Float) {
        if (shockwaveShader == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        shockwaveStartTime = SystemClock.uptimeMillis()
        isShockwaveActive = true

        val w = targetView.width.toFloat()
        val h = targetView.height.toFloat()
        shockwaveShader?.setFloatUniform("uResolution", w, h)
        shockwaveShader?.setFloatUniform("uCenter", centerX, centerY)
        shockwaveShader?.setFloatUniform("uIntensity", 32.0f)
        targetView.invalidate()
    }

    fun triggerChromaticAberration() {
        if (aberrationShader == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        aberrationStartTime = SystemClock.uptimeMillis()
        isAberrationActive = true

        val w = targetView.width.toFloat()
        val h = targetView.height.toFloat()
        aberrationShader?.setFloatUniform("uResolution", w, h)
        targetView.invalidate()
    }

    /**
     * Called on each frame tick to update shader uniforms and chain multi-shaders if both are active.
     */
    fun update() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val now = SystemClock.uptimeMillis()

        var shockwaveEffect: RenderEffect? = null
        var aberrationEffect: RenderEffect? = null

        // 1. Update Shockwave Refraction Shader
        if (isShockwaveActive && shockwaveShader != null) {
            val elapsed = now - shockwaveStartTime
            if (elapsed < shockwaveDurationMs) {
                val progress = elapsed.toFloat() / shockwaveDurationMs
                shockwaveShader?.setFloatUniform("uProgress", progress)
                shockwaveEffect = RenderEffect.createRuntimeShaderEffect(shockwaveShader!!, "compositedImage")
            } else {
                isShockwaveActive = false
                shockwaveShader?.setFloatUniform("uProgress", 0.0f)
            }
        }

        // 2. Update Edge Chromatic Aberration Shader
        if (isAberrationActive && aberrationShader != null) {
            val elapsed = now - aberrationStartTime
            if (elapsed < aberrationDurationMs) {
                val progress = elapsed.toFloat() / aberrationDurationMs
                val intensity = (1.0f - progress)
                aberrationShader?.setFloatUniform("uIntensity", intensity)
                aberrationEffect = RenderEffect.createRuntimeShaderEffect(aberrationShader!!, "compositedImage")
            } else {
                isAberrationActive = false
            }
        }

        // 3. CRT Scanline & Phosphor Vignette Shader
        var crtEffect: RenderEffect? = null
        if (isCrtEnabled && crtShader != null) {
            val w = targetView.width.toFloat()
            val h = targetView.height.toFloat()
            crtShader?.setFloatUniform("uResolution", w, h)
            crtShader?.setFloatUniform("uScanlineIntensity", 0.18f)
            crtEffect = RenderEffect.createRuntimeShaderEffect(crtShader!!, "compositedImage")
        }

        // 4. Composite via RenderEffect Chaining (GPU hardware pipeline)
        val actionEffect = when {
            shockwaveEffect != null && aberrationEffect != null -> {
                RenderEffect.createChainEffect(shockwaveEffect, aberrationEffect)
            }
            shockwaveEffect != null -> shockwaveEffect
            aberrationEffect != null -> aberrationEffect
            else -> null
        }

        val finalEffect = when {
            actionEffect != null && crtEffect != null -> {
                RenderEffect.createChainEffect(crtEffect, actionEffect)
            }
            actionEffect != null -> actionEffect
            crtEffect != null -> crtEffect
            else -> null
        }

        targetView.setRenderEffect(finalEffect)
    }
}
